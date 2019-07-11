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

package net.refractions.fastPIP;


/**
 * 
 * @author dblasby
 *  
 * 
 *   This is a companion to FastPIP.  
 *  
 *  To find out if a segment (2 point line) in inside a polygon:
 *     a. test its two endpoints if they are inside the polygon (fastPIP)
 *     b. find all the polygon ring (exterior and holes) segments that
 *        are nearby the edge
 *     c. if there are none --> segment is inside polygon
 *     d. if there are, then test to see if the test segment intersects
 *        any of these segments.  If so -> segment outside (touches) polygon
 * 
 * NOTE: YOU DONT WANT TO CALL THIS ON LONG SEGMENTS OR ON A POLYGON WITH LONG
 *       EDGES.  It will give the correct answer, but it might be slow.
 * 
 *     Analyse a polygon 
 *        + put all the edges (holes and exterior) into an STRTree
 *        + query STRtree with segment bbox
 *        + if 0 results segment is inside polygon
 *        + if >0, then compute intersection of the lines.  if it intersects any of them,
 *           then segment is NOT inside polygon
 * 
 * NOTE: callers responsibility to test to see if the end points are
 *       inside the polygon or not.
 * 
 * NOTE: holes cannot share an edge
 */

import java.util.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.index.strtree.STRtree;

public class FastSegInPolygon
{
	STRtree segTree = new STRtree();
	
	public FastSegInPolygon(Polygon p)
	{
		LineString exteriorRing =p.getExteriorRing();
		handleRing(exteriorRing);
		
		for (int t=0;t<p.getNumInteriorRing();t++)
		{
			LineString ls = p.getInteriorRingN(t);
			handleRing(ls);
		}
	}
	
	private void handleRing(LineString ls)
	{		
			Coordinate[] coords = ls.getCoordinates();
			int n = ls.getNumPoints();
			for (int t=0;t<(n-1);t++)
			{
				Edge e= new Edge(coords[t].x, coords[t].y, coords[t+1].x, coords[t+1].y );
				segTree.insert(e.getEnvelope(), e);
			}
	}
	
	/**
	 *   Tests to see if the segment c1->c2 inside the polygon.
	 *   
	 *   c1->c2 should be "small". 
	 *   
	 *   ASSUMES c1 and c2 ARE INSIDE THE POLYGON (use FastPIP).
	 *    
	 * @param c1
	 * @param c2
	 */
	public boolean testSegment(Coordinate c1, Coordinate c2)
	{
		Envelope searchEnv = new Envelope(c1,c2);
		
		List possibleEdges = segTree.query(searchEnv);
		
		if (possibleEdges.size() ==0)
			return true;
			
			Coordinate[] cs = new Coordinate[2];
			cs[0] = c1;
			cs[1] = c2;
			
			LineString line = new LineString(cs,new PrecisionModel(), 0 );
			
		Iterator it = possibleEdges.iterator();
		
		while (it.hasNext())
		{		
			Edge e = (Edge) it.next();
			e.computeLine();
			if (line.intersects(e.line))
				return false;
		}
		
		return true;
		
		
	}
}
