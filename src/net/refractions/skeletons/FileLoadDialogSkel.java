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


import com.vividsolutions.jump.task.*;
import java.io.*;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

/**
 * A generic dialog used to load files.
 */
public class FileLoadDialogSkel extends JDialog {
  public static String lastDir = null; //remember the last directory the 
                                //  last file was loaded from
  private static final String DOUBLE = "DOUBLE";
  private static final String FILE = "FILE";
  JFrame mainFrame;
  JPanel mainPanel, topPanel, bottomPanel;
  
  List fieldList;
  Map fields;
  JLabel topoLbl, lake20kLbl, lake50kLbl;
  JTextField topoFileFld, lake20kFileFld, lake50kFileFld;
  JButton topoFileLoadBtn, lake20kFileLoadBtn, lake50kFileLoadBtn, okBtn, cancelBtn;
  
  TaskMonitor monitor;
  
  private boolean cancelled; //true if cancel was pressed
  
  
  /**
   * Creates a new FileLoadDialog.
   * @param owner Owner frame of the dialog.
   * @param title Title of the dialog.
   */
  public FileLoadDialogSkel(Frame owner, String title) {
    super(owner, true);
    
    this.setTitle(title);
    cancelled = false;
    fieldList = new ArrayList();
    fields = new HashMap();
  }

  /**
   * Shows the dialog. True is returned if the user pushed Ok, false
   * if the user presses cancel.
   * <br>
   * <br>
   * Note: The FileLoagDialog is modal so this method blocks the current 
   * thread of execution.
   */
  public boolean openDialog() {
    this.show();
    return(!(cancelled));
  }
  
  /**
   * Adds a field to the FileLoadDialog.
   * @param name Name of the field to which it is titled, and later 
   * referenced in a call to getFilename(). 
   * @param manditory True if it is manditory that the file be 
   * specified, otherwise false.
   * @param defPath The default file path to be placed in the text 
   * box for the file. 
   * @param currDir The current directory to start the FileChooser
   *  when the user clicks the Browse button.
   * @param eff {@link ExtensionFileFilter} to be used for the FileChooser
   *  when the user clicks the Browse button.
   */
  public void addField(String name, boolean manditory, String defPath, String currDir, ExtensionFileFilter eff) {
    Object[] components = new Object[8];
    
    JLabel l = new JLabel(name);
    if (manditory) {
      l.setForeground(new Color(255,0,0));  
    }
    components[0] = l;
    components[1] = new String(FILE);
    components[2] = new JTextField(40);
    components[3] = new Boolean(manditory);
	components[4] = new JButton("Browse");
    components[5] = defPath;
    components[6] = currDir;
    components[7] = eff;
    
    fields.put(name, components);
    fieldList.add(name);
  }
  
  /**
	 * Adds a field to the FileLoadDialog.
	 * @param name Name of the field to which it is titled, and later 
	 * referenced in a call to getFilename(). 
	 * @param manditory True if it is manditory that the value be 
	 * specified, otherwise false.
	 * @param value The default file path to be placed in the text 
	 * box for the file. 
	 */
  public void addFieldDouble(String name, boolean manditory, double value) {
	  Object[] components = new Object[5];
    
	  JLabel l = new JLabel(name);
	  if (manditory) {
		l.setForeground(new Color(255,0,0));  
	  }
	  components[0] = l;
	  components[1] = new String(DOUBLE);
	  components[2] = new JTextField(40);
	  components[3] = new Boolean(manditory);
	  components[4] = new Double(value);
    
	  fields.put(name, components);
	  fieldList.add(name);
	}
  
  /**
   * Builds the FileLoadDialog.
   * <br>
   * <br>
   * Note: This method must be called after all calls to addField(). 
   */
  public void build() {
    int n = fields.size();
    Box b;
    
    mainPanel = new JPanel();
	topPanel = new JPanel();
	bottomPanel = new JPanel();
    
    topPanel.setLayout(new GridLayout(n, 3, 5, 5));
    
    Set s = fields.keySet();
    for (int i = 0; i < fieldList.size(); i++) 
    {
	  String key = (String)fieldList.get(i);
      Object[] components = (Object[])fields.get(key);
     
      b = Box.createHorizontalBox();
	  b.add(Box.createHorizontalGlue());
   	  b.add((JComponent)components[0]);  //label for the text field
      topPanel.add(b);
	  topPanel.add((JComponent)components[2]);  //text field describing what is being entered in
	  b = Box.createHorizontalBox();
	
	//TODO: this is a quick hack, smooth me out later. But all I need from it is to work for now
	  if (components[1].equals(DOUBLE))
	  {
		topPanel.add(b);
	  }
	  else	// if FILE
	  {
		b.add((JComponent)components[4]);  //browse button
		b.add(Box.createHorizontalGlue());
		topPanel.add(b);
		//		default path to be placed in text field
		if (components[5] != null) ((JTextField)components[2]).setText((String)components[5]);	
  
		//set up file chooser
		final JTextField txtFld = (JTextField)components[2];
		final String currDir = (components[6] != null) ? (String)components[6] : System.getProperty("user.dir");
		final ExtensionFileFilter eff = (ExtensionFileFilter)components[7];
		((JButton)components[4]).addActionListener(new ActionListener() 
			{
				public void actionPerformed(ActionEvent ae) {
				  fileLoad(txtFld, eff, currDir);	
				}
			} ); 
	  }// end else
	 
	      
	}// end for loop
    
    okBtn = new JButton("Ok");
	cancelBtn = new JButton("Cancel");
	
    b = Box.createHorizontalBox();
    b.add(Box.createHorizontalGlue());
    b.add(okBtn);
    b.add(Box.createHorizontalGlue());
    b.add(cancelBtn);
    b.add(Box.createHorizontalGlue());
    
    bottomPanel.add(b);
    
    b = Box.createVerticalBox();
    b.add(topPanel);
    b.add(bottomPanel);
    
    mainPanel.add(b);
    
    this.addWindowListener( 
      new WindowAdapter() {
        public void windowClosing(WindowEvent we) {
          closeDialog(true);	
        }	
      }
    );
    
    okBtn.addActionListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent av) {
          ok();	
        }
      }
    );
    
    cancelBtn.addActionListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent av) {
          cancel();	
        }
      } 
    );
    
    this.getContentPane().add(mainPanel);  
    this.pack();
    this.setSize(650, 40 + 28*(n+1));
  }
  
  /**
   * Returns the path to the file loaded for a field added by addField()
   * @param key The name of the field added with a call to addField().
   */
  public String getFilename(String key) {
    Object[] o = (Object[])fields.get(key);
    if (o == null) return(null);
    JTextField fld = (JTextField)o[2];
    if (fld.getText() == null && fld.getText().equals("")) return(null);
    return(fld.getText());
  }
  
  /*
   * Creates a FileChooser which provided the standard point and click view of the 
   * file system.
   */
  private void fileLoad(JTextField fld, ExtensionFileFilter eff, String currDir) {
  	JFileChooser chooser = new JFileChooser();
  	if (eff != null) chooser.setFileFilter(eff);
  	if (currDir != null) chooser.setCurrentDirectory(new File(currDir));
    if (FileLoadDialogSkel.lastDir != null) chooser.setCurrentDirectory(new File(FileLoadDialogSkel.lastDir));	
   
    int rv = chooser.showOpenDialog(this);
    if (rv == JFileChooser.APPROVE_OPTION) {
      File f = chooser.getSelectedFile();
      try {
        fld.setText(f.getCanonicalPath());  	
        lastDir = f.getParent();
      }
      catch(Exception e) {
        e.printStackTrace();
        System.out.println(e.getMessage());
      }
    }
  }
  
  /*
   * Closes the dialog.
   */
  private void closeDialog(boolean cancel) {
    this.cancelled = cancel;
    this.setVisible(false);
    this.dispose();  
  }
  
  /*
   * Verifies each file loaded. Checking that manditory fields have been specified and 
   * files specified actually exist.
   */
  private void ok() {
    int n = fields.size();
    
    for (Iterator itr = fields.keySet().iterator(); itr.hasNext(); ) {
      String key = (String)itr.next();
      Object[] o = (Object[])fields.get(key);
      String filename = ((JTextField)o[2]).getText();
      String type = (String)o[1];
      if (((Boolean)o[3]).booleanValue() && (filename == null || filename.equals(""))) { 
        JUMPUIUtil.showError(this, key + " must be entered.");
        return; 	
      }
      if (filename != null && !(filename.equals("")) && !((new File(filename)).exists()) && type.equals(FILE)) {
        JUMPUIUtil.showError(this, filename + " does not exist");	
        return;
      }
    }
    
    closeDialog(false);
  }
  
  /*
   * Cancels the file load.
   */
  private void cancel() {
    closeDialog(true);
  }
}

