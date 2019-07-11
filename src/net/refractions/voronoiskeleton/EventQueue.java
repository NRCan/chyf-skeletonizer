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
 * The priority queue that holds events
 */
final public class EventQueue
{

	Event root;			// the event key used for the actual event lookup
	private int size;
	
	
	/**
	 * 
	 */
	public EventQueue()
	{
		size = 0;
	}
	
	
	/**
	 * @return
	 */
	public int size()
	{
		return size;
	}
	
	
	/**
	 * @param ev
	 * Insert a new event into the list. Inserts with a bias towards x_pos 
	 */
	public void insert (Event ev)
	{
		boolean inserted = false;
		if (size() == 0 || root == null)		// if this linked list queue is empty 
		{
			//root = new Event(ev);
			root = ev;
			size++;
			return;
		}
		Event current = root;
		while (!inserted)
		{
			// if e.x is greater than the last one, but less than the next one, then it fits here
			if (ev.x_pos < current.x_pos)
			{
				Event e = new Event(ev);
				e.next = current;
				if (current.prev == null)
				{
					e.next = current;
					current.prev = e;
					root = e;
				}
				else
				{
					e.prev = current.prev;
					current.prev.next = e;
				}
				current.prev = e;
				inserted = true;
				size++;
				break;		// redundant
			}
			else	// e.x is greater than current
			{
				if (current.next == null)
				{
					Event e = new Event(ev);
					current.next = e;
					e.prev = current;
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
	 * @param ev
	 * Remove an indicated event from the priority queue
	 */
	public void remove (Event ev)
	{
		boolean removed = false;
		Event current = root;
		while (!removed)
		{
			// if we found the point
			if (current.equals(ev)/*ev.point.x == current.point.x && ev.point.x == current.point.x*/)
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
	 * @return Remove and return the first event in the priority queue
	 */
	public Event pop ()
	{
		Event e = root;
		if (root == null || size() == 0)
		{
			System.out.println("none left");
			return null;		// no points
		}
			
		if (root.next != null)
		{
			root.next.prev = null;
			Event n = root.next;
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
		return e;
	}
	
	
	/**
	 * @return return the first event in the priority queue
	 */
	public Event peak ()
	{
		Event ev = root;
		return ev;
	}

}
