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

import java.util.ArrayList;


public class SkeletonLineInHoleException extends Exception 
{
	ArrayList mybadHoles = null;
	
		public SkeletonLineInHoleException(ArrayList badHoles)
		{
			mybadHoles = badHoles;
		}
}
