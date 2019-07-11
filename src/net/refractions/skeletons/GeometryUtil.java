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


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.refractions.voronoiskeleton.Segment;

import com.vividsolutions.jts.algorithm.RobustCGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateList;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.io.DriverProperties;
import com.vividsolutions.jump.io.ShapefileWriter;

/**
 * 
 * @author dblasby
 *
 *  This is general geometry functions that do manipulations of geometries.
 *   They were put here to make the {@link keletonizer} class simpler.
 * 
 *    translate(<geom>,x,y) -- translate the geometry 
 *    write_shape(fname, <...>) -- write out a simple shapefile (for debugging)
 */
public class GeometryUtil
{
	
	public static boolean DEBUG=false;
	/**
	 * translates the skeleton lines.  Result has brand new Coordinates.
	 * 
	 * @param lines
	 * @param x
	 * @param y
	 */
	public static void translate(SkelLineString[] lines,double x, double y)
	{
		PrecisionModel pm = new PrecisionModel();
	
		for (int t=0;t<lines.length;t++)
		{
			Coordinate[] cs = lines[t].getCoordinates();
			cs[0] = new Coordinate (cs[0].x +x, cs[0].y +y);
			cs[1] = new Coordinate (cs[1].x +x, cs[1].y +y);
			SkelLineString L =  new SkelLineString(cs,pm,0, lines[t].keepMe);
			if (lines[t].refPointA != null)
			{
				L.refPointA = new Coordinate( lines[t].refPointA.x+x, lines[t].refPointA.y+y);
			}
			else if (DEBUG)
			{
				System.out.println("null ref point A: "+cs[0]);
			}
			if (lines[t].refPointB != null)
			{
				L.refPointB = new Coordinate( lines[t].refPointB.x+x, lines[t].refPointB.y+y);
			}
			else if (DEBUG)
			{
				System.out.println("null ref point B: "+cs[0]);
			}

			lines[t] = L;
		}
	}
	
	/**
	 *    result has brand new coordinates
	 * @param lines
	 * @param x
	 * @param y
	 */
	public  static void translate(LineString[] lines,double x, double y)
	{
		PrecisionModel pm = new PrecisionModel();
	
		for (int t=0;t<lines.length;t++)
		{
			Coordinate[] cs = lines[t].getCoordinates();
			cs[0] = new Coordinate (cs[0].x +x, cs[0].y +y);
			cs[1] = new Coordinate (cs[1].x +x, cs[1].y +y);
			lines[t] = new SkelLineString(cs,pm,0);
		}
	}

/**
 *   makes new coordinates and points
 * 
 * @param points  list of JTS Points
 * @param x
 * @param y
 * @return
 */
	public static  ArrayList translate(ArrayList points,double x, double y)
	{
		PrecisionModel pm = new PrecisionModel();
		ArrayList result = new ArrayList();
	
		for (int t=0;t<points.size();t++)
		{
			com.vividsolutions.jts.geom.Point pt = (com.vividsolutions.jts.geom.Point) points.get(t);
			Coordinate c = new Coordinate(pt.getCoordinate().x +x,pt.getCoordinate().y+y );
			result.add( new com.vividsolutions.jts.geom.Point(c,pm,0));
		}
		return result;
	}

/**
 *   makes new coordinates and points
 * @param points
 * @param x
 * @param y
 */
	public  static void translate(com.vividsolutions.jts.geom.Point[] points,double x, double y)
	{
		PrecisionModel pm = new PrecisionModel();
		ArrayList result = new ArrayList();
	
		for (int t=0;t<points.length;t++)
		{
			com.vividsolutions.jts.geom.Point pt = points[t];
			Coordinate c = new Coordinate(pt.getCoordinate().x +x,pt.getCoordinate().y+y );
			points[t]=  new com.vividsolutions.jts.geom.Point(c,pm,0);
		}
	}


/**
 *   makes new coordinates
 * @param points
 * @param x
 * @param y
 */
	public  static void translate(com.vividsolutions.jts.geom.Coordinate[] points,double x, double y)
	{
		for (int t=0;t<points.length;t++)
		{
			points[t]=  new Coordinate(points[t].x +x,points[t].y+y );
		}
	}	
	
	/**
	 *  makes new coordinates and rings
	 * @param p
	 * @param x
	 * @param y
	 * @return
	 */
	public  static Polygon translate(Polygon p,double x, double y)
	{
		Coordinate[] ringCoords = ( (LineString) p.getExteriorRing().clone()).getCoordinates();
		translate(ringCoords,x,y);
		LinearRing exterior  = new LinearRing(ringCoords,p.getPrecisionModel(), p.getSRID() ); 
	
	
		LinearRing[] holes = new LinearRing[p.getNumInteriorRing()];
	
		for (int t=0;t<p.getNumInteriorRing();t++)
		{
			Coordinate[] holeRingCoords = ( (LineString) p.getInteriorRingN(t).clone()).getCoordinates();
			translate(holeRingCoords,x,y);
			holes[t]  = new LinearRing(holeRingCoords,p.getPrecisionModel(), p.getSRID() ); 
		}
		return new Polygon(exterior,holes,p.getPrecisionModel(), p.getSRID() );
	}
	
	
	/**
	 *  writes out a simple shapefile
	 * @param fname
	 * @param lines
	 * @param n      lines.length
	 */
	public  static void write_shape(String fname, LineString[] lines,int n)
	{
		FeatureSchema fs = new FeatureSchema();
		fs.addAttribute("GEOM",AttributeType.GEOMETRY);
		fs.addAttribute("ID",AttributeType.INTEGER);
	
		FeatureCollection fc = new FeatureDataset(fs);
	
		for (int t=0;t<n;t++)
		{
			Feature f = new BasicFeature(fs);
			f.setGeometry (lines[t]);
			f.setAttribute("ID", new Integer(t));
			fc.add(f);
		}
		ShapefileWriter sfw = new ShapefileWriter();
		DriverProperties dp = new DriverProperties(fname);
	
		  try
		{
			sfw.write(fc,dp);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
     
	}
	
		/**
		 *  write out a shapefile 
		 * @param fname
		 * @param lines
		 */ 
		public  static void write_shape(String fname, Segment[] lines)
		{
			FeatureSchema fs = new FeatureSchema();
			fs.addAttribute("GEOM",AttributeType.GEOMETRY);
			fs.addAttribute("ID",AttributeType.INTEGER);
			fs.addAttribute("INFO",AttributeType.STRING);
	
			FeatureCollection fc = new FeatureDataset(fs);
	
			for (int t=0;t<lines.length;t++)
			{
				Feature f = new BasicFeature(fs);
				Coordinate[] c = new Coordinate[2];
				c[0] = new Coordinate( lines[t].p1.x,lines[t].p1.y);
				c[1] = new Coordinate( lines[t].p2.x,lines[t].p2.y);
				LineString ls = new LineString(c,new PrecisionModel(), 0);
				f.setGeometry (ls);
				f.setAttribute("ID", new Integer(t));
				f.setAttribute("INFO",lines[t].toStringFull());
				fc.add(f);
			}
			ShapefileWriter sfw = new ShapefileWriter();
			DriverProperties dp = new DriverProperties(fname);
	
			  try
			{
				sfw.write(fc,dp);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
     
		}
		
	/**
	 * write out a simple shapefile
	 * @param fname
	 * @param cl
	 */
		public  static  void write_shape(String fname,CoordinateList cl)
		{
			FeatureSchema fs = new FeatureSchema();
			fs.addAttribute("GEOM",AttributeType.GEOMETRY);
			fs.addAttribute("ID",AttributeType.INTEGER);
	 
			FeatureCollection fc = new FeatureDataset(fs);
	
			for (int t=0;t<cl.size();t++)
			{
				Feature f = new BasicFeature(fs);
				com.vividsolutions.jts.geom.Point p = new com.vividsolutions.jts.geom.Point((Coordinate)cl.get(t), null,0);
				f.setGeometry (p);
				if (cl.get(t) instanceof SkelCoordinate)
				{
					f.setAttribute("ID",new Integer(( (SkelCoordinate)cl.get(t)).metaNum));
				}
				else
				{
					f.setAttribute("ID", new Integer(-1));
				}
				fc.add(f);
			}
			ShapefileWriter sfw = new ShapefileWriter();
			DriverProperties dp = new DriverProperties(fname);
	
			  try
			{
				sfw.write(fc,dp);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
     
		}
		
		
		/**
		 *  write out a simple shapefile
		 * @param fname
		 * @param p
		 */
		public  static  void write_shape(String fname,Polygon p)
		{
			FeatureSchema fs = new FeatureSchema();
			fs.addAttribute("GEOM",AttributeType.GEOMETRY);
			fs.addAttribute("ID",AttributeType.INTEGER);

			FeatureCollection fc = new FeatureDataset(fs);
			Feature f = new BasicFeature(fs);
			f.setGeometry (p);
			f.setAttribute("ID", new Integer(1));
			fc.add(f);
			ShapefileWriter sfw = new ShapefileWriter();
							DriverProperties dp = new DriverProperties(fname);
		
							  try
							{
								sfw.write(fc,dp);
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}				
		}
	
	
		/** 
		 *  write out a simple shapfeile
		 * @param fname
		 * @param cl  list of JTS coodinate
		 */
		public   static void write_shape(String fname,java.util.List cl)
		{
			FeatureSchema fs = new FeatureSchema();
			fs.addAttribute("GEOM",AttributeType.GEOMETRY);
			fs.addAttribute("ID",AttributeType.INTEGER);

			FeatureCollection fc = new FeatureDataset(fs);

			for (int t=0;t<cl.size();t++)
			{
				Feature f = new BasicFeature(fs);
				com.vividsolutions.jts.geom.Point p = new com.vividsolutions.jts.geom.Point((Coordinate)cl.get(t), null,0);
				f.setGeometry (p);
				f.setAttribute("ID", new Integer(t));
				fc.add(f);
			}
			ShapefileWriter sfw = new ShapefileWriter();
			DriverProperties dp = new DriverProperties(fname);

			  try
			{
				sfw.write(fc,dp);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
	/** 
	 *  write out a simple shapfeile
	 * @param fname
	 * @param cl  list of JTS coodinate
	 */
	public   static void write_shape(String fname,Coordinate[] cl)
	{
		FeatureSchema fs = new FeatureSchema();
		fs.addAttribute("GEOM",AttributeType.GEOMETRY);
		fs.addAttribute("ID",AttributeType.INTEGER);

		FeatureCollection fc = new FeatureDataset(fs);

		for (int t=0;t<cl.length;t++)
		{
			Feature f = new BasicFeature(fs);
			com.vividsolutions.jts.geom.Point p = new com.vividsolutions.jts.geom.Point(cl[t], null,0);
			f.setGeometry (p);
						   if (cl[t] instanceof SkelCoordinate)
							{
								f.setAttribute("ID",new Integer(( (SkelCoordinate)cl[t]).metaNum));
							}
							else
							{
								f.setAttribute("ID", new Integer(t));
							}
			fc.add(f);
		}
		ShapefileWriter sfw = new ShapefileWriter();
		DriverProperties dp = new DriverProperties(fname);

		  try
		{
			sfw.write(fc,dp);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}
		
	/**
	 *   takes a list of JTS points and returns a list of coodinates
	 * @param points
	 * @return
	 */
	public static ArrayList asCoords(List points)
	{
		ArrayList result = new ArrayList();
		for(int t=0;t<points.size(); t++)
		{
			com.vividsolutions.jts.geom.Point p = (com.vividsolutions.jts.geom.Point) points.get(t);
			result.add( p.getCoordinate() );
		}
		return result;
	}
	
		 /**
		 *   takes a list of JTS points and returns a list of coodinates
		 * @param points
		 * @return
		 */
		public static Coordinate[] asCoords(com.vividsolutions.jts.geom.Point[] points)
		{
			Coordinate[] result = new Coordinate[points.length];
			for(int t=0;t<points.length; t++)
			{
				com.vividsolutions.jts.geom.Point p = points[t];
				result[t] =  p.getCoordinate() ;
			}
			return result;
		}
		
			
			
	/**
	 *  makes a polygon from the arraylist of coordinate[]
	 *  1st = exterior ring
	 * @param ringList
	 * @return
	 */
	public static Polygon buildPoly(List ringList)
	{
		Coordinate[] cs;
	
		//cs =  ( Coordinate[]) ( (ArrayList)ringList.get(0)).toArray( new Coordinate[0]);
		cs = ( Coordinate[]) ringList.get(0);
		LinearRing exterior  = new LinearRing( cs, new PrecisionModel(), 0);
	
		LinearRing[] holes = new LinearRing[ringList.size()-1];
		for (int t=0;t<ringList.size()-1;t++)
		{
				//cs =  ( Coordinate[]) ( (ArrayList)ringList.get(t+1)).toArray( new Coordinate[0]);
				cs = ( Coordinate[]) ringList.get(t+1);
				LinearRing lr  = new LinearRing( cs, new PrecisionModel(), 0);
				holes[t] = lr;
		}
	
		return new Polygon(exterior,holes,new PrecisionModel(), 0);
	}
	
	/**
		 *  makes a polygon from the arraylist of arraylist of coordinate
		 *  1st = exterior ring
		 * @param ringList  (list of list of coordinate
		 * @return
		 */
		public static Polygon buildPoly2(List ringList)
		{
			Coordinate[] cs;
	
			cs =  ( Coordinate[]) ( (ArrayList)ringList.get(0)).toArray( new Coordinate[0]);
			//cs = ( Coordinate[]) ringList.get(0);
			LinearRing exterior  = new LinearRing( cs, new PrecisionModel(), 0);
	
			LinearRing[] holes = new LinearRing[ringList.size()-1];
			for (int t=0;t<ringList.size()-1;t++)
			{
					cs =  ( Coordinate[]) ( (ArrayList)ringList.get(t+1)).toArray( new Coordinate[0]);
					//cs = ( Coordinate[]) ringList.get(t+1);
					LinearRing lr  = new LinearRing( cs, new PrecisionModel(), 0);
					holes[t] = lr;
			}
	
			return new Polygon(exterior,holes,new PrecisionModel(), 0);
		}
		

	/**
	 *  gets all the non-redundant points (ring has 1st and last the same)
	 * @param p
	 * @return  ArrayList of Coordinate
	 */
	public static ArrayList pointsFromPoly(Polygon p)
	{
		ArrayList result  = new ArrayList();
	
		Coordinate[] cs;
	
		cs = p.getExteriorRing().getCoordinates();
		for (int u=0;u<cs.length-1;u++)
			result.add(cs[u]);
	
		for (int t=0;t<p.getNumInteriorRing();t++)
		{
			cs = p.getInteriorRingN(t).getCoordinates();
			for (int u=0;u<cs.length-1;u++)
					result.add(cs[u]);
		}
		return result;
	}
	
	/**
	 *  takes a list set of Coordinates and a polygon.  Returns a set of coordinates that exist as vertices
	 *   on the polygon.
	 * @param p
	 * @param riverInOutPoints
	 * @return
	 */
	public static com.vividsolutions.jts.geom.Coordinate[] removeUnused(Polygon p, com.vividsolutions.jts.geom.Coordinate[] riverInOutPoints)
	{
		Coordinate[] cs = (Coordinate[]) p.getCoordinates().clone();
		Arrays.sort(cs);
		
		ArrayList result = new ArrayList();
		for (int t=0;t<riverInOutPoints.length;t++)
		{
			if (Arrays.binarySearch(cs,riverInOutPoints[t]) >=0)
				result.add(riverInOutPoints[t]);
			
		}
		return (com.vividsolutions.jts.geom.Coordinate[]) result.toArray( new com.vividsolutions.jts.geom.Coordinate[0]);
	}
	
	/**
	 *   reverses the order of the coordinates
	 * @param cs
	 * @return
	 */
	public static Coordinate[] reverse(Coordinate[] cs)
	{
		int len = cs.length;
		Coordinate[] result = new Coordinate[cs.length];
		for (int t=0;t<len;t++)
		{
			result[t] = cs[len-t-1];
		}
		return result;
	}
	
		/**
		 *   reverses the order of the coordinates
		 * @param cs
		 * @return
		 */
		public static LineString reverse(LineString ls)
		{
			Coordinate[] cs = reverse(ls.getCoordinates());
			return new LineString(cs,ls.getPrecisionModel(), ls.getSRID());
		}
		
	
	

	/**
	 *  densify so that none of the edges are "too" big.
	 * @param cs  input coordinate list (ring)
	 * @return    output coordinate list (ring)
	 */
	public static Coordinate[] densify(Coordinate[] cs,double minSegmentDistance )
	{
		ArrayList resultCoords  =new ArrayList(cs.length);
		Coordinate last = cs[0];
		resultCoords.add(cs[0]);
		
		for (int t=1; t<cs.length;t++)
		{
			double dist = cs[t].distance(last) ;
			if (dist >minSegmentDistance)
			{
				//need to densify this segment
					int ndivisions = (int) (dist/minSegmentDistance);
	
					if (ndivisions <1)
						ndivisions =1;
		
					//allways emit first point, never emit last
	
					for (int u=0;u<ndivisions;u++)
					{
						double r = ((double)u+1)/((double)ndivisions+1);
						Coordinate c=	new Coordinate (
										    last.x + r* (cs[t].x-last.x),
											last.y + r* (cs[t].y-last.y)
											  );
						resultCoords.add(c);
					}			
			}
			resultCoords.add(cs[t]);
			last = cs[t];
		}
		
		
		return (Coordinate[]) resultCoords.toArray(new Coordinate[resultCoords.size()]);
	}
	
	/**
	 *   take a polygon and returns a list of Coordinate[]
	 * 
	 * @param p
	 * @return
	 */
	public static List ringize(Polygon p)
	{
			ArrayList result = new ArrayList();
			Coordinate[] ringCoords = ( (LineString) p.getExteriorRing().clone()).getCoordinates();

			result.add(ringCoords);

			for (int t=0;t<p.getNumInteriorRing();t++)
			{
				Coordinate[] holeRingCoords = ( (LineString) p.getInteriorRingN(t).clone()).getCoordinates();
				
				holeRingCoords = reverse(holeRingCoords);
				result.add(holeRingCoords);
			}
			return result;
	}
	
	/**
	 *   finds any edge with length> longestEdge and cuts the edge up.
	 * 
	 *   end result is a polygon with every vertex->vertex distance less than longestEdge
	 *  
	 * @param inPoly
	 * @param longestEdge
	 * @return   re-constructed polygon. Has same coordinates as original, plus a few new ones.
	 */
	public static Polygon simpleDensify(Polygon inPoly,double longestEdge)
	{
		List rings = ringize(inPoly);
		ArrayList newRings = new ArrayList();
		for (int t=0;t<rings.size(); t++)
		{
			Coordinate[] ringCoords = (Coordinate[]) rings.get(t);
			newRings.add( densify(ringCoords, longestEdge));
		}
		return buildPoly(newRings);
	}
		/**
		 *   This returns a new Polygon with the following properties:
		 *         a. exterior ring is CW
		 *         b. all holes are CCW
		 *         c. sharp angles at the input coordinates have been clipped
		 * 
		 *   It also returns an ArrayList of 2 point SkelLineStrings.  
		 *    These will need to be added into the resulting skeleton.  They connect the old river
		 *    entry point to the new location after the polygon has the corner clipped. 
		 *    
		 * @param p
		 * @param riverInOutPoints
		 * @param criticalCutAngle    definition of "sharp angle"
		 * @return  new Polygon, Array list of SkelLineString
		 */
	    public static Object[] clipSharpCorners(Polygon p, com.vividsolutions.jts.geom.Coordinate[] riverInOutPoints, double criticalCutAngle)
		{
			ArrayList rings = new ArrayList(); // list of Coordinate[]  - CW for exterior, CCW for rings
			RobustCGAlgorithms algo = new RobustCGAlgorithms();
			
			ArrayList connectingLines = new ArrayList();
		
			Coordinate[] ringCoords = ( (LineString) p.getExteriorRing().clone()).getCoordinates();
			if (RobustCGAlgorithms.isCCW(ringCoords))
			{
				ringCoords = reverse(ringCoords);
			}
			rings.add(ringCoords);
	
			for (int t=0;t<p.getNumInteriorRing();t++)
			{
				Coordinate[] holeRingCoords = ( (LineString) p.getInteriorRingN(t).clone()).getCoordinates();
				if (!(RobustCGAlgorithms.isCCW(holeRingCoords)))
				{
					holeRingCoords = reverse(holeRingCoords);
				}
				rings.add(holeRingCoords);
			}
		
			for (int t=0;t<riverInOutPoints.length;t++)
			{
				Coordinate c = riverInOutPoints[t];
				Object[] vector_info = findVector(rings,c );
				Coordinate[] vectors = new Coordinate[3];
				vectors[0] = (Coordinate) vector_info[0];
				vectors[1] = (Coordinate) vector_info[1];
				vectors[2] = (Coordinate) vector_info[2];
			
				double angle = angle(vectors);
				if ( (angle <=0) && (angle >-criticalCutAngle) )
				{
					//System.out.println("sharp corner river entry/exit vertex at ::"+ c);
					connectingLines.add(
							handleBadVertex(p,vectors,rings,((Integer)vector_info[3]).intValue(),((Integer)vector_info[4]).intValue(), Math.abs(angle) )
									);
				}
			}
			
			Object[] result = new Object[2];
			result[0] = buildPoly(rings); // polygon
			result[1] = connectingLines; // arraylist of skellinestring
			return result;
			
//			//re-construct polygon
//			Coordinate[] newRingCoords = densify((Coordinate[])rings.get(0) );
//			LinearRing outer = new LinearRing(newRingCoords , p.getPrecisionModel(),p.getSRID());
//			LinearRing[] holes = new LinearRing[rings.size()-1];
//			for (int t=0; t<rings.size()-1;t++ )
//			{
//				newRingCoords = densify((Coordinate[])rings.get(t+1) );
//				LinearRing hole = new LinearRing( newRingCoords, p.getPrecisionModel(),p.getSRID());
//				holes[t] = hole;
//			}
//			Polygon result = new Polygon(outer,holes,p.getPrecisionModel(),p.getSRID());

			//return result;
		}
		
		

	/*
	 *    1. find the cut edge (3 points)
	 *    2. remove the river entry point
	 *    3. add the 3 points
	 *    4. update the river entry point list, and post-processing connection lines.
	 *    
	 * 
	 *  parameter angle - see createCutLine()
	 */
	public static SkelLineString handleBadVertex(Polygon p,Coordinate[] cs, ArrayList rings,int ringNumber,int pointNumber,double angle)
	{
		
		SkelLineString result = null;
		
		Coordinate[] cut_edge = createCutLine (  p, cs, 3.0/4.0, angle);
		if (cut_edge == null)
			cut_edge = createCutLine (  p, cs, 1.0/2.0, angle);
		if (cut_edge == null)
			cut_edge = createCutLine (  p, cs, 1.0/4.0, angle);
		if (cut_edge == null)
			cut_edge = createCutLine (  p, cs, 1.0/8.0, angle);
		if (cut_edge == null)
			cut_edge = createCutLine (  p, cs, 1.0/16.0, angle);
		if (cut_edge == null)
			cut_edge = createCutLine (  p, cs, 1.0/32.0, angle);
		if (cut_edge == null)
		     cut_edge = createCutLine (  p, cs, 1.0/64.0, angle);
		if (cut_edge == null)
		     cut_edge = createCutLine (  p, cs, 1.0/128.0, angle);
		if (cut_edge == null)
		     cut_edge = createCutLine (  p, cs, 1.0/256.0, angle);
		if (cut_edge == null)
		     cut_edge = createCutLine (  p, cs, 1.0/512.0, angle);
		if (cut_edge == null)
		     cut_edge = createCutLine (  p, cs, 1.0/1024.0, angle);
		if (cut_edge == null)
		{
			//System.out.println("found very convex river entry/exit point that I cannot fix - "+ cs[1]);
			throw new IllegalStateException ("found very convex river entry/exit point that I cannot fix - "+ cs[1]);
		}
		
		//System.out.println("cut line:::"+cut_edge[0]+","+cut_edge[1]+","+cut_edge[2]);
		
		Coordinate[] extraConnectLineCoords = new Coordinate[2];
		extraConnectLineCoords[0] = new Coordinate(cs[1]);
		extraConnectLineCoords[1] = new Coordinate(cut_edge[1]);
		result =new SkelLineString(extraConnectLineCoords, p.getPrecisionModel(),p.getSRID(),true);

		//extraConnectingLines.add(extraLine);
		
		//update coordinateArray
		List ringCoords = new  ArrayList(Arrays.asList( (Coordinate[]) rings.get(ringNumber)));
		
		ringCoords.remove(pointNumber);
		ringCoords.add( pointNumber,cut_edge[2]);
		ringCoords.add( pointNumber,cut_edge[1]);
		ringCoords.add( pointNumber,cut_edge[0]);

		if (pointNumber ==0)
		{
			ringCoords.remove(ringCoords.size() -1); // get rid of last point
			ringCoords.add(cut_edge[0]);
		}
		rings.remove(ringNumber);
		rings.add(ringNumber,   ringCoords.toArray(new Coordinate[0])); 
		return result;
	}
	
	
	/**
		 *    1. find the cut edge (3 points)
		 *    2. intersect the line with all the lines -> shouldnt intersect any of the lines except the 
		 *       given ring
		 *    3. the intersection points should be either 0,1, or 2.
		 *       if >2 then need to move c closer to the apex and try again
		 *       if =0 then we're good
		 *       if =1 or 2, then we must check to see if the intersection points are really-really close to the original points
		 *                   if so, we're good, otherwise move c closer to apex.
		 *   4. return null if the computed line is "bad"
		 *        otherwise return the 3 new coordinates to replace the other
		 * 
		 *   @parm ratio -- 3/4, 1/2, 1/4, or 1/8.  Smaller number means move the cut point closer to the apex of the triangle
		 *   @param angle --- angle of the river entry point where we're cutting (see code)
		 */
	public static Coordinate[] createCutLine ( Polygon p,Coordinate[] cs, double ratio,double angle)
	{
				double v_x = cs[0].x - cs[1].x; 
				double v_y = cs[0].y - cs[1].y; 
	
				double w_x = cs[2].x - cs[1].x;
				double w_y = cs[2].y - cs[1].y;
		
				double lenV = Math.sqrt( v_x*v_x + v_y*v_y);
				double lenW = Math.sqrt( w_x*w_x + w_y*w_y);
		
				double minLen ;
				if (lenV < lenW)
					minLen = lenV;
				else
					minLen = lenW;
				
					// this is to handle the special case of two river entry points close together.
				    // these river points should be at least 1.5m apart, so we choose our length to be at most this
				    // value. 
				    // technically, we dont really want the length of the cut line to be > 1.5m either
				    //   minLen*tan( Math.toradians(angle)) is the approximate length.
				   //  this doesnt need to be implemented at this stage.
				  //
				   // this technique can cause problems with very small angled entry areas.  In these cases, we
				   // would may want a minimum cut line distance...
				    
				if (minLen > 1.4)
				     minLen = 1.4; // might want to make this smarter with angle
		
				Coordinate[] result = new Coordinate[3];
				result[0] = new Coordinate ( cs[1].x + (v_x/lenV*ratio *minLen),cs[1].y + (v_y/lenV*ratio *minLen)) ;
				result[2] = new Coordinate ( cs[1].x + (w_x/lenW*ratio *minLen),cs[1].y + (w_y/lenW*ratio *minLen)) ;
				result[1] = new Coordinate ((result[0].x+result[2].x)/2.0,(result[0].y+result[2].y)/2.0);
				
				LineString l = new LineString(result,p.getPrecisionModel(), p.getSRID());
				
		     	Geometry intersect=null;
				try{
					intersect = p.intersection(l);
				}
				catch (Exception e)
				{
					return null;
				}
				if (intersect.getGeometryType().equalsIgnoreCase( "MULTILINESTRING"))
				{
					     intersect = checkCutLine(result, (MultiLineString)intersect);
					      
					     if (intersect == null) // null -> bad intersection, return immediately	
					          return null;
				}	
				if (!(intersect.getGeometryType().equalsIgnoreCase("LINESTRING")))
					return null;
				
				LineString line_intersect = (LineString)  intersect;
				
				if  (Math.abs(line_intersect.getLength() - l.getLength()) > 0.01)
				{
					return null; // points shifted
				}
				return result;//all good		
	 }
	
	/**
	 *  verifies that a cutline intersecting with the polygon is okay
	 * 
	 *    a) if there's >2 peices, then there's a problem (actually, if there's >3 but this rarely occurs)
	 *    b) if there's 1 its fine
	 *    c) if there's 2, then its only fine if you can group it into two bins:
	 *         1. actual intersection
	 *         2. a little tiny part thats equal to cutline[0] or cutline[2]
	 *         
	 * 
	 * @param cutline  3 coodinats that make up the cut line
	 * @param intersection cutline intesected with the polygon
	 * @return  the main linestring or NULL if there's a problem.
	 */
	public static LineString checkCutLine(Coordinate[]  cutline, MultiLineString intersection)
	{
	     if (intersection.getNumGeometries() == 1)
	          	return (LineString) intersection.getGeometryN(0);
	     if (intersection.getNumGeometries() !=2)	
	           return null;
	     
	     LineString l1 = (LineString) intersection.getGeometryN(0);
	     LineString l2 = (LineString) intersection.getGeometryN(1);
	     
	     if (l2.getLength() > l1.getLength())
	     {
	          LineString t= l1;
	          l1 = l2;
	          l2 = t;
	     }
	     
	     	// l2 is the "short" one
	     if (l2.getLength() > 0.0001)
	          return null; // this is an actual intersection
	     
	     Point p1 = new Point(cutline[0],intersection.getPrecisionModel(), intersection.getSRID() );
	     Point p2 = new Point(cutline[2],intersection.getPrecisionModel(), intersection.getSRID() );
	     
	     if (p1.distance(l2) < 0.001)
	          	return l1;
	     if (p2.distance(l2) < 0.001)
	          return l1;
	     
	     return null;
	}
		
		
	    /**
		 *   returns 3 coordinates, in the correct order for angle determination
		 *    and an Integer for the Ring# (0=exterior)
		 *    and an Integer for the Point# (0=1st in ring)
		 * 
		 * @param rings
		 * @param apex
		 * @return
		 */
		public static Object[] findVector(ArrayList rings,Coordinate apex )
		{
			Object[] result = new Object[5];
			//find the correct ring, then return the point before, it, and after
			for (int t=0;t<rings.size();t++)
			{
				Coordinate[] ringCoords = (Coordinate[]) rings.get(t);
				for (int u=0;u<ringCoords.length;u++)
				{
					if (ringCoords[u].equals2D(apex))
					{
						if (u==0) //special case
						{
							result[0] = ringCoords[ ringCoords.length-2];
							result[1] = apex;
							result[2] = ringCoords[1];
							result[3] = new Integer(t);
							result[4] = new Integer(u);
							return result;
						}
						else
						{
							result[0] = ringCoords[ u-1];
							result[1] = apex;
							result[2] = ringCoords[u+1];
							result[3] = new Integer(t);
							result[4] = new Integer(u);
							return result;
						}
					}
				}
			}
			return null; // this shouldnt happen
		}		
		
		
		
  /**
	* use dot product then cross product
	* -angle -> could be a bad one
	* takes 3 coordinates in the array in order (as you walk the polygon)
	* @param cs
	* @return
	*/

	public static double angle(Coordinate[] cs)
	{
			
		double v_x = cs[0].x - cs[1].x; 
		double v_y = cs[0].y - cs[1].y; 
		
		double w_x = cs[2].x - cs[1].x;
		double w_y = cs[2].y - cs[1].y;
		
		double dotProd = v_x*w_x + v_y*w_y;
		double lenV = Math.sqrt( v_x*v_x + v_y*v_y);
		double lenW = Math.sqrt( w_x*w_x + w_y*w_y);
		double invCos = dotProd/ (  lenV * lenW );
		
		double angle= Math.acos(invCos);
		
		//use cross product to find if its a good or bad turn
		if ((v_x*w_y - v_y*w_x) >0)
			angle *= -1;
		return angle*180.0/Math.PI;
	}
	
	/**
	 *   Find the points on the bounding box of p
	 *   return either the xmin,xmax or ymin,ymax points (which ever one is greater dist)
	 * 
	 * @param p
	 * @return
	 */
	public static Coordinate[] getExtream(Polygon p)
	{
		Coordinate[] cs = p.getExteriorRing().getCoordinates();
		
		double maxx = cs[0].x;
		double minx = cs[0].x;
		double miny = cs[0].y;
		double maxy = cs[0].y;
		
		Coordinate c_maxx = cs[0];
		Coordinate c_maxy = cs[0];
		Coordinate c_minx = cs[0];
		Coordinate c_miny = cs[0];
		
		for (int t=0;t<cs.length ;t++)
		{
			if (cs[t].x <minx)
			{
				c_minx = cs[t];
				minx = cs[t].x;
			}
			if  (cs[t].y <miny)
			{
				c_miny = cs[t];
				miny = cs[t].y;
			}
			if (cs[t].x >maxx)
			{
				c_maxx = cs[t];
				maxx = cs[t].x;
			}
			if  (cs[t].y >maxy)
			{
				c_maxy = cs[t];
				maxy = cs[t].y;
			}
		}
		
		Coordinate result[] = new Coordinate[2];
		if ( (maxx-minx) > (maxy-miny) )
		{
			result[0]= c_maxx;
			result[1] = c_minx;		
		}
		else
		{
			result[0]= c_maxy;
			result[1] = c_miny;
		}
		return result;		 
	}
	
	public static Coordinate[] find2Coords(Polygon p)
	{
		return getExtream(p);
	}
	
	/**
	 *   Scan 1/2 way around the exterior ring from coordinate c.
	 * 
	 * @param p
	 * @param c
	 * @return
	 */
	public static Coordinate find1Coordinate(Polygon p, Coordinate c)
	{
		LineString ex = p.getExteriorRing();
		double len = ex.getLength();
		
		Coordinate[] cs = ex.getCoordinates();
		
		//find c
		int index = -1;
		for (int t=0;t<cs.length;t++)
		{
			if (cs[t].equals2D(c))
			{
				index =t;
				break;
			}
		}
		if (index ==-1)
			return null;
		
		Coordinate result = findByDistance( cs, len/2.0, index);
		if (result == null)
		{
			result = findByDistance( reverse(cs), len/2.0, cs.length-1-index);
		}
		if (result.equals(c))
			return null;
		return result;
		
	}
	
	/**
	 *   starting at cs[startIndex], find 1st vertex that is > dist away from it
	 *    if you run out of length, return null
	 * 
	 * @param cs
	 * @param dist
	 * @param startIndex
	 * @return
	 */
	public static Coordinate findByDistance(Coordinate[] cs, double distWant, int startIndex)
	{
		Coordinate result = null;
		double distance_so_far=0;
		for (int t=startIndex;t<(cs.length-1) ;t++)
		{
			double dist = cs[t].distance(cs[t+1]);
			distance_so_far += dist;
			if (distance_so_far>=distWant)
				return cs[t];
		}
		return result;
		
	}
	/**
	 * 
	 *   Finds importantVertexes on Polygon.  If a vertex is very close (<0.1) to the 
	 *   important vertex, it is removed.
	 * 
	 *    This is very inefficient for a bunch of points near an importantVertex.
	 *  
	 *    ASSUMES: always a "good" vertex between 2 river entry points
	 *     
	 * 
	 * 
	 * @param p
	 * @param importantVertex
	 * @param criticalDistance  usually 0.1 or 1.0 is a good distance
	 * @return
	 */
	public static Polygon removeClosePoints(Polygon p, Coordinate[] importantVertex,double criticalDistance)
	{
		Arrays.sort(importantVertex);
		boolean somethinghappened = true;
		
		
		while (somethinghappened)
		{
			Object[]  info =removeClosePoints_sub( p,importantVertex,criticalDistance) ;
			somethinghappened = (((Boolean)info[1]).booleanValue());
			p = (Polygon) info[0];
		}
		return p;
	}
	
	/**
	 *  Finds importantVertexes on Polygon.  If a vertex is very close (<0.1) to the 
	 *  important vertex, it is removed.
	 * 
	 * You should continue to call this function until it returns false.
	 * 
	 * 
	 * @param p
	 * @param importantVertex  (sorted)
	 * @return  result[0] = Polygon result[1] = boolean (true = polygon was changed)
	 */
	private static Object[] removeClosePoints_sub(Polygon p, Coordinate[] importantVertex,double criticalDistance)
	{
		List rings = ringize(p);  //list of Coordinate[]
		boolean somethingHappened = false;
		
		for (int t=0;t<rings.size();t++)
		{
			Coordinate[] ring = (Coordinate[]) rings.get(t);
			Object[] info = removeClosePoints_sub(ring,  importantVertex,criticalDistance);
			if (((Boolean)info[1]).booleanValue())
			{
				//change happened
				somethingHappened = true;
				rings.remove(t);
				rings.add(t,info[0]);
			}
		}
		
		Object[] result = new Object[2];
		result[0] = p;
		result[1] = new Boolean(somethingHappened);
		if (somethingHappened)
		{
			result[0] = buildPoly(rings);
		}
		return result;
	}
	
		/**
		 *  Finds importantVertexes on Polygon.  If a vertex is very close (<0.1) to the 
		 *  important vertex, it is removed.
		 * 
		 * You should continue to call this function until it returns false.
		 * 
		 * 
		 * @param ring
		 * @param importantVertex (sorted)
		 * @return  result[0] = Coordinate[] result[1] = boolean (true = polygon was changed)
		 */
		private static Object[] removeClosePoints_sub(Coordinate[] ring, Coordinate[] importantVertex,double criticalDistance)
		{
			boolean removedPoint = false;
			
				for (int t=0;t<ring.length-1;t++) // -1  means dont do 1st point twice
				{
					if (ring[t] != null)  // null ==> already removed so dont look at it
					{
						if (Arrays.binarySearch(importantVertex, ring[t])>=0)
						{
							//ring[t] is an important vertex, find distance to ring[t+1] and ring[t-1]
							    // note be smart about t=0 or t=last point
							    // we could throw a null pointer exception if there are 2 river entry points
							    // separated by a single point thats really code to one of the river entry points
							    //  we dont handle this case and just throw an error.
							if (t==0)  //first point on ring
							{
								//check against ring[1] and ring[(ring.length-2)]
								if (ring[0].distance(ring[1]) < criticalDistance)
								{
									//remove ring[1]
									removedPoint = true;
									ring[1] = null;
								}
								if (ring[0].distance(ring[ring.length-2]) < criticalDistance)
								{
									ring[ring.length-2] = null;
									removedPoint = true;
								}
							}
							else if (t==1)  //second point on ring
							{
								//check against ring[0] and ring[2]
								if ((ring[0] != null) && (ring[1].distance(ring[0]) < criticalDistance))
								{
									removedPoint = true;
									ring[0] = null;
									ring[ring.length-1] = null; //special case (1st point == last point)
								}
								if (ring[1].distance(ring[2]) < criticalDistance)
								{
									ring[2] = null;
									removedPoint = true;
								}
																
							}
							else if (t== (ring.length-2))  //second to last point on ring
							{
								//	check against ring[0]== ring[ring.length-1] and ring[t-1]
							   if (ring[t].distance(ring[0]) <criticalDistance)
							   {
								   //remove ring[1]
								   removedPoint = true;
								   ring[0] = null;
								   ring[ring.length-1] = null; //special case (1st point == last point)
							   }
							   if ((ring[t-1] != null) &&(ring[t].distance(ring[t-1]) < criticalDistance))
							   {
								   ring[t-1] = null;
								   removedPoint = true;
							   }
							}
							else  // middle of ring
							{
									//check against ring[t+1] and ring[t-1]
								   if (ring[t].distance(ring[t+1]) < criticalDistance)
								   {
									   //remove ring[t+1]
									   removedPoint = true;
									   ring[t+1] = null;
								   }
								   if ((ring[t-1] != null) &&(ring[t].distance(ring[t-1]) < criticalDistance))
								   {
									   ring[t-1] = null;
									   removedPoint = true;
								   }							   
							}
							
						}
					}
				}
			
			
			
			Object[] result = new Object[2];
			result[0] = ring;  // might be changed (next lines)
			result[1] = new Boolean(removedPoint);
						
			if (removedPoint)
			{
					//compress ring
					ArrayList resultRing = new ArrayList();
					for (int t=0;t<ring.length;t++)
					{
						if (ring[t] != null)
							resultRing.add(ring[t]);
					}
					if (!(resultRing.get(0).equals(resultRing.get(resultRing.size()-1))))
					{
						resultRing.add(resultRing.get(0));
					}
					result[0]  = (Coordinate[]) resultRing.toArray(new Coordinate[resultRing.size()]);	
			}
			
			return result;
		}
}
