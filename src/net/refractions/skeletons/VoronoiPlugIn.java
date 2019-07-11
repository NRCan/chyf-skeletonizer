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

import java.util.Iterator;
import com.vividsolutions.jts.geom.Geometry;
//import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.CoordinateList;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jump.feature.*;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.plugin.ThreadedBasePlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import java.util.LinkedList;

/**
 * @author Brent Owens
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class VoronoiPlugIn extends ThreadedBasePlugIn {
	
	Voronoier skeleton;

	public void initialize( PlugInContext context ) {
		context.getFeatureInstaller().addMainMenuItem( this, "Skeletonizer", "Generate Voronoi", null, null );
	}       
        
	public boolean execute( PlugInContext context ) throws Exception { 	 
		return true;
	}
	
	public void run( TaskMonitor tm, PlugInContext context ) {
		skeleton = new Voronoier();
		
		LinkedList riverPoints = new LinkedList();
		Feature feat;
		Geometry lake = null;
		
		for (Iterator selected = context
									.getLayerViewPanel()
									.getSelectionManager()
									.getFeaturesWithSelectedItems()
									.iterator(); 
																	selected.hasNext();)	// for loop
		{
			Feature f = (Feature)selected.next();
			Geometry g = f.getGeometry();
			
			
			if( g.getClass() == Point.class ) 
			{
				System.out.println("river point");
				riverPoints.add((Point)g);
		
			}
			else if( g.getClass() == Polygon.class ) 
			{
				System.out.println("lake polygon");
				lake = (Polygon)g;
			
			}
		}// end for loop
		
		// got the points and the lake, now do the magic!
		Point[] points = new Point[riverPoints.size()];
		for (int i=0; i<riverPoints.size(); i++)
			points[i] = (Point)riverPoints.get(i);
		
		LineString[] g2 = skeleton.voronize(5, 100, (Polygon)lake, points);
		//MultiPoint g2 = skeleton.getDensePoints(5, 5, lake, points);		
		FeatureSchema schema = new FeatureSchema();
		schema.addAttribute( "geom", AttributeType.GEOMETRY );
		schema.addAttribute( "index", AttributeType.INTEGER);
		FeatureDataset dataSet = new FeatureDataset( schema );
		for (int t=0; t<g2.length; t++)
		{
			feat = new BasicFeature( schema );
			feat.setAttribute( "geom", g2[t] );
			feat.setAttribute("index", new Integer(t));
			dataSet.add( feat );
		}

		/** dens points**/
//		feat = new BasicFeature( schema );
//		feat.setAttribute( "geom", g2 );
//		feat.setAttribute("index", new Integer(0));
//		dataSet.add( feat );
		
		
		context.getLayerManager().addLayer( "DP", "Lake", dataSet );		
			

	}
	
	public String getName() {
		return "SkeletonizerPlugIn";
	}

}

