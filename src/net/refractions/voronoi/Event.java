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
public class Event 
{
	public Point 	point;
	public Arc 		arc;
	public double 	x_pos;
	public boolean 	valid;
	public Event	next, prev;		// the event keys used for eventQueue lookup
	public HashKey	hk;
	
	public Event(Event e)
	{
		point = e.point;
		arc = e.arc;
		x_pos = e.x_pos;
		valid = e.valid;
		hk = e.hk;
		next = null;
		prev = null;
	}
	
	public Event(Point p, Arc a, boolean v)
	{
		point = p;
		arc = a;
		x_pos = p.x;
		valid = v;
		next = null;
		prev = null;
	}
	
	public Event(Point p, Arc a, double x, boolean v)
	{
		point = p;
		arc = a;
		x_pos = x;
		valid = v;
		next = null;
		prev = null;
	}
	
	public Event(Point p, Arc a, double x)
	{
		point = p;
		arc = a;
		x_pos = x;
		valid = true;
		next = null;
		prev = null;
	}
	
	public Event(Point p, Arc a)
	{
		point = p;
		arc = a;
		x_pos = 0;
		valid = true;
		next = null;
		prev = null;
	}
	
}
