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

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.geom.EnvelopeUtil;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import java.awt.Component;
import javax.swing.AbstractButton;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;

/**
 * Various utility functions related to the JUMP user interface.
 */
public class JUMPUIUtil {
  int ZOOM_STEP = 200;
   
  PlugInContext context;
  JInternalFrame layerFrame;
  
  private static ExtensionFileFilter shapefileFilter = null;
  private static ExtensionFileFilter objectStreamFilter = null;
  private static ExtensionFileFilter csvFilter = null;
  
  /**
   * Creates the JUMPUI Utility
   * @param context The current PlugIn context.
   */
  public JUMPUIUtil(PlugInContext context) {
    this(context, null);	
  }
  
  /**
   * Creates the JUMPUI Utility
   * @param context The current PlugIn context.
   * @param layerFrame The current view panel task frame.
   */
  public JUMPUIUtil(PlugInContext context, JInternalFrame layerFrame) {
    this.context = context;
    this.layerFrame = layerFrame;
  }
  
  /**
   * Activates the {@link SelectFeaturesTool}
   */
  public void activateSelectTool() {
    ((AbstractButton)context.getWorkbenchFrame().getToolBar().getComponentAtIndex(11)).doClick();	
  }
  
  /**
   * Acivates the {@link ZoomTool}
   *
   */
  public void activateZoomTool() {
    ((AbstractButton)context.getWorkbenchFrame().getToolBar().getComponentAtIndex(0)).doClick();
  }
  
  /**
   * Zooms the task view panel out.
   * @param i Zoom factor.
   */
  public void zoomOut(int i) {
    Envelope env = currentEnv();
    zoom(EnvelopeUtil.expand(env, i*ZOOM_STEP));
  }
  
  /**
   * Zooms the task view panel in.
   * @param i Zoom factor.
   */
  public void zoomIn(int i) {
    Envelope env = currentEnv();
    zoom(EnvelopeUtil.expand(env, -1*i*ZOOM_STEP));
  }
  
  /**
   * Zooms the layer view panel to a specific buffered envelope.
   */
  public void zoom(Envelope env, double buffer) {
    zoom(EnvelopeUtil.expand(env, buffer));  
  }
  
  /**
   * Zooms the layer view panel to a specific Envelope.
   */
  public void zoom(Envelope env) {
  	try {
      context.getWorkbenchFrame().activateFrame(layerFrame);
      context.getLayerViewPanel().getViewport().zoom(centeredEnvelope(env));
      context.getLayerViewPanel().getSelectionManager().clear();
    }
    catch(Exception e) {
      showError(e);  	
    }
  }
  
  public void zoom(TaskFrame frame, Envelope env) {
    try {
      context.getWorkbenchFrame().activateFrame(frame);
      frame.getLayerViewPanel().getViewport().zoom(centeredEnvelope(env));
      context.getLayerViewPanel().getSelectionManager().clear();  
    }
    catch(Exception e) {
      showError(e);   
    }
  }
  
  public void zoom(Feature f) {
    zoom(f, 0);  
  }
  
  public void zoom(Feature f, int buffer) {
    zoom(f.getGeometry().getEnvelopeInternal(), buffer);  
  }
  /**
   * Creates a new envelope that is centered in the task view panel.
   * @param e Original envelope
   */
  public Envelope centeredEnvelope(Envelope e) {
  	LayerViewPanel panel = context.getLayerViewPanel().getViewport().getPanel();
		
    double scale = Math.min(panel.getWidth() / e.getWidth(), panel.getHeight() / e.getHeight());
               
    double xmin = (e.getMinX() + e.getMaxX())/2.0 - panel.getWidth()*0.5/scale;
    double xmax = (e.getMinX() + e.getMaxX())/2.0 + panel.getWidth()*0.5/scale;
    double ymin = (e.getMinY() + e.getMaxY())/2.0 - panel.getHeight()*0.5/scale;
    double ymax = (e.getMinY() + e.getMaxY())/2.0 + panel.getHeight()*0.5/scale;
         
    return new Envelope(xmin,xmax,ymin,ymax);
  }	
  
  /**
   * Returns the current envelope of the task view panel.
   */
  public Envelope currentEnv() {
    Envelope env = null;
    
    try {
      context.getWorkbenchFrame().activateFrame(layerFrame);
      env = context.getLayerViewPanel().getViewport().getEnvelopeInModelCoordinates(); 
    }
    catch(Exception e) {
      showError(e);
    }
    
    return(env);
  }
  
  public static void showError(Component parent, String message) {
    JOptionPane.showMessageDialog(parent, message); 
  }
  
  /**
   * Displays an error message dialog with no parent.
   * @param message Error message.
   */
  public static void showError(String message) {
    showError(null, message); 
  }
  
  /**
   * Displays an exception error message dialog.
   * @param parent Parent of the dialog.
   * @param e The exception.
   */
  public static void showError(Component parent, Exception e) {
    showError(parent, e.getMessage());
  }
  
  /**
   * Displays an exception error message dialog with no parent.
   * @param e The exception.
   */
  public static void showError(Exception e) {
    e.printStackTrace();
    showError(null, e.getMessage());  
  }
  /**
   * Returns an ExtensionFileFilter specifically for ESRI shapefiles.
   */
  
  public static ExtensionFileFilter shapefileFilter() {
    if (shapefileFilter == null) {
      shapefileFilter = new ExtensionFileFilter("shp", "ESRI Shapefile (*.shp)");		
    }
    return(shapefileFilter);
  }	
  
  /**
   * Returns an ExtensionFileFilter specifically for Java Object Stream files.
   */
  
  public static ExtensionFileFilter objectStreamFilter() {
    if (objectStreamFilter == null) {
      objectStreamFilter = new ExtensionFileFilter("obj", "Java Object Stream (*.obj)");  	
    }
    return(objectStreamFilter);
  }
  
  /**
   * Returns an ExtensionFileFilter specifically for Comma Seperated Value files.
   */
  
   public static ExtensionFileFilter csvFileFilter() {
    if (csvFilter == null) {
      csvFilter = new ExtensionFileFilter("csv", "Comma Seperated Values (*.csv)");    
    }
    return(csvFilter);
  }
  
  
}