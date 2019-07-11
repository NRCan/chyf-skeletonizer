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

//import java.util.LinkedList;

/**
 * @author bowens
 *
 * The queue of points that were origionally inserted into the voronoi program.
 */
final public class PointQueue
{
	
	Point root;			// initial point
	Point last;			// last point in list, used for insertSorted
	private int size;	// number of points


	/**
	 * 
	 */
	public PointQueue()
	{
		size = 0;
		root = null;
	}


	/**
	 * @return
	 */
	public int size()
	{
		return size;
	}
 
 
 	public void insertSorted(Point p)
 	{
		if (size() == 0 || root == null)		// if this linked list queue is empty 
		{
			root = new Point(p);
			root.metaInfo = p.metaInfo;
			size++;
			last = root;
			root.prev = null;
			root.next = null;
			return;
		}
		
		Point p1 = new Point(p);
		p1.metaInfo = p.metaInfo;
		
		p1.prev = last;
		p1.next = null;
		
		last.next = p1;
		last = p1;
		size++;
 	}
 
	/**
	 * @param p
	 * Insert the new point into the list.
	 */
	public void insert (Point p)
	{
		boolean inserted = false;
		if (size() == 0 || root == null)		// if this linked list queue is empty 
		{
			//System.out.println("inserted root");
			root = new Point(p);
			root.metaInfo = p.metaInfo;
			size++;
			return;
		}
		Point current = root;
		while (!inserted)
		{
			 // if e.x is greater than the last one, but less than the next one, then it fits here
			if (p.x < current.x || (p.x == current.x && p.y > current.y))
			{
				Point p1 = new Point(p);
				p1.metaInfo = p.metaInfo;
				p1.next = current;
				if (current.prev == null)
				{
					p1.next = current;
					current.prev = p1;
					root = p1;
				}
				else
				{
					p1.prev = current.prev;
					current.prev.next = p1;
				}
				current.prev = p1;
				inserted = true;
				size++;
				break;		// redundant, but I like redundancy
			}
			else	// e.x is greater than current
			{
				if (current.next == null)
				{
					Point p1 = new Point(p);
					p1.metaInfo = p.metaInfo;
					current.next = p1;
					p1.prev = current;
					size++;
					inserted = true;
					break;
				}
				else
					current = current.next;
			}
		}// end while
	}


	 /**
	 * @param p
	 * Search for and remove the point from the list.
	 */
	public void remove (Point p)
	 {
		 boolean removed = false;
		 Point current = root;
		 while (!removed)
		 {
			 // if we found the point
			 if (current.equals(p)/*ev.point.x == current.point.x && ev.point.x == current.point.x*/)
			 {
				 if (current.equals(root))
				 {
					 if (current.next == null)
					 {
						 root = null;
					 }
					 else
					 {
						 root = root.next;
						 root.prev = null;
					 }
				}
				else
				{
					 if (current.next == null)	// if we are at the end of the list
					 {
						 current.prev.next = null;
						 current.prev = null;
					 }
					 else	// just another node in the middle of the list
					 {
						 current.prev = null;
						 current.next.prev = current.prev;
						 current.prev.next = current.next;
					 }
				}
				removed = true;
				size--;
				break;
			 }
			 else
			 {
				 current = current.next;
				 if (current.next == null)
					 System.out.println("ERROR: Didn't find event");
			 }
		 }
	 }


	/**
	 * @return The point at the head of the queue
	 * Remove and return the point on the front of the queue.
	 */
	public Point pop ()
	{
		Point p = root;
		if (root == null || size() == 0)
		{
			System.out.println("none left");
			return null;		// no points
		}
			
		if (root.next != null)
	 	{
		 	root.next.prev = null;
		 	Point n = root.next;
		 	root = n;
	 	}
	 	else
	 	{
	 		root = null;
	 	}
	 	size--;
		//root.prev = null;
		//remove(0);
		//System.out.println("Popped");
		return p;
	}
	
	
	 /**
	 * @return the point at the front of the queue
	 * Return the point at the front of the queue.
	 */
	public Point peak ()
	 {
		 Point p = root;
		 return p;
	 }
 
 
 	/**
	 * @param p
	 * Print out the coordinates of all points in the list.
	 */
	public void printCoords(Point p)
 	{
 		if (p == null)
 			return;
 		System.out.println("x:" + p.x + " y:" + p.y);
 		printCoords(p.next);
 	}
 

}
