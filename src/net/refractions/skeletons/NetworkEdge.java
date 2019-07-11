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
import java.util.Iterator;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.PrecisionModel;
//import com.vividsolutions.jump.feature.Feature;

public class NetworkEdge implements Serializable 
{
	public NetworkHashableEdge edgeHash;	// network hashable edge used for storage
	public NetworkNode unode = null;    	//upstream node
	public NetworkNode dnode = null;    	//downstream node
	public int id = -1;          			//unique identifier
	public float length = 0f;   			//length of edge
	public boolean visited = false;      	//used when traversing the graph *not used anymore* (actually it is)
	public int weight = 0;
	public boolean mainStem = false;	
	public boolean keep = true;				// all edges are assumed not to be leaves
   
   	public boolean orginallyWasKeep = false; //if the orginal line this is based on was set to keepme
   
	public short useMe = 0; // this will be set to something else if this isnt wanted in the output.
	                              // usually it means its disconnected from the main network.
	                             // see dox on functions that modify this value
	                             
	public Coordinate refPointA=null;
	public Coordinate refPointB=null;
	
	public NetworkEdge() {}
	
	public String toString()
	{
		return  "keep = "+keep+", originallyWasKeep ="+orginallyWasKeep+", mainStem="+mainStem+", visited="+visited+", useMe="+useMe;
	}

	public NetworkEdge(NetworkNode u, NetworkNode d, boolean orig_keepme) 
	{
		unode = u;
		dnode = d;
		edgeHash = new NetworkHashableEdge( unode.coord.x, unode.coord.y, 
											dnode.coord.x, dnode.coord.y);
		orginallyWasKeep = orig_keepme;
	}
   
    public SkelLineString asSkelLineString()
    {
		Coordinate[] cs = new Coordinate[2];
		cs[0] = unode.coord.toCoordinate();
		cs[1] = dnode.coord.toCoordinate();
    	return new SkelLineString(cs , new PrecisionModel() , 0, keep );
    }
   
	public NetworkNode getUpstreamNode()
	{
		return unode;
	}
	
	public NetworkNode getDownstreamNode()
	{
		return dnode;
	}
   
	public List upstreamEdges() {
		return(unode.edges); 
	}
   
	public List downstreamEdges() {
		return(dnode.edges); 
	}
   
	public int getUpstreamDegree()
	{
		return unode.degree();
	}
	
	public int getDownstreamDegree()
	{
		return unode.degree();
	}
   
//   public int nuedges() {
//     return(unode.edges.size());  
//   }
   
//   public NetworkEdge uedge(int i)  {
//     return((NetworkEdge)unode.edges.get(i));  
//   }
//   
//   public Iterator uitr() {
//     return(unode.itr());  
//   }
//   
//   public List dedges() {
//     return(dnode.edges); 
//   }
//   
//   public int ndedges() {
//     return(dnode.edges.size());  
//   }
//   
//   public NetworkEdge dedge(int i) {
//     return((NetworkEdge)dnode.edges.get(i));  
//   }
//   
//   public Iterator ditr() {
//     return(dnode.itr()); 
//   }
   
	public NetworkNode oppositeNode(NetworkNode node) 
	{
		if (node.equals(unode)) 
			return(dnode);
		if (node.equals(dnode)) 
			return(unode);
		return(null);  
	}
   
	public double length() 
	{
		if (length == -1d) 
		{
			double x1 = dnode.coord.x;
			double y1 = dnode.coord.y;
			double x2 = unode.coord.x;
			double y2 = unode.coord.y;
       
			return(Math.sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1)) );
     	} 
		return(length);
	}
   
   
	public boolean equals(Object o) {
		if (o instanceof NetworkEdge) 
		{
			NetworkEdge n = (NetworkEdge)o;
			
			return(unode.equals(n.unode) && dnode.equals(n.dnode));  
		}
		return(false);
	}
   
   
	public int hashCode() {
		return(dnode.hashCode() ^ unode.hashCode());
	}
   
   
	public Geometry asGeometry() {
		Coordinate[] coords = new Coordinate[2];
		coords[0] = new Coordinate(dnode.coord.x, dnode.coord.y);
		coords[1] = new Coordinate(unode.coord.x, unode.coord.y);

		return(new LineString(coords, new PrecisionModel(), 0));
	   }

//   public Feature asFeature() {
//     return(new EdgeFeature(this));
//   }
   
//   public String toString() {
//     return(String.valueOf(id)); 
//   }
   
   
	public double distance(NetworkHashableCoordinate coord) {
		return(
			new LineSegment(
				new Coordinate(unode.coord.x, unode.coord.y),
				new Coordinate(dnode.coord.x, dnode.coord.y)
			).distance(coord.toCoordinate())
		);  
	}
}
