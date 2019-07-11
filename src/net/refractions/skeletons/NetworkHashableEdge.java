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

import java.io.Serializable;

/**
 * @author bowens
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class NetworkHashableEdge implements Serializable 
{

	public double x1, x2, y1, y2;	// x and y values for the coordinates defining the edge
	
	

	public NetworkHashableEdge(double xa, double xb, double ya, double yb) 
	{
		super();
		x1 = xa;
		x2 = xb;
		y1 = ya;
		y2 = yb;
	}
	
	
	public boolean equals(Object other) 
	{
		if (other instanceof NetworkHashableEdge) 
		{
			NetworkHashableEdge e = (NetworkHashableEdge)other;
			return(x1 == e.x1 && y1 == e.y1 && x2 == e.x2 && y2 == e.y2); 
		}  
		return(false);
	}


	public int hashCode() 
	{
		long v = Double.doubleToLongBits(x2-x1+y2-y1);
		return((int)(v^(v>>>32)));
	}
	
	
}
