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
 * Provides a file chooser interface which allows a user to navigate the file
 * system and select either tileset or palette files. When making selections,
 * only a single file can be selected; the user may use file filters for only
 * tileset or palette files, or may make a selection from all files. The file
 * chooser keeps track of the last tileset and palette files selected by the
 * user as referenced files.
 * 
 * @author Nicholas Lovdahl
 */
public final class SNESFileChooser extends JFileChooser {
  /**
   * Creates a file chooser which can be used to pick tileset or palette files
   * to open or save to. The new file chooser begins without any referenced
   * tileset or palette files.
   * 
   * @param parent the component that owns the file chooser.
   */
  public SNESFileChooser(Component parent) {
    parent_ = parent;
    
    referenced_tileset_file_ = null;  // no referenced files to begin with
    referenced_palette_file_ = null;
    
    setAcceptAllFileFilterUsed(true);
    setMultiSelectionEnabled(false);
    setFileSelectionMode(JFileChooser.FILES_ONLY);
  }
  
  /**
   * Gets the currently referenced tileset file. If there is no currently
   * referenced tileset file, then null is returned instead.
   * 
   * @return the currently referenced tileset file, or null is there is not one.
   */
  public File getReferencedTilesetFile() { return referenced_tileset_file_; }
  
  /**
   * Returns the name of the currently referenced tileset file, or
   * {@link #DEFAULT_TILESET_FILE_NAME} if there is no currently referenced
   * tileset file.
   * 
   * @return the path for the current tileset file.
   */
  public String getReferencedTilesetFileShortName() {
    if (referenced_tileset_file_ != null) {
      return referenced_tileset_file_.getName();
    } else {
      return DEFAULT_TILESET_FILE_NAME;
    }
  }
  
  /**
   * Returns the absolute path of the currently referenced tileset file, or
   * {@link #DEFAULT_TILESET_FILE_NAME} if there is no currently referenced
   * tileset file.
   * 
   * @return the path for the current tileset file.
   */
  public String getReferencedTilesetFileLongName() {
    if (referenced_tileset_file_ != null) {
      return referenced_tileset_file_.getAbsolutePath();
    } else {
      return DEFAULT_TILESET_FILE_NAME;
    }
  }
  
  /**
   * Resets the tileset file currently referenced; after calling this method,
   * there will be no referenced tileset file.
   */
  public void resetReferencedTilesetFile() { referenced_tileset_file_ = null; }
  
  /**
   * Gets the currently referenced palette file. If there is no currently
   * referenced palette file, then null is returned instead.
   * 
   * @return the currently referenced palette file, or null is there is not one.
   */
  public File getReferencedPaletteFile() { return referenced_palette_file_; }
  
  /**
   * Returns the name of the currently referenced palette file, or
   * {@link #DEFAULT_PALETTE_FILE_NAME} if there is no currently referenced
   * palette file.
   * 
   * @return the path for the current palette file.
   */
  public String getReferencedPaletteFileShortName() {
    if (referenced_palette_file_ != null) {
      return referenced_palette_file_.getName();
    } else {
      return DEFAULT_PALETTE_FILE_NAME;
    }
  }
  
  /**
   * Returns the absolute path of the currently referenced palette file, or
   * {@link #DEFAULT_PALETTE_FILE_NAME} if there is no currently referenced
   * palette file.
   * 
   * @return the path for the current palette file.
   */
  public String getReferencedPaletteFileLongName() {
    if (referenced_palette_file_ != null) {
      return referenced_palette_file_.getAbsolutePath();
    } else {
      return DEFAULT_PALETTE_FILE_NAME;
    }
  }
  
  /**
   * Resets the palette file currently referenced; after calling this method,
   * there will be no referenced palette file.
   */
  public void resetReferencedPaletteFile() { referenced_palette_file_ = null; }
  
  /**
   * Prompts the user to select a tileset file through a file chooser using the
   * dialog for opening a file and returns the file selected. If the user aborts
   * the selection process, the referenced file is not changed and null is
   * returned instead. This method only changes the currently referenced tileset
   * file. It does not read or write to the chosen file.
   * 
   * @return true if the user makes a selection, false if the selection process
   *         was aborted.
   */
  public File selectTilesetFileToOpen() {
    resetChoosableFileFilters();
    setFileFilter(TILESET_FILE_FILTER);
    base_extension_ = CHR_EXTENSION;
    File chosen_file = chooseFileToOpen();
    
    // if the user made a selection (not null)
    if (chosen_file != null) {
      referenced_tileset_file_ = chosen_file;
    }
    
    return chosen_file;
  }
  
  /**
   * Prompts the user to select a tileset file through a file chooser using the
   * dialog for saving a file and returns the file selected. If the user aborts
   * the selection process, the referenced file is not changed and null is
   * returned instead. This method only changes the currently referenced tileset
   * file. It does not read or write to the chosen file.
   * 
   * @return the file selected by the user, or null if the user aborted the
   *         selection process.
   */
  public File selectTilesetFileToSave() {
    resetChoosableFileFilters();
    setFileFilter(TILESET_FILE_FILTER);
    base_extension_ = CHR_EXTENSION;
    File chosen_file = chooseFileToSave();
    
    // if the user made a selection (not null)
    if (chosen_file != null) {
      referenced_tileset_file_ = chosen_file;
    }
    
    return chosen_file;
  }
  
  /**
   * Prompts the user to select a palette file through a file chooser using the
   * dialog for opening a file and returns the file selected. If the user aborts
   * the selection process, the referenced file is not changed and null is
   * returned instead. This method only changes the currently referenced palette
   * file. It does not read or write to the chosen file.
   * 
   * @return the file selected by the user, or null if the user aborted the
   *         selection process.
   */
  public File selectPaletteFileToOpen() {
    resetChoosableFileFilters();
    setFileFilter(PALETTE_FILE_FILTER);
    base_extension_ = PAL_EXTENSION;
    File chosen_file = chooseFileToOpen();
    
    // if the user made a selection (not null)
    if (chosen_file != null) {
      referenced_palette_file_ = chosen_file;
    }
    
    return chosen_file;
  }
  
  /**
   * Prompts the user to select a palette file through a file chooser using the
   * dialog for saving a file and returns the file selected. If the user aborts
   * the selection process, the referenced file is not changed and null is
   * returned instead. This method only changes the currently referenced palette
   * file. It does not read or write to the chosen file.
   * 
   * @return the file selected by the user, or null if the user aborted the
   *         selection process.
   */
  public File selectPaletteFileToSave() {
    resetChoosableFileFilters();
    setFileFilter(PALETTE_FILE_FILTER);
    base_extension_ = PAL_EXTENSION;
    File chosen_file = chooseFileToSave();
    
    // if the user made a selection (not null)
    if (chosen_file != null) {
      referenced_palette_file_ = chosen_file;
    }
    
    return chosen_file;
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
        chosen_file = new File(chosen_file + base_extension_);
      } else if (chosen_file_extension_index == chosen_file_name.length() - 1) {
        // if the file ends with a dot, add the extension (minus the dot)
        chosen_file = new File(chosen_file + base_extension_.substring(1));
      }
    }
    
    return chosen_file;
  }
  
  /** The name given for a tileset without a referenced file. */
  public static final String DEFAULT_TILESET_FILE_NAME = "[Untitled Tileset]";
  /** The name given for a palette without a referenced file. */
  public static final String DEFAULT_PALETTE_FILE_NAME = "[Untitled Palette]";
  /** The file extension for tileset files. */
  public static final String CHR_EXTENSION = ".chr";
  /** The file extension for palette files. */
  public static final String PAL_EXTENSION = ".pal";
  
  private String base_extension_;
  private File referenced_tileset_file_;
  private File referenced_palette_file_;
  
  private final Component parent_;
  
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
