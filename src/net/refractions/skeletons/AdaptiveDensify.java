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


import java.util.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.index.strtree.STRtree;

// this will add redundent points to the diagram (1st and last point of ring)
// remove them when you sort
public class AdaptiveDensify
{
	public double shift = 0.001;	// used for estimating points on either side of a river point
	public int riverInputID = 1;

	ArrayList currentRing = new ArrayList();  //list of coordinate
	ArrayList allRings = new ArrayList();     //list of list of coordinate
	                             			 
	ArrayList allEdges = new ArrayList();
	
	ArrayList outputLines = new ArrayList();
	
	protected Polygon inputPolygon =null;
	public Polygon outputPolygon = null;
	
	
	double MINSUBDIVIDESIZE = 1.25;  //probably want <2.  This is the minium distance between points this will emit (approx)
	
	double minAngle = (180-45);   // when to pay attention to a joint.  should be >90
	
	double searchDistanceCoefficient = 2; // looks for edges that are twice (if =2) my length away
	double rescalingCooefficience = 1.75 ;// how much to split an edge up. 2= twice the nearest edge distance
									// should be >1, 2 is probably too high.
	
	int npoints = 0;
	int nobtuse =0;
	
	 
	public AdaptiveDensify(Polygon p)
	{
		inputPolygon = p;

		for (int t=-1;t<p.getNumInteriorRing();t++)// go through each ring, -1 = outside
		{
			Coordinate[] ringCoords = null;
			if (t==-1)
			{
				ringCoords = p.getExteriorRing().getCoordinates();
			}
			else
			{
				ringCoords = p.getInteriorRingN(t).getCoordinates();
			}
			
			handleRing(ringCoords);  //populates allEdges and handles riverpoints.
			allEdges.add( new AdaptiveDensifyEdge() );
		}
		densify();
	}
	
		/**
		 * 
		 * @param p
		 * @param factor  - should be 1 (normal), or 0.5, or 0.25,  0.125 is probably too slow
		 */
		public AdaptiveDensify(Polygon p,double factor)
		{
			 inputPolygon = p;
			 MINSUBDIVIDESIZE = 1.25*factor; 


			for (int t=-1;t<p.getNumInteriorRing();t++)// go through each ring, -1 = outside
			{
				Coordinate[] ringCoords = null;
				if (t==-1)
				{
					ringCoords = p.getExteriorRing().getCoordinates();
				}
				else
				{
					ringCoords = p.getInteriorRingN(t).getCoordinates();
				}
			
				handleRing(ringCoords);  //populates allEdges and handles riverpoints.
				allEdges.add( new AdaptiveDensifyEdge() );
			}
			densify();
		}
		


	public void handleRing(Coordinate[] ringCoords)
	{
		Coordinate lastPoint = null;
		
		lastPoint = ringCoords[0];
		
			//for each coord in the list
		for (int t =1; t< java.lang.reflect.Array.getLength(ringCoords);t++)
		{
			Coordinate c =  ringCoords[t];
			if (!(lastPoint.equals(c))) // remove redundant points
			{
					AdaptiveDensifyEdge e = new AdaptiveDensifyEdge(lastPoint, c,true);
					allEdges.add(e);
					lastPoint = c; 
			}
			else
			{
				lastPoint = c;
			}
		}
	}

		    
	    




	// we work from e in two directions (forward and back)
	// 1. remove E
	// 2. find E's next forward edge (Ef)
	// 3. remove Ef 
	// 4. find Ef's forward edge (Eff)
	// 5. if this angle isnt too bent, remove Eff and continue (otherwise skip to step #8)
	// 6. find Efff
	// 7. if angle isnt too bad, remove Efff
	
	// 8. find E'e next backward edge (Er) 
	//     same as above
	
	// this should probably be smarter if we made a mini-network, but 
	//  its probably not worth it    
	//
	// this is O(n^2), but n is small.
public void removeRedundant(AdaptiveDensifyEdge e, List nearbyEdges)
{
	ArrayList toRemove = new ArrayList();  //remove all the edges at the end.
	
	toRemove.add(e);
	
	AdaptiveDensifyEdge Ef = findForwards(e, nearbyEdges);
	if (Ef != null)
	{
		double angle_for = angle(e, Ef)*180.0/Math.PI;
		if (angle_for >90)   // for very curvy lines, we densify alot!
			toRemove.add( Ef);
	}
	AdaptiveDensifyEdge Er = findBackwards(e, nearbyEdges);
	if (Er != null)
	{
		double angle_bac = angle(Er,e)*180.0/Math.PI;
		if (angle_bac >90)
			toRemove.add( Er );  // this is all the original implementation did
	}

	
	AdaptiveDensifyEdge hot = Ef; // current edge we're looking at
	
	for (int t=0;t<3;t++) // look at 3 edges
	{
		if (hot !=null)
		{
			AdaptiveDensifyEdge forward = findForwards(hot, nearbyEdges);
			if (forward !=null)
			{
				//should we remove foward -- remove if angle hot&forward is <30
				double angle = angle(hot, forward);
				if (angle*180.0/Math.PI > minAngle)
				{
					toRemove.add(forward);
					hot = forward;
				}
				else
				{
					hot = null; //abort
				}
			}
			else
			{
				hot = null ;//abort
			}
		}
	}
	
	//backwards
	 hot = Er; // current edge we're looking at
	
		for (int t=0;t<3;t++) // look at 3 edges
		{
			if (hot !=null)
			{
				AdaptiveDensifyEdge back = findBackwards(hot, nearbyEdges);
				if (back !=null)
				{
					//should we remove foward -- remove if angle hot&forward is <30
					double angle = angle(back, hot);
					if (angle*180.0/Math.PI > minAngle)
					{
						toRemove.add(back);
						hot = back;
					}
					else
					{
						hot = null; //abort
					}
				}
				else
				{
					hot = null ;//abort
				}
			}
		}

	nearbyEdges.removeAll(toRemove);	
}

public AdaptiveDensifyEdge findForwards(AdaptiveDensifyEdge e, List someEdges)
{
	Iterator it = someEdges.iterator();
	while (it.hasNext())
	{
		AdaptiveDensifyEdge e2 = (AdaptiveDensifyEdge) it.next();
		if ( e2.c1.equals(e.c2) )
		{
				return e2;
		}
	}	
	return null;
}

public AdaptiveDensifyEdge findBackwards(AdaptiveDensifyEdge e, List someEdges)
{
	Iterator it = someEdges.iterator();
	while (it.hasNext())
	{
		AdaptiveDensifyEdge e2 = (AdaptiveDensifyEdge) it.next();
		if ( e2.c2.equals(e.c1) )
		{
				return e2;
		}
	}	
	return null;
}

	// calculate the angle the joint between firstEdge and forwardEdge
	//   firstEdge.c1 , firstEdge.c2 = forwardEdge.c1,  forwardEdge.c2
	// use dot product
public double angle(AdaptiveDensifyEdge firstEdge, AdaptiveDensifyEdge forwardEdge)
{
		//reverse first egde in dot product
	double v_x = firstEdge.c1.x - firstEdge.c2.x; 
	double v_y = firstEdge.c1.y - firstEdge.c2.y; 
	
	double w_x = forwardEdge.c2.x - forwardEdge.c1.x;
	double w_y = forwardEdge.c2.y - forwardEdge.c1.y;
	
	double dotProd = v_x*w_x + v_y*w_y;
	double lenV = Math.sqrt( v_x*v_x + v_y*v_y);
	double lenW = Math.sqrt( w_x*w_x + w_y*w_y);
	double invCos = dotProd/ (  lenV * lenW );
	
	return Math.acos(invCos);
	
}

	//remove e, edge before e, and edge after e from nearbyEdges	    
public void removeRedundant2(AdaptiveDensifyEdge e, List nearbyEdges)
{
	nearbyEdges.remove(e);

	Iterator it = nearbyEdges.iterator();
	while (it.hasNext())
	{
		AdaptiveDensifyEdge e2 = (AdaptiveDensifyEdge) it.next();
		if (e2.c1.equals(e.c1) || e2.c1.equals(e.c2) )
		{
				it.remove();
		}
		else if (e2.c2.equals(e.c1) || e2.c2.equals(e.c2) )
		{
			it.remove();
		}
	}	
}

	
public void densify_process(AdaptiveDensifyEdge e, List nearbyEdges)
{
	removeRedundant(e,nearbyEdges);
	double minDistance = 999999999;
	
	Iterator it = nearbyEdges.iterator();
	while (it.hasNext())
	{
		AdaptiveDensifyEdge e2 = (AdaptiveDensifyEdge) it.next();
		double dist = distance_seg_seg(e2.c1,e2.c2,    e.c1, e.c2);
		if (dist < minDistance)
			minDistance = dist;
	}
	//System.out.println("min dist = "+minDistance);
	
	if (minDistance<99999999)
	{
		//might need to subdivide this edge
		if (minDistance <e.length()*rescalingCooefficience)
		{
			//need to subdivide
			subdivide_edge(e, minDistance/rescalingCooefficience);
			return;
		}
	}
	emitPoint(e.c1);
}

public void subdivide_edge(AdaptiveDensifyEdge e, double size)
{
	if (size < MINSUBDIVIDESIZE)
		size = MINSUBDIVIDESIZE;
		
	int ndivisions = (int) (e.length()/size-1);
	
	if (ndivisions <1)
		ndivisions =1;
		
	//allways emit first point, never emit last
	
	emitPoint(e.c1);
	for (int t=0;t<ndivisions;t++)
	{
		double r = ((double)t+1)/((double)ndivisions+1);
		Coordinate c=	new Coordinate (
						    e.c1.x + r* (e.c2.x-e.c1.x),
					        e.c1.y + r* (e.c2.y-e.c1.y)
							  );
		emitPoint(c);
	}					
	
}
	    
	    // run the densify algorithm
public void densify()
{
			    //set up sptial tree
    STRtree strTree = new STRtree();
    for (int t=0;t<allEdges.size(); t++)
    {
		    AdaptiveDensifyEdge e = (AdaptiveDensifyEdge) allEdges.get(t);
		    if (!(e.pseudoEdge))        //dont put in the pseudo nodes
		   		 strTree.insert( e.getEnvelope() ,e);
    }


	    for (int t=0;t<allEdges.size(); t++)
	    {
		    AdaptiveDensifyEdge e = (AdaptiveDensifyEdge) allEdges.get(t);
			if (!(e.pseudoEdge))        //dont put in the pseudo nodes
			{
		   		 double d= e.length();
		   		 Envelope env = e.getEnvelope();
		   		 env.init(      env.getMinX() - d*searchDistanceCoefficient, env.getMinX() + d*searchDistanceCoefficient, 
		   		 				env.getMinY() - d*searchDistanceCoefficient, env.getMinY() + d*searchDistanceCoefficient);
		   		 //get nearby edges
		   		 List nearbyEdges = strTree.query(env);
				densify_process(e,nearbyEdges);
			}
			else
			{
				closeRing(); // pseudo-edge --> close the ring
			}
	    }
		formPolygon();
   }
		   
// find the distance from AB to CD
public double distance_seg_seg(Coordinate A, Coordinate B, Coordinate C, Coordinate D)
{

	double	s_top, s_bot,s;
	double	r_top, r_bot,r;


//printf("seg_seg [%g,%g].[%g,%g]  by [%g,%g]->[%g,%g]  \n",A->x,A->y,B->x,B->y, C->x,C->y, D->x, D->y);
		//A and B are the same point

	if (  ( A.x == B.x) && (A.y == B.y) )
		return distance_pt_seg(A,C,D);

		//U and V are the same point

	if (  ( C.x == D.x) && (C.y == D.y) )
		return distance_pt_seg(D,A,B);

	// AB and CD are line segments
	/* from comp.graphics.algo

	Solving the above for r and s yields
				(Ay-Cy)(Dx-Cx)-(Ax-Cx)(Dy-Cy)
			 r = ----------------------------- (eqn 1)
				(Bx-Ax)(Dy-Cy)-(By-Ay)(Dx-Cx)

			(Ay-Cy)(Bx-Ax)-(Ax-Cx)(By-Ay)
		s = ----------------------------- (eqn 2)
			(Bx-Ax)(Dy-Cy)-(By-Ay)(Dx-Cx)
	Let P be the position vector of the intersection point, then
		P=A+r(B-A) or
		Px=Ax+r(Bx-Ax)
		Py=Ay+r(By-Ay)
	By examining the values of r & s, you can also determine some other limiting conditions:
		If 0<=r<=1 & 0<=s<=1, intersection exists
		r<0 or r>1 or s<0 or s>1 line segments do not intersect
		If the denominator in eqn 1 is zero, AB & CD are parallel
		If the numerator in eqn 1 is also zero, AB & CD are collinear.

	*/
	r_top = (A.y-C.y)*(D.x-C.x) - (A.x-C.x)*(D.y-C.y) ;
	r_bot = (B.x-A.x)*(D.y-C.y) - (B.y-A.y)*(D.x-C.x) ;

	s_top = (A.y-C.y)*(B.x-A.x) - (A.x-C.x)*(B.y-A.y);
	s_bot = (B.x-A.x)*(D.y-C.y) - (B.y-A.y)*(D.x-C.x);

	if  ( (r_bot==0) || (s_bot == 0) )
	{
		return (
			min(distance_pt_seg(A,C,D),
				min(distance_pt_seg(B,C,D),
					min(distance_pt_seg(C,A,B),
						distance_pt_seg(D,A,B)    ) ) )
			 );
	}
	s = s_top/s_bot;
	r=  r_top/r_bot;

	if ((r<0) || (r>1) || (s<0) || (s>1) )
	{
		//no intersection
		return (
			min(distance_pt_seg(A,C,D),
				min(distance_pt_seg(B,C,D),
					min(distance_pt_seg(C,A,B),
						distance_pt_seg(D,A,B)    ) ) )
			 );

	}
	else
		return -0; //intersection exists

}

public double min (double a, double b)
{
	if (a<b)
		return a;
	else
		return b;
}
//distance from p to line A->B
public double distance_pt_seg( Coordinate p, Coordinate A, Coordinate B)
{
	double	r,s;


	//if start==end, then use pt distance
	if (  ( A.x == B.x) && (A.y == B.y) )
		return p.distance(A);

	//otherwise, we use comp.graphics.algorithms Frequently Asked Questions method

	/*(1)     	      AC dot AB
			    r = ---------
					||AB||^2
		r has the following meaning:
		r=0 P = A
		r=1 P = B
		r<0 P is on the backward extension of AB
		r>1 P is on the forward extension of AB
		0<r<1 P is interior to AB
	*/

	r = ( (p.x-A.x) * (B.x-A.x) + (p.y-A.y) * (B.y-A.y) )/( (B.x-A.x)*(B.x-A.x) +(B.y-A.y)*(B.y-A.y) );

	if (r<0)
		return (p.distance(A));
	if (r>1)
		return(p.distance(B));


	/*(2)
			(Ay-Cy)(Bx-Ax)-(Ax-Cx)(By-Ay)
		s = -----------------------------
					L^2

		Then the distance from C to P = |s|*L.

	*/

	s = ((A.y-p.y)*(B.x-A.x)- (A.x-p.x)*(B.y-A.y) )/ ((B.x-A.x)*(B.x-A.x) +(B.y-A.y)*(B.y-A.y) );

	return Math.abs(s) * Math.sqrt(((B.x-A.x)*(B.x-A.x) +(B.y-A.y)*(B.y-A.y) ));
}

	    // add a point to the current ring
	public void emitPoint(Coordinate c)
	{
		currentRing.add(c);
		npoints++;
	}
	
		// end of points in this ring.  Ensure 1st point = last point
	public void closeRing()
	{
		if (currentRing.size() < 3)
			throw new IllegalStateException("AdpativeDensify:: ring has <3 points!!");
		Coordinate first,last;
		first = (Coordinate) currentRing.get(0);
		last = (Coordinate) currentRing.get(currentRing.size()-1);
		if (!(first.equals2D(last)))
		{
			currentRing.add(first);
		}
		allRings.add(currentRing);
		currentRing = new ArrayList();
	}
	
		//have a list of rings (allRings).  1st = external ring, rest = holes
	public void formPolygon()
	{
System.out.println("emitted points ="+npoints);
		if (currentRing.size() != 0)
		{
			throw new IllegalStateException("AdpativeDensify::formPolygon called with active ring.  Call closeRing() first!");
		}
		if (allRings.size() <1)
		{
			throw new IllegalStateException("AdpativeDensify::formPolygon called with no rings rings!");
		}
		ArrayList ring = (ArrayList) allRings.get(0);
		Coordinate[] ringCoords = (Coordinate[]) ring.toArray(new Coordinate[1]);
		LinearRing external = new LinearRing(ringCoords,inputPolygon.getPrecisionModel(), inputPolygon.getSRID() );
		
		LinearRing[] holes = new LinearRing[allRings.size()-1];
		for (int t=1; t<allRings.size(); t++) // for each hole
		{
					ArrayList Aring = (ArrayList) allRings.get(t);
					Coordinate[] AringCoords = (Coordinate[]) Aring.toArray(new Coordinate[1]);
					LinearRing hole = new LinearRing(AringCoords,inputPolygon.getPrecisionModel(), inputPolygon.getSRID() );
					holes[t-1] = hole;
		}
		
		if (allRings.size() == 1)
		{
			//no holes polygon
			outputPolygon = new Polygon(external,inputPolygon.getPrecisionModel(), inputPolygon.getSRID() );
		}
		else
		{
			//multiholes
			outputPolygon = new Polygon(external,holes,inputPolygon.getPrecisionModel(), inputPolygon.getSRID() );
		}
		
		//this actually takes a bit of time!
		if (!(outputPolygon.isValid()))
		{
			
			throw new IllegalStateException("AdpativeDensify::formPolygon created a polygon thats invalid!");
		} 
		
	}
	    
}
