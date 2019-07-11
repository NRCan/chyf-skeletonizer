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
 *
 * @author dblasby
 *
 *   Fast method for computing point in polygon
 *   Requires approximately O(nlogn) setup time.
 *   Every PIP(point) should be approximately O(logn)
 * 
 *   1. for each ring in p, make a FastPinRing object
 *       - see javadoc for that class 
 *       - basically, we make an STRtree for all the vertex-to-vertex
 *         segments.  We use this to make the ray-edge intersection quick 
 *   2. when we test a PIP
 *        a. is Point in the BBOX of the polygon?  no - false
 *        b. is Point in the exterior ring of the polygon? no - false
 *        c. is Point in any of the holes of the polygon?  yes - false
 *        d. return true
 * 
 *   NOTE: for points on the boundary, I believe this return false.
 **/

import java.util.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.index.strtree.STRtree;

public class FastPIP
{
	Envelope polyEnvelope = null;
	FastPinRing exteriorRing = null;
	ArrayList   holeRings = new ArrayList();
	ArrayList   holeEnvelopes = new ArrayList();
	
	STRtree     holeSTR = new STRtree(); 
	
	public FastPIP(Polygon p)
	{
		polyEnvelope = p.getEnvelopeInternal(); 
		//set up FastPinRing for each ring
		
		exteriorRing = new FastPinRing(p.getExteriorRing());
		for (int t=0;t<p.getNumInteriorRing();t++)
		{
			LineString ls = p.getInteriorRingN(t);
			FastPinRing fastHole =  new FastPinRing(ls );
			Envelope e = ls.getEnvelopeInternal()  ;
			
			holeRings.add ( fastHole );
			holeEnvelopes.add (e);
			
			holeSTR.insert(e,fastHole );
		}
	}
	
	//returns true if c inside this polygon
	public boolean PIP(Coordinate c)
	{
		if (!(polyEnvelope.contains(c)))
		{
			return false;
		}
		
		if (!(exteriorRing.PinRing(c)) )
		{
			return false;
		}
		
		List holes = holeSTR.query( new Envelope(c) );
		
		for (int t=0;t<holes.size(); t++)
		{
			FastPinRing hole = (FastPinRing) holes.get(t);
			if (hole.PinRing(c))
				return false;
		}
		
		return true;
			
	}
}
