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
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jump.feature.Feature;

import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *  Helper class for the {@link StreamNetwork}.  Also see {@link NetworkEdge}.
 *  Basically, a NetworkNode has a location and a set of edge comming into/out of it.
 */
public class NetworkNode implements Serializable 
{
	public NetworkHashableCoordinate coord;
	//public double x;
	//public double y;
	public List edges;
	public boolean mainStem = false;;
	public int weight;
	public boolean visited;

  
	public NetworkNode() {
		edges = new ArrayList();
	}
  
	public NetworkNode(double x1, double y1)
	{
		//x = x1;
		//y = y1;
		coord = new NetworkHashableCoordinate(x1, y1);
	}
  
	public NetworkNode(Coordinate c) {
		this();
		//this.x = c.x;
		//this.y = c.y;
		coord = new NetworkHashableCoordinate(c.x, c.y);
	}
    
	public int degree() {
		return(edges.size());  
	}
   
	public Iterator itr() {
		return(edges.iterator());  
	}

  
	public List edges() 
	{
		List edges2 = new ArrayList();
		edges.addAll(edges);
		return(edges2);
	}
  
  
	public boolean isInitial() {
		return(edges.size() == 0); 
	}
  
	public boolean isTerminal() {
		return(edges.size() == 0); 
	}
  
	public boolean equals(Object obj) 
	{
		if (obj instanceof NetworkNode) 
			return(coord.equals(((NetworkNode)obj).coord));
		return(false);
	}
   
	public int hashCode() 
	{
		long v = Double.doubleToLongBits(coord.x + coord.y);
		//long v = Double.doubleToLongBits(x + y);
		return((int)(v^(v>>>32)));
	}
  
//  public Geometry asGeometry() {
//    return(new Point(coord.toCoordinate(), new PrecisionModel(), 0));
//  }
    
//  public Feature asFeature() {
//    return(new NodeFeature(this));  
//  }


	public String toString () {
		return coord.toString();
	}
  
  private void readObject(ObjectInputStream in) {
    try {
      in.defaultReadObject();
      edges = new ArrayList();
    }
    catch(Exception e) {
      e.printStackTrace();
    } 
  }
  
  public double distance(Coordinate c) {
    return(Math.sqrt((coord.x-c.x)*(coord.x-c.x) + (coord.y-c.y)*(coord.y-c.y)));  
  }
}
