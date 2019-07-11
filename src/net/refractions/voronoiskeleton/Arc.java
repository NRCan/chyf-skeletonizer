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

package net.refractions.voronoiskeleton;

/**
 * @author bowens
 *
 * A parabolic arc that is represented by a point. It also has its neighbours stored with it.
 * 
 * 
 *   This is actually a double-linked list of arcs.
 *    List is from bottom to top (-y to +y).
 *   
 */
final public class Arc 
{
	public static int		id;  // unique for each arc
	
	Point 	point;   // what point this arc is generated from (site point)
	
	
	Arc		next, prev;  //linked list pointers
	
	Event	event;			// the event key used for the actual event lookup
	                       // this is a circle that has this arc's point on its circumference
	                       
	Segment	s0, s1; // voronoi segments this arc creates.
	
	HashKey	hk;    // based on arc hash.
	
	
	/**
	 * 
	 */
	public Arc()
	{
		id++;
	}
	
	
	/**
	 * @param p
	 */
	public Arc(Point p)
	{
		id++;
		point = p;
	}
	
	
	/**
	 * @param p
	 * @param prev
	 */
	public Arc (Point p, Arc prev)
	{
		id++;
		point = p;
		this.prev = prev;
		next = null;
	}
	
	
	/**
	 * @param p
	 * @param prev
	 * @param next
	 */
	public Arc (Point p, Arc prev, Arc next)
	{
		id++;
		point = p;
		this.prev = prev;
		this.next = next;
	}
	
	public String toString()
	{
					String result =  "key="+hk.key+", pt="+point;
					if (prev != null)
							result += ", prev="+prev.hk.key;
					else
							result += ", prev=null";
									
					if (next != null)
						result += ", next="+next.hk.key;
					else
						result += ", next=null";
					
					if (s0 != null)
							result += ", s0="+s0;
					else
							result += ", s0=null";		
					if (s1 != null)
							result += ", s1="+s1;
					else
							result += ", s1=null";			
					return result;
	}

}
