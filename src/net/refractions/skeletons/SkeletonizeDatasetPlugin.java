/*
 * 
 * The Skeletonizer Utility is distributed under GNU General Public Licence � It
 * is free software and can be redistributed and/or modified under the terms of
 * the GNU General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but comes
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. Please see the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if this is not the case, please write to:
 * 
 * The Free Software Foundation, Inc. 59 Temple Place - Suite 330 Boston - MA
 * 02111-1307 - USA.
 *  
 */

package net.refractions.skeletons;

import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.vividsolutions.jts.geom.Envelope;
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
import com.vividsolutions.jump.io.DriverProperties;
import com.vividsolutions.jump.io.IllegalParametersException;
import com.vividsolutions.jump.io.ShapefileReader;
import com.vividsolutions.jump.io.ShapefileWriter;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedBasePlugIn;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.renderer.style.BasicStyle;

import net.refractions.voronoiskeleton.Voronoi;

public class SkeletonizeDatasetPlugin implements ThreadedPlugIn {
	GetFnamesDialog fnameDialog = null;

	FeatureCollection inputPoint = null;

	FeatureCollection inputPoly = null;

	FeatureCollection badPolyCollection = null;

	FeatureCollection resultCollection = null;

	String outputReport = "";

	String att_POLYID = "POLYID";

	String att_POINTID = "POINTID";

	boolean directionalize = false;

	boolean fuse = false;

	public void initialize(PlugInContext context) throws Exception {
		context.getFeatureInstaller().addMainMenuItem(this,
				new String[] { "Skeletonize" }, "Skeletonize Wizard...", false,
				null, new MultiEnableCheck());
	}

	//get user to specify input/output datasets
	public boolean execute(PlugInContext context) throws Exception {
		fnameDialog = new GetFnamesDialog(null, true);
		GUIUtil.centreOnScreen(fnameDialog);

		if (fnameDialog.doDialog()) {
			//System.out.println("go go go");

			//					System.out.println("poly in:"+fnameDialog.getPolygonFilename());
			//					System.out.println("point in:"+fnameDialog.getPointFilename());
			//					System.out.println("line out:"+fnameDialog.getLineFilename());
			//					System.out.println("text out:"+fnameDialog.getReportFilename());

			if ((fnameDialog.getPolygonFilename() == null)
					|| (fnameDialog.getPointFilename() == null)
					|| (fnameDialog.getLineFilename() == null)
					|| (fnameDialog.getReportFilename() == null)) {
				throw new Exception(
						"one of the files is null - use the 'Browse...' to specify a file");
			}
			return true;
		} else {
			//System.out.println("canceled");
			return false;
		}
	}

	public void run(TaskMonitor monitor, PlugInContext context) {
		ShapefileWriter sfw = new ShapefileWriter();
		ShapefileReader sfr = new ShapefileReader();
		DriverProperties dp = null;

		FeatureSchema fs = new FeatureSchema();

		fs.addAttribute("GEOM", AttributeType.GEOMETRY);
		fs.addAttribute(att_POLYID, AttributeType.INTEGER);
		fs.addAttribute(att_POINTID, AttributeType.INTEGER);

		resultCollection = new FeatureDataset(fs);

		try {
			//load polygon file
			monitor.report("Loading Polygon Shapefile");
			dp = new DriverProperties(fnameDialog.getPolygonFilename());
			inputPoly = sfr.read(dp);

			Layer layerPoly = context.addLayer(StandardCategoryNames.REFERENCE,
					"WaterBodies", inputPoly);
			boolean firingEvents = layerPoly.getLayerManager().isFiringEvents();
			layerPoly.getLayerManager().setFiringEvents(false);
			layerPoly.setVisible(true);
			try {
				BasicStyle bs = layerPoly.getBasicStyle();
				bs.setFillColor(new Color(100, 100, 255));
				bs.setLineColor(new Color(0, 0, 0));
				bs.setLineWidth(2);
			} finally {
				layerPoly.getLayerManager().setFiringEvents(firingEvents);
			}
			layerPoly.fireAppearanceChanged();

			//load point file
			monitor.report("Loading Point Shapefile");
			dp = new DriverProperties(fnameDialog.getPointFilename());
			inputPoint = sfr.read(dp);

			Layer layerPoint = context.addLayer(
					StandardCategoryNames.REFERENCE, "Entry/Exit points",
					inputPoint);
			firingEvents = layerPoint.getLayerManager().isFiringEvents();
			layerPoint.getLayerManager().setFiringEvents(false);
			layerPoint.setVisible(true);
			try {
				BasicStyle bs = layerPoint.getBasicStyle();
				bs.setLineColor(new Color(200, 0, 0));
				bs.setLineWidth(2);
			} finally {
				layerPoint.getLayerManager().setFiringEvents(firingEvents);
			}
			layerPoint.fireAppearanceChanged();

			//process

			makeSkeletons(monitor);

			//			 monitor.report("Saving bad Polygon Shapefile");
			//			 dp = new DriverProperties("c:\\bad_poly.shp");
			//			 if (badPolyCollection.size() >0)
			//				 sfw.write(badPolyCollection,dp);
			//			 else
			//				 System.out.println("NO BAD POLYS TO WRITE OUT!");

			//finish
			//save lines
			monitor.report("Saving Line Shapefile");
			dp = new DriverProperties(fnameDialog.getLineFilename());
			sfw.write(resultCollection, dp);

			// save report
			monitor.report("Saving Report");
			FileWriter fw = new FileWriter(fnameDialog.getReportFilename());

			fw.write(outputReport);
			fw.close();

			Layer layerResult = context.addLayer(
					StandardCategoryNames.REFERENCE, "Skeleton",
					resultCollection);
			firingEvents = layerResult.getLayerManager().isFiringEvents();
			layerResult.getLayerManager().setFiringEvents(false);
			layerResult.setVisible(true);
			try {
				BasicStyle bs = layerResult.getBasicStyle();
				bs.setLineColor(new Color(0, 0, 250));
				bs.setLineWidth(2);
			} finally {
				layerResult.getLayerManager().setFiringEvents(firingEvents);
			}
			layerResult.fireAppearanceChanged();

		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException(e.toString());
		}
	}

	// assumes these are setup::
	//FeatureCollection inputPoint = null;
	//FeatureCollection inputPoly = null;

	// this sets up the badPolyCollection
	// and populates resultCollection

	private void makeSkeletons(TaskMonitor monitor) throws Exception {

		outputReport = "" + new Date() + "\n";
		;

		DriverProperties dp;

		FeatureSchema fs = new FeatureSchema();

		fs.addAttribute("GEOM", AttributeType.GEOMETRY);
		fs.addAttribute(att_POLYID, AttributeType.INTEGER);
		fs.addAttribute(att_POINTID, AttributeType.INTEGER);

		badPolyCollection = new FeatureDataset(fs);

		if (monitor != null)
			monitor.report("Bulding Skeletons");
		Iterator it = inputPoly.iterator();
		int ndone = 0;
		int n = inputPoly.size();

		while (it.hasNext()) {
			Feature f = (Feature) it.next();
			Geometry g = f.getGeometry();

			Polygon poly = null;
			if (g.getGeometryType().equalsIgnoreCase("Polygon"))
				poly = (Polygon) g;
			if (g.getGeometryType().equalsIgnoreCase("MultiPolygon")) {
				MultiPolygon mp = (MultiPolygon) g;
				poly = (Polygon) mp.getGeometryN(0);
			}
			Integer id = null;
			try {

				id = (Integer) f.getAttribute("ID");
				if (id == null)
					id = new Integer(-1);
				if (poly == null) {
					throw new IllegalStateException(
							"polygon shapefile doesnt have polygons in it!");
				}
			} catch (Exception ex) {
				try {
					id = new Integer(f.getID());
				} catch (Exception exx) {
					id = new Integer(ndone);
				}
			}
			if (monitor != null)
				monitor.report(ndone, n, "       (Polygon ID = "
						+ id.toString() + " with " + poly.getNumPoints()
						+ " undensified points)");
			//System.out.println("doing polygon ID = "+id.toString());

			// have polygon - lets find points in the bounding box of the
			// geometry
			Envelope e = poly.getEnvelopeInternal();
			List points = inputPoint.query(e);

			Point[] pts = new Point[points.size()];
			Iterator it_pts = points.iterator();
			int t = 0;
			while (it_pts.hasNext()) {
				Feature f_pt = (Feature) it_pts.next();
				Geometry gg = f_pt.getGeometry();
				Point p = null;
				if (gg.getGeometryType().equalsIgnoreCase("POINT")) {
					p = (Point) gg;
				}
				if (gg.getGeometryType().equalsIgnoreCase("MULTIPOINT")) {
					MultiPoint mpt = (MultiPoint) gg;
					p = (Point) mpt.getGeometryN(0);
				}
				pts[t] = p;
				t++;
			}

			//          					try{
			//Skeletonizer skel = new Skeletonizer((Polygon)poly, pts, 1.0,
			// false);

			SkelLineString[] newlines = null;
			try {
				newlines = makeSkeleton(poly, pts);
			} 
			catch (TooFewRiverPointsException eTooFew) {
					outputReport = outputReport +"\n" + "Error occured processing polygon +"+id.toString()+" ("+eTooFew.toString()+")";
					//be quite
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				outputReport = outputReport +"\n" + "Error occured processing polygon +"+id.toString()+" ("+ex.toString()+")";
				Feature ff = new BasicFeature(fs);
		          ff.setGeometry(poly);
		          	ff.setAttribute(att_POLYID, id);
		          	ff.setAttribute(att_POINTID,new Integer(-1));
		          badPolyCollection.add(ff);
			}
			
			if (newlines != null) {
				add(resultCollection, newlines, fs, id.intValue());
				ndone++;
			}

				
		}

		outputReport += "\n";
		outputReport += " (easy fast ones - NEVER enhance precision)     --> "
				+ nLevel0Okay + "\n";
		outputReport += " (easy fast ones - SOMETIMES enhance precision) --> "
				+ nLevel1Okay + "\n";
		outputReport += " (double densified, enhanced circle)            --> "
				+ nLevel2Okay + "\n";
		outputReport += " (quadruple densified, enhanced circle)         --> "
				+ nLevel3Okay + "\n";
		outputReport += " (3/4 densified, jiggled, enhanced circle)      --> "
				+ nLevel4Okay + "\n";
		outputReport += " (failed)                                       --> "
				+ badPolyCollection.size() + "\n";

		outputReport += "" + new Date() + "\n";

	}

	int nLevel0Okay = 0;

	int nLevel1Okay = 0;

	int nLevel2Okay = 0;

	int nLevel3Okay = 0;

	int nLevel4Okay = 0;

	private Skeletonizer makeSkeleton(Polygon wb, Point[] wbInOutPoints, double densifyFactor, 
			boolean jiggle, int enhancedCircle, int enhancedIntersect, boolean useCLIP ) throws Exception {
		Skeletonizer skel = new Skeletonizer(wb, wbInOutPoints, densifyFactor, jiggle,
				enhancedCircle, enhancedIntersect, useCLIP, Skeletonizer.KEEPHOLES);
		if (directionalize) skel.directionalize(skel.skeleton, inputPoint.getFeatures());
		else if (fuse) {
			skel.fuse(skel.skeleton);
		}
		return skel;
		
	}
	private SkelLineString[] makeSkeleton(Polygon wb, Point[] wbInOutPoints)
			throws Exception {

		int what_to_do_with_holes = Skeletonizer.KEEPHOLES;

		Skeletonizer skel = null;
		try {
			try {
				skel = makeSkeleton(wb, wbInOutPoints, 1.0, false,Voronoi.NEVER, Voronoi.NEVER, true);
//				skel = new Skeletonizer(wb, wbInOutPoints, 1.0, false,
//						Voronoi.NEVER, Voronoi.NEVER, true,
//						what_to_do_with_holes);
//				if (directionalize)
//					skel.directionalize(skel.skeleton, inputPoint
//									.getFeatures());
				//	return skel.skeleton;
			} catch (Exception e) {
//				skel = new Skeletonizer(wb, wbInOutPoints, 1.0, false,
//						Voronoi.SOMETIMES, Voronoi.SOMETIMES, false,
//						what_to_do_with_holes);
//				if (directionalize)
//					skel
//							.directionalize(skel.skeleton, inputPoint
//									.getFeatures());
				//	return skel.skeleton;
				skel = makeSkeleton(wb, wbInOutPoints, 1.0, false,Voronoi.SOMETIMES, Voronoi.SOMETIMES, false);
			}
		} catch (TooFewRiverPointsException eTooFew) {
			throw eTooFew; // immediately bomb out - dont have to try again
						   // because its not going to work!
		} catch (Exception e) {
			try {
				e.printStackTrace();
				System.out
						.println("got error "
								+ e.toString()
								+ " trying with densify=0.75, enhanced precision circle");
//				skel = new Skeletonizer(wb, wbInOutPoints, 0.5, false,
//						Voronoi.ALWAYS, Voronoi.SOMETIMES, false,
//						what_to_do_with_holes);
//				if (directionalize)
//					skel
//							.directionalize(skel.skeleton, inputPoint
//									.getFeatures());
				skel = makeSkeleton(wb, wbInOutPoints, 0.5, false,Voronoi.ALWAYS, Voronoi.SOMETIMES, false);
				//return skel.skeleton;
			} catch (Exception ex) {
				try {
					ex.printStackTrace();
					System.out
							.println("got error "
									+ ex.toString()
									+ " trying with densify=0.25 enhanced precision circle");
//					skel = new Skeletonizer(wb, wbInOutPoints, 0.25, false,
//							Voronoi.ALWAYS, Voronoi.SOMETIMES, false,
//							what_to_do_with_holes);
//					if (directionalize)
//						skel.directionalize(skel.skeleton, inputPoint
//								.getFeatures());
					//return skel.skeleton;
					skel = makeSkeleton(wb, wbInOutPoints, 0.25, false, Voronoi.ALWAYS, Voronoi.SOMETIMES, false);
				} catch (Exception exx) {
					try {
						exx.printStackTrace();
						System.out
								.println("got error "
										+ exx.toString()
										+ " trying with densify=0.0825 enhanced precision circle");
//						skel = new Skeletonizer(wb, wbInOutPoints, 0.0825,
//								false, Voronoi.ALWAYS, Voronoi.SOMETIMES,
//								false, what_to_do_with_holes);
//						if (directionalize)
//							skel.directionalize(skel.skeleton, inputPoint
//									.getFeatures());
						skel = makeSkeleton(wb, wbInOutPoints, 0.0825, false, Voronoi.ALWAYS, Voronoi.SOMETIMES,false);
						//return skel.skeleton;
					} catch (Exception exxx) {

						try {
							exxx.printStackTrace();
							System.out
									.println("got error "
											+ exxx.toString()
											+ " trying with densify=0.25 full precision math ");
							//writeErrorToDatabase(polygon_id, "WARNING:
							// required full precision math");
//							skel = new Skeletonizer(wb, wbInOutPoints, 0.25,
//									false, Voronoi.ALWAYS, Voronoi.ALWAYS,
//									false, what_to_do_with_holes); //both are
//																   // ALWAYS, so
//																   // this will
//																   // take some
//																   // time to
//																   // compute!
//							if (directionalize)
//								skel.directionalize(skel.skeleton, inputPoint
//										.getFeatures());
							skel = makeSkeleton(wb, wbInOutPoints, 0.25, false, Voronoi.ALWAYS, Voronoi.ALWAYS, false);
						} catch (Exception exxxx) {
							throw exxxx;
						}
					}
				}
			}
		}
		return skel.skeleton;
	}

	//	private SkelLineString[] makeSkeleton(Polygon wb, Point[]
	// riverInOutPoints) throws Exception
	//	{
	//// try {
	//// try{
	//				Skeletonizer skel = new Skeletonizer(wb, riverInOutPoints, 1.0, false ,
	// Voronoi.NEVER, Voronoi.NEVER, true, Skeletonizer.SMARTHOLES);
	//				skel.directionalize(skel.skeleton, inputPoint.getFeatures());
	//				
	//				nLevel0Okay++;
	//				return skel.skeleton;
	//// }
	//// catch(Exception e)
	//// {
	//// e.printStackTrace();
	//// Skeletonizer skel = new Skeletonizer(wb, riverInOutPoints, 1.0, false
	// , Voronoi.SOMETIMES,Voronoi.SOMETIMES,false, Skeletonizer.SMARTHOLES);
	//// skel.directionalize(skel.skeleton, inputPoint.getFeatures());
	////
	//// nLevel1Okay++;
	//// return skel.skeleton;
	//// }
	//			
	//// }
	//// catch (TooFewRiverPointsException eTooFew)
	//// {
	//// throw eTooFew; // immediately bomb out - dont have to try again
	// because its not going to work!
	//// }
	//// catch (Exception e)
	//// {
	//// try{
	//// e.printStackTrace();
	//// System.out.println("got error "+e.toString()+" trying with
	// densify=0.75, enhanced precision circle");
	////
	//// Skeletonizer skel = new Skeletonizer(wb, riverInOutPoints, 0.5,
	// false,Voronoi.ALWAYS,Voronoi.SOMETIMES,false, Skeletonizer.SMARTHOLES);
	//// skel.directionalize(skel.skeleton, inputPoint.getFeatures());
	////
	//// nLevel2Okay++;
	//// return skel.skeleton;
	//// }
	//// catch (Exception ex)
	//// {
	//// try{
	//// ex.printStackTrace();
	//// System.out.println("got error "+ex.toString()+" trying with
	// densify=0.25 enhanced precision circle");
	////
	//// Skeletonizer skel = new Skeletonizer(wb, riverInOutPoints, 0.25,
	// false,Voronoi.ALWAYS,Voronoi.SOMETIMES,false, Skeletonizer.SMARTHOLES);
	//// skel.directionalize(skel.skeleton, inputPoint.getFeatures());
	////
	//// nLevel3Okay++;
	//// return skel.skeleton;
	//// }
	//// catch (Exception exx) // try with jiggle - back to decent subdivision
	//// {
	//// try{
	//// exx.printStackTrace();
	//// System.out.println("got error "+exx.toString()+" trying with
	// densify=0.75, jiggle enhanced precision circle");
	////
	//// Skeletonizer skel = new Skeletonizer(wb, riverInOutPoints, 0.75,
	// true,Voronoi.ALWAYS,Voronoi.SOMETIMES,false, Skeletonizer.SMARTHOLES);
	//// skel.directionalize(skel.skeleton, inputPoint.getFeatures());
	////
	//// nLevel4Okay++;
	//// return skel.skeleton;
	//// }
	//// catch (Exception exxx)
	//// {
	//// throw exxx;
	//// }
	//// }
	////
	//// }
	//// }
	//	}

	private void add(FeatureCollection fc, LineString[] lines,
			FeatureSchema fs, int polyid) {
		for (int t = 0; t < lines.length; t++) {
			Feature f = new BasicFeature(fs);
			f.setGeometry(lines[t]);
			f.setAttribute(att_POLYID, new Integer(polyid));
			f.setAttribute(att_POINTID, new Integer(-1));
			fc.add(f);
		}
	}

	/**
	 * call with args[0] = points shapefile [1] = polygon shapefile [2] = output
	 * line shapefile [3] = output report shapefile [4] = output bad polygon
	 * shapefile
	 */
	public static void main(String[] args) throws Exception {
		if (args.length < 5) {
			System.out.println("Usage: SkeletonizeDatasetPlugin <points shapefile> <polygon shapefile> <output line shapefile> <output report txt> <output bad polygon shapefile> OPTIONS");
			System.out.println("OPTIONS:");
			System.out.println("-f: fuse the skeletons");
			System.out.println("-d : directionalize the skeletons (will fuse even if fuse option not specified)");
			return;
		}

		SkeletonizeDatasetPlugin THIS = new SkeletonizeDatasetPlugin();
		
		for (int i = 0; i < args.length; i++) {
		  if (args[i].equals("-d")) {
		    THIS.directionalize = true; 	
		  }
		  if (args[i].equals("-f")) {
		  	THIS.fuse = true;
		  }
		}

		FeatureSchema fs = new FeatureSchema();
		fs.addAttribute("GEOM", AttributeType.GEOMETRY);
		fs.addAttribute(THIS.att_POLYID, AttributeType.INTEGER);
		fs.addAttribute(THIS.att_POINTID, AttributeType.INTEGER);

		THIS.resultCollection = new FeatureDataset(fs);

		ShapefileWriter sfw = new ShapefileWriter();
		ShapefileReader sfr = new ShapefileReader();
		DriverProperties dp = null;

		//		try{
		dp = new DriverProperties(args[0]);
		THIS.inputPoint = sfr.read(dp);

		dp = new DriverProperties(args[1]);
		THIS.inputPoly = sfr.read(dp);

		//do work
		THIS.makeSkeletons(null);

		String outfile = args[2];
		String reportfile = args[3];
		String badoutfile = args[4];

		if (outfile.indexOf(File.separator) == -1) {
			outfile = System.getProperty("user.dir") + File.separator + outfile;
		}
		if (reportfile.indexOf(File.separator) == -1) {
			reportfile = System.getProperty("user.dir") + File.separator
					+ reportfile;
		}
		if (badoutfile.indexOf(File.separator) == -1) {
			badoutfile = System.getProperty("user.dir") + File.separator
					+ badoutfile;
		}
		
		if (THIS.resultCollection == null || THIS.resultCollection.isEmpty()) {
			System.out.println("No Skeletons to Write out.");
			System.exit(-1);
		}
		//save lines
		//dp = new DriverProperties(args[2]);
		dp = new DriverProperties(outfile);
		sfw.write(THIS.resultCollection, dp);

		// save report
		FileWriter fw = new FileWriter(reportfile);

		fw.write(THIS.outputReport);
		fw.close();

		//save bad polys (if any)
		dp = new DriverProperties(badoutfile);
		if (THIS.badPolyCollection.size() > 0)
			sfw.write(THIS.badPolyCollection, dp);
		else
			System.out.println("NO BAD POLYS TO WRITE OUT!");

		//		}
		//		catch( Exception e)
		//		{
		//			e.printStackTrace();
		//		}

	}

	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

}