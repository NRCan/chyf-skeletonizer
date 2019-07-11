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

/**
 * @author bowens
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class SkelCoordinate extends Coordinate {

	public int metaNum =-9999;		// meta information used for line segment elimination
	public boolean entryPoint = false;
	/**
	 * @param arg0  x
	 * @param arg1  y
	 * @param arg2  metanumber
	 */
	public SkelCoordinate(double arg0, double arg1, int arg2) {
		super(arg0, arg1);
		metaNum = arg2;

	}
	
	/**
	 * @param arg0  x
	 * @param arg1  y
	 * @param arg2  metanumber
	 * @param entryPoint true if this is a riverEntryPoint
	 */
	
	public SkelCoordinate(double arg0, double arg1, int arg2,boolean entryPoint) {
			super(arg0, arg1);
			metaNum = arg2;
			this.entryPoint = entryPoint;
		}
		

	/**
	 * 
	 */
	public SkelCoordinate() {
		super();
	}

	/**
	 * @param arg0
	 */
	public SkelCoordinate(Coordinate arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public SkelCoordinate(double arg0, double arg1) {
		super(arg0, arg1);
	}

}
