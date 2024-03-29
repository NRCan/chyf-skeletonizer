/*
*
*  The Skeletonizer Utility is distributed under GNU General Public 
*  Licence � It is free software and can be  redistributed and/or 
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
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;

/**
 * @author bowens
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class SkelPoint extends Point {

	public int metaNum;		// meta information used for line segment elimination

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 */
	public SkelPoint(SkelCoordinate arg0, PrecisionModel arg1, int arg2) {
		super(arg0, arg1, arg2);
		// TODO Auto-generated constructor stub
		metaNum = arg0.metaNum;
	}

}
