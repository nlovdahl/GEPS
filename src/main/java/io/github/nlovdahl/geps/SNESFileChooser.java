/* This file is part of GEPS. GEPS is a Graphics Editing Program for SNES
homebrew development. Copyright (C) 2020 Nicholas Lovdahl

GEPS is free software: you can redistribute it and / or modify it under the
terms of the GNU General Public License as published by the Free Software
Foundation, either version 3 of the License, or (at your option) any later
version.

GEPS is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with
GEPS. If not, see <https://www.gnu.org/licenses/>. */

package io.github.nlovdahl.geps;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import java.awt.Component;
import java.io.File;

/**
 * A file chooser interface which allows a user to navigate the file system and
 * select either a tileset file or a palette file. This is provided through the
 * invocation of methods to select either type of file, respectively. In any
   * case, it is possible to select from any files to open or save to, and only
   * a single file may be chosen.
 * 
 * @author Nicholas Lovdahl
 * 
 * @see Tileset
 * @see Palette
 */
public final class SNESFileChooser extends JFileChooser {
  /**
   * Creates a file chooser which can be used to pick tileset or palette files
   * to open or save to. The parent of this file chooser is set to be the given
   * component.
   * 
   * @param parent the component that owns the file chooser.
   * @throws NullPointerException if parent is null.
   */
  public SNESFileChooser(Component parent) {
    if (parent == null) {
      throw new NullPointerException(
        "Cannot create SNES File Chooser will a null parent.");
    }  // else, the parent should be valid
    
    parent_ = parent;
    
    setAcceptAllFileFilterUsed(true);
    setMultiSelectionEnabled(false);
    setFileSelectionMode(JFileChooser.FILES_ONLY);
  }
  
  /**
   * Prompts the user to select a tileset file to be opened. The user will be
   * able to see and select files ending with the associated tileset file
   * extension by default, although they may chose any file regardless of its
   * extension. If the selection process is aborted, null is returned.
   * 
   * @return the tileset file chosen, or null if no selection was made.
   */
  public File chooseTilesetToOpen() {
    setFileFilter(TILESET_FILE_FILTER);
    
    return chooseFileToOpen();
  }
  
  /**
   * Prompts the user to select a tileset file to be save to. The user will be
   * able to see and select files ending with the associated tileset file
   * extension by default, although they may chose any file regardless of its
   * extension. If the selection process is aborted, null is returned.
   * 
   * @return the tileset file chosen, or null if no selection was made.
   */
  public File chooseTilesetToSave() {
    setFileFilter(TILESET_FILE_FILTER);
    base_extension = CHR_EXTENSION;
    
    return chooseFileToSave();
  }
  
  /**
   * Prompts the user to select a palette file to be opened. The user will be
   * able to see and select files ending with the associated palette file
   * extension by default, although they may chose any file regardless of its
   * extension. If the selection process is aborted, null is returned.
   * 
   * @return the palette file chosen, or null if no selection was made.
   */
  public File choosePaletteToOpen() {
    setFileFilter(PALETTE_FILE_FILTER);
    
    return chooseFileToOpen();
  }
  
  /**
   * Prompts the user to select a palette file to be save to. The user will be
   * able to see and select files ending with the associated palette file
   * extension by default, although they may chose any file regardless of its
   * extension. If the selection process is aborted, null is returned.
   * 
   * @return the palette file chosen, or null if no selection was made.
   */
  public File choosePaletteToSave() {
    setFileFilter(PALETTE_FILE_FILTER);
    base_extension = PAL_EXTENSION;
    
    return chooseFileToSave();
  }
  
  // launch the prompt to choose a file using whatever filter is already set
  private File chooseFileToOpen() {
    File chosen_file = null;
    
    // ask the user to make a selection
    if (showOpenDialog(parent_) == JFileChooser.APPROVE_OPTION) {
      chosen_file = getSelectedFile();
    }
    
    return chosen_file;
  }
  
  // launch the prompt to choose a file using whatever filter is already set
  private File chooseFileToSave() {
    File chosen_file = null;
    
    // ask the user to make a selection
    if (showSaveDialog(parent_) == JFileChooser.APPROVE_OPTION) {
      chosen_file = getSelectedFile();
      String chosen_file_name = chosen_file.getName();
      int chosen_file_extension_index = chosen_file_name.lastIndexOf(".");
      
      // if the file has no extension, add the current base extension
      if (chosen_file_extension_index == -1) {
        chosen_file = new File(chosen_file + base_extension);
      } else if (chosen_file_extension_index == chosen_file_name.length() - 1) {
        // if the file ends with a dot, add the extension (minus the dot)
        chosen_file = new File(chosen_file + base_extension.substring(1));
      }
    }
    
    return chosen_file;
  }
  
  private String base_extension;
  
  private final Component parent_;
  
  /** The file extension for tileset files. */
  private static final String CHR_EXTENSION = ".chr";
  /** The file extension for palette files. */
  private static final String PAL_EXTENSION = ".pal";
  
  // allow only files with an extension associated with tilesets
  private static final FileFilter TILESET_FILE_FILTER = new FileFilter() {
    @Override
    public boolean accept(File file) {
      return file.isDirectory() ||  // accept directories to allow navigation
             file.getName().toLowerCase().endsWith(CHR_EXTENSION);
    }
    @Override
    public String getDescription() {
      return "Tileset Files (*" + CHR_EXTENSION + ")";
    }
  };
  // allow only files with an extension associated with palettes
  private static final FileFilter PALETTE_FILE_FILTER = new FileFilter() {
    @Override
    public boolean accept(File file) {
      return file.isDirectory() ||  // accept directories to allow navigation
             file.getName().toLowerCase().endsWith(PAL_EXTENSION);
    }
    @Override
    public String getDescription() {
      return "Palette Files (*" + PAL_EXTENSION + ")";
    }
  };
}
