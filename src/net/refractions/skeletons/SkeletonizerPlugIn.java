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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import net.refractions.util.jump.JUMPUtil;
import net.refractions.voronoiskeleton.Voronoi;
import net.refractions.voronoiskeleton.Segment;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedBasePlugIn;

/**
 * @author Brent Owens
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class SkeletonizerPlugIn extends ThreadedBasePlugIn {
    
    Skeletonizer skeleton;
    GetFnamesDialog fnameDialog = null ;
    
    FeatureCollection inputPoint = null;
    FeatureCollection inputPoly  = null;
    
    FeatureCollection resultCollection= null;
    
    String    outputReport = "";
    
    boolean do_deriveAreas    = true;
    boolean do_directionalize = true;
    
    
    boolean cheat = false;
    public LineString[] g2;
    
    public void initialize( PlugInContext context ) {
        context.getFeatureInstaller().addMainMenuItem( this, "Skeletonizer", "Generate Skeletons", null, null );
    }       
    
    public boolean execute( PlugInContext context ) throws Exception 
    { 	
        return true;
    }
    
    public void doit()
    {
        net.refractions.voronoiskeleton.Point c1 = new net.refractions.voronoiskeleton.Point(0,0,1);
        net.refractions.voronoiskeleton.Point c2 = new net.refractions.voronoiskeleton.Point(10,10,2);
        net.refractions.voronoiskeleton.Point c3 = new net.refractions.voronoiskeleton.Point(20,-10,3);
        net.refractions.voronoiskeleton.Point c4 = new net.refractions.voronoiskeleton.Point(30,10,4);
        
        net.refractions.voronoiskeleton.Voronoi voron = new net.refractions.voronoiskeleton.Voronoi(true,net.refractions.voronoiskeleton.Voronoi.SOMETIMES,net.refractions.voronoiskeleton.Voronoi.SOMETIMES);
        
        voron.addPointPreSorted(c1);
        voron.addPointPreSorted(c2);
        voron.addPointPreSorted(c3);
        voron.addPointPreSorted(c4);
        
        Coordinate[] cs = new Coordinate[4];
        cs[0] = c1.asCoordinate();
        cs[1] = c2.asCoordinate();
        cs[2] = c3.asCoordinate();
        cs[3] = c4.asCoordinate();
        
        
        voron.run();
        ArrayList voronoiEdges = voron.get_output();
        
        
        GeometryUtil.write_shape("c:\\before_endpoint.shp",(Segment[]) voronoiEdges.toArray( new Segment[1]) );
        GeometryUtil.write_shape("c:\\voronoi_points.shp",cs);
        
    }
    
    public void doit2(Polygon p)
    {
        net.refractions.voronoiskeleton.Voronoi voron = new net.refractions.voronoiskeleton.Voronoi(true,net.refractions.voronoiskeleton.Voronoi.SOMETIMES,net.refractions.voronoiskeleton.Voronoi.SOMETIMES);
        Coordinate[] cs = p.getCoordinates();
        Coordinate[] cs2 = new Coordinate[cs.length-1];
        System.arraycopy(cs,0,cs2,0,cs.length-1);
        
        Arrays.sort(cs2);
        
        for (int t=0;t<cs2.length;t++)
        {
            net.refractions.voronoiskeleton.Point pp = new net.refractions.voronoiskeleton.Point(cs2[t].x,cs2[t].y,t);
            voron.addPointPreSorted(pp);
        }
        
        GeometryUtil.write_shape("c:\\voronoi_points.shp",cs);
        
        voron.run();
        ArrayList voronoiEdges = voron.get_output();
        
        
        GeometryUtil.write_shape("c:\\before_endpoint.shp",(Segment[]) voronoiEdges.toArray( new Segment[1]) );
    }
    
    
    public void run(Feature polygon, FeatureCollection points) throws Exception {
        ArrayList pnts = new ArrayList();
        for (Iterator iter = points.getFeatures().iterator(); iter.hasNext();) {
            Feature element = (Feature) iter.next();
            pnts.add((Point)element.getGeometry() );
        }
        skeleton = new Skeletonizer(
                (Polygon)polygon.getGeometry(), (Point[])pnts.toArray(new Point[pnts.size()]), 0.5, false, net.refractions.voronoiskeleton.Voronoi.SOMETIMES,net.refractions.voronoiskeleton.Voronoi.SOMETIMES,false
        );
        g2 = skeleton.skeleton;
    }
    
    public void run( TaskMonitor tm, PlugInContext context ) 
    {
        if (false)
        {
            doit();
            return ;
        }
        //		ArrayList riverPoints = new ArrayList();
        //		ArrayList riverDirection = new ArrayList();
        //		ArrayList riverCode = new ArrayList();
        //		Feature feat;
        //		Geometry lake = null;
        
        ArrayList points = new ArrayList();
        Polygon polygon = null;
        
      //  Feature polyf = null;
      //  ArrayList pointf = new ArrayList();
        
        for (Iterator selected = context.getLayerViewPanel().getSelectionManager().getFeaturesWithSelectedItems().iterator();selected.hasNext();)	// for loop
        {
            Feature f = (Feature)selected.next();
            Geometry g = f.getGeometry();
            
            if( g.getClass() == MultiPoint.class )   //added by DB to handle shapefile-type-points
            {
                Point p = (Point) ((MultiPoint)g).getGeometryN(0);
                points.add(p);
            }
            if( g.getClass() == Point.class ) 
            {
                points.add(g);
            }
            else if( g.getClass() == Polygon.class ) 
            {
                polygon = (Polygon)g;
            }else if (g.getClass() == MultiPolygon.class){
                polygon = (Polygon)((MultiPolygon)g).getGeometryN(0);
            }
        }// end for loop
        
        try {
            skeleton = new Skeletonizer(polygon, (Point[])points.toArray(new Point[points.size() ]), 0.5, false, Voronoi.SOMETIMES, Voronoi.SOMETIMES, false, Skeletonizer.KEEPHOLES  );
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
        g2 = skeleton.skeleton;
 
        FeatureSchema schema = new FeatureSchema();
        schema.addAttribute( "geom", AttributeType.GEOMETRY );
        schema.addAttribute( "index", AttributeType.INTEGER);
        FeatureDataset dataSet = new FeatureDataset( schema );
        for (int t=0; t<g2.length; t++)
        {
            Feature feat = new BasicFeature( schema );
            feat.setAttribute( "geom", g2[t] );
            feat.setAttribute("index", new Integer(t));
            dataSet.add( feat );
        }
        context.getLayerManager().addLayer( "DP", "Lake", dataSet );		
    }
    
    public String getName() {
        return "SkeletonizerPlugIn";
    }
    
    public static void main(String[] args) {
        try {
            if (args.length < 2) {
                System.out.println(
                        "Usage: " + SkeletonizerPlugIn.class.getName() + 
                        " POLYFILE POINTFILE [OUTFILE]"
                );
                System.exit(-1);
            }
            
            FeatureCollection polyfc = JUMPUtil.readShapefile(args[0]);
            FeatureCollection pointfc = JUMPUtil.readShapefile(args[1]);
            
            String outfile = null;
            if (args.length == 3) outfile = args[2];
            
            SkeletonizerPlugIn skelplugin = new SkeletonizerPlugIn();
            skelplugin.run((Feature)polyfc.iterator().next(), pointfc);
            
            LineString[] skeletons = skelplugin.g2;
            
            if (outfile != null) JUMPUtil.writeShapefile(outfile, skeletons);
            
        }
        catch(Exception e) {
            e.printStackTrace();	
        }
    }
    
}





