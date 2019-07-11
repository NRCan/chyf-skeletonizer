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
public class Arc 
{
	public static int		id;
	Point 	point;
	Arc		next, prev;
	Event	event;			// the event key used for the actual event lookup
	Segment	s0, s1;
	HashKey	hk;
	
	public Arc()
	{
		id++;
	}
	
	public Arc(Point p)
	{
		id++;
		point = p;
	}
	
	public Arc (Point p, Arc a)
	{
		id++;
		point = p;
		prev = a;
		next = null;
	}
	
	public Arc (Point p, Arc a, Arc b)
	{
		id++;
		point = p;
		prev = a;
		next = b;
	}

}
