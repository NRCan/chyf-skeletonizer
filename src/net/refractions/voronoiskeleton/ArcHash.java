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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
//import java.util.Map;

/**
 * @author bowens
 *
 * A hashMap that stores arcs
 * 
 * 
 *  This is a bunch of Arcs.
 * 
 * 
 *  Maps the Arc's hash key to the Arc.
 * 
 */
final public class ArcHash extends HashMap
{
	public static int 	keyCount;


	
	/**
	 * 
	 */
	public ArcHash()
	{
		super(100000,0.8f);
		keyCount = 0;
	}
	
	public ArcHash(int n)
	{
		super(n,0.8f);
		keyCount = 0;
	}
	
	/* 
	 * @see java.util.Map#size()
	 */
	public int size()
	{
		return super.size();
	}
	
	
	/**
	 * @return
	 */
	public int getNewKey()
	{
		keyCount++;
		return keyCount;
	}
	
	
	/* (non-Javadoc)
	 * @see java.util.Map#remove(java.lang.Object)
	 */
	public Object remove(Object i)
	{
		Arc ret = (Arc)super.remove(i);
		return ret;
	}
	
	
	/**
	 * @param k
	 * @param e
	 */
	public void put(HashKey k, Arc e)
	{
		//keyCount++;
		Object o = super.put(k, e);
	}
	
	
	/*
	 * @see java.util.Map#get(java.lang.Object)
	 */
	public Object get(Object k)
	{
		return super.get(k);
	}
	
	public String toStringSimple()
	{
		String result = "";
		Collection c = this.values();
		
		Iterator it = c.iterator();
		while (it.hasNext())
		{
			Arc a = (Arc) it.next();
			result += a.toString();
			result += "\n";
		}
		return result;
	}
	
		public String toString(Arc root)
		{
			String result = "";
			
			Arc current =root;
			
			while (current != null)
			{
				result += current.toString();
				result += "\n";
				current  = current.next;
			}
			return result;
		}
		
		public Arc getRoot()
		{
			Collection c = this.values();
			if (c.size() == 0)
				return null;
		
			Iterator it = c.iterator();
			Arc a = (Arc) it.next();
			
			// keep going a.prev until a.prev == null
			while (a.prev != null)
				a = a.prev;
			return a;
		}
		
		public String toString()
		{
			Arc root = getRoot();
			if (root == null)
				return "No Arc";
			return toString(getRoot());
		}

}

