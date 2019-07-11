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

package net.refractions.voronoi;

//import java.util.LinkedList;

/**
 * @author bowens
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class PointQueue
{
	
	Point root;
	private int size;


	public PointQueue()
	{
		size = 0;
		root = null;
	}

	public int size()
	{
		return size;
	}
/* (non-Javadoc)
 * @see java.util.LinkedList#add(java.lang.Object)
 */
 
 
	public void insert (Point p)
	{
		boolean inserted = false;
		if (size() == 0 || root == null)		// if this linked list queue is empty 
		{
			//System.out.println("inserted root");
			root = new Point(p);
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
	
	
	 public Point peak ()
	 {
		 Point p = root;
		 return p;
	 }
 
 	public void printCoords(Point p)
 	{
 		if (p == null)
 			return;
 		System.out.println("x:" + p.x + " y:" + p.y);
 		printCoords(p.next);
 	}
 
 
 
 /*
	public boolean add(Object o) {
		insert((Point)o);
		return true;
	}


	public void insert (Point p)
	{
		int i=0;
		boolean inserted = false;
		if (size() == 0)		// if this linked list queue is empty 
		{
			super.add(0, p);
			return;
		}
		Point current = (Point)get(i);
		while (!inserted)
		{
			if (current == null)
			{
				super.add(i, p);
				inserted = true;
			}
			else
			{
				if (p.x >= current.x)
				{
					super.add(i, p);
					inserted = true;
					break;		// redundant, but I like redundancy
				}
				else
				{
					i++;
					if (i >= size())
						current = null;
					else
						current = (Point)get(i);
				}
			}
		}
	}


	public void remove (Point p)
	{
		int i=0;
		boolean removed = false;
		Point current = (Point)get(i);
		while (!removed)
		{
			if (p.x == current.x && p.x == current.x)
			{
				Point pp = (Point) remove(i);
				removed = true;
				break;
			}
			else
			{
				i++;
				current = (Point)get(i);
			}
		}
	}


	public Point pop ()
	{
		Point p = (Point)removeFirst();
		//remove(0);
		return p;
	}
	
	
	public Point peak ()
	{
		Point p = (Point)get(0);
		return p;
	}
*/

}
