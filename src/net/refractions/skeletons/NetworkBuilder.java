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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import com.vividsolutions.jts.geom.Coordinate;


/**
 * @author bowens
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class NetworkBuilder 
{
	public HashMap nodes;	// hashmap of origional nodes
	public HashMap edges;	// hashset of origional edges
	public HashSet output;	// hashset of the trimmed (kept) edges
	
	public ArrayList leaves; 
	
	// root Node (first edge that is inserted that needs to be kept for recursive traversal)
	public NetworkEdge root = null;
	
	
	
	
	/**
	 * 
	 */
	public NetworkBuilder() 
	{
		nodes = new HashMap();
		edges = new HashMap();
		output = new HashSet();
		leaves = new ArrayList();
	}
	
	
	
	public void addEdge(SkelLineString edge)
	{
		// insert the 2 coords and edge
	}
	
	/**
	 *  remove edges that have start point=end point
	 * @param edgeArray
	 * @return
	 */
	public SkelLineString[] removeNullEdges(SkelLineString[] edgeArray)
	{
		ArrayList result = new ArrayList();
		for (int t=0;t<edgeArray.length;t++)
		{
			SkelLineString line = edgeArray[t];
			Coordinate[] cs = line.getCoordinates();
			Coordinate c1 = cs[0];
			Coordinate c2 = cs[cs.length-1];
//	if (c1.distance(c2) < 0.0001)
//	{
//		System.out.println("ooch");
//	}
			if (! (c1.equals2D(c2)))
			{
				result.add(line);
			}
		}
		return (SkelLineString[]) result.toArray(new SkelLineString[result.size()]);
	}
	
	public void addEdges(SkelLineString[] edgeArray)
	{
		edgeArray = removeNullEdges(edgeArray);



		
		// insert each coord and edge
		//System.out.println("# of incoming edges = " + edgeArray.length);
		for (int i=0; i<edgeArray.length; i++)
		{
			NetworkNode c0 = new NetworkNode(edgeArray[i].getCoordinateN(0));
			NetworkHashableCoordinate c0hk = c0.coord;
			NetworkNode c1 = new NetworkNode(edgeArray[i].getCoordinateN(edgeArray[i].getNumPoints()-1));
			NetworkHashableCoordinate c1hk = c1.coord;


			
			// check to see if the first node exists yet
			if (nodes.get(c0hk) == null)// if it doesn't exist
			{
				c0.mainStem = edgeArray[i].keepMe;
				nodes.put(c0hk, c0);		// insert the new node into the hashMap
			}
			
			// check to see if the second node exists yet
			if (nodes.get(c1hk) == null)// if it doesn't exist
			{
				c1.mainStem = edgeArray[i].keepMe;
				nodes.put(c1hk, c1);		// insert the new node into the hashMap
			}
			
			// create the edge
				//NetworkEdge edge = new NetworkEdge(c0,c1,edgeArray[i].keepMe );
			NetworkEdge edge = new NetworkEdge( (NetworkNode) nodes.get(c0hk),(NetworkNode) nodes.get(c1hk), edgeArray[i].keepMe );
	
			edge.refPointA = edgeArray[i].refPointA;
			edge.refPointB = edgeArray[i].refPointB;

			edge.mainStem = edgeArray[i].keepMe;// mark if river entrance
			
			// if the root doesn't exist and this river is a mainStem
			if (root == null && edge.mainStem)
				root = edge;		// mark it as the root
			
			// give each NetworkNode a link to this edge
			c0 = (NetworkNode)nodes.remove(c0hk);
			c1 = (NetworkNode)nodes.remove(c1hk);
			c0.edges.add(edge);
			c1.edges.add(edge);
			nodes.put(c0hk, c0);
			nodes.put(c1hk, c1);
			
			// insert the edge into the hashMap
			edges.put(edge.edgeHash, edge);
			
		}// end for each edge being inserted
		//System.out.println("# of placed edges = " + edges.size());
	}

	
	
	/**
	 * 
	 * @param edge
	 * @param nextNode
	 * @return
	 * 
	 * Assumes that the incoming edge is a new leaf
	 */
	private void checkParent(NetworkEdge edge)
	{		
		NetworkNode parent;		// the parent node that still has some edges
		
		NetworkNode dn = (NetworkNode)nodes.get(edge.dnode.coord);
		
		if (dn.degree() == 1)
			parent = edge.unode;	// parent is the other node, not the leaf node
		else
			parent = edge.dnode;
			
		//if (parent.degree() != 1)
		//	System.out.println("messed it up");
		
		// mark that we don't want to keep it, then save the edge
		NetworkEdge e = (NetworkEdge)edges.get(edge.edgeHash);
		e.keep = false;
		edges.put(e.edgeHash, e);
		
		// get the node from the hashMap
		 NetworkNode p = (NetworkNode)nodes.remove(parent.coord);
	
		 // edit the node so it no longer contains that edge
		 for (int j=0; j<p.degree(); j++)
		 {
			 // find the edge in the node's list and remove it
			 if (((NetworkEdge)p.edges.get(j)).equals(edge))
				 p.edges.remove(j);		// the list is now updated
		 }
		 nodes.put(p.coord, p);	// put it back into the hashMap
	
		 // check its parent (checkParent() method) : removes parent if it is a new leaf
		 if (p.degree() == 1)
			 checkParent((NetworkEdge)p.edges.get(0));
	}
	
	
	public void trimNetwork()
	{
		// flag all leaves and add them to the queue
		Set s = edges.keySet();
		//Iterator it = s.iterator();
		Object[] objs = s.toArray();
//		System.out.println("leaves:");
		for (int i=0; i<objs.length; i++)
		{
			NetworkHashableEdge e = (NetworkHashableEdge)objs[i];	// get the hashkey
			NetworkEdge edge = (NetworkEdge)edges.remove(e);			// get the edge
			if (!edge.mainStem && (edge.dnode.degree() == 1 || edge.unode.degree() == 1))
			{
				edge.keep = false;			// don't keep it
				leaves.add(edge);
//				System.out.println((int)edge.unode.coord.x + "," + (int)edge.unode.coord.y + "   " + (int)edge.dnode.coord.x + "," + (int)edge.dnode.coord.y);
			}
			edges.put(e, edge);
			
		}
		
//		System.out.println("leaves.size() = " + leaves.size());
		
		// for each leaf in the queue
		for (int i=0; i<leaves.size(); i++)
		{
			// remove it from the queue
			NetworkEdge edge = (NetworkEdge)leaves.get(i);
			NetworkNode parent;
			if (edge.dnode.degree() == 1)
				parent = edge.unode;	// parent is the other node, not the leaf node
			else
				parent = edge.dnode;
			
			// get the node from the hashMap
			NetworkNode p = (NetworkNode)nodes.remove(parent.coord);
			
			// edit the node so it no longer contains that edge
			boolean flag = false;
			for (int j=0; j<p.degree(); j++)
			{
				// find the edge in the node's list and remove it
				if (((NetworkEdge)p.edges.get(j)).equals(edge))
				{
					flag = true;
					//System.out.println("removed edge form the list");
					p.edges.remove(j);		// the list is now updated
				}
			}
			//if (!flag)
			//	System.out.println("this is not good");
			nodes.put(p.coord, p);	// put it back into the hashMap
			
			// check its parent (checkParent() method) : removes parent if it is a new leaf
			if (p.degree() == 1)
			{
				//System.out.println("checking parent");
				checkParent((NetworkEdge)p.edges.get(0));
			}
			
		}// end for each leaf
			
	}
	
	
	
	public NetworkEdge[] getNetworkEdges()
	{
		ArrayList eds = new ArrayList();
		Set s = edges.keySet();
		Object[] objs = s.toArray();
		for (int i=0; i<objs.length; i++)
		{
			NetworkHashableEdge e = (NetworkHashableEdge)objs[i];	// get the hashkey
			NetworkEdge edge = (NetworkEdge)edges.get(e);			// get the edge
			if (edge.keep)
				eds.add(edge);
		}
		NetworkEdge[] e = new NetworkEdge[eds.size()];
		for (int i=0; i<eds.size(); i++)
		{
			e[i] = (NetworkEdge)eds.get(i);
		}
		
//		int size = output.size();
//		NetworkEdge[] e = new NetworkEdge[size];
//		Object[] op = output.toArray();
//		for (int i=0; i<size; i++)
//		{
//			e[i] = (NetworkEdge)op[i];
//		}
		return e;
	}
	
	/**
	 *   
	 *   1. start at a keepme edge
	 *   2. start traversing the network from this edge
	 *   3. when finished traversing, all the non-reachable edges will be marked with useMe=1
	 *      others with useMe =0
	 *
	 */
	public void markUnconnected()
	{
		ArrayList keepmeEdges = new ArrayList();
		
		NetworkEdge[] allEdges = getNetworkEdges();
		NetworkEdge  start_edge = null;  // will eventually be a keepme edge
		
		for (int t=0;t<allEdges.length;t++)
		{
			if (allEdges[t].orginallyWasKeep)
				start_edge = allEdges[t];
			allEdges[t].visited = false;       //set all  edges to unvisited
			allEdges[t].useMe = 0;
		}
		
		if (start_edge == null)
			throw new IllegalStateException("markUnconnected::no keepme edges in network.");
		
		NetworkEdge e = start_edge;
		
		Stack hotEdges = new Stack();
		hotEdges.add(e);
		
		
				// keep looping while there are still edges to visit		
		while (  (!(hotEdges.empty())) )
		{
			e = (NetworkEdge) hotEdges.pop();	

			e.visited = true;
			e.useMe = 1;      // mark as "connected to network"
			
			//put other edges back in stack
			List upstream = e.upstreamEdges();
			Iterator upIt = upstream.iterator();
			while (upIt.hasNext())
			{
				NetworkEdge e2 = (NetworkEdge) upIt.next();
				if (!(e2.visited))
				{
					hotEdges.push(e2);
				}
			}
			List downstream = e.downstreamEdges();
			Iterator downIt = downstream.iterator();
			while (downIt.hasNext())
			{
				NetworkEdge e2 = (NetworkEdge) downIt.next();
				if (!(e2.visited))
				{
					hotEdges.push(e2);
				}
			}
		}
	}
	
	
	/**
	 *   tests the network.
	 *   1. find all "keepme" edges
	 *   2. start at any keepme edge, and start traversing the network
	 *      should be able to get to each of the other keepme edges
	 * 
	 *    if you cannot, the network is disconnected (wrong)
	 *    otherwise, its connected (good)
	 * 
	 *  O(n*m) n = # of edges, m= # keep mes
	 * @return  list of the edges not connected - should have size() ==0 (or error) type =NetworkEdge
	 */
	public ArrayList testConnectivity() 
	{
		ArrayList keepmeEdges = new ArrayList();
		
		NetworkEdge[] allEdges = getNetworkEdges();
		
		for (int t=0;t<allEdges.length;t++)
		{
			if (allEdges[t].orginallyWasKeep)
				keepmeEdges.add(allEdges[t]);
			allEdges[t].visited = false;       //set all edges to unvisited
		}
		
		if (keepmeEdges.size() ==0)
			throw new IllegalStateException("testConnectivity::no keepme edges in network.  Diagram is screwed!");
		
		NetworkEdge e = (NetworkEdge) keepmeEdges.get(0);
		keepmeEdges.remove(0);
		
		Stack hotEdges = new Stack();
		hotEdges.add(e);
		
		
				// keep looping while there are still edges to visit
				// and havent arrived at all our keepme edges		
		while ( (keepmeEdges.size() >0) && (!(hotEdges.empty())) )
		{
			e = (NetworkEdge) hotEdges.pop();	
			if ((e.orginallyWasKeep ) && (keepmeEdges.contains(e)))
			{
				keepmeEdges.remove(e);	
			}
			e.visited = true;
			//put other edges back in stack
			List upstream = e.upstreamEdges();
			Iterator upIt = upstream.iterator();
			while (upIt.hasNext())
			{
				NetworkEdge e2 = (NetworkEdge) upIt.next();
				if (!(e2.visited))
				{
					hotEdges.push(e2);
				}
			}
			List downstream = e.downstreamEdges();
			Iterator downIt = downstream.iterator();
			while (downIt.hasNext())
			{
				NetworkEdge e2 = (NetworkEdge) downIt.next();
				if (!(e2.visited))
				{
					hotEdges.push(e2);
				}
			}
		}
		return keepmeEdges;
	}

}
