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

import com.vividsolutions.jts.geom.*;

/**
 * @author chodgson
 *
 */
public class DPAlgorithms {
	
	static LineString NewBasicDP( LineString line, double tolerance ) {
		Coordinate inputPts[] = line.getCoordinates();
		boolean use[] = new boolean[inputPts.length];
		for (int i = 0; i < use.length; i++) {
		   use[i] = true;
		}
		simplify( inputPts, 0, inputPts.length - 1, use, tolerance );

		CoordinateList coordList = new CoordinateList();
		for( int i = 0; i < use.length; i++ ) {
			if( use[i] ) {
				coordList.add( inputPts[i] ); 
			} 
		}
		return new LineString( coordList.toCoordinateArray(), line.getPrecisionModel(), 0 );
	}
	
	private static double perpendicularDistance( 	double Ax, double Ay,
													double Bx, double By,
													double Cx, double Cy ) {
		 double dx,dy;
		 double alpha;
		 double CPy;

		 dx = Bx - Ax;
		 dy = By - Ay;
		 alpha = Math.atan2(dy,dx);
		 dx = Cx - Ax;
		 dy = Cy - Ay;
		 CPy = Math.abs(-Math.sin(alpha)*dx + Math.cos(alpha)*dy);
		 return CPy;
	}
	
	private static void simplify( Coordinate inputPts[], int i, int j, boolean[] use, double tolerance ) {
		if((i+1) == j) {
			return;
		}
		double Ax = inputPts[i].x;   double Ay = inputPts[i].y;
		double Bx = inputPts[j].x;   double By = inputPts[j].y;
		double maxDistance = -1.0;
		int maxK = i;
		for( int k=i+1; k<j; k++ ) {
			double Cx = inputPts[k].x;   double Cy = inputPts[k].y;
			double distance = perpendicularDistance( Ax, Ay, Bx,By, Cx, Cy );
			if( distance > maxDistance ) {
			   maxDistance = distance;
			   maxK = k;
			}
		 }
		if( maxDistance <= tolerance ) {
			for( int k=i+1; k<j; k++ ) {
				use[k] = false;
			}
		} else {
			simplify( inputPts, i, maxK, use, tolerance );
			simplify( inputPts, maxK, j, use, tolerance );
		}
	}
	
	static LineString BasicDP( LineString line, double tolerance ) {
		CoordinateNode node = new CoordinateNode( line.getCoordinates() );
		DPAlgorithms.BasicDPRecursive( node, node.prev, tolerance );
		System.out.println( "num: " + node.toCoordinateArray().length );
		return new LineString( node.toCoordinateArray(), line.getPrecisionModel(), 0 );
	}
	
	private static void BasicDPRecursive( CoordinateNode firstNode, CoordinateNode lastNode, double tolerance ) {
		LineSegment seg = new LineSegment( firstNode.c, lastNode.c );
		CoordinateNode max_dist_node = null;
		double max_dist = 0;
		for( CoordinateNode node = firstNode.next; !node.equals( lastNode ); node = node.next ) {
			double dist = seg.distance( node.c );
			if( dist > max_dist ) {
				max_dist_node = node;
				max_dist = dist;
			}
		}
		if( max_dist < tolerance ) {
			// simplify out interior points
			firstNode.next = lastNode;
			lastNode.prev = firstNode;
			return;
		} else {
			// divide and conquer
			DPAlgorithms.BasicDPRecursive( firstNode, max_dist_node, tolerance );
			DPAlgorithms.BasicDPRecursive( max_dist_node, lastNode, tolerance );
		}
		
	}
}

class CoordinateNode {
	public CoordinateNode prev;
	public CoordinateNode next;
	public Coordinate c;
	
	CoordinateNode( CoordinateNode prev, CoordinateNode next, Coordinate c ) {
		this.prev = prev;
		this.next = next;
		this.c = c;
	}
	
	CoordinateNode( Coordinate[] coords ) {
		this.c = coords[0];
		CoordinateNode node = this;
		CoordinateNode lastNode = this;
		for( int i = 1; i < coords.length; i++ ) {
			node = new CoordinateNode( lastNode, null, coords[i] );
			lastNode.next = node;
			lastNode = node;
		}
		lastNode.next = this;
		this.prev = lastNode;
	}

	Coordinate[] toCoordinateArray() {
		// figure out how big the list is
		int count = 1;
		for( CoordinateNode node = this; !node.next.equals( this ); node = node.next ) {
			count++;			
		}
		// allocate an array of the correct size
		Coordinate[] array = new Coordinate[count];
		int i = 0;
		for( CoordinateNode node = this; i < count; node = node.next ) {
			array[i++] = node.c;			
		}
		return array;
	}

}