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

import java.util.Comparator;
import com.vividsolutions.jts.geom.Coordinate;


public class PointSortY implements Comparator
{
     public  int compare(Object o1, Object o2)
     {
		Coordinate c1 = (Coordinate) o1;
		Coordinate c2 = (Coordinate) o2;
		
		if (c1.y<c2.y)
			return -1;
		if (c1.y>c2.y)
			return 1;
		if (c1.x < c2.x)
			return -1;
		if (c1.x > c2.x)
			return 1;
		return 0;//equal         	
     }
 
		
}
