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

/**
 * @author bowens
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class Segment {
	
	public Point p1;
	public Point p2;
	boolean done;
	public HashKey hk;
	public Point refPointA;			// one of the reference points that caused the segment
	public Point refPointB;			// one of the reference points that caused the segment


	public Segment(Point a, Point b)
	{
		p1 = a;
		p2 = b;
		done = false;
	}
	
	public Segment(Point a)
	{
		p1 = a;
		p2 = new Point (0,0);
		done = false;
	}
	
	
	
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
