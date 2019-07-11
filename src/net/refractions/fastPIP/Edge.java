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

import java.util.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.index.strtree.STRtree;
/**
 *
 * @author dblasby
 *
 *    Simple storage for an edge
 */

public class Edge
{
	public double x1,y1;
	public double x2,y2;
	
	LineString line = null;
	
	public Edge(double x1,double y1, double x2, double y2)
	{
		this.x1 = x1;
		this.x2 = x2;
		this.y1 = y1;
		this.y2 = y2;
	}
	
	public void computeLine()
	{
		if (line == null)
		{
			Coordinate[] cs = new Coordinate[2];
			cs[0] = new Coordinate(x1,y1);
			cs[1] = new Coordinate(x2,y2);
			
			line = new LineString(cs,new PrecisionModel(), 0 );
		}
	}
	
	public Envelope getEnvelope()
	{
		return new Envelope(x1,x2,y1,y2);
	}
}
