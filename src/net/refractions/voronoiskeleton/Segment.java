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
 * A voronoi segment that contains two points that are the start and end of the line segment,
 * two points(refPointA, refPointB) that caused the generation of the segment, and extra 
 * meta information used for trimming.
 * 
 *   During creation, the end points (p1 and p2) will not be valid.
 */
final public class Segment 
{
	public Point p1;
	public Point p2;
	boolean done =false; // should turn this to true when both end points are finished
				// this is not kept up to date very well.  Dont rely on it.
				
	public HashKey hk;  // hash key for puting this in the seghash
	
	public Point refPointA;			// one of the reference points that caused the segment
	public Point refPointB;			// one of the reference points that caused the segment
	public boolean keepMe;			// indicates that this must be kept, also a special flag
	public boolean trashMe;			// used for clipping
	


	public String toString()
	{
		return "(p1="+p1+",p2="+p2+")";
	}
	
	/**
	 * for writing to shapefile
	 * @return
	 */
	public String toStringFull()
	{
		return "hk="+hk.key+",refPointA="+refPointA+",refPointB="+refPointB+",keepme="+keepMe+", trashMe="+trashMe+"done="+done;
	}


	/**
	 * @param a
	 * @param b
	 */
	public Segment(Point a, Point b)
	{
		p1 = a;
		p2 = b;
		done = false;
		keepMe = false;
		trashMe = false;
	}
	
	
	/**
	 * @param a
	 */
	public Segment(Point a)
	{
		p1 = a;
		p2 = new Point (-99999,-99999);  // will get filled in later as arc progresses
		done = false;
		keepMe = false;
		trashMe = false;
	}
	
	
	
	/**
	 * @param p
	 * Finish the segment with this new point.
	 */
	public void finish(Point p)
	{
		// Set the end point and mark as "done."
		if (!done) 
		{ 
			p2 = p; 
			done = true;
		}
	}
}
