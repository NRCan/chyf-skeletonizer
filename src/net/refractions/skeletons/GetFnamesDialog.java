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

import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;


public class GetFnamesDialog extends javax.swing.JDialog 
{
	  boolean cancelled = true;
	  JFileChooser shapeChooser = null;
	  JFileChooser textChooser = null;
	  
	  private File polyShape = null;
	  private File pointShape = null;
	  
	  private File lineOutShape = null;
	  private File txtOut       = null;
	  

	  
	  
	  
			JPanel mainPanel                = new JPanel();
			JPanel cancelAcceptPanel = new JPanel();
			JButton browse_pointInputButton = new JButton("Browse...");
			JTextField browse_pointInputLabel   = new JTextField("");
			JLabel jlabel1   = new JLabel("Input Point Shapefile" );
		
			JButton browse_polyInputButton  = new JButton("Browse...");
			JTextField browse_polyInputLabel    = new JTextField("");
			JLabel jlabel2   = new JLabel("Input Polygon Shapefile");
		
			
			JButton browse_lineOutputButton = new JButton("Browse...");
			JTextField browse_lineOutputLabel   = new JTextField("");
			JLabel jlabel3   = new JLabel("Output Line Shapefile");
			
			JButton browse_textOutputButton = new JButton("Browse...");
			JTextField browse_textOutputLabel   = new JTextField("");	
			JLabel jlabel4   = new JLabel("Output report file");	
		
			JLabel divider = new JLabel("");
			JLabel divider2 = new JLabel("");
			JLabel divider3 = new JLabel("");
			JLabel divider4 = new JLabel("");
			JLabel divider5 = new JLabel("");
		
			JButton accept = new JButton("Skeletonize");
			JButton cancel = new JButton("Cancel");
			
			
    
	  /** Creates new form form1 */
	  public GetFnamesDialog(java.awt.Frame parent, boolean modal) {
		 super(parent, modal);
		 shapeChooser = new JFileChooser();
		 SimpleFileFilter filter = new SimpleFileFilter(
				   new String("shp"), "ESRI Shapefiles");
		 shapeChooser.addChoosableFileFilter(filter);
		 textChooser = new JFileChooser();
		 initComponents();
	  }
	  
public void getShapePoly()
{
		int returnVal = shapeChooser.showOpenDialog(this);
	    if(returnVal == JFileChooser.APPROVE_OPTION) 
	    {
			File f = shapeChooser.getSelectedFile();
			 try 
			 {
				browse_polyInputLabel.setText( f.getCanonicalPath() );
				polyShape = f;
			 }
			 catch (Exception e)
			 {
			 }
	   }
	    
}

public void getShapePoint()
{
		int returnVal = shapeChooser.showOpenDialog(this);
	    if(returnVal == JFileChooser.APPROVE_OPTION) 
	    {
			File f = shapeChooser.getSelectedFile();
			 try 
			 {
				browse_pointInputLabel.setText( f.getCanonicalPath() );
				pointShape = f;
			 }
			 catch (Exception e)
			 {
			 }
	   }
	    
}

public void getShapeLine()
{
		int returnVal = shapeChooser.showOpenDialog(this);
	    if(returnVal == JFileChooser.APPROVE_OPTION) 
	    {
			try 
			{
					File f = shapeChooser.getSelectedFile();
			
						String ex = getExtension(f);
						if (ex == null)
						{
							f = new File( f.getCanonicalPath() + ".shp");
							ex = "shp";
						}
					
						if (!(ex.equalsIgnoreCase("shp")))
						{
							f = new File( f.getCanonicalPath() + ".shp");
						}
		 
						 if (f.exists())
						 {
							 if (!(f.canWrite()))
							 {
								JOptionPane.showMessageDialog(null,"cannot write to file");
								browse_lineOutputLabel.setText( "" );
								lineOutShape = null;
								
							 }
 
							Object[] options = {"Overwrite File",
										    "Cancel"  };
							int response = JOptionPane.showOptionDialog(null,
							    "File Exists - Overwrite?",
							    "File Exists",
							    JOptionPane.YES_NO_OPTION,
							    JOptionPane.QUESTION_MESSAGE,
							    null,
							    options,
							    options[1]);

						  if ( (response == JOptionPane.NO_OPTION) || (response == JOptionPane.CANCEL_OPTION) )
						  {
								browse_lineOutputLabel.setText( "" );
								lineOutShape = null;
								
						  }
				 }
				 

				browse_lineOutputLabel.setText( f.getCanonicalPath() );
				lineOutShape = f;
			 }
			 catch (Exception e)
			 {
				browse_lineOutputLabel.setText( "" );
				lineOutShape = null;
			 }
	   }
	   
}

public void getTextfile()
{
		int returnVal = textChooser.showOpenDialog(this);
	    if(returnVal == JFileChooser.APPROVE_OPTION) 
	    {
			File f = textChooser.getSelectedFile();
			 try 
			 {
					 if (f.exists())
					 {
						 if (!(f.canWrite()))
						 {
							JOptionPane.showMessageDialog(null,"cannot write to file");
							browse_lineOutputLabel.setText( "" );
												lineOutShape = null;
												
											 }
 
											Object[] options = {"Overwrite File",
									    "Cancel"  };
						int response = JOptionPane.showOptionDialog(null,
						    "File Exists - Overwrite?",
						    "File Exists",
						    JOptionPane.YES_NO_OPTION,
						    JOptionPane.QUESTION_MESSAGE,
						    null,
						    options,
						    options[1]);

					  if ( (response == JOptionPane.NO_OPTION) || (response == JOptionPane.CANCEL_OPTION) )
					  {
							browse_textOutputLabel.setText("" );
							txtOut = null;
							
					  }
			 		}
								 
								 
				browse_textOutputLabel.setText( f.getCanonicalPath() );
				txtOut = f;
			 }
			 catch (Exception e)
			 {
				browse_textOutputLabel.setText("" );
				txtOut = null;
			 }
	   }
	    
}

public String getPolygonFilename() throws IOException{
	String s = browse_polyInputLabel.getText();
	if (s == null || s.equals("")) return(null);
	return(s);
}

public String getPointFilename() throws IOException {
	String s = browse_pointInputLabel.getText();
	if (s == null || s.equals("")) return(null);
	return(s);
}

public String getLineFilename() throws IOException {
	String s = browse_lineOutputLabel.getText();
	if (s == null || s.equals("")) return(null);
	return(s);
}

public String getReportFilename() throws IOException {
	String s = browse_textOutputLabel.getText();
	if (s == null || s.equals("")) return(null);
	return(s);
	
}

	  
	  
	private void initComponents() 
	{
		
		browse_polyInputButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				getShapePoly();
			 }
			 });
			 
		browse_pointInputButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						getShapePoint();
					 }
					 });
		
		browse_lineOutputButton.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent evt) {
			getShapeLine();
		 }
		 });
		 
		browse_textOutputButton.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent evt) {
			getTextfile();
		 }
		 });
		 
		 
		 accept.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent evt) {
			ButtonContinueActionPerformed(null);
		 }
		 });
		 
		 cancel.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent evt) {
			ButtonCancelActionPerformed(null);
		 }
		 });
		
		
		jlabel1.setMaximumSize(new java.awt.Dimension(400, 400));
		jlabel1.setMinimumSize(new java.awt.Dimension(4, 40));
			  
		

		
		mainPanel.setLayout(new java.awt.GridLayout( 0,3));
		cancelAcceptPanel.setLayout(new javax.swing.BoxLayout(cancelAcceptPanel, javax.swing.BoxLayout.X_AXIS));
		
		cancelAcceptPanel.add(accept);
		cancelAcceptPanel.add(cancel);
		
		
		mainPanel.add(jlabel1);
		mainPanel.add(browse_pointInputLabel);
		mainPanel.add(browse_pointInputButton);
		mainPanel.add(jlabel2);
		mainPanel.add(browse_polyInputLabel);
		mainPanel.add(browse_polyInputButton);
		
	    mainPanel.add(divider3);
	    mainPanel.add(divider4);
	    mainPanel.add(divider5);
		
		mainPanel.add(jlabel3);
		mainPanel.add(browse_lineOutputLabel);
		mainPanel.add(browse_lineOutputButton);
		mainPanel.add(jlabel4);
		mainPanel.add(browse_textOutputLabel);
		mainPanel.add(browse_textOutputButton);
		
		mainPanel.add(divider);
		mainPanel.add(divider2);
		//mainPanel.add(divider3);
		
		mainPanel.add(cancelAcceptPanel);
		
		
		getContentPane().add(mainPanel, java.awt.BorderLayout.CENTER);
		pack();
	}
	
			/**
		    * Return the extension portion of the file's name .
		    *
		    * @param f filename to look at
		    */
		    public String getExtension(File f) {
		    if(f != null) {
			   String filename = f.getName();
			   int i = filename.lastIndexOf('.');
			   if(i>0 && i<filename.length()-1) {
			    return filename.substring(i+1).toLowerCase();
			   };
		    }
		    return null;
		   }
		   
	/**
		 * close the form but set the "dont go any further" flag
		 * @param evt
		 */
	    private void ButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {
		  // System.out.println("cancel");
		   cancelled = true;
		   setVisible(false);
		   dispose();
       
	    }

   

	    /** Closes the dialog */
	    private void closeDialog(java.awt.event.WindowEvent evt) {
		   setVisible(false);
		   dispose();
	    }
    
    
		/**
		 * show the dialog
		 * @return boolean false if user hit the cancel button
		 */
	    public boolean doDialog()
	    {
			cancelled = true;
			show();
			return (!(cancelled));
	    }
	    
	/**
	 * close the form and record the users input
	 * @param evt ignored
	 */
    private void ButtonContinueActionPerformed(java.awt.event.ActionEvent evt) {
       
	  // System.out.println("continue");
	   cancelled = false;
	   setVisible(false);
	   dispose();

    }
    
	    
	  
}
