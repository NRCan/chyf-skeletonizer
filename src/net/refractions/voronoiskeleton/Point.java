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

//import java.math.BigDecimal;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * @author bowens
 *
 * A voronoi point that is used as a node in a list.
 * 
 *  These are input point sites.
 * 
 *  They from a double-linked list based on sorting the coordinates
 *   x min is first, for xi=xj then ymin is first.  There are no
 *   points with xi=xj and yi=yj.
 */
final public class Point 
{
	public double	x;  // location
	public double	y;
	public int		metaInfo =-2;	// edge ring-section number
								// numbering from the polgon 
								// see Skeletonizer.numberPolygon();
	
	public Point next, prev;  // linked list.
	
	/**
	 * @param p
	 */
	public Point(Point p)
	{
		x = p.x;
		y = p.y;
		next = null;
		prev = null;
	}
	
	public Coordinate asCoordinate()
	{
		return new Coordinate(x,y);
	}
	
	/**
	 * @param x1
	 * @param y1
	 */
	public Point(double x1, double y1)
	{
		x = x1;
		y = y1;
		next = null;
		prev = null;
	}
	
	
	/**
	 * @param x1
	 * @param y1
	 * @param meta
	 */
	public Point(double x1, double y1, int meta)
	{
		x = x1;
		y = y1;
		next = null;
		prev = null;
		metaInfo = meta;
	}
	
	public String toString()
	{
		return "("+x+","+y+")";
	}
}
