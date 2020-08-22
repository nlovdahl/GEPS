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

import java.awt.Color;
import java.util.Deque;
import java.util.LinkedList;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * The controller for the palette. The controller handles making changes to the
 * palette's data model. The controller also has methods for reading and writing
 * palettes to files. The palette controller also adds the notion of a
 * subpalette, a subset of the palette covering a number of palette entries
 * based on the number of bits per pixel being used, as well as a selected color
 * from this subpalette.
 * 
 * @author Nicholas Lovdahl
 * 
 * @see Palette
 * @see PaletteInterpreter
 */
public final class PaletteController {
  /**
   * Creates a palette controller using the given number of bits per pixel. The
   * initially selected color from the subpalette will be the first color in the
   * palette, and the subpalette will be such that it includes the initially
   * selected color. If {@link #resetPalette()} is called, then the selected
   * color and subpalette will be set as previously described.
   * 
   * @param bpp the number of bits per pixel to be used. This should be between
   *        {@link Tileset#MIN_BPP} and {@link Tileset#MAX_BPP}.
   * @throws IllegalArgumentException if bpp is invalid.
   */
  public PaletteController(int bpp) {
    if (bpp < Tileset.MIN_BPP) {
      throw new IllegalArgumentException(
        "Cannot set bpp to value less than " +
        Integer.toString(Tileset.MIN_BPP) + ".");
    } else if (bpp > Tileset.MAX_BPP) {
      throw new IllegalArgumentException(
        "Cannot set bpp to value more than " +
        Integer.toString(Tileset.MAX_BPP) + ".");
    }  // else, the bpp should be valid
    
    bpp_ = bpp;
    subpalette_start_index_ = 0;
    selected_color_index_ = 0;
    
    unsaved_changes_ = false;
    current_palette_ = new Palette();
    undo_states_ = new LinkedList<>();
    redo_states_ = new LinkedList<>();
  }
  
  /**
   * Gets the color in the specified index in the palette of the palette
   * controller. This method is a wrapper for the {@link Palette#getColor(int)}
   * method.
   * 
   * @param index the index of the color in the palette to return. 
   * @return the color in the specified index in the palette.
   */
  public Color getColor(int index) { return current_palette_.getColor(index); }
  
  /**
   * Gets the color in the specified index in the current subpalette of the
   * palette controller. This method is essentially a wrapper for the
   * {@link Palette#getColor(int)} method, except that it uses an index relative
   * to the current subpalette.
   * 
   * @param index the index of the color in the subpalette to return.
   * @return the color in the specified index in the subpalette.
   */
  public Color getSubpaletteColor(int index) {
    return current_palette_.getColor(getSubpaletteStartIndex() + index);
  }
  
  /**
   * Gets the currently selected color from the subpalette. This method is
   * essentially a wrapper for the {@link Palette#getColor(int)} method, except
   * that it automatically uses the index of the currently selected color.
   * 
   * @return the currently selected color in the subpalette.
   */
  public Color getSelectedColor() {
    return current_palette_.getColor(selected_color_index_);
  }
  
  /**
   * Sets the color for a specified index in the palette based on the given RGB
   * components. This method is essentially a wrapper for the
   * {@link Palette#setColor(int, int, int, int)} method. If setting the color
   * would change the palette, then the controller will save the previous state
   * of the palette for a possible undo and record that there are now unsaved
   * changes.
   * 
   * @param index the index in the palette of the color to set.
   * @param r the value for the red component of the color.
   * @param g the value for the green component of the color.
   * @param b the value for the blue component of the color.
   */
  public void setColor(int index, int r, int g, int b) {
    Color existing_color = Palette.clampColor(current_palette_.getColor(index));
    if (existing_color.getRed() != r || existing_color.getGreen() != g ||
        existing_color.getBlue() != b) {
      saveForUndo();
      redo_states_.clear();
      unsaved_changes_ = true;
      
      current_palette_.setColor(index, r, g, b);  // clamped in Palette
    }
  }
  
  /**
   * Sets the color for a specified index in the palette based on the given
   * color. This method is a wrapper for the
   * {@link Palette#setColor(int, java.awt.Color)} method. If setting the color
   * would change the palette, then the controller will save the previous state
   * of the palette for a possible undo and record that there are now unsaved
   * changes.
   * 
   * @param index the index in the palette of the color to set.
   * @param color the color whose values will be used to set the color entry in
   *              the palette.
   */
  public void setColor(int index, Color color) {
    if (Palette.clampColor(current_palette_.getColor(index)) != color) {
      saveForUndo();
      redo_states_.clear();
      unsaved_changes_ = true;
      
      current_palette_.setColor(index, color);  // should be clamped in Palette
    }
  }
  
  /**
   * Gets the number of bits per pixel being used.
   * 
   * @return the number of bits per pixel.
   */
  public int getBPP() { return bpp_; }
  
  /**
   * Sets the palette controller to use the specified number of bits per pixel.
   * This is used in determining the subpalette (how many color are in it).
   * This is limited between {@link #MIN_BPP} and {@link #MAX_BPP}.
   * This will also update the starting index of the current subpalette of
   * colors in the palette, since this may change.
   * 
   * @param bpp the number of bits per pixel to be set to.
   * @throws IllegalArgumentException if bpp is less than {@link #MIN_BPP} or
   *         more than {@link #MAX_BPP}.
   */
  public void setBPP(int bpp) {
    if (bpp < Tileset.MIN_BPP) {
      throw new IllegalArgumentException(
        "Cannot set bpp to value less than " +
        Integer.toString(Tileset.MIN_BPP) + ".");
    } else if (bpp > Tileset.MAX_BPP) {
      throw new IllegalArgumentException(
        "Cannot set bpp to value more than " +
        Integer.toString(Tileset.MAX_BPP) + ".");
    }  // else, we have a legal bpp value
    
    bpp_ = bpp;
    // changing the bpp could change where the subpalette and selected color are
    setSubpalette(selected_color_index_);
    setSelectedColor(selected_color_index_);
  }
  
  /**
   * Returns the index marking the start of the current subpalette. This will be
   * the index of the first color in the subpalette, not the index before the
   * subpalette.
   * 
   * @return the index marking the start of the subpalette.
   */
  public int getSubpaletteStartIndex() { return subpalette_start_index_; }
  
  /**
   * Returns the index marking the end of the current subpalette. This will be
   * the index of the last color in the subpalette, not the index after the
   * subpalette.
   * 
   * @return the index marking the end of the subpalette.
   */
  public int getSubpaletteEndIndex() {
    return subpalette_start_index_ + (1 << bpp_) - 1;
  }
  
  /**
   * Sets the subpalette so that it will include the given index. This does not
   * necessarily set the subpalette start index to the given index; the size and
   * starting index of the subpalette is also decided by the number of bits per
   * pixel being used.
   * 
   * @param index the index which should be included by the subpalette.
   * @throws IllegalArgumentException if index is less than 0 or more than
   *         {@link Palette#PALETTE_MAX_SIZE}.
   */
  public void setSubpalette(int index) {
    if (index < 0) {
      throw new IllegalArgumentException(
        "Cannot set subpalette to include index less than 0.");
    } else if (index >= Palette.PALETTE_MAX_SIZE) {
      throw new IllegalArgumentException(
        "Cannot set subpalette to include index greater than or equal to " +
        Palette.PALETTE_MAX_SIZE + ".");
    }  // else, we have a legal index
    
    subpalette_start_index_ = index - (index % (1 << bpp_));
  }
  
  /**
   * Resets the current palette, and the currently selected color and subpalette
   * so that they are the same as when the palette controller was first created
   * - this will also remove any states for possible undos and redos, and record
   * that there are now no unsaved changes. The subpalette and selected color
   * from the palette will also be reset such that the selected color is the
   * first color in the reset palette. The number of bits per pixel being used
   * will not be changed.
   */
  public void resetPalette() {
    subpalette_start_index_ = 0;
    selected_color_index_ = 0;
    undo_states_.clear();
    redo_states_.clear();
    unsaved_changes_ = false;
    current_palette_ = new Palette();
  }
  
  /**
   * Reads the given file to interpret its data as a new palette for the
   * controller and returns the number of bytes that were not loaded. This
   * method will also clear all saved undo and redo states and record that there
   * are no unsaved changes. If there is a problem reading the file, then the
   * palette will not be altered, the undo and redo states will remain, and the
   * status of unsaved changed will be unchanged.
   * 
   * @param file the file to load the palette from.
   * @return the number of bytes from the file that were not loaded.
   * @throws FileNotFoundException if file cannot be found and or accessed.
   * @throws IOException if there is an IO problem reading from the file.
   */
  public long loadPalette(File file) throws FileNotFoundException, IOException {
    long file_size, bytes_loaded;
    
    try (FileInputStream input_stream = new FileInputStream(file)) {
      file_size = file.length();
      
      int max_bytes = 2 * Palette.PALETTE_MAX_SIZE;  // 2 bytes per entry
      // read max_bytes at most and decode them
      byte[] palette_data = input_stream.readNBytes(max_bytes);
      current_palette_ = PaletteInterpreter.decodeBytes(palette_data);
      bytes_loaded = palette_data.length;
      
      undo_states_.clear();
      redo_states_.clear();
      unsaved_changes_ = false;
    }
    
    return file_size - bytes_loaded;
  }
  
  /**
   * Writes the current palette to the given file. This method will also record
   * that there are no unsaved changes. If there is a problem saving to the
   * file, then that status of unsaved changes will be unchanged. If the given
   * file does not already exist, then it should be created. Alternatively, if
   * the file already exists, then the existing file will be overwritten.
   * 
   * @param file the file to save the controller's current palette to.
   * @throws FileNotFoundException if the file cannot be accessed.
   * @throws IOException if there is an IO problem writing to the file.
   */
  public void savePalette(File file) throws FileNotFoundException, IOException {
    // try with resources to create an output stream that does not append
    try (FileOutputStream output_stream = new FileOutputStream(file, false)) {
      output_stream.write(PaletteInterpreter.encodePalette(current_palette_));
      unsaved_changes_ = false;
    }  // output stream should auto-close since we use try with resources
  }
  
  /**
   * Returns whether or not there are unsaved changes to the palette. This will
   * not change for changes which do not actually alter the contents of the
   * palette, such as changing the subpalette.
   * 
   * @return true if there are unsaved changes to the palette, false otherwise.
   */
  public boolean hasUnsavedChanges() { return unsaved_changes_; }
  
  /**
   * Takes an index for an entry in the palette and returns whether or not that
   * index is within the current subpalette.
   * 
   * @param index the index to check against the current subpalette.
   * @return true if the index is in the current subpalette and false otherwise.
   */
  public boolean isIndexInSubpalette(int index) {
    return (index >= getSubpaletteStartIndex()) &&
           (index <= getSubpaletteEndIndex());
  }
  
  /**
   * Gets the index in the palette for the currently selected color from the
   * subpalette.
   * 
   * @return the index of the currently selected color from the subpalette.
   */
  public int getSelectedColorIndex() { return selected_color_index_; }
  
  /**
   * Gets the index of the currently selected color relative to the current
   * subpalette.
   * 
   * @return the index relative to the current subpalette of the selected color.
   */
  public int getSelectedColorSubpaletteIndex() {
    return selected_color_index_ - subpalette_start_index_;
  }
  
  /**
   * Sets the index of the selected color based on the given index. If the given
   * index is within the subpalette, this method simply sets the selected
   * color's index. If not, however, this method will instead use the given
   * index to select a different index in the current subpalette.
   * 
   * @param index the index used to set the selected color.
   */
  public void setSelectedColor(int index) {
    if (isIndexInSubpalette(index)) {
      selected_color_index_ = index;
    } else {  // else, select a different index from the subpalette
      selected_color_index_ = getSubpaletteStartIndex() + (index % (1 << bpp_));
    }
  }
  
  /**
   * Gets an integer containing a color code that can be given to the SNES.
   * The color code consists of the last (least significant) 16 bits. This
   * method is essentially a wrapper for the
   * {@link Palette#getSNESColorCode(java.awt.Color)} method, although it uses
   * an index to pick a color in the palette instead of taking a color directly.
   * 
   * @param index the index of the color for which to return a color code for.
   * @return an integer containing a color code that can be given to the SNES.
   */
  public int getSNESColorCode(int index) {
    return Palette.getSNESColorCode(current_palette_.getColor(index));
  }
  
  /**
   * Gets a string with the color code that can be given to the SNES in a
   * hexadecimal format. This method is essentially a wrapper for the
   * {@link Palette#getSNESColorCodeString(java.awt.Color)} method, although it
   * uses an index to pick a color in the palette instead of taking a color
   * directly.
   * 
   * @param index the index of the color for which to return a color code
   *              string.
   * @return a hex string corresponding to the color code to give to the SNES. 
   */
  public String getSNESColorCodeString(int index) {
    return Palette.getSNESColorCodeString(current_palette_.getColor(index));
  }
  
  /**
   * Returns whether it is possible to undo - that is, whether it is possible
   * to revert to a previous state for the palette.
   * 
   * @return whether the palette's state can be undone.
   */
  public boolean canUndo() { return undo_states_.size() > 0; }
  
  /**
   * Returns whether it is possible to redo - that is, whether it is possible
   * to restore the palette to a previously undone states.
   * 
   * @return whether the palette's state can be redone.
   */
  public boolean canRedo() { return redo_states_.size() > 0; }
  
  /**
   * Reverts the palette to its previous state. This method can be called
   * multiple times to move back to further states so long as those states have
   * been recorded. If it is not possible to undo, this does nothing.
   */
  public void undo() {
    if (canUndo()) {
      // if we hit the cap for redos, pop the oldest one before proceeding
      if (redo_states_.size() >= MAX_REDOS) { redo_states_.removeLast(); }
      // save the current state for a possible redo and restore the latest undo
      redo_states_.addFirst(current_palette_);
      current_palette_ = undo_states_.removeFirst();
    }  // else, there is nothing to undo...
  }
  
  /**
   * Restores the palette to a previously undone state. This method can be
   * called multiple times to move to further states so long as those states
   * have been recorded. If it is not possible to redo, this does nothing.
   */
  public void redo() {
    if (canRedo()) {
      // if we hit the cap for undos, pop the oldest one before proceeding
      if (undo_states_.size() >= MAX_UNDOS) { undo_states_.removeLast(); }
      // push the current palette to the undos and pop the first redo palette
      undo_states_.addFirst(current_palette_);
      current_palette_ = redo_states_.removeFirst();
    }  // else, there is nothing to redo
  }
  
  private void saveForUndo() {
    // if we hit the cap for undos, pop the oldest one before proceeding
    if (undo_states_.size() >= MAX_UNDOS) { undo_states_.removeLast(); }
    // make a copy of the palette in its current state and save it
    undo_states_.addFirst(new Palette(current_palette_));
  }
  
  /** The maximum number of states that will be recorded to be undone. */
  public static final int MAX_UNDOS = 30;
  /** The maximum number of states that will be recorded to be redone. */
  public static final int MAX_REDOS = 30;
  
  private int bpp_;
  private int subpalette_start_index_;
  private int selected_color_index_;
  
  private boolean unsaved_changes_;
  private Palette current_palette_;
  private final Deque<Palette> undo_states_;
  private final Deque<Palette> redo_states_;
}
