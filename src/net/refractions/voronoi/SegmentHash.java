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

import java.util.HashMap;
//import java.util.Map;

/**
 * @author bowens
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class SegmentHash extends HashMap
{
	public static int 	keyCount;
	//private Map			theMap;
	
	public SegmentHash()
	{
		keyCount = 0;
	}
	
	public int size()
	{
		return super.size();
	}
	
	public int getNewKey()
	{
		keyCount++;
		return keyCount;
	}
	
	public Object remove(Object i)
	{
		Segment ret = (Segment)super.remove(i);
		return ret;
	}
	
	public void put(HashKey k, Segment e)
	{
		//keyCount++;
		Object o = super.put(k, e);
	}
	
	public Object get(Object k)
	{
		return super.get(k);
	}

}

