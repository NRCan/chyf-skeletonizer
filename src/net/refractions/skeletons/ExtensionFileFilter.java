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

import java.io.*;
import javax.swing.filechooser.FileFilter;

/**
 * A simple File Filter which filters out files based on the file
 * extension.
 */
public class ExtensionFileFilter extends FileFilter {
  String extension, desc;
  
  /**
   * Creates a new ExtensionFileFilter
   * @param ext The extension (without dot) of the new filters.
   * @param desc Description of the file type.
   */
  public ExtensionFileFilter(String ext, String desc) {
    this.extension = ext;
    this.desc = desc;
  }
  
  /**
   * Determines if the file is accpeted by striping off the extension.
   */
  public boolean accept(File f) {
  	if (f.isDirectory()) return true;
    
    String ext = getExtension(f);
    return(ext != null && ext.equals(extension));
  }
  
  /**
   * Returns the description of the file type filtered.
   */
  public String getDescription() {
    return(desc);	
  }
  
  /**
   * Returns the extension of the file type filtered.
   */
  public String getExtension() {
  	return(extension);
  }
  
  /**
   * Strips of the exension of a file.
   */
  private String getExtension(File f) {
    String s = f.getName();
    int i = s.lastIndexOf(".");
    String ext = null;
    
    if (i > 0 && i < s.length()) ext = s.substring(i+1).toLowerCase();
  	
  	return(ext);
  }
}