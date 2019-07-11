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
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.*;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.plugin.ThreadedBasePlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

/**
 * @author chodgson
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class DouglasPeuckerPlugIn extends ThreadedBasePlugIn {

	public void initialize( PlugInContext context ) {
		context.getFeatureInstaller().addMainMenuItem( this, "Skeletons", "Douglas-Peucker Line Simplification", null, null );
		context.getFeatureInstaller().addMainMenuItem( new DouglasPeuckerPlugIn2(), "Skeletons", "Douglas-Peucker Line Simplification2", null, null );
	}       
        
	public boolean execute( PlugInContext context ) throws Exception { 	 
		return true;
	}
	
	public void run( TaskMonitor tm, PlugInContext context ) {
		Iterator selected = context
									.getLayerViewPanel()
									.getSelectionManager()
									.getFeaturesWithSelectedItems()
									.iterator();
		Feature f = (Feature)selected.next();
		Geometry g = f.getGeometry();
		if( g.getClass() == LineString.class ) {
			g = DPAlgorithms.BasicDP( (LineString)g, 10 );
			FeatureSchema schema = new FeatureSchema();
			schema.addAttribute( "geom", AttributeType.GEOMETRY );
			FeatureDataset dataSet = new FeatureDataset( schema );
			f = new BasicFeature( schema );
			f.setAttribute( "geom", g );
			dataSet.add( f );
			context.getLayerManager().addLayer( "foo1", "foo2", dataSet );
		}
	}
	
	public String getName() {
		return "DouglasPeuckerPlugin";
	}

}

class DouglasPeuckerPlugIn2 extends ThreadedBasePlugIn {

	public void initialize( PlugInContext context ) {
	}       
        
	public boolean execute( PlugInContext context ) throws Exception { 	 
		return true;
	}
	
	public void run( TaskMonitor tm, PlugInContext context ) {
		LineString theLake = null;
		Iterator selected = context
									.getLayerViewPanel()
									.getSelectionManager()
									.getFeaturesWithSelectedItems()
									.iterator();
		Feature f = (Feature)selected.next();
		Geometry g = f.getGeometry();
		if( g.getClass() == Polygon.class ) {
			theLake = ((Polygon)g).getExteriorRing();
		}
		if( theLake.getClass() == LineString.class ) {
			g = DPAlgorithms.NewBasicDP( theLake, 10 );
			FeatureSchema schema = new FeatureSchema();
			schema.addAttribute( "geom", AttributeType.GEOMETRY );
			FeatureDataset dataSet = new FeatureDataset( schema );
			f = new BasicFeature( schema );
			f.setAttribute( "geom", g );
			dataSet.add( f );
			context.getLayerManager().addLayer( "foo1", "foo2", dataSet );
		}		
		else
			System.out.println("garbage");

	}
	
	public String getName() {
		return "DouglasPeuckerPlugin";
	}

}
