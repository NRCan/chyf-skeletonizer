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



import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.refractions.fastPIP.FastPIP;
import net.refractions.fastPIP.FastSegInPolygon;
import net.refractions.graph.build.line.DirectedLineStringGraphGenerator;
import net.refractions.graph.build.line.LineStringGraphGenerator;
import net.refractions.graph.path.DijkstraShortestPathFinder;
import net.refractions.graph.path.LineStringPath;
import net.refractions.graph.path.LinearPath;
import net.refractions.graph.path.Path;
import net.refractions.graph.path.PathSet;
import net.refractions.graph.structure.DirectedEdge;
import net.refractions.graph.structure.DirectedNode;
import net.refractions.graph.structure.Edge;
import net.refractions.graph.structure.Graph;
import net.refractions.graph.structure.GraphVisitor;
import net.refractions.graph.structure.Graphable;
import net.refractions.graph.structure.Node;
import net.refractions.graph.traverse.GraphTraversal;
import net.refractions.graph.traverse.basic.BasicGraphTraversal;
import net.refractions.graph.traverse.basic.DummyGraphWalker;
import net.refractions.graph.traverse.standard.DepthFirstIterator;
import net.refractions.graph.traverse.standard.DijkstraIterator;
import net.refractions.graph.util.feature.LineStringMerger;
import net.refractions.graph.util.graph.GraphFuser;
import net.refractions.util.jump.JUMPUtil;
import net.refractions.voronoiskeleton.Point;
import net.refractions.voronoiskeleton.Segment;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.index.strtree.STRtree;
import com.vividsolutions.jump.feature.Feature;


public class Skeletonizer {
    
    public double shift = 0.01;	// used for estimating points on either side of a river point
    private int gLineSegCount = 0; 
    
    //public ArrayList actuallyInsertedPoint = null; // list of com.vividsolutions.jts.geom.Point.   Actually used riverpoints
    
    double minSegmentDistance = 50;
    
    double densifyFactor = 1.0;  // densify more (or less).  Try with 1, then 1/2 then 1/4 dont try with more!
    boolean jiggle = false;      // true = move non river-entry points by a wee bit to stop co-circular events
    
    public SkelLineString[] skeleton = null;
    
    double criticalCutAngle = 67; // degrees.  This is the point at which we start clipping the triangle off of the polygon.
    
    
    int useEnhancedPrecisionCircle = net.refractions.voronoiskeleton.Voronoi.SOMETIMES;
    int  useEnhancedPrecisionIntersect =net.refractions.voronoiskeleton.Voronoi.SOMETIMES;
    boolean USECLIPVORONOI = false;
    
    public static  int REMOVEHOLES = 1;
    public static  int KEEPHOLES = 2;
    public static  int SMARTHOLES  = 3;
    
    // if there were any required holes in the polygon, this will be a list of 
    // those holes.
    //  called with REMOVEHOLES  --> this will always be empty
    //  called with KEEPHOLES    --> this will always be a list of all the holes in the polygon
    //  called with SMARTHOLES   --> this will be a list of the required holes to keep the skeleton lines inside the polygon
    public ArrayList keptHoles  = null;
    
    
    boolean WRITESHAPE = false;
    boolean SILENT =     true;
    boolean DO1AND2POINT   = false;
    boolean FORCEJIGGLE = false;
    boolean QA = true;
    
    
    //		boolean WRITESHAPE = true;
    //		boolean SILENT =     false;
    //		boolean DO1AND2POINT   =true;
    //		boolean FORCEJIGGLE = false;
    
    
//    public Skeletonizer(
//            Feature polyf, Collection pointfc, double densifyFactor, 
//            boolean jiggle, int enhancedCircle, int enhancedIntersect, boolean useCLIP 
//    ) throws Exception  {
//        	this.skeleton = null;
//        	Polygon polygon = null;
//        	if (polyf.getGeometry() instanceof Polygon) {
//        	  polygon = (Polygon) polyf.getGeometry();	
//        	}
//        	else if (polyf.getGeometry() instanceof MultiPolygon) {
//        		MultiPolygon mp = (MultiPolygon)polyf.getGeometry();
//        		polygon = (Polygon) mp.getGeometryN(0);
//        	}
//        	else throw new IllegalStateException("Not a polygonal geometry");
//        	
//        	com.vividsolutions.jts.geom.Point[] points = 
//        		new com.vividsolutions.jts.geom.Point[pointfc.size()];
//        	int i = 0;
//        	for (Iterator itr = pointfc.iterator(); itr.hasNext(); i++) {
//        		Feature f = (Feature)itr.next();
//        		if (f.getGeometry() instanceof com.vividsolutions.jts.geom.Point) {
//        	    points[i] = (com.vividsolutions.jts.geom.Point) f.getGeometry();	
//        		}
//        		else if (f.getGeometry() instanceof MultiPoint) {
//        			MultiPoint mp = (MultiPoint)f.getGeometry();
//        			points[i] = (com.vividsolutions.jts.geom.Point)mp.getGeometryN(0);
//        		}
//        		else throw new IllegalStateException("Not a point geometry");
//        	}
//        	
//        	Skeletonizer skeletonizer = new Skeletonizer(
//        	  polygon,points,densifyFactor,jiggle,enhancedCircle,enhancedIntersect,useCLIP, Skeletonizer.KEEPHOLES 
//        	);
//        	this.skeleton = skeletonizer.skeleton ;
////        	SkelLineString[] skeletons = skeletonizer.skeleton;
//        	
////        	directionalize(skeletons, pointfc);
////        	if (QA) qa(skeleton, pointfc);
//    }
    
    /**
     *  this version does not remove holes!
     * @param wb
     * @param riverInOutPoints
     * @param densifyFactor
     * @param jiggle
     * @param enhancedCircle
     * @param enhancedIntersect
     * @param useCLIP
     * @throws Exception
     */
    public Skeletonizer(Polygon wb, 
            com.vividsolutions.jts.geom.Point[] riverInOutPoints, 
            double densifyFactor, 
            boolean jiggle, 
            int enhancedCircle,
            int enhancedIntersect,
            boolean useCLIP ) throws Exception
            {
        this.densifyFactor = densifyFactor;
        this.jiggle = jiggle || FORCEJIGGLE;
        this.useEnhancedPrecisionCircle = enhancedCircle;
        this.useEnhancedPrecisionIntersect = enhancedIntersect;
        USECLIPVORONOI= useCLIP;
        skeleton =  skeletonize( wb,  riverInOutPoints);	
        
        keptHoles = new ArrayList();
        for (int t=0;t<wb.getNumInteriorRing();t++)
        {
            keptHoles.add(wb.getInteriorRingN(t));
        }
        
        
            }
    
    
    
    /**
     * 
     *  holeRemoval = Skeletonizer.KEEPHOLES   (normal operation)
     *              = Skeletonizer.REMOVEHOLES (compute without holes, if line overlaps hole, throw error)
     *              = Skeletonizer.SMARTHOLES  (minimizes computation with holes. 
     *                             see Skeletonizer.removedHoles and Skeletonizer.keptHoles
     *                             for what happened to the holes) 
     * 
     * @param wb
     * @param riverInOutPoints
     * @param densifyFactor
     * @param jiggle
     * @param enhancedCircle
     * @param enhancedIntersect
     * @param useCLIP
     * @param holeRemoval
     * @throws Exception
     */
    public Skeletonizer(Polygon wb, 
            com.vividsolutions.jts.geom.Point[] riverInOutPoints, 
            double densifyFactor, 
            boolean jiggle, 
            int enhancedCircle,
            int enhancedIntersect,
            boolean useCLIP ,
            int holeRemoval) throws Exception
            {
        this.densifyFactor = densifyFactor;
        this.jiggle = jiggle || FORCEJIGGLE;
        this.useEnhancedPrecisionCircle = enhancedCircle;
        this.useEnhancedPrecisionIntersect = enhancedIntersect;
        USECLIPVORONOI= useCLIP;
        
        if (holeRemoval == KEEPHOLES) // normal operation
        {
            skeleton =  skeletonize( wb,  riverInOutPoints);	
            keptHoles = new ArrayList();
            for (int t=0;t<wb.getNumInteriorRing();t++)
            {
                keptHoles.add(wb.getInteriorRingN(t));
            }
            return;
        }
        
        if (holeRemoval == REMOVEHOLES)
        {
            Polygon wb2 = removePolygonHoles(wb);
            skeleton =  skeletonize( wb2,  riverInOutPoints);
            ArrayList badHoles = testLinesOnHoles(wb,skeleton);
            if (badHoles == null)  // all good!
                return;
            keptHoles = new ArrayList(); // no kept holes
            throw new SkeletonLineInHoleException(badHoles);
        }
        
        if (holeRemoval == SMARTHOLES)
        {
            
            if (smartHoleHandler(wb, riverInOutPoints))
            {
                System.out.println(" -- has "+keptHoles.size()+" holes");
                return;
            }
            else
                throw new Exception("Skeletonizer - smart holeRemoval; couldnt do it!");
            
        }
        
        throw new Exception("Skeletonizer - holeRemoval; unknown value - "+ holeRemoval);
            }
    
    public void fuse(SkelLineString[] skeletons) {
        LineStringGraphGenerator gg = new LineStringGraphGenerator();
        gg.setRemovingOrphanNodes(true);
        
        for (int i = 0; i < skeletons.length; i++) gg.add(skeletons[i]);
        
        GraphFuser.EdgeMerger merger = new LineStringMerger() {
            public Object merge(List edges) {
                LineString merged = (LineString)super.merge(edges);
                
                double avg = 0d;
                for (Iterator itr = edges.iterator(); itr.hasNext();) {
                    Edge e = (Edge)itr.next();
                    SkelLineString line = (SkelLineString)e.getObject();
                    
                    Double width = line.getWidth();
                    if (width != null) avg += width.doubleValue();
                }
                
                avg /= ((double)edges.size());
                
                SkelLineString skline = new SkelLineString(
                        merged.getCoordinates(), merged.getPrecisionModel(), merged.getSRID()		
                );
                skline.setWidth(new Double(avg));
                
                return(skline);
            }
        };
        
        GraphFuser fuser = new GraphFuser(gg.getGraph(),gg.getGraphBuilder(),merger);
        fuser.fuse();
        
        
        //replace the skeletons with our lines
        skeleton = new SkelLineString[gg.getGraph().getEdges().size()];
        int i = 0;
        
        for (Iterator eitr = gg.getGraph().getEdges().iterator(); eitr.hasNext();i++) {
            Edge e = (Edge)eitr.next();
            LineString line = (LineString)e.getObject();
            
            skeleton[i] = new SkelLineString(line);
        }
        
    }
    
    public void directionalize(SkelLineString[] skeletons, Collection points) {
        
        //build a graph out of the skeletons
        LineStringGraphGenerator gg = new LineStringGraphGenerator();
        gg.setRemovingOrphanNodes(true);
        
        for (int i = 0; i < skeletons.length; i++) gg.add(skeletons[i]);
        
        ArrayList inputs = new ArrayList();
        ArrayList outputs = new ArrayList();
        
        //get input/output nodes from the graph from the points
        for (Iterator itr = points.iterator(); itr.hasNext();) {
            Feature f = (Feature)itr.next();
            com.vividsolutions.jts.geom.Point p = null;
            if (f.getGeometry() instanceof com.vividsolutions.jts.geom.Point) {
                p = (com.vividsolutions.jts.geom.Point)f.getGeometry();
            }
            else if (f.getGeometry() instanceof MultiPoint){
                MultiPoint mp = (MultiPoint)f.getGeometry();
                p = (com.vividsolutions.jts.geom.Point)mp.getGeometryN(0);
            }
            else throw new IllegalStateException("Not a point geometry");
            
            Coordinate c = p.getCoordinate();
            
            Node n = gg.getNode(c);
            if (n == null) continue;
            
            //points should be classified as INPUT or OUTLET
            if (f.getAttribute("FLOW") == null) 
                throw new IllegalStateException("FLOW flag not set for point");
            
            String flow = f.getString("FLOW").toUpperCase().trim();
            
            if (flow.equals("OUTLET")) {
                outputs.add(n);
            }
            else inputs.add(n);
        }
        
        //next, fuse the graph
        GraphFuser.EdgeMerger merger = new LineStringMerger() {
            public Object merge(List edges) {
                LineString merged = (LineString)super.merge(edges);
                
                double avg = 0d;
                for (Iterator itr = edges.iterator(); itr.hasNext();) {
                    Edge e = (Edge)itr.next();
                    SkelLineString line = (SkelLineString)e.getObject();
                    
                    Double width = line.getWidth();
                    if (width != null) avg += width.doubleValue();
                }
                
                avg /= ((double)edges.size());
                
                SkelLineString skline = new SkelLineString(
                        merged.getCoordinates(), merged.getPrecisionModel(), merged.getSRID()		
                );
                skline.setWidth(new Double(avg));
                
                return(skline);
            }
        };
        
        GraphFuser fuser = new GraphFuser(gg.getGraph(),gg.getGraphBuilder(),merger);
        fuser.fuse();
        
        //look for any two edges that share the same node, remove the longest one
        HashSet duplicates = new HashSet();
        for (Iterator itr = gg.getGraph().getEdges().iterator(); itr.hasNext();) {
            Edge e = (Edge)itr.next();
            List edges = e.getNodeA().getEdges(e.getNodeB());
            if (edges.size() > 1) {
                //choose the longest one to remove
                Edge longest = (Edge) edges.get(0);
                for (int i = 1; i < edges.size(); i++) {
                    Edge e1 = (Edge)edges.get(i);
                    LineString l1 = (LineString) longest.getObject();
                    LineString l2 = (LineString) e1.getObject();
                    
                    if (l1.getLength() < l2.getLength()) longest = e1;
                }
                duplicates.add(longest);  
            }
        }
        
        for (Iterator itr = duplicates.iterator(); itr.hasNext();) {
            Edge e = (Edge)itr.next();
            gg.getGraphBuilder().removeEdge(e);
        }
        
        DijkstraIterator.EdgeWeighter weighter = new DijkstraIterator.EdgeWeighter() {
            public double getWeight(Edge e) {
                SkelLineString line = (SkelLineString)e.getObject();
                Double width = line.getWidth();
                
                if (width != null && width.doubleValue() > 0d) {
                    return line.getLength()/width.doubleValue();
                }
                return line.getLength();
            }
        };
        
        Comparator sorter = new Comparator() {
            public int compare(Object o1, Object o2) {
                LinearPath p1 = (LinearPath)o1;
                LinearPath p2 = (LinearPath)o2;
                
                if (p1.getLength() > p2.getLength()) return(-1);
                if (p1.getLength() < p2.getLength()) return(1);
                return(0);
            }
        };
        
        //for each output node, calculate a shorest path to every other node
        HashMap out2pf = new HashMap();
        for (Iterator itr = outputs.iterator(); itr.hasNext();) {
            Node output = (Node)itr.next();
            DijkstraShortestPathFinder pf = new DijkstraShortestPathFinder(
                    gg.getGraph(), output, weighter		
            );
            pf.calculate();
            
            //calculate paths to all inputs
            ArrayList paths = new ArrayList();
            for (Iterator iitr = inputs.iterator(); iitr.hasNext();) {
                Node input = (Node)iitr.next();
                Path p = pf.getPath(input);
                
                paths.add(new LineStringPath(p));
            }
            
            //sort the paths
            Collections.sort(paths, sorter);
            
            out2pf.put(output, new Object[]{pf,paths});
        }
        
        Set entries = out2pf.entrySet();
        Iterator itr = entries.iterator();
        
        //find the longest paths out of all the paths, this will be our main path
        Object[] main = (Object[]) ((Map.Entry)itr.next()).getValue();
        for (; itr.hasNext(); ) {
            Object[] obj = 	(Object[]) ((Map.Entry)itr.next()).getValue();
            
            LinearPath p1 = (LinearPath) ((List)main[1]).get(0);
            LinearPath p2 = (LinearPath) ((List)obj[1]).get(0);
            
            if (p2.getLength() > p1.getLength()) {
                main = obj;	
            }
        }
        
        //create a path set, and populate it with paths relative to the main path
        PathSet pathset = new PathSet(gg.getGraph());
        
        List paths = ((List)main[1]);
        for (Iterator pitr = paths.iterator(); pitr.hasNext();) {
            Path p = (Path)pitr.next();	
            pathset.add(p);
        }
        
        LinearPath mainp = (LinearPath)paths.get(0);
        
        //calculate paths to from outputs
        DijkstraShortestPathFinder pf = (DijkstraShortestPathFinder)main[0];
        for (Iterator oitr = outputs.iterator(); oitr.hasNext();) {
            Node output = (Node)oitr.next();
            if (!output.equals(mainp.getLast())) {
                Path p = pf.getPath(output);
                
                //truncate it via the path set, then reverse since it is an output
                pathset.truncate(p);
                p.reverse();
                
                pathset.add(new LineStringPath(p));
            }
        }
        
        //for each edge that is not part of a path, throw it away
        for (Iterator eitr = gg.getGraph().getEdges().iterator(); eitr.hasNext();) {
            Edge e = (Edge)eitr.next();
            e.setVisited(false);  
        }
        
        for (Iterator pitr = pathset.getPaths().iterator(); pitr.hasNext();) {
            Path p = (Path)pitr.next();
            for (Iterator eitr = p.getEdges().iterator(); eitr.hasNext();) {
                Edge e = (Edge)eitr.next();
                e.setVisited(true);
            }
        }
        
        ArrayList f = new ArrayList();
        for (Iterator pitr = pathset.getPaths().iterator(); pitr.hasNext();) {
            LinearPath p = (LinearPath)pitr.next();
            f.add(p.asLineString());
        }
        
        
        GraphVisitor gv = new GraphVisitor() {
            public int visit(Graphable component) {
                com.vividsolutions.jts.geom.Point p = (com.vividsolutions.jts.geom.Point)component.getObject();
                if (p.getCoordinate().equals(new Coordinate(561848.7091688288,1618476.9391626187))) {
                    return(Graph.PASS_AND_CONTINUE);
                }
                return(Graph.FAIL_QUERY);
            }
        };
        
        List toremove = gg.getGraph().getVisitedEdges(false);
        for (Iterator ritr = toremove.iterator(); ritr.hasNext(); ) {
            Edge e = (Edge)ritr.next();
            gg.remove(e.getObject());
        }
        
        //loops can throw the generator off, remove any loop edges manually
        toremove = gg.getGraph().queryEdges(
                new GraphVisitor() {
                    public int visit(Graphable component) {
                        Edge e = (Edge)component;
                        if (e.getNodeA().equals(e.getNodeB())) return(Graph.PASS_AND_CONTINUE);
                        return(Graph.FAIL_QUERY);
                    }
                }
        );
        for (Iterator ritr = toremove.iterator(); ritr.hasNext(); ) {
            Edge e = (Edge)ritr.next();
            gg.getGraphBuilder().removeEdge(e);
            if (e.getNodeA().getEdges().size() == 0) {
                gg.getGraphBuilder().removeNode(e.getNodeA());
            }
        }
        
        //directionalize each path in the set
        for (Iterator pitr = pathset.getPaths().iterator(); pitr.hasNext();) {
            Path p = (Path)pitr.next();
            
            for (int i = 0; i < p.size()-1; i++) {
                Node a = (Node)p.get(i);
                Node b = (Node)p.get(i+1);
                
                Edge e = (Edge)p.getEdges().get(i);
                
                if (!e.getNodeA().equals(a)) {
                    e.reverse();
                }
                
                Coordinate c1 = 
                    ((com.vividsolutions.jts.geom.Point)a.getObject()).getCoordinate();
                Coordinate c2 = 
                    ((com.vividsolutions.jts.geom.Point)b.getObject()).getCoordinate();
                
                LineString l = (LineString) e.getObject();
                if (!c1.equals(l.getCoordinateN(0))) {
                    e.setObject(
                            net.refractions.util.geom.GeometryUtil.reverseGeometry(l,false)
                    );
                }
            }
        }
        
        //look for degree 2 nodes, remove them, and merge the line string on either
        // side
        GraphVisitor visitor = new GraphVisitor() {
            public int visit(Graphable component) {
                Node n = (Node)component;
                if (n.getDegree() == 2) return(Graph.PASS_AND_STOP);
                return(Graph.FAIL_QUERY);
            }
        };
        
        while(true) {
            ArrayList toadd = new ArrayList();
            List l = gg.getGraph().queryNodes(visitor);
            
            if (l.isEmpty()) break;
            
            Node n = (Node) l.get(0);
            
            Edge e1 = (Edge) n.getEdges().get(0);
            Edge e2 = (Edge) n.getEdges().get(1);	
            
            if (!e1.getNodeB().equals(n) || !e2.getNodeA().equals(n)) {
                Edge e = e1;
                e1 = e2;
                e2 = e;
            }
            
            //merge the linestrings 
            LineString l1 = (LineString)e1.getObject();
            LineString l2 = (LineString)e2.getObject();
            
            LineString merged = net.refractions.util.geom.GeometryUtil.joinLinestrings(
                    l1,l2    		
            );
            if (!merged.getCoordinateN(0).equals(l1.getCoordinateN(0))) {
                merged = 	(LineString)net.refractions.util.geom.GeometryUtil.reverseGeometry(
                        merged, false
                );
            }
            
            gg.remove(l1);
            gg.remove(l2);
            gg.add(merged);    
        }
        
        //replace the skeletons with our lines
        skeleton = new SkelLineString[gg.getGraph().getEdges().size()];
        int i = 0;
        
        for (Iterator eitr = gg.getGraph().getEdges().iterator(); eitr.hasNext();i++) {
            Edge e = (Edge)eitr.next();
            LineString line = (LineString)e.getObject();
            
            skeleton[i] = new SkelLineString(line);
        }
        
        qa(skeleton, points);
    }
    
    public void qa(SkelLineString[] skeletons, Collection points) {
        //build a directed graph of the skeletons, and run some qa 
        // checks
        //System.out.println("running qa checks");
        DirectedLineStringGraphGenerator gg = new DirectedLineStringGraphGenerator();
        for (int i = 0; i < skeletons.length; i++) {
            gg.add(skeletons[i]);
        }
        
        HashSet inputs = new HashSet();
        HashSet outputs = new HashSet();
        
        for (Iterator itr = points.iterator(); itr.hasNext();) {
            Feature f = (Feature)itr.next();
            com.vividsolutions.jts.geom.Point p = null;
            if (f.getGeometry() instanceof com.vividsolutions.jts.geom.Point) {
                p = (com.vividsolutions.jts.geom.Point)f.getGeometry();
            }
            else if (f.getGeometry() instanceof MultiPoint){
                MultiPoint mp = (MultiPoint)f.getGeometry();
                p = (com.vividsolutions.jts.geom.Point)mp.getGeometryN(0);
            }
            else throw new IllegalStateException("Not a point geometry");
            
            Coordinate c = p.getCoordinate();
            
            Node n = gg.getNode(c);
            if (n == null) continue;
            
            //points should be classified as INPUT or OUTLET
            if (f.getAttribute("FLOW") == null) 
                throw new IllegalStateException("FLOW flag not set for point");
            
            String flow = f.getString("FLOW").toUpperCase().trim();
            
            if (flow.equals("OUTLET")) {
                outputs.add(n);
            }
            else inputs.add(n);
        }
        
        //for each input, we should be able to walk a path to an output
        for (Iterator itr = inputs.iterator(); itr.hasNext();) {
            DirectedNode n = (DirectedNode)itr.next();
            DirectedNode next = n;
            while(n.getOutDegree() > 0) {
                next = ((DirectedEdge)n.getOutEdges().get(0)).getOutNode(); 
                
                if (next.equals(n)) {
                    System.out.println("QA: Error: encountered loop while walking out ouput");
                    break;
                }
                
                n = next;
            }
            
            if (!outputs.contains(n)) {
                System.out.println("QA: Error: walk ended on non output node");
            }
        }
        
        //make sure graph is connected
        DepthFirstIterator dfitr = new DepthFirstIterator();
        GraphTraversal traversal = new BasicGraphTraversal(
                gg.getGraph(), new DummyGraphWalker(), dfitr 		
        );
        dfitr.setSource((Graphable) gg.getGraph().getNodes().iterator().next());
        traversal.init();
        traversal.traverse();
        
        List l = gg.getGraph().getVisitedNodes(false);
        if (!l.isEmpty())
            System.out.println("QA: Error: Graph not fully connected");
    }
    
    /**
     *  do the smart thing with holes
     *  return true if it worked, otherwise return false
     * @param wb
     * @param riverInOutPoints
     * @return
     */
    public boolean smartHoleHandler(Polygon wb,com.vividsolutions.jts.geom.Point[] riverInOutPoints )
    throws Exception
    {
        //	1. run with no holes
        //2. if all good --> done
        //3. if intersection, re-run with just those holes in the polygon
        //4. if all good --> done
        //5. we could have a problem with adding a hole causing an old hole to
        //     become a problem.
        //   add the new bad holes to the old bad holes
        //6. re-run
        //7. we could have a problem with adding a hole causing an old hole to
        //     become a problem.
        //   add the new bad holes to the old bad holes
        //8. re-run
        //9. if all good-->done
        //10. throw error (we'll need to fix this later)
        
        ArrayList allBadHoles = new ArrayList();
        ArrayList badHoles;
        
        // run with no holes
        Polygon wb2 = removePolygonHoles(wb);
        skeleton =  skeletonize( wb2,  riverInOutPoints);
        badHoles = testLinesOnHoles(wb,skeleton);
        if (badHoles == null)  // all good!
        {
            keptHoles = new ArrayList();
            return true;
        }
        allBadHoles.addAll(badHoles);
        
        //run with the bad holes
        wb2 = removePolygonHoles(wb, allBadHoles);
        skeleton =  skeletonize( wb2,  riverInOutPoints);
        badHoles = testLinesOnHoles(wb,skeleton);
        if (badHoles == null)  // all good!
        {
            keptHoles = new ArrayList();
            keptHoles.addAll(allBadHoles);
            return true;
        }
        
        allBadHoles.addAll(badHoles);
        
        //run with the bad holes
        wb2 = removePolygonHoles(wb, allBadHoles);
        skeleton =  skeletonize( wb2,  riverInOutPoints);
        badHoles = testLinesOnHoles(wb,skeleton);
        if (badHoles == null)  // all good!
        {
            keptHoles = new ArrayList();
            keptHoles.addAll(allBadHoles);
            return true;
        }
        
        allBadHoles.addAll(badHoles);
        
        //run with the bad holes
        wb2 = removePolygonHoles(wb, allBadHoles);
        skeleton =  skeletonize( wb2,  riverInOutPoints);
        badHoles = testLinesOnHoles(wb,skeleton);
        if (badHoles == null)  // all good!
        {
            keptHoles = new ArrayList();
            keptHoles.addAll(allBadHoles);
            return true;
        }
        
        
        return false;
    }
    
    /**
     *  takes an input polygon and returns a new polygons without the holes (just the exterior ring)
     * @param wb
     * @return
     */
    public Polygon removePolygonHoles(Polygon wb)
    {
        LinearRing outside = (LinearRing) wb.getExteriorRing();
        return new Polygon(outside,wb.getPrecisionModel(), wb.getSRID());
    }
    
    /**
     *  takes an input polygon and returns a new polygons without the holes (except for the 
     *    holes in keeprings).
     * @param wb
     * @return
     */
    public Polygon removePolygonHoles(Polygon wb, ArrayList keeprings)
    {
        LinearRing outside = (LinearRing) wb.getExteriorRing();
        LinearRing[] inside = (LinearRing[]) keeprings.toArray( new LinearRing[keeprings.size()]);
        
        return new Polygon(outside,inside,wb.getPrecisionModel(), wb.getSRID());
    }
    
    /**
     *  for each holes in the wb, test to see if
     *     there is a skeleton line < 0.001 m away from it
     * 
     * @param wb input polygon (get holes from this)
     * @param skeleton    (skel lines)
     * @return  NULL = all okay, otherwise list (ArrayList of LineString) of holes that have skel lines close to them
     */
    private ArrayList testLinesOnHoles(Polygon wb, SkelLineString[] skeleton)
    {
        ArrayList badHoles = new ArrayList();
        // build index on skeleton lines
        STRtree skelLineTree = new STRtree();
        for (int t=0;t<skeleton.length;t++)
        {
            Envelope e = skeleton[t].getEnvelopeInternal();
            skelLineTree.insert(e,skeleton[t]);
        }
        
        for (int hole=0; hole<wb.getNumInteriorRing();hole++)
        {
            LineString testHole = wb.getInteriorRingN(hole);
            Envelope testEnv = testHole.getEnvelopeInternal();
            // expand a bit
            testEnv = new Envelope( testEnv.getMinX() -0.001, testEnv.getMaxX() +0.001,
                    testEnv.getMinY() -0.001, testEnv.getMaxY() +0.001);
            List possibleTouchingLines = skelLineTree.query(testEnv);
            Iterator it = possibleTouchingLines.iterator();
            boolean badHole = false; 
            while (it.hasNext())
            {
                LineString ls = (LineString) it.next();
                if (ls.distance(testHole) < 0.001)
                {
                    badHole = true;
                }
            }
            if (badHole)
            {
                //System.out.println(testHole);
                badHoles.add(testHole);
            }
        }
        if (badHoles.size() == 0)
            return null;
        else
            return badHoles;
    }
    
    
    /**
     *   find the closest point in ps to the edge.  
     *     must be less <0.1 units or will return null
     * 
     * 
     * @param line  line
     * @param ps    list of com.vividsolutions.jts.geom.Coordinate
     * @return      one of the points in ps or null
     */
    public com.vividsolutions.jts.geom.Point findClosest(SkelLineString line, List ps)
    {
        double minDist = 9999999999.9;
        com.vividsolutions.jts.geom.Point closestPoint =null;
        
        for (int t=0;t<ps.size();t++)
        {
            com.vividsolutions.jts.geom.Point p = (com.vividsolutions.jts.geom.Point) ps.get(t);
            double dist = p.distance(line);
            if (dist < minDist)
            {
                minDist = dist;
                closestPoint = p;
            }
        }
        if (minDist<0.15)
            return closestPoint;
        return null;
    }
    
    
    /**
     *   change any of the "keepMe" (exit/entry point edges) so its clipped to the
     *   node.  
     * 
     *   Also might add a wee little edge to connect the keepme edge to the actual vertex node.
     *   This happens sometimes when a river entry point is at the extream left of the polygon.
     *    The inserted line is <0.01m long.
     * 
     * @param inputSkelLines
     * @return
     */
    public SkelLineString[] trimExitEntryPoint(Polygon poly,SkelLineString[] inputSkelLines,Coordinate[] riverPoints) throws Exception
    {
        
        if (WRITESHAPE)
            GeometryUtil.write_shape("c:\\atTrimEntryPoint.shp",inputSkelLines,inputSkelLines.length);
        
        List closestPts = new ArrayList();  //list of all the riverPoints (as points)
        for (int t=0;t<riverPoints.length;t++)
        {
            closestPts.add( new com.vividsolutions.jts.geom.Point(riverPoints[t], new PrecisionModel(), 0) );
            //Arrays.asList( ( (Coordinate[])  riverPoints.clone() )); //we can delete from this one.  List of Coordinate
        }
        
        
        ArrayList result = new ArrayList();
        
        FastPIP fastPIP = new FastPIP(poly); // prep for fast point in polygon
        
        for (int t=0;t<inputSkelLines.length;t++)  // for each keepme edge, do a clip
        {
            if (inputSkelLines[t].keepMe)  // have a keepme edge
            {
                com.vividsolutions.jts.geom.Point p1 = inputSkelLines[t].getStartPoint();
                com.vividsolutions.jts.geom.Point p2 = inputSkelLines[t].getEndPoint();
                
                boolean p1_inside = fastPIP.PIP(p1.getCoordinate());
                boolean p2_inside = fastPIP.PIP(p2.getCoordinate());
                
                if (  ((p1_inside && p2_inside))  )  //  both inside --> keep 
                {
                    // do nothing
                }
                else if (!(p1_inside ||  p2_inside) ) // both outside --> delete
                {
                    inputSkelLines[t] = null;
                }
                else
                {
                    //one inside, one outside!  we clip
                    com.vividsolutions.jts.geom.Point pt = findClosest(inputSkelLines[t],closestPts );  // which river point this keepme edge belongs to
                    if (pt == null) 
                    {
                        if (!(SILENT))
                            System.out.println(("keepme edge isnt near enough to a river exit point! "+inputSkelLines[t].getStartPoint()+","+inputSkelLines[t].getEndPoint() ));
                        throw new Exception ("keepme edge isnt near enough to a river exit point! "+inputSkelLines[t].getStartPoint()+","+inputSkelLines[t].getEndPoint() );
                    }
                    closestPts.remove(pt);
                    // trim the line
                    //  keep point inside the polygon
                    //  replace outside point with pt
                    if (p1_inside)
                    {			
                        Coordinate[] cs = new Coordinate[2];
                        cs[0] = p1.getCoordinate();
                        cs[1] = pt.getCoordinate();
                        SkelLineString L = new SkelLineString(cs, new PrecisionModel(), 0, true);
                        L.refPointA = inputSkelLines[t].refPointA;
                        L.refPointB = inputSkelLines[t].refPointB;
                        
                        inputSkelLines[t] =L;
                        
                    }
                    else
                    {
                        Coordinate[] cs = new Coordinate[2];
                        cs[0] = p2.getCoordinate();
                        cs[1] = pt.getCoordinate();
                        SkelLineString L = new SkelLineString(cs, new PrecisionModel(), 0, true);
                        L.refPointA = inputSkelLines[t].refPointA;
                        L.refPointB = inputSkelLines[t].refPointB;
                        
                        inputSkelLines[t] =L;
                    }
                }							
            }
            if (inputSkelLines[t] != null)
                result.add(inputSkelLines[t]);
        }
        
        if (closestPts.size() != 0)
        {
            repairMissingEdges(result, closestPts,fastPIP);  // try to repair
        }
        
        if (closestPts.size() != 0)
        {
            repairMissingEdges(result, closestPts,fastPIP);
            if (!(SILENT))
                System.out.println("there are "+closestPts.size()+" river exit/entry points that doesnt have edges");
            if (WRITESHAPE)
                GeometryUtil.write_shape("c:\\missing_edges_inout.shp",GeometryUtil.asCoords(closestPts) );
            throw new IllegalStateException ("there are "+closestPts.size()+" river exit/entry points that doesnt have edges");
        }
        
        return (SkelLineString[]) result.toArray( new SkelLineString[1]) ;
    }
    
    
    /**
     *  Takes a full list of SkelLineString (input), and a list of 
     *  river entry/exit points that a skel line doesnt intersect.
     * 
     *  For each point
     *    find closest skelline
     *          determine which vertex is closest
     *          if distance < 0.02 and vertex is inside polygon
     *             add a wee little line to connect the skeleton to the point
     *  
     *  return true if we can do this for all the points, otherwise false.
     * 
     * @param input full list of SkelLineString   (will have lines added to it)
     * @param closestPts list of com.vividsolutions.jts.geom.Point
     * @param fastPIP     fast point in polygon to verify
     * @return  true if everything good, else false
     */
    private boolean repairMissingEdges(ArrayList input, List closestPts,FastPIP fastPIP)
    {
        
        Iterator it = closestPts.iterator();
        while (it.hasNext())
        {
            com.vividsolutions.jts.geom.Point riverPoint = (com.vividsolutions.jts.geom.Point) it.next();
            for (int t=0;t<input.size();t++)
            {
                SkelLineString line = (SkelLineString) input.get(t);
                if ( (line.keepMe) && (line.distance(riverPoint) < 0.02) )
                {
                    com.vividsolutions.jts.geom.Point startPt = line.getStartPoint();
                    com.vividsolutions.jts.geom.Point endPt = line.getEndPoint();
                    if  ( (riverPoint.distance(startPt)<0.02) && (fastPIP.PIP(startPt.getCoordinate())) )
                    {
                        //add riverPoint -> startPt line
                        Coordinate[] cs = new Coordinate[2];
                        cs[0] = startPt.getCoordinate();
                        cs[1] = riverPoint.getCoordinate();
                        SkelLineString L =  new SkelLineString(cs, new PrecisionModel(), 0, true);
                        L.refPointA = line.refPointA;
                        L.refPointB = line.refPointB;
                        
                        input.add( L );
                        it.remove();
                        break; //out of line list (for t)
                    }
                    else if ( (riverPoint.distance(endPt)<0.02) && (fastPIP.PIP(endPt.getCoordinate())) )
                    {
                        //add riverPoint -> endPt line
                        Coordinate[] cs = new Coordinate[2];
                        cs[0] = endPt.getCoordinate();
                        cs[1] = riverPoint.getCoordinate();
                        
                        SkelLineString L =  new SkelLineString(cs, new PrecisionModel(), 0, true);
                        L.refPointA = line.refPointA;
                        L.refPointB = line.refPointB;
                        
                        input.add( L );
                        it.remove();
                        break; //out of line list (for t)
                    }
                }
            }
        }
        
        return closestPts.size() ==0;
    }
    
    
    
    
    /**
     * 
     *  take a list of voronoi segments
     *    + keep any that have keepMe set
     *    + throw away any that have one of the end points outside the polygon
     * 
     * @param wb     waterbody to clip to
     * @param edges  voronoi segments
     * @return       list of SkeletonLineString
     */
    public ArrayList trimByEndpoints(Polygon wb, ArrayList edges)
    {
        ArrayList result = new ArrayList();
        FastPIP fastPIP = new FastPIP( (Polygon) wb);
        
        for (int i=0; i<edges.size(); i++)
        {				
            Coordinate c1 = new Coordinate(((Point)((Segment)edges.get(i)).p1).x, ((Point)((Segment)edges.get(i)).p1).y);
            Coordinate c2 = new Coordinate(((Point)((Segment)edges.get(i)).p2).x, ((Point)((Segment)edges.get(i)).p2).y);
            Coordinate[] c = {c1, c2};
            
            if ( ((Segment)edges.get(i)).keepMe)
            {
                
                SkelLineString L = new SkelLineString(c, new PrecisionModel(), 0, ((Segment)edges.get(i)).keepMe);
                L.refPointA = ((Segment)edges.get(i)).refPointA.asCoordinate();
                L.refPointB = ((Segment)edges.get(i)).refPointB.asCoordinate();
                result.add(L);
            }
            else
            {
                SkelLineString L = new SkelLineString(c, new PrecisionModel(), 0, ((Segment)edges.get(i)).keepMe);
                L.refPointA = ((Segment)edges.get(i)).refPointA.asCoordinate();
                L.refPointB = ((Segment)edges.get(i)).refPointB.asCoordinate();
                if (fastPIP.PIP(c1) &&fastPIP.PIP(c2) )
                {
                    result.add(L);
                }
            }
        }
        return result;
        
    }
    
    /**
     *   make a network out of the edges 
     *   trim the network
     *   return resulting edges
     * 
     * @param edges list of SkeletonLineSegments
     */
    public SkelLineString[] computeNetwork(ArrayList edges) 
    {
        NetworkBuilder skeletonNetwork = new NetworkBuilder();
        skeletonNetwork.addEdges( (SkelLineString[] ) edges.toArray( new SkelLineString[1] ));
        
        //System.out.println("       + trimming Network");
        /* traverse the network and keep only the edges that are linked to a river */
        
        
        if (WRITESHAPE)
        {
            ArrayList retFinalPre = new ArrayList();
            NetworkEdge[] out = skeletonNetwork.getNetworkEdges();
            for (int i=0; i<out.length; i++)
            {
                
                Coordinate[] c = new Coordinate[2];
                c[0] = new Coordinate(out[i].dnode.coord.x, out[i].dnode.coord.y);
                c[1] = new Coordinate(out[i].unode.coord.x, out[i].unode.coord.y);
                retFinalPre.add( new SkelLineString(c, new PrecisionModel(), 0, out[i].orginallyWasKeep));
            }
            SkelLineString[] retFinal = (SkelLineString[]) retFinalPre.toArray(new SkelLineString[retFinalPre.size()] );
            
            GeometryUtil.write_shape("c:\\during_network.shp",retFinal,retFinal.length);
        }		
        
        
        
        skeletonNetwork.trimNetwork();
        
        //System.out.println("       + getting Network");		
        
        
        //System.out.println("       + reformulating Network edges to SkelLineStrings");
        
        
        ArrayList disconnectedImportantEdges = skeletonNetwork.testConnectivity();
        
        if (disconnectedImportantEdges.size()>0)  //error
        {
            throw new IllegalStateException("network is not connected - "+disconnectedImportantEdges.size()+ " unconnected");
        }
        
        
        
        skeletonNetwork.markUnconnected();
        
        NetworkEdge[] out = skeletonNetwork.getNetworkEdges();
        ArrayList retFinalPre = new ArrayList() ;// of SkelLineString[]
        
        for (int i=0; i<out.length; i++)
        {
            if (out[i].useMe == 1)  // disconnected
            {
                Coordinate[] c = new Coordinate[2];
                c[0] = new Coordinate(out[i].dnode.coord.x, out[i].dnode.coord.y);
                c[1] = new Coordinate(out[i].unode.coord.x, out[i].unode.coord.y);
                SkelLineString L = new SkelLineString(c, new PrecisionModel(), 0, out[i].orginallyWasKeep);
                L.refPointA = out[i].refPointA;
                L.refPointB = out[i].refPointB;
                retFinalPre.add( L );
            }
        }
        SkelLineString[] retFinal = (SkelLineString[]) retFinalPre.toArray(new SkelLineString[retFinalPre.size()] );
        return retFinal;
    }
    
    
    /**
     * 
     * @param p     polygon
     * @param lines lines to test <line>.within(p)
     * @return list of SkelLineString
     */
    public ArrayList trimByWithin(Polygon p,ArrayList lines )
    {
        ArrayList result = new ArrayList();
        FastSegInPolygon segInPoly = new FastSegInPolygon(p);
        
        for (int t=0;t<lines.size(); t++)
        {
            SkelLineString line = (SkelLineString) lines.get(t);
            if (line.keepMe)
            {
                result.add(line);
            }
            else
            {
                Coordinate[] cords = line.getCoordinates();
                
                if (  segInPoly.testSegment(cords[0],cords[1])   )
                    result.add(line);
            }
        }
        return result;
    }
    
    /**
     *    1. create the voronoi diagram w/initial clipping  (say 80,000 edges)
     *    2. clip any edges that have an end point outside the polygon (resulting in 40,000)
     *    3. compute the network   
     *    4. throw away "dangling" ends of the network  (resulting in 3,000)
     *    5. clip any of these edges not within the polygon (might remove one or two)
     *    6. recompute network
     *    7. throw away "dangling" end of the network
     * 
     * @param voron  diagram with points inserted into it
     * @return
     */
    public SkelLineString[] produceSkeleton(Polygon wb, net.refractions.voronoiskeleton.Voronoi voron )
    {
        if (!(SILENT))
            System.out.println("computing voronoi diagram");	
        voron.run();
        ArrayList voronoiEdges = voron.get_output();
        
        if (WRITESHAPE)   
            GeometryUtil.write_shape("c:\\before_endpoint.shp",(Segment[]) voronoiEdges.toArray( new Segment[1]) );
        
        //System.out.println("trimming edges by end points inside polygon (voronoi returned "+voronoiEdges.size() +" edges)");	
        ArrayList trimmedEdges = trimByEndpoints( wb, voronoiEdges);
        //System.out.println("trimming edges by edge.within(polygon)");	
        ArrayList trimmedEdges2 = trimByWithin( wb, trimmedEdges);
        //System.out.println("after edge.within(polygon), there are " + trimmedEdges2.size()+" edges.");
        if (WRITESHAPE)   
            GeometryUtil.write_shape("c:\\before_network.shp",(LineString[]) trimmedEdges2.toArray( new LineString[1]), trimmedEdges2.size() );
        
        SkelLineString[]  netOut =  computeNetwork(trimmedEdges2) ;
        if (!(SILENT))
            System.out.println("after network removal, there are " + netOut.length+" edges.");	
        if (WRITESHAPE)
            GeometryUtil.write_shape("c:\\after_network.shp",netOut,netOut.length);
        return netOut;
    }
    
    /**
     *   1. get polygon and input points
     *   3. modify the oringal polygon so it has the riverInOutPoints removed and replaced with 2 points
     *   4. insert the points into the voronoi diagram
     *   5. process the diagram (other function)
     * 
     * @param wb            waterbody
     * @param riverPoints   vertices of the polygon
     * @return              skeleton
     */
    public SkelLineString[] skeletonizeMain(Polygon wb, com.vividsolutions.jts.geom.Coordinate[] riverInOutPoints) throws Exception
    {
        net.refractions.voronoiskeleton.Voronoi voron = new net.refractions.voronoiskeleton.Voronoi(!(USECLIPVORONOI),useEnhancedPrecisionCircle,useEnhancedPrecisionIntersect);
        //net.refractions.voronoi.Voronoi voron = new net.refractions.voronoi.Voronoi();
        
        if (Array.getLength(riverInOutPoints) <2)
        {
            throw new TooFewRiverPointsException ("skeleton must have at least 2 river entry/exit points! has "+riverInOutPoints.length);
        }
        
        if (!(wb.isValid() ))
        {
            throw new IllegalStateException ("waterbody is an INVALID polygon!");
        }
        
        
        
        
        ArrayList voronoiPoints =  GeometryUtil.pointsFromPoly(handleRiverPoints(wb, riverInOutPoints,true));
        
        
        if (!(SILENT))
            System.out.println("Prepare:: inserting points into voronoi diagram - npoints = "+voronoiPoints.size() );			
        Coordinate[] finalCoordinates = (Coordinate[]) voronoiPoints.toArray(new Coordinate[voronoiPoints.size()]);
        
        //adjustEqualY(finalCoordinates);	
        //adjustEqualX(finalCoordinates);
        if (jiggle)
            jiggeCoords(finalCoordinates);
        
        finalCoordinates = makeUnique(finalCoordinates);
        
        java.util.Arrays.sort(finalCoordinates);
        //CoordinateList finalPoints = new CoordinateList(finalCoordinates);
        
        for (int t=1;t<finalCoordinates.length;t++)
        {
            if (finalCoordinates[t-1].equals2D( finalCoordinates[t] ))
            {
                throw new IllegalStateException ("equal point put into voronoi diagram! " + finalCoordinates[t-1]);
            }
        }
        //translate to origin for better precision
        
        if (WRITESHAPE)
            GeometryUtil.write_shape("c:\\voronoi_points.shp",finalCoordinates);
        
        if (!(SILENT))
            System.out.println("inserting points into voronoi diagram");		
        // convert the points into Points that Voronoi can read, and insert them
        for( int t =0 ;t < finalCoordinates.length;t++ )
        {	
            Coordinate obj = finalCoordinates[t];
            if (obj instanceof SkelCoordinate)
            {
                SkelCoordinate pt = (SkelCoordinate)obj;
                voron.addPointPreSorted(new net.refractions.voronoiskeleton.Point(pt.x , pt.y , pt.metaNum));
            }
            else  // this isnt supposed to happen for polys with >2 river points
            {
                Coordinate pt = (Coordinate)obj;
                voron.addPointPreSorted(new net.refractions.voronoiskeleton.Point(pt.x, pt.y, 0));
            }
        }
        SkelLineString[] result =  produceSkeleton( wb,voron );
        
        if (!(SILENT))
            System.out.println("trimming exit points");
        result = trimExitEntryPoint( wb,result,riverInOutPoints);
        
        return result;
    }
    
    
    
    /**
     *   main entry
     *    prepare data 
     *        - remove unnecessary riverInOutPoints
     *        - add extra riverInOutPoints if there are only 0 or 1
     *        - remove polygon points that are <0.1m from a river entry point
     *        - translate to origin (for precision)
     *        - remove acute angled portions of the polygon at riverInOutPoints
     *                  + add the translated riverInOutpoints
     *        - densify so vertex-vertex distance less than 50m for all vertices on a ring
     *        - number vertices of the polygon
     *        - run adaptive densify
     *             + at this point we have a "good polygon" and river entry points.
     *        - run the skeletonizer
     *        - put in fantom edges (connecting the real in/out point to the re-located
     *                               point for acute angles)
     * @param wb
     * @param riverInOutPoints
     * @return
     */
    
    public SkelLineString[] skeletonize(Polygon wb, com.vividsolutions.jts.geom.Point[] riverInOutPointsPoints) throws Exception
    {
        if (!(SILENT))
            System.out.println("skeleton starts at "+ new Date());
        
        Coordinate[] riverInOutPoints = makeUnique(GeometryUtil.asCoords(riverInOutPointsPoints));
        riverInOutPoints = GeometryUtil.removeUnused(wb,riverInOutPoints);  // only return the ones that are actually on the polygon
        
        // handle 0 or 1 river entry points
        if ( riverInOutPoints.length <2)
        {
            if (DO1AND2POINT)
            {
                if (riverInOutPoints.length ==0)
                {
                    //no points
                    riverInOutPoints  = GeometryUtil.find2Coords(wb);
                }
                else
                {
                    //1
                    Coordinate c1=riverInOutPoints[0];
                    Coordinate c2=GeometryUtil.find1Coordinate(wb,c1);
                    if (c2 == null)
                    {
                        throw new IllegalStateException ("Couldnt find 2nd point for wb with 1 river entry point");
                    }
                    riverInOutPoints = new Coordinate[2];
                    riverInOutPoints[0] = c1;
                    riverInOutPoints[1] = c2;
                }
            }
            else
            {
                throw new TooFewRiverPointsException ("skeleton must have at least 2 river entry/exit points! has "+riverInOutPoints.length);
            }
            
        }
        
        
        //remove coordinates from polygon that are <0.1m from a river entry point				
        wb = GeometryUtil.removeClosePoints(wb,riverInOutPoints,0.25);
        
        
        Envelope env = wb.getEnvelopeInternal();
        double move_x = (env.getMinX()+env.getMaxX())/2.0;
        double move_y = (env.getMinY()+env.getMaxY())/2.0;
        
        GeometryUtil.translate(riverInOutPoints,-move_x,-move_y);
        Polygon wb_translated = GeometryUtil.translate(wb,-move_x,-move_y);
        
        if (WRITESHAPE)
            GeometryUtil.write_shape("c:\\poly_translated.shp",wb_translated);				
        
        Object[] ret = GeometryUtil.clipSharpCorners(wb_translated, riverInOutPoints,criticalCutAngle);	//handle acute angles and densify to 50m
        Polygon wb_CornerClipped = (Polygon) ret[0];
        ArrayList extraConnectingLines =  (ArrayList) ret[1];  //of 2point SkelLineString.  Always from original to new.
        Polygon wb_CornerClippedSimpleDensify = GeometryUtil.simpleDensify(wb_CornerClipped,50);
        
        if (WRITESHAPE)
            GeometryUtil.write_shape("c:\\poly.shp",wb_CornerClippedSimpleDensify);	
        
        Coordinate[] newRiverCoords = reformulate(riverInOutPoints,extraConnectingLines );
        
        
        //densify
        Polygon wbNumbered = handleRiverPoints(wb_CornerClippedSimpleDensify, newRiverCoords,false); // this numbers the points
        AdapativeDensifyNumbered ad = new AdapativeDensifyNumbered( wbNumbered,densifyFactor);
        Polygon wbDense = ad.outputPolygon; 
        
        
        
        SkelLineString[] skelLines = skeletonizeMain( wbDense,  newRiverCoords);
        
        
        // need to add the little connecting lines
        ArrayList resultList = new ArrayList (  Arrays.asList(skelLines)) ;
        resultList.addAll(extraConnectingLines);
        
        //convert our skellinestring back
        SkelLineString[] result =(SkelLineString[]) resultList.toArray(new SkelLineString[resultList.size()]);
        
        //move back to correct location
        GeometryUtil.translate(result,move_x,move_y);
        
        result = deleteZeroLengthLines(result);
        
        if (WRITESHAPE)
        {
            GeometryUtil.write_shape("c:\\finalSkel.shp", result,result.length);
        }
        
        if (!(SILENT))
            System.out.println("skeleton ends at "+ new Date());	
        
        return result;
    }
    
    
    
    
    
    
    /**
     *  Due to precision errors, very small lines (10^-12m) may now be zero length
     *  because when translated back from the origin.
     *  
     *  We delete any edge that has first point = last point (strictly equal!!).
     * 
     *  This is safe to do because there will be another edge that has this point as well. 
     * 
     * @param result - input skel lines
     * @result same as result, but any zero length lines removed
     */
    private SkelLineString[] deleteZeroLengthLines(SkelLineString[] input)
    {
        ArrayList result = new ArrayList();
        for (int t=0;t<input.length ; t++)
        {
            if (input[t].getLength() < 0.000001)
            {
                // this might be a zero length one!
                Coordinate c1 = input[t].getCoordinateN(0);                            // first one
                Coordinate c2 = input[t].getCoordinateN( input[t].getNumPoints() - 1); //last one
                
                if ( (c1.x == c2.x) && (c1.y == c2.y) )
                {
                    // do nothing, we delete it!
                    if (!(SILENT))
                        System.out.println("deleted zero length edge");
                }
                else
                {
                    result.add(input[t]);
                }
                
            }
            else
            {
                result.add(input[t]);
            }
        }
        
        return (SkelLineString[]) result.toArray(new SkelLineString[0] );
    }
    
    
    
    
    /**
     *  takes the orginal river entry points and the result of the corner clipper
     *    makes a new list of coordinates that reflects the new location of the river entry points
     * @param original     original river entry points
     * @param trans  SkelLineString produced by the sharp angle corner clipper
     * @return
     */
    public Coordinate[] reformulate(Coordinate[] original,ArrayList trans )
    {
        Coordinate[] result = new Coordinate[original.length];
        
        for (int t=0;t<original.length;t++)
        {
            result[t] = original[t];
        }
        
        // need to change riverInOutPoints to reflect new ones
        for (int t=0;t<trans.size(); t++)
        {
            SkelLineString translation = (SkelLineString)  trans.get(t);
            
            Coordinate[] cs = translation.getCoordinates();
            Coordinate oldCord = cs[0];
            Coordinate newCord = cs[1];
            for (int u=0;u<original.length;u++)
            {
                Coordinate rivPoint = original[u];
                if (rivPoint.equals2D(oldCord))
                {
                    result[u]= newCord;
                }
            }
        }
        return result;
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    /**
     *  
     *  number = 0
     *  for each ring
     *     number += 10
     *     for each point in ring
     *        if point is a river entry point then
     *           number += 11
     *           Add river entry point A
     *           number++
     *           Add river entry point B
     *           number +=10
     * 
     * 
     *   For example, a ring with two entry point would go like this:
     *     1st point labelled 10
     *     2nd point labelled 10
     *      ...
     *     (hit river entry point)
     *      new point labelled 21
     *      new point labelled 22
     *     next point labelled 33
     *      ..
     *     (hit river entry point)
     *        new point labelled 44
     *        new point labelled 45
     *     next point labelled 56
     *     ....
     * 
     *
     * Dont emit the ring's start and end point twice (they are equal)
     * 
     * This is modified version of brent's code (thats why there are strange numbers) 
     * 
     * 
     * @param wb             input polygon 
     * @param riverCoords    river entry points
     * @param insertPoints   true if you want the river entry point replaced with 2 points
     * @return               list of list of skelCoordinates.  This is a list of rings.  remove the last point from each ring and its ready to go in voronoi diagram 
     */
    public Polygon handleRiverPoints(Polygon wb, Coordinate[] riverCoords,boolean insertPoints)
    {
        ArrayList allRings = new ArrayList(); // list of LineString (all the rings, including exterior)
        ArrayList resultRingList = new ArrayList();  //hold emitted points
        
        ArrayList ringCoords;
        
        allRings.add(wb.getExteriorRing());
        for (int t=0;t<wb.getNumInteriorRing();t++)
        {
            allRings.add(wb.getInteriorRingN(t));
        }
        
        //these are now in sorted order so we can do a binary search!
        Arrays.sort(riverCoords);
        
        
        int number = 0;
        
        for (int t=0;t<allRings.size();t++) //for each ring
        {
            ringCoords = new ArrayList();
            number +=10;
            LineString ls = (LineString) allRings.get(t);
            Coordinate[] cs = ls.getCoordinates();
            for (int u=0;u<(cs.length-1);u++) //For each point in ring { dont do the last point (same as first) }
            {
                Coordinate c=cs[u]; 
                int location = Arrays.binarySearch(riverCoords,c);
                if (location>=0)
                {
                    //found -- this is a river entry point
                    number+=11;
                    if (insertPoints)
                    {
                        //normal case
                        Coordinate[] newPoints = makeRiverEntryPoints(u,  cs);
                        ringCoords.add(new SkelCoordinate(newPoints[0].x,newPoints[0].y,number,true ));
                        number +=1;
                        ringCoords.add(new SkelCoordinate(newPoints[1].x,newPoints[1].y,number,true ));
                        number +=11;
                        //actuallyInsertedPoint.add(new com.vividsolutions.jts.geom.Point(c, new PrecisionModel(), 0 ));
                    }
                    else
                    { 
                        //for the adapative densify
                        ringCoords.add( new SkelCoordinate(c.x,c.y,number));
                    }
                }
                else
                {
                    //not found  -- this is not a river entry point
                    ringCoords.add( new SkelCoordinate(c.x,c.y,number));
                }
            }
            ringCoords.add( ringCoords.get(0));
            resultRingList.add(ringCoords);
        }
        return  GeometryUtil.buildPoly2(resultRingList);
    }
    
    
    
    /**
     *    replace a point with two points very near to it.
     *    use the edges that come into and go out of this edge
     *    be wise if location =0 (first points in ring --> use 2nd to last point in ring)  
     *    dont call with location = cs.length-1 (last point --> call with location =0)
     * 
     * @param location  location in the coordinate array of the point you want to replace
     * @param cs        ring coords
     * @return          array of 2 coordinates
     */
    public Coordinate[] makeRiverEntryPoints(int location, Coordinate[] cs)
    {
        Coordinate me,prev,next;
        Coordinate[] result = new Coordinate[2];
        me = cs[location];
        next =cs[location+1];
        
        if (location ==0)
        {
            prev = cs[cs.length-2];  //special case
        }
        else
        {
            prev = cs[location-1];  //normal case
        }
        
        
        // get angle of prev/rem with horiz line
        double thetaPR = getAngle(prev, me);
        
        // get angle of rem/next with horiz line
        double thetaNR = getAngle(next, me);
        
        // create new prev point to replace removed
        double px = me.x + shift * Math.cos(Math.toRadians(thetaPR));
        double py = me.y + shift * Math.sin(Math.toRadians(thetaPR));
        result[0] =  new Coordinate(px, py);
        
        
        // create new next point to replace removed
        double nx = me.x + shift * Math.cos(Math.toRadians(thetaNR));
        double ny = me.y + shift * Math.sin(Math.toRadians(thetaNR));
        result[1] =  new Coordinate(nx, ny);
        if (Math.abs(px-nx)<0.00001)
        {
            System.out.println("river entry points are on a verticle line!");
            result[0].x -= 0.0001;
            result[1].x += 0.0001;
        }
        
        return result;
    }
    
    
    /**
     * @param p
     * @param removed
     * @return
     */
    private double getAngle(com.vividsolutions.jts.geom.Coordinate p,
            com.vividsolutions.jts.geom.Coordinate removed)
    {
        
        double px, py, rx=1, ry=0;
        boolean neg = false;
        px = p.x - removed.x;
        
        py = p.y - removed.y;
        
        if (p.y < removed.y)
            neg = true;
        // use dot product		theta = arccos((v.w)/(|v|x|w|))
        double prevll = Math.sqrt(px*px + py*py);
        px = px/prevll;	// normalize the sucker
        py = py/prevll;
        double theta = Math.toDegrees(Math.acos(px*rx + py*ry));
        // looks like it absolute values the angle, this is fixed below
        if (neg)
            theta = 360-theta;
        
        return theta;
    }
    
    
    
    
    
    /**
     * @param finalCoordinates
     */
    private void jiggeCoords(Coordinate[] finalCoordinates)
    {
        for (int t=0;t<finalCoordinates.length; t++)
        {
            if (finalCoordinates[t] instanceof SkelCoordinate)
            {
                if (!(( (SkelCoordinate) finalCoordinates[t]).entryPoint))
                {
                    finalCoordinates[t].x += Math.random()/25.0-1.0/50.0;
                    finalCoordinates[t].y += Math.random()/25.0-1.0/50.0;
                }
            }
            else
            {
                finalCoordinates[t].x += Math.random()/25.0-1.0/50.0;
                finalCoordinates[t].y += Math.random()/25.0-1.0/50.0;
            }
            
        }
        Arrays.sort(finalCoordinates);
        
    }
    
    
    
    
    private boolean adjustY(Coordinate[] cs)
    {
        boolean result = false;
        
        for (int t=1;t<cs.length;t++)
        {
            if (cs[t-1].y == cs[t].y)
            {
                //have a problem
                result = true;
                cs[t-1].y -= 0.001;
            }
        }
        return result;
    }
    
    private void adjustEqualY(Coordinate[] cs)
    {
        java.util.Arrays.sort(cs,new PointSortY() );
        //keep going until not adjusted
        while ( adjustY(cs) )
        {
            // sort (just to be on the safe side)
            java.util.Arrays.sort(cs,new PointSortY());
        }
    }
    
    
    
    /**
     *   This will take 2 points that have equal X coords and change the x coord slightly.
     *    
     * @param cs  array of Coordiante (in sorted order)
     * @return    true if an adjustment was made (you should keep calling this until its false)
     */
    private boolean adjust(Coordinate[] cs)
    {
        boolean result = false;
        
        for (int t=1;t<cs.length;t++)
        {
            if (cs[t-1].x == cs[t].x)
            {
                //have a problem
                result = true;
                cs[t-1].x -= 0.001;
            }
        }
        return result;
    }
    
    /**
     *   Adjusts coords so you dont have 2 with equal X  
     * @param cs  array of Coordiante (in sorted order)
     */
    private void adjustEqualX(Coordinate[] cs)
    {
        //keep going until not adjusted
        while ( adjust(cs) )
        {
            // sort (just to be on the safe side)
            java.util.Arrays.sort(cs);
        }
    }
    
    
    /**
     *   This will sort then remove points that are equal
     * @param in  array of coordinate
     * @return
     */
    public Coordinate[] makeUnique(Coordinate[] in)
    {
        if (in.length >1)
        {
            
            Coordinate[] input =  (Coordinate[]) in.clone();
            ArrayList uniques = new ArrayList();
            Arrays.sort(input);
            
            uniques.add(input[0]);
            for (int t=1;t<input.length;t++)
            {
                if (!(input[t-1].equals2D(input[t])))
                {
                    uniques.add(input[t]);
                }
            }
            return (Coordinate[]) uniques.toArray( new Coordinate[uniques.size()]);
        }
        else
        {
            return  (Coordinate[]) in.clone();
        }
    }
    
}
