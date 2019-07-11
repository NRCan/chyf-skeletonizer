/*
*
*  The Skeletonizer Utility is distributed under GNU General Public 
*  Licence – It is free software and can be  redistributed and/or 
*  modified under the terms of the GNU General Public License as 
*  published by the Free  Software Foundation; either version 2 of 
*  the License, or (at your option) any later version.
*
*  This program is distributed in the hope that it will be useful,
*  but comes WITHOUT ANY WARRANTY; without even the implied warranty 
*  of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. Please 
*  see the GNU General Public License for more details. 
*
*  You should have received a copy of the GNU General Public License 
*  along with this program; if this is not the case, please write to:
*
*  		            The Free Software Foundation, Inc.
*		                 59 Temple Place - Suite 330
*		   	                    Boston - MA
*		                     02111-1307 - USA.
*
*/


package net.refractions.skeletons;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.CoordinateList;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.Envelope;
import java.util.ArrayList;
import java.util.Iterator;
import net.refractions.voronoi.Segment;
import net.refractions.voronoi.Point;

/**
 * @author bowens
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class Voronoier 
{

	public Voronoier() 
	{
		
	}


	private double shift = 0.001;	// used for estimating points on either side of a river point
	int gLineSegCount = 10; 
/***************************************************************
 * runTestDP
 * 
 * @param DPvalue value for the Douglas Peucker algorithm
 * @param lake the polygon to be simplified
 * @return LineString the simplified lake border
 * 
 * note: only does the outer ring of the polygon
 * note: make sure lake is in BC Albers-83, not LL
 **************************************************************/
	public Geometry runTestDP(double DPvalue, Polygon lake)
	{
		// convert lake polygon into linestring
		LineString theLake = lake.getExteriorRing();
		
		// perform DP simplification
		LineString g = DPAlgorithms.NewBasicDP(theLake, DPvalue );
		
		Geometry geom = g.getEnvelope();

		return geom;
	}


/**************************************************************
 * skeletonizeMLS
 * 
 * @param DPvalue
 * @param lake
 * @param riverEntries
 * @return a MultiLineString
 **************************************************************/
	public MultiPoint getDensePoints(	double DPvalue, 
									double denseValue, 
									Geometry lake1, 
									Geometry[] riverEntries)
	{
		System.out.println("type= " + lake1.getGeometryType());
		System.out.println("valid? " + lake1.isValid());
		net.refractions.voronoi.Voronoi voron = new net.refractions.voronoi.Voronoi();

//				get bounding box of the lake
		Envelope env = (lake1.getEnvelope()).getEnvelopeInternal();
		int minX = (int)env.getMinX();
		int maxX = (int)env.getMaxX();
		int minY = (int)env.getMinY();
		int maxY = (int)env.getMaxY();

		CoordinateList finalPoints = new CoordinateList();
		System.out.println("Total incoming points = " + lake1.getNumPoints());

		// go through each line string, DP it, and convert it to coordinates
		if( lake1 instanceof Polygon )
		{
			Polygon poly = (Polygon) lake1;
			LineString[] islands = new LineString[poly.getNumInteriorRing()];
			// remove the duplicate last points of the island LineStrings
			for (int i=0; i<islands.length; i++)
			{
				// get interior ring
				LineString s = poly.getInteriorRingN(i);
				// convert it to an array of size-1
				Coordinate[] noEndList = new Coordinate[s.getNumPoints()-1];

				for (int j=0; j<s.getNumPoints()-1; j++)
				{
					noEndList[j] = s.getCoordinateN(j);
				}
				islands[i] = new LineString(noEndList, s.getPrecisionModel(), s.getSRID());
			}
	
			/** for each island **/
			for( int i=0; i < poly.getNumInteriorRing(); i++ )
			{
				CoordinateList inner = new CoordinateList();
				inner.add( islands[i].getCoordinates(),false);
				finalPoints.add(densify(denseValue, inner).toCoordinateArray(), false);
			}// end for each island
	
	
			/** for the OUTER RING **/
			CoordinateList outer = new CoordinateList();
			outer.add(poly.getExteriorRing().getCoordinates(),false);
			Coordinate[] noEndList = new Coordinate[outer.size()-1];
	
			for (int j=0; j<noEndList.length; j++)
			{
				noEndList[j] = outer.getCoordinate(j);
			}
			CoordinateList asdf = new CoordinateList(noEndList);
			//islands[i] = new LineString(noEndList, s.getPrecisionModel(), s.getSRID());
			finalPoints.add(densify(denseValue, asdf).toCoordinateArray(), false);
	
		}// end if a polygon

		GeometryFactory gf = new GeometryFactory(lake1.getPrecisionModel(), lake1.getSRID());
		//LineString stringy = gf.createLineString( finalPoints.toCoordinateArray() );

		MultiPoint ret = gf.createMultiPoint(finalPoints.toCoordinateArray());

		
		return ret;
	}


/**************************************************************
 * skeletonizeMLS
 * 
 * @param DPvalue
 * @param lake
 * @param riverEntries
 * @return a MultiLineString
 **************************************************************/
	public LineString[] voronize(	double DPvalue, 
									double denseValue, 
									Geometry lake1, 
									Geometry[] riverEntries)
	{
				
		System.out.println("type= " + lake1.getGeometryType());
		System.out.println("valid? " + lake1.isValid());
		net.refractions.voronoi.Voronoi voron = new net.refractions.voronoi.Voronoi();
		
//		get bounding box of the lake
		Envelope env = (lake1.getEnvelope()).getEnvelopeInternal();
		int minX = (int)env.getMinX();
		int maxX = (int)env.getMaxX();
		int minY = (int)env.getMinY();
		int maxY = (int)env.getMaxY();
		
		CoordinateList finalPoints = new CoordinateList();
		System.out.println("Total incoming points = " + lake1.getNumPoints());

		// go through each line string, DP it, and convert it to coordinates
		if( lake1 instanceof Polygon )
		{
			Polygon poly = (Polygon) lake1;
			LineString[] islands = new LineString[poly.getNumInteriorRing()];
			// remove the duplicate last points of the island LineStrings
			for (int i=0; i<islands.length; i++)
			{
				// get interior ring
				LineString s = poly.getInteriorRingN(i);
				// convert it to an array of size-1
				Coordinate[] noEndList = new Coordinate[s.getNumPoints()-1];
		
				for (int j=0; j<s.getNumPoints()-1; j++)
				{
					noEndList[j] = s.getCoordinateN(j);
				}
				islands[i] = new LineString(noEndList, s.getPrecisionModel(), s.getSRID());
			}
			
			/** for each island **/
			for( int i=0; i < poly.getNumInteriorRing(); i++ )
			{
				CoordinateList inner = new CoordinateList();
				inner.add( islands[i].getCoordinates(),false);
				finalPoints.add(densify(denseValue, inner).toCoordinateArray(), false);
			}// end for each island
			
			
			/** for the OUTER RING **/
			CoordinateList outer = new CoordinateList();
			outer.add(poly.getExteriorRing().getCoordinates(),false);
			Coordinate[] noEndList = new Coordinate[outer.size()-1];
			
			for (int j=0; j<noEndList.length; j++)
			{
				noEndList[j] = outer.getCoordinate(j);
			}
			CoordinateList asdf = new CoordinateList(noEndList);
			//islands[i] = new LineString(noEndList, s.getPrecisionModel(), s.getSRID());
			finalPoints.add(densify(denseValue, asdf).toCoordinateArray(), false);
			
		}// end if a polygon
		
		GeometryFactory gf = new GeometryFactory(lake1.getPrecisionModel(), lake1.getSRID());
		LineString stringy = gf.createLineString( finalPoints.toCoordinateArray() );
		//System.out.println("\nmunch length =" + finalPoints.size());
		//Coordinate pt;
		// add the points to the voronoi diagram
		Coordinate first = null;
		Coordinate prev = null;
		for( Iterator i=finalPoints.iterator(); i.hasNext(); )
		{	Object obj = i.next();
			//pt = (Coordinate) i.next();
			if (obj instanceof SkelCoordinate)
			{
				SkelCoordinate pt = (SkelCoordinate)obj;
				if (first == null)
					first = pt;
				//System.out.println("metaNum=" + pt.metaNum);
				voron.addPoint(
					new net.refractions.voronoi.Point(pt.x, pt.y)
				);
			}
			else
			{
				Coordinate pt = (Coordinate)obj;
				if (first == null)
					first = pt;
	
				voron.addPoint(
					new net.refractions.voronoi.Point(pt.x, pt.y)
				);
			}
		}
		
		
		/** Run the VORONOI **/
		voron.run();
		ArrayList ll = new ArrayList(voron.get_output());
		int size = ll.size();
		System.out.println("Number of voronoi edges = " + size);
		SkelLineString[] ls = new SkelLineString[size];
		int keepmeCount = 0;
		
		for (int i=0; i<size; i++)
		{
			//System.out.println("i= " + i);
			
//			if ((Segment)ll.get(i) == null)
//				System.out.println("(Segment)voron.outputSegments.get(i) == null, i=" + i + "size=" + size);
//			else if (((Segment)ll.get(i)).p1 == null)
//				System.out.println("((Segment)voron.outputSegments.get(i)).p1 == null");
				
			Coordinate c1 = new Coordinate(((Point)((Segment)ll.get(i)).p1).x, ((Point)((Segment)ll.get(i)).p1).y);
			Coordinate c2 = new Coordinate(((Point)((Segment)ll.get(i)).p2).x, ((Point)((Segment)ll.get(i)).p2).y);
			Coordinate[] c = {c1, c2};
			
			SkelLineString L = new SkelLineString(c, new PrecisionModel(), 0);
			ls[i] = L;
			if (ls[i].keepMe)
				keepmeCount++;
		}
		
//		System.out.println("Edges to be kept = " + keepmeCount);
		
		/** skeletonize the voronoi **/
/*	
		LinkedList kept = new LinkedList();
		// go through each line segment
		for (int i=0; i<ls.length; i++)
		{
			// if a line crosses the lake shore and it can be removed, remove it
			
			if (!ls[i].crosses(((Polygon)lake1).getExteriorRing()) || ls[i].keepMe) // used deMorgan's for (AB')'
			{
				kept.add(ls[i]);
			}
				
		}
		
		int keptSize = kept.size();
		for (int i=0; i<kept.size(); i++)
		{
			// if it it isn't in the polygon at all and isn't supposed to be kept
			if (!((SkelLineString)kept.get(i)).within((Polygon)lake1) && !((SkelLineString)kept.get(i)).keepMe)
			{
				kept.remove(i);
				i--;
				keptSize = kept.size();
			}
		}

		// remove edges that cross an island's shore and aren't supposed to be kept
		for (int i=0; i<((Polygon)lake1).getNumInteriorRing(); i++)
		{
			keptSize = kept.size();
			for (int j=0; j<keptSize; j++)
			{
				// if it crosses an island's shore and it isn't supposed to be kept
				if ((((SkelLineString)kept.get(j)).crosses(((Polygon)lake1).getInteriorRingN(i)) && !((SkelLineString)kept.get(j)).keepMe)) // used deMorgan's for (AB)'
				{
					kept.remove(j);
					j--;
					keptSize = kept.size();
				}
				
			}
		}

		
		SkelLineString[] ret = new SkelLineString[kept.size()];
		for (int i=0; i<kept.size(); i++)
		{
			ret[i] = (SkelLineString)kept.get(i);
		}


		PrecisionModel p = ret[0].getPrecisionModel();
		int p_srid = ret[0].getSRID();
		
		// TODO: build the network
		NetworkBuilder skeletonNetwork = new NetworkBuilder();
		skeletonNetwork.addEdges(ret);
		
		// TODO: traverse the network and keep only the edges that are linked to a river
		skeletonNetwork.trimNetwork();
		
		NetworkEdge[] out = skeletonNetwork.getNetworkEdges();
		LineString[] retFinal = new LineString[out.length];
		for (int i=0; i<out.length; i++)
		{
			Coordinate[] c = new Coordinate[2];
			c[0] = new Coordinate(out[i].dnode.coord.x, out[i].dnode.coord.y);
			c[1] = new Coordinate(out[i].unode.coord.x, out[i].unode.coord.y);
			retFinal[i] = new LineString(c, p, p_srid);
		}

*/
		System.out.println("DONE");
//		return retFinal;
		return ls;
		//return ret;
	}


/**
 * @param s
 * @param river
 * @return
 * 
 * Return a linestring with the newly inserted river point (actually 2 points)
 */
	private LineString insertRiverPoint(LineString s, com.vividsolutions.jts.geom.Point river) 
	{
		com.vividsolutions.jts.geom.Point p1, p2, prev, next, removed;
		SkelCoordinate newA, newB;
		
		CoordinateList output = new CoordinateList();
		
		//Coordinate[] noEndList = new Coordinate[s.getNumPoints()];
		
		/** get rid of the duplicate end point
		 // don't do this here!!!!!
		 
		for (int i=0; i<ss.getNumPoints()-1; i++)
		{
			noEndList[i] = ss.getCoordinateN(i);
		}
		LineString s = new LineString(noEndList, ss.getPrecisionModel(), ss.getSRID());
		
		SkelPoint[] resultArray = new SkelPoint[s.getNumPoints()];
										   **/
		
	/** find prev and next.
	 This point might match a vertex, I will assume this to start and make it a requirement later**/
		
		// find the matching vertex and get the prev and next vertices of it
		for (int i=0; i<s.getNumPoints(); i++)
		{
			// if the points match
			if (s.getPointN(i).getCoordinate().x == river.getCoordinate().x &&
				s.getPointN(i).getCoordinate().y == river.getCoordinate().y)
			{
				//System.out.println("found a matching point in the LineString");
				// get prev and next
				if (i-1 < 0) // if we are at the start of the list
					p1 = s.getPointN(s.getNumPoints()-1);	//grab the last one
				else
					p1 = s.getPointN(i-1);		// else grab the previous one in the list
				
				if (i+1 >= s.getNumPoints()) // if we are at the end of the list
					p2 = s.getPointN(0);		// grab the first one in the list
				else
					p2 = s.getPointN(i+1);		// else grab the next one
				
				prev = p1;
				next = p2;
				
				removed = s.getPointN(i);
				
				// get angle of prev/rem with horiz line
				double thetaPR = getAngle(prev, removed);
					
				// get angle of rem/next with horiz line
				double thetaNR = getAngle(next, removed);
				
				// create new prev point to replace removed
				double px = removed.getCoordinate().x + shift * Math.cos(Math.toRadians(thetaPR));
				double py = removed.getCoordinate().y + shift * Math.sin(Math.toRadians(thetaPR));
				newA =  new SkelCoordinate(px, py);
				newA.metaNum = ++gLineSegCount;
				//System.out.println("p=" + px + " " + py);
				
				// create new next point to replace removed
				double nx = removed.getCoordinate().x + shift * Math.cos(Math.toRadians(thetaNR));
				double ny = removed.getCoordinate().y + shift * Math.sin(Math.toRadians(thetaNR));
				newB =  new SkelCoordinate(nx, ny);
				newB.metaNum = ++gLineSegCount;
				//System.out.println("n=" + nx + " " + ny);
				
				gLineSegCount = gLineSegCount+10;
				// insert the two into the list and return the LineString
				output.add(newA, false);
				output.add(newB, false);
				
			}// end if the points match
			else	// points don't match
			{
				output.add(s.getPointN(i).getCoordinate(), false);
			}
		}// end for each point in the ring
		
		LineString ls = new LineString(output.toCoordinateArray(), s.getPrecisionModel(), s.getSRID());
		
		return ls;
	}



	 double getAngle(com.vividsolutions.jts.geom.Point p,
							com.vividsolutions.jts.geom.Point removed)
	{
		
		double px, py, rx=1, ry=0;
		boolean neg = false;
		px = p.getCoordinate().x - removed.getCoordinate().x;

		py = p.getCoordinate().y - removed.getCoordinate().y;
//		System.out.println("R.x=" + removed.getCoordinate().x + ", R.y=" + removed.getCoordinate().y);
//		System.out.println("p.x=" + p.getCoordinate().x + ", p.y=" + p.getCoordinate().y);
//		System.out.println("px=" + px + ", py=" + py);
		
		if (p.getCoordinate().y < removed.getCoordinate().y)
			neg = true;
		// use dot product		theta = arccos((v.w)/(|v|x|w|))
		double prevll = Math.sqrt(px*px + py*py);
		px = px/prevll;	// normalize the sucker
		py = py/prevll;
		double theta = Math.toDegrees(Math.acos(px*rx + py*ry));
		// looks like it absolute values the angle, this is fixed below
		if (neg)
			theta = 360-theta;
//		System.out.println("theta=" + theta);
		
		return theta;
	}


	private CoordinateList densify(double denseValue, CoordinateList cl)
	{
		Coordinate first = null, prev = null;
		CoordinateList ret = new CoordinateList();
		
		for( Iterator i=cl.iterator(); i.hasNext(); )
		{	
			Coordinate pt = (Coordinate) i.next();
			if (first == null)
				first = pt;
		
			ret.add(pt, false);
			// interpolate between this point and the next one
			if (prev != null)
			{
				Coordinate ptp = prev;
				CoordinateList dense = interpolate(denseValue, ptp, pt);	// interpolate the points
				if (dense != null)
				{
					for (Iterator j=dense.iterator(); j.hasNext(); )
					{
						Coordinate pt2 = (Coordinate) j.next();
						ret.add(pt2, false);	
					}
				}
			}
			prev = pt;
		}
		return ret;
	}
	
/**************************************************************
 
 * 
 * @param 
 * @param 
 * @return
 * 
 * x1,y1 *
 * 		  \
 * 		   \
 * 			\
 * 			 \
 * 			  \
 * 			   * x2,y2
 * 
 **************************************************************/
	private CoordinateList interpolate(double denseValue, Coordinate pp1, Coordinate pp2)
	{
		Coordinate p1, p2;
		if (pp1.x < pp2.x)
		{
			p1 = pp1;
			p2 = pp2;
		}
		else
		{
			p1 = pp2;
			p2 = pp1;
		}
		
		//System.out.println("densify called");
		CoordinateList cl = new CoordinateList();
		double dx = p2.x - p1.x;
		double dy = p2.y - p1.y;
		//double m = dy / dx;						// slope of the line segment
		double L = Math.sqrt(dx*dx + dy*dy);	// linear line distance
		double divisions = Math.floor(L/denseValue)-1;		// number of times the line is split. Use -1 for number of points to create, otherwise we get that many points and n+1 divisions
		double xIncrement = dx/(divisions+1);
		double yIncrement = dy/(divisions+1);
		double fudge1 = 0.000001;
		double fudge2 = -0.000001;
		
		if (divisions < 1)
			return null;
		
		for (int i=0; i<divisions; i++)
		{
			double newX;
			double newY;

			newX = p1.x + xIncrement*(i+1);
			newY = p1.y + yIncrement*(i+1);
			Coordinate c = new Coordinate(newX, newY);
			cl.add(c, false);
		}
		
		return cl;
	}
	
}
