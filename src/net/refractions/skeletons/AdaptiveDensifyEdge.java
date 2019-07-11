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


import com.vividsolutions.jts.geom.*;

public class AdaptiveDensifyEdge
{
	Coordinate c1,c2;
	public boolean subdivide =true;
	public boolean pseudoEdge =false ;// ring boundary edges - ignore me!
	
	public AdaptiveDensifyEdge()
	{
		c1 = null;
		c2 =null;
		subdivide = false;
		pseudoEdge = true;
	}
	
	
	public AdaptiveDensifyEdge(Coordinate c1,Coordinate c2,boolean subdivide )
	{
		this.c1 = c1;
		this.c2 = c2;
		this.subdivide = subdivide;
	}
	
	public  Envelope getEnvelope()
	{
		return new Envelope(c1,c2);
	}
	
	public double length()
	{
		return c1.distance(c2);
	}
}
