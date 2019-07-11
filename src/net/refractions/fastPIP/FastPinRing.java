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

package net.refractions.fastPIP;

/**
 *   FastPinRing
 *     takes a linearRing and builds an index on the edges
 *     
 *   For PinRing()
 *     - make a very thin (y), but long (x) direction query box.
 *        box starts at c, goes in positive X direction.  height = 0.001
 *     - query edges that intersect this box
 *     - use normal PIP crossing to determine answer.
 */
import java.util.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.index.strtree.STRtree;

import com.vividsolutions.jts.algorithm.RobustDeterminant;

public class FastPinRing
{
	STRtree segTree = new STRtree();
	Envelope myEnvelope = null;
	
	double eta = 0.001; // expand bbox by this amount in all directions "just to be sure"
	
	/**
	 *   create the STRtree
	 *   
	 * @param lr linear ring
	 */
	public FastPinRing(LineString lr)
	{
		myEnvelope = lr.getEnvelopeInternal();
		
		Coordinate[] coords = lr.getCoordinates();
		int n = lr.getNumPoints();
		for (int t=0;t<(n-1);t++)
		{
			Edge e= new Edge(coords[t].x, coords[t].y, coords[t+1].x, coords[t+1].y );
			segTree.insert(e.getEnvelope(), e);
		}
	}
	
	public boolean PinRing(Coordinate c)
	{
		Envelope searchEnvelope = new Envelope (c.x-eta,myEnvelope.getMaxX()+eta,c.y-eta,c.y+eta);
		
		List shortList = segTree.query(searchEnvelope);
		
		//taken from RobustAlgos for point in ring
		
		int crossings = 0;
		double xInt =0;
		
		Iterator it = shortList.iterator();
		
		while (it.hasNext())
		{		
			Edge e = (Edge) it.next();
		    double x1 = e.x1 - c.x;
		    double y1 = e.y1 - c.y;
		    double x2 = e.x2 - c.x;
		    double y2 = e.y2 - c.y;

			if (((y1 > 0) && (y2 <= 0)) ||
			    ((y2 > 0) && (y1 <= 0))) {
			  /*
			   *  segment straddles x axis, so compute intersection.
			   */
			  xInt = RobustDeterminant.signOfDet2x2(x1, y1, x2, y2) / (y2 - y1);
			  //xsave = xInt;
			  /*
			   *  crosses ray if strictly positive intersection.
			   */
			  if (0.0 < xInt) 
			  {
			    crossings++;
			  }
			}
		}
		
		if ((crossings % 2) == 1) 
		{
			 return true;
		}
		else
		{
			 return false;
		}		
	}
	
}


