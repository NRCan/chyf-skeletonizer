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

import com.vividsolutions.jts.geom.Coordinate;

public class NetworkHashableCoordinate implements Serializable {
  public double x, y;
  
  public NetworkHashableCoordinate(double x, double y) {
    this.x = x; this.y = y;  
  }
  
  public boolean equals(Object other) {
    if (other instanceof NetworkHashableCoordinate) {
      NetworkHashableCoordinate c = (NetworkHashableCoordinate)other;
      return(x == c.x && y == c.y); 
    }  
    return(false);
  }
  
  public int hashCode() {
    long v = Double.doubleToLongBits(x+y);
    return((int)(v^(v>>>32)));
  } 
  
  public String toString() {
    return("(" + x + "," + y + ")"); 
  }
  
  public Coordinate toCoordinate() {
    return(new Coordinate(this.x, this.y));  
  }
  
  public double distance(NetworkHashableCoordinate c) {
    return(Math.sqrt((x-c.x)*(x-c.x) + (y-c.y)*(y-c.y)));  
  }
}