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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.PrecisionModel;


public class SkelLineString extends LineString {

    public Coordinate refPointA = null;
    public Coordinate refPointB = null;
    
    private Double width = null;
    
	public boolean keepMe = false;

	public SkelLineString(LineString other) {
	  this(other.getCoordinates(), other.getPrecisionModel(), other.getSRID());	
	}
	
	public SkelLineString(Coordinate[] arg0, PrecisionModel arg1, int arg2) {
		super(arg0, arg1, arg2);
		
	}
	
	public SkelLineString(Coordinate[] arg0, PrecisionModel arg1, int arg2, boolean rem) {
		super(arg0, arg1, arg2);
		
		keepMe = rem;
	}
	
	public Double getWidth() {
		if (width == null) {
			if (refPointA == null || refPointB == null) width = new Double(1);
			else {
				double x = (refPointA.x + refPointB.x) / 2;
				double y = (refPointA.y + refPointB.y) / 2;
				
				Coordinate middle = new Coordinate(x, y);
				double d1 = refPointA.distance( middle ) + refPointB.distance( middle );
				double d2 = refPointB.distance( refPointA );
				
				width = new Double(Math.min(d1, d2));	
			}
	  }
	  return(width);
	}
	
	public void setWidth(Double width) {
	  this.width = width;	
	}
	
	
	
}
