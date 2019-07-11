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

package net.refractions.voronoi;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * @author bowens
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class Voronoi 
{
	private int			globalCount = 0;
	public boolean		noOutsideEdges = false;		// set to true if you want bounding box clipping
	
	private EventQueue 	siteEventQ;
	private PointQueue 	pointListQ;
	private EventHash	eventHash;
	private SegmentHash	segHash;
	private ArcHash		arcHash;
	private Arc 		arcRoot;
	private Point		firstPoint;					// used to remove duplicate end point
	
	public LinkedList	outputSegments;				// an array list of Segments
	
	// bounding box coords
	public double bb_x0 = 0;
	public double bb_x1 = 0;
	public double bb_y0 = 0;
	public double bb_y1 = 0;
	
	private boolean firstRun;	// used to remove the duplicate first/last points that can be generated with jts geometries
	
/******************************************************************
 * 
 * 
 *Description: user calls this to enter the points
 ******************************************************************/
	
	public Voronoi()
	{
		siteEventQ = new EventQueue();
		pointListQ = new PointQueue();
		outputSegments = new LinkedList();
		eventHash = new EventHash();
		segHash = new SegmentHash();
		arcHash = new ArcHash();
		
		firstRun = true;
	}
	
	
	public void addPoint(Point p)
	{
		if (pointListQ.size() == 0)
			firstPoint = p;
		else
		{
			if (firstPoint != null)
				if (p.x == firstPoint.x && p.y == firstPoint.y)
					return;
		}
		
		pointListQ.insert(p);

		if (firstRun)
		{
			bb_x0 = p.x;
			bb_x1 = p.x;
			bb_y0 = p.y;
			bb_y1 = p.y;
			firstRun = false;
		}
		//System.out.println("x=" + p.x + ", y=" + p.y);
		// Keep track of bounding box size.
		if (p.x < bb_x0) bb_x0 = p.x;
		if (p.y < bb_y0) bb_y0 = p.y;
		if (p.x > bb_x1) bb_x1 = p.x;
		if (p.y > bb_y1) bb_y1 = p.y;

			
	}
	/*		// O(n^4) so don't use me
	public void delaunay()
	{
		int n = pointListQ.size();
		double x[] = new double[n];
		double y[] = new double[n];
		double z[] = new double[n];
		int i, j, k, m;
		double xn, yn, zn;
		boolean flag = false;
		
		System.out.println("num points=" + n);
		System.out.println("bb_x0=" + bb_x0);
		
		for (int b=0; b<n; b++)
		{
			Point p = pointListQ.pop();
			System.out.println("b:" + b + "   p.x=" + p.x + " p.y=" + p.y);
			x[b] = p.x;
			y[b] = p.y;
			z[b] = x[b]*x[b] + y[b]*y[b];
		}
		
		for (i=0; i<n-2; i++)
		{
			for (j=i+1; j<n; j++)
			{
				for (k=i+1; k<n; k++)
				{
					if (j != k)
					{
						xn = (y[j]-y[i])*(z[k]-z[i]) - (y[k]-y[i])*(z[j]-z[i]);
						yn = (x[k]-x[i])*(z[j]-z[i]) - (x[j]-x[i])*(z[k]-z[i]);
						zn = (x[j]-x[i])*(y[k]-y[i]) - (x[k]-x[i])*(y[j]-y[i]);
						
						if (zn < 0)
						{
							flag = true;
							for (m=0; m<n; m++)
							{
								if (flag && ((x[m]-x[i])*xn + 
											 (y[m]-y[i])*yn + 
											 (z[m]-z[i])*zn <= 0))
									flag = true;
								else
									flag = false;
							}
						}
						if (flag)
						{
							Segment s1 = new Segment(new Point(x[i], y[i]), new Point(x[j], y[j]));
							if(!outputSegments.add(s1))
								System.out.println("problem inserting");
							Segment s2 = new Segment(new Point(x[j], y[j]), new Point(x[k], y[k]));
							if(!outputSegments.add(s2))
								System.out.println("problem inserting");
							Segment s3 = new Segment(new Point(x[k], y[k]), new Point(x[i], y[i]));
							if(!outputSegments.add(s3))
								System.out.println("problem inserting");
						}
					}
				}// for 3
			}// for 2
		}// for 1
	}
*/
	
	
	public void run()
	{
		//pointListQ.printCoords(pointListQ.root);
		int count = 0;
		System.out.println("RUNNING");
		while (pointListQ.size() > 0)
		{
			count++;
			// if the next event type to process is an event(circle) and not a point
			if (siteEventQ.size()>0 && ((Event)eventHash.get(((Event)siteEventQ.peak()).hk)).x_pos <= pointListQ.peak().x)
				process_event();
			else
				process_point();
			//System.out.println("Count1=" + count);
		}
		//System.out.println("Final count1=" + count);
		
		count = 0;
		// finish with the circle points
		while (siteEventQ.size()>0)
		{
			process_event();
			count++;
			//System.out.println("Count2=" + count);
		}
		//System.out.println("Final count2=" + count);
			
		finish_edges(); // Clean up dangling edges.
	}
	
	
	private void process_point()
	{
		//System.out.println("process_point");
		
		// Get the next point from the queue.
		Point p = (Point)pointListQ.pop();
		// Add a new arc to the parabolic front.
		front_insert(p);
	}
	
	
	private void process_event()
	{
		//System.out.print("process_event");
		
		Event e = (Event)eventHash.remove(siteEventQ.root.hk);
		Event trash = siteEventQ.pop();
		
		if (e.valid)
		{
			// Start a new edge.
			Segment s = new Segment(e.point);
			s.hk = new HashKey(segHash.getNewKey());
			segHash.put(s.hk, s);
			outputSegments.add(s);
			
			// Remove the associated arc from the front.
			Arc a = (Arc)arcHash.get(e.arc.hk);
			if (a.prev != null)
			{
				Arc ap = (Arc)arcHash.remove(a.prev.hk);
				Arc an;
				if (a.next != null)
					an = (Arc)arcHash.get(a.next.hk);
				else
					an = null;

				ap.next = an;
				ap.s1 = s;
				arcHash.put(ap.hk, ap);
			}
			if (a.next != null)
			{
				Arc an = (Arc)arcHash.remove(a.next.hk);
				Arc ap;
				if (a.prev != null)
					ap = (Arc)arcHash.get(a.prev.hk);
				else
					ap = null;

				an.prev = ap;
				an.s0 = s;
				arcHash.put(an.hk, an);
			}
			
			// Finish the edges before and after a.
			if (a.s0 != null)
			{
				Segment sx0 = (Segment)segHash.remove(a.s0.hk);
				sx0.finish(e.point);
				segHash.put(sx0.hk, sx0);
			}
			
			if (a.s1 != null)
			{
				Segment sx1 = (Segment)segHash.remove(a.s1.hk);
				sx1.finish(e.point);
				segHash.put(sx1.hk, sx1);
			}
			
			// Recheck circle events on either side of p:
			if (a.prev != null)
			{
				Arc ax = (Arc)arcHash.get(a.prev.hk);
				new_check_circle_event(ax, e.x_pos);
			}
			if (a.next != null)
			{
				Arc ax = (Arc)arcHash.get(a.next.hk);
				new_check_circle_event(ax, e.x_pos);
			}
			
		}
	}
	
	
	private void front_insert(Point  p)
	{
		// System.out.println("front_insert");
		
		if (arcRoot == null) 
		{
		  arcRoot = new Arc(p);
		  arcRoot.hk = new HashKey(arcHash.getNewKey());
		  arcHash.put(arcRoot.hk, arcRoot);
		  return;
		}
			
		// Find the current arc(s) at height p.y (if there are any).
		Arc current = (Arc)arcHash.get(arcRoot.hk);
		while (current != null)
		{
			Point z = intersect(p,current);
			if (z != null) 
			{
				Arc ac = (Arc)arcHash.remove(current.hk);
				Point zz = null;
				if (ac.next != null)		// if the point lies on this arc
					zz = intersect(p,(Arc)arcHash.get(ac.next.hk));
				// if a next arc exists
				if (ac.next != null && zz == null)
				{
					Arc anew = new Arc(ac.point,ac,(Arc)arcHash.get(ac.next.hk));
					anew.hk = new HashKey(arcHash.getNewKey());
					arcHash.put(anew.hk, anew);
					Arc acn = (Arc)arcHash.remove(ac.next.hk);
					acn.prev = anew;
					arcHash.put(acn.hk, acn);
					ac.next = anew;
					arcHash.put(ac.hk, ac);
				}
			 	else // no next exists, so create it
			 	{
					Arc anew = new Arc(ac.point,ac);
					anew.hk = new HashKey(arcHash.getNewKey());
					arcHash.put(anew.hk, anew);
					ac.next = anew;
					arcHash.put(ac.hk, ac);
			 	}

			 	//i.next.s1 = i.s1
			 	Arc i = (Arc)arcHash.get(ac.hk);
				Arc in = (Arc)arcHash.remove(i.next.hk);
				Segment sx;
				if (i.s1 != null)
					sx = (Segment)segHash.get(i.s1.hk);
				else
					sx = null;
				in.s1 = sx;
				arcHash.put(in.hk, in);
				//i.next.prev = new arc(p,i,i.next)
				Arc inp = new Arc(p,i,in);
				inp.hk = new HashKey(arcHash.getNewKey());
				arcHash.put(inp.hk, inp);
				in = (Arc)arcHash.remove(in.hk);
				in.prev = inp;
				arcHash.put(in.hk, in);
				//i.next = i.next.prev
				i = (Arc)arcHash.remove(i.hk);
				i.next = (Arc)arcHash.get(inp.hk);
				arcHash.put(i.hk, i);
				//i = i.next;
				i = (Arc)arcHash.get(i.next.hk);	// set current to be the next one
			 	
				// Add new half-edges connected to i's endpoints.
				//new seg(z);
				Segment seg1 = new Segment(z);
				seg1.hk = new HashKey(segHash.getNewKey());
				Arc refAip = (Arc)arcHash.get(i.prev.hk);	// save the reference points in the segment
				Arc refAi  = (Arc)arcHash.get(i.hk);
				seg1.refPointA = refAip.point;
				seg1.refPointB = refAi.point;
				segHash.put(seg1.hk, seg1);
				outputSegments.add(seg1);
				//new seg(z);
				Segment seg2 = new Segment(z);
				seg2.hk = new HashKey(segHash.getNewKey());
				Arc refAin = (Arc)arcHash.get(i.next.hk);	// save the reference points in the segment
				refAi  = (Arc)arcHash.get(i.hk);
				seg2.refPointA = refAi.point;
				seg2.refPointB = refAin.point;
				segHash.put(seg2.hk, seg2);
				outputSegments.add(seg2);
				//i.prev.s1 = new seg(z);
				Arc ip = (Arc)arcHash.remove(i.prev.hk);
				ip.s1 = seg1;
				arcHash.put(ip.hk, ip);
				//i.next.s0 = new seg(z);
				Arc inx = (Arc)arcHash.remove(i.next.hk);
				inx.s0 = seg2;
				arcHash.put(inx.hk, inx);
				//i.s0 = new seg(z)    /    i.s1 = new seg(z)
				i = (Arc)arcHash.remove(i.hk);
				i.s0 = seg1;
				i.s1 = seg2;
				arcHash.put(i.hk, i);
				
				Arc a = (Arc)arcHash.get(i.hk);
				new_check_circle_event(a, p.x);			//I don't think this is needed
				new_check_circle_event((Arc)arcHash.get(a.prev.hk), p.x);
				new_check_circle_event((Arc)arcHash.get(a.next.hk), p.x);
				
				return;
		 	}// end if
		 	
		 	// increment current
		 	Arc curn = (Arc)arcHash.get(current.hk);
		 	if (curn.next != null)
		 		current = (Arc)arcHash.get(curn.next.hk);
		 	else
		 		current = curn.next;	
		}// end while
		
		//for (i = root; i.next; i=i.next) 
		current = arcRoot;
		while (current != null)		// Find the last node.
		{
			// increment loop
			Arc curn = (Arc)arcHash.get(current.hk);
			if (curn.next != null)
				current = (Arc)arcHash.get(curn.next.hk);
			else
				return;
		}
		//i.next = new arc(p,i);
		Arc ax = (Arc)arcHash.remove(current.hk);
		Arc new_arc = new Arc(p, ax);
		new_arc.hk = new HashKey(arcHash.getNewKey());
		arcHash.put(new_arc.hk, new_arc);
		ax.next = new_arc;
		arcHash.put(ax.hk, ax);
		
		//Insert segment between p and i
		//start.x = X0;
		ax = (Arc)arcHash.remove(ax.hk);
		Arc arc_axn = (Arc)arcHash.remove(ax.next.hk);
		//start.y = (i.next->p.y + i.p.y) / 2;
		double y = (arc_axn.point.y + ax.point.y) / 2;
		Point start = new Point(bb_x0, y);
		//new seg(start);
		Segment seg_new = new Segment(start);
		seg_new.hk = new HashKey(segHash.getNewKey());
		segHash.put(seg_new.hk, seg_new);
		outputSegments.add(seg_new);
		//i.s1 = i.next.s0 = new seg(start);
		ax.s1 = seg_new;
		arcHash.put(ax.hk, ax);
		arc_axn.s0 = seg_new;
		arcHash.put(arc_axn.hk, arc_axn);

	}


	private double[] circle(Point a, Point b, Point c)
	{
		double x;

		// Check that bc is a "right turn" from ab.
		if ((b.x-a.x)*(c.y-a.y) - (c.x-a.x)*(b.y-a.y) > 0)
		{
			return null;
		}

		// Algorithm from O'Rourke 2ed p. 189.
		double 	A = b.x - a.x,
				B = b.y - a.y,
				C = c.x - a.x,
				D = c.y - a.y,
				E = A*(a.x+b.x) + B*(a.y+b.y),
				F = C*(a.x+c.x) + D*(a.y+c.y),
				G = 2*(A*(c.y-b.y) - B*(c.x-b.x));
	
		if (G == 0) 
			return null;  // Points are co-linear.
	
		// Point o is the center of the circle.
		double px = (D*E-B*F)/G;
		double py = (A*F-C*E)/G;
	
		// o.x plus radius equals max x coordinate.
		x = px + Math.sqrt( (a.x - px)*(a.x - px) + (a.y - py)*(a.y - py) );	// a^2 + b^2
		double value[] = new double[3];
		value[0] = px;
		value[1] = py;
		value[2] = x;
		return value;
	}
	
	
	
	private void new_check_circle_event(Arc i, double x0)
	{
		Arc ax = (Arc)arcHash.remove(i.hk);
		//if (i.e && i.e.x != x0)
		if (ax.event != null)
		{
			Event ex = (Event)eventHash.get(ax.event.hk);
			if (ex.x_pos != x0)
			{
				ex = (Event)eventHash.remove(ax.event.hk);		// get it
				ex.valid = false;
				eventHash.put(ex.hk, ex);
			}
		}
		//i.e = NULL;
		ax.event = null;
		arcHash.put(ax.hk, ax);
		
		//if (!i.prev || !i.next)
		if (ax.prev == null || ax.next == null)
			return;
		
		//if (circle(i.prev.p, i.p, i.next.p, &x,&o) && x > x0)
		ax = (Arc)arcHash.get(ax.hk);
		Arc axp = (Arc)arcHash.get(ax.prev.hk);
		Arc axn = (Arc)arcHash.get(ax.next.hk);
		double[] ret = circle(axp.point, ax.point, axn.point);
		
		if (ret != null) 
		{
			double px = ret[0];
			double py = ret[1];
			double x =  ret[2];
			if (x > x0)
			{
				//i.e = new event(x, o, i);
				Point o = new Point(px, py);
				Event e = new Event(o, ax, x);
				e.hk = new HashKey(eventHash.getNewKey());
				eventHash.put(e.hk, e);
				ax = (Arc)arcHash.remove(ax.hk);
				ax.event = e;
				arcHash.put(ax.hk, ax);
				//events.push(i.e);
				siteEventQ.insert((Event)eventHash.get(e.hk));
			}
		}	
	}


	private Point intersect(Point p, Arc i)
	{
		Point result = new Point(bb_x0, bb_y0);
		if (i.point.x == p.x) 
			return null;

		double a = 0, b = 0;
		if (i.prev != null) // Get the intersection of i.prev, i.
			a = intersection(i.prev.point, i.point, p.x).y;
		if (i.next != null) // Get the intersection of i.next, i.
			b = intersection(i.point, i.next.point, p.x).y;

		if ((i.prev == null || a <= p.y) && (i.next == null || p.y <= b)) 
		{
			result.y = p.y;

			// Plug it back into the parabola equation.
			result.x = (i.point.x*i.point.x + (i.point.y-result.y)*(i.point.y-result.y) - p.x*p.x) / (2*i.point.x - 2*p.x);
			return result;
	 	}
	   
		return null;
	}

	
	private Point intersection(Point p0, Point p1, double L)
	{
		Point result = new Point(bb_x0, bb_y0);
		Point p = p0;

		if (p0.x == p1.x)
			result.y = (p0.y + p1.y) / 2;
		else if (p1.x == L)
			result.y = p1.y;
		else if (p0.x == L) {
			result.y = p0.y;
			p = p1;
		} 
		else 
		{
			// Use the quadratic formula.
			double z0 = 2*(p0.x - L);
			double z1 = 2*(p1.x - L);
	
			double a = 1/z0 - 1/z1;
			double b = -2*(p0.y/z0 - p1.y/z1);
			double c = (p0.y*p0.y + p0.x*p0.x - L*L)/z0
				   - (p1.y*p1.y + p1.x*p1.x - L*L)/z1;
	
			result.y = ( -b - Math.sqrt(b*b - 4*a*c) ) / (2*a);
		}
		// Plug back into one of the parabola equations.
		result.x = (p.x*p.x + (p.y-result.y)*(p.y-result.y) - L*L)/(2*p.x-2*L);
		return result;
	}


	private void finish_edges()
	{
		// Advance the sweep line so no parabolas can cross the bounding box.
		double L = bb_x1 + (bb_x1-bb_x0) + (bb_y1-bb_y0);

		// Extend each remaining segment to the new parabola intersections.
		Arc current = (Arc)arcHash.get(arcRoot.hk);
		int count = 0;

		while (current != null)
		{
			count++;
			Arc ac = (Arc)arcHash.get(current.hk);
			if (ac.s1 != null)
			{
				//i.s1.finish(intersection(i.p, i.next.p, l*2));
				Arc acn = (Arc) arcHash.get(ac.next.hk);
				Segment sx = (Segment)segHash.remove(ac.s1.hk);
				sx.finish(intersection(ac.point, acn.point, L*2));			
				segHash.put(sx.hk, sx);
			}
			if (current.next == null)
				current = null;
			else
				current = (Arc)arcHash.get(current.next.hk);
			
		}
		//System.out.println("edges=" + count);
	}
	
	
	private boolean inBoundary(Segment s)
	{
		if (s.p1.x < bb_x0 || s.p1.x > bb_x1 || s.p1.y < bb_y0 || s.p1.y > bb_y1)
			if (s.p2.x < bb_x0 || s.p2.x > bb_x1 || s.p2.y < bb_y0 || s.p2.y > bb_y1)
				return false;
		return true;
	}
	
	
	public ArrayList get_output()
	{
		ArrayList out = new ArrayList();
		if (noOutsideEdges)
		{
			for (int i=0; i<outputSegments.size(); i++)
			{
				if (inBoundary((Segment)segHash.get(((Segment)outputSegments.get(i)).hk)))
					out.add((Segment)segHash.get(((Segment)outputSegments.get(i)).hk));
			}
		}
		else
		{
			for (int i=0; i<outputSegments.size(); i++)
			{
				out.add((Segment)segHash.get(((Segment)outputSegments.get(i)).hk));
			}
		}
		return out;
	}
	
	
	public Segment get_seg(int i)
	{
		Segment seg1 = (Segment)outputSegments.get(i);
		Segment seg2 = (Segment)segHash.get(seg1.hk);
		if (seg2 == null)
			System.out.println("null seg returned");
		return seg2;
	}

}
