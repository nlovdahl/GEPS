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
 * The controller for the tileset. The controller handles making changes to the
 * tileset's data model. The controller also has methods for reading and writing
 * tilesets to files. Unlike {@link Tileset}, the tileset controller has an
 * explicit sense of shape - the controller allows tilesets to be used as if
 * they had a width and height even though the tileset itself is essentially
 * amorphous.
 * 
 * @author Nicholas Lovdahl
 * 
 * @see Tileset
 * @see TilesetInterpreter
 */
public final class TilesetController {
  /**
   * Creates a tileset controller using the given number of bits per pixel and
   * an initial tileset with the specified width and height.
   * 
   * @param width the width of the initial tileset in tiles.
   * @param height the height of the initial tileset in tiles.
   * @param bpp the number of bits per pixel to be used. This should be between
   *        {@link Tileset#MIN_BPP} and {@link Tileset#MAX_BPP}.
   * @param bitplane_format the number corresponding to a bitplane format for
   *        the tileset. This should be one of {@link Tileset#BITPLANE_SERIAL},
   *        {@link Tileset#BITPLANE_PLANAR}, or
   *        {@link Tileset#BITPLANE_INTERTWINED}.
   * @throws IllegalArgumentException if either the width or height are less
   *         than one, or if bpp or bitplane_format are invalid.
   */
  public TilesetController(int width, int height,
                           int bpp, int bitplane_format) {
    if (width < 1 || height < 1) {
      throw new IllegalArgumentException(
        "Cannot initialize a tileset controller with a width of " +
        Integer.toString(width) + " and a height of " +
        Integer.toString(height) + ".");
    } else if (width * height > Tileset.MAX_TILES) {
      throw new IllegalArgumentException(
        "Width and height would initialize a tileset with too many tiles.");
    } else if (bpp < Tileset.MIN_BPP) {
      throw new IllegalArgumentException(
        "Cannot set bpp to value less than " +
        Integer.toString(Tileset.MIN_BPP) + ".");
    } else if (bpp > Tileset.MAX_BPP) {
      throw new IllegalArgumentException(
        "Cannot set bpp to value more than " +
        Integer.toString(Tileset.MAX_BPP) + ".");
    } else if (bitplane_format != Tileset.BITPLANE_SERIAL &&
               bitplane_format != Tileset.BITPLANE_PLANAR &&
               bitplane_format != Tileset.BITPLANE_INTERTWINED) {
      throw new IllegalArgumentException(
        Integer.toString(bitplane_format) + " does not correspond to a valid " +
        "bitplane format.");
    }  // else, we have legal values
    
    tileset_width_ = width;
    
    stroke_active_ = false;
    
    unsaved_changes_ = false;
    current_tileset_ = new Tileset(width * height, bpp, bitplane_format);
    undo_states_ = new LinkedList<>();
    redo_states_ = new LinkedList<>();
  }
  
  /**
   * Gets the current tileset from the controller.
   * 
   * @return the controller's current tileset.
   */
  public Tileset getTileset() { return current_tileset_; }
  
  /**
   * Sets the current tileset for the controller to the given tileset. This will
   * clear all undo and redo states and the controller will not have any unsaved
   * changes after calling this method.
   * 
   * @param tileset the new tileset for the controller.
   * @throws NullPointerException if tileset is null.
   */
  public void setTileset(Tileset tileset) {
    if (tileset == null) {
      throw new NullPointerException("Cannot use a null tileset.");
    }  // else, the tileset should be valid
    
    unsaved_changes_ = false;
    undo_states_.clear();
    redo_states_.clear();
    current_tileset_ = tileset;
  }
  
  /**
   * Gets the width of the tileset - the number of tiles from left to right in
   * the tileset.
   * 
   * @return the number of tiles in the tileset from left to right.
   */
  public int getWidthInTiles() { return tileset_width_; }
  
  /**
   * Gets the height of the tileset - the number of tiles from top to bottom in
   * the tileset.
   * 
   * @return the number of tiles in the tileset from top to bottom.
   */
  public int getHeightInTiles() {
    int tileset_height = current_tileset_.getNumberOfTiles() / tileset_width_;
    // add on if there are extra tiles (but not enough to fill a row)
    if (current_tileset_.getNumberOfTiles() % tileset_width_ != 0) {
      tileset_height++;
    }
    
    return tileset_height;
  }
  
  /**
   * Gets the width of the tileset in pixels.
   * 
   * @return the width of the tileset in pixels.
   */
  public int getWidthInPixels() {
    return tileset_width_ * Tileset.TILE_WIDTH;
  }
  
  /**
   * Gets the height of the tileset in pixels.
   * 
   * @return the height of the tileset in pixels.
   */
  public int getHeightInPixels() {
    return getHeightInTiles() * Tileset.TILE_HEIGHT;
  }
  
  /**
   * Gets the number of tiles in the tileset. This method is a wrapper for
   * {@link Tileset#getNumberOfTiles()} method.
   * 
   * @return the number of tiles in the current tileset.
   */
  public int getNumberOfTiles() { return current_tileset_.getNumberOfTiles(); }
  
  /**
   * Gets the number of bits per pixel being used for the current tileset. This
   * method is a wrapper for the {@link Tileset#getBPP()} method.
   * 
   * @return the number of bits per pixel.
   */
  public int getBPP() { return current_tileset_.getBPP(); }
  
  /**
   * Gets the number denoting which bitplane format is being used for the
   * current tileset. This method is a wrapper for the
   * {@link Tileset#getBitplaneFormat()} method.
   * 
   * @return the number denoting the current tileset's bitplane format.
   */
  public int getBitplaneFormat() {
    return current_tileset_.getBitplaneFormat();
  }
  
  /**
   * Reads the given file to interpret its data as a new tileset for the
   * controller and returns the number of bytes that were not loaded. This
   * method will also clear all saved undo and redo states and record that there
   * are no unsaved changes. If there is a problem reading the file, then the
   * tileset will not be altered, the undo and redo states will remain, and the
   * status of unsaved changed will be unchanged.
   * 
   * @param file the file to load the tileset from.
   * @return the number of bytes from the file that were not loaded.
   * @throws FileNotFoundException if file cannot be found and or accessed.
   * @throws IOException if there is an IO problem reading from the file.
   */
  public long loadTileset(File file) throws FileNotFoundException, IOException {
    long file_size, bytes_loaded;
    
    // try with resources to create an input stream
    try (FileInputStream input_stream = new FileInputStream(file)) {
      file_size = file.length();
      
      int max_bits = Tileset.MAX_TILES * Tileset.bitsPerTile(getBPP());
      int max_bytes = max_bits / 8;
      // add a byte if there are leftover bits, but not enough for a whole byte
      if (max_bits % 8 != 0) { max_bytes++; }
      // read max_bytes at most and decode them
      byte[] tileset_data = input_stream.readNBytes(max_bytes);
      current_tileset_ = TilesetInterpreter.decodeBytes(
                           tileset_data, getBPP(), getBitplaneFormat());
      bytes_loaded = tileset_data.length;
      
      undo_states_.clear();
      redo_states_.clear();
      unsaved_changes_ = false;
    }  // input stream should auto-close since we use try with resources
    
    return file_size - bytes_loaded;
  }
  
  /**
   * Writes the current tileset to the given file. This method will also record
   * that there are no unsaved changes. If there is a problem saving to the
   * file, then that status of unsaved changes will be unchanged. If the given
   * file does not already exist, then it should be created. Alternatively, if
   * the file already exists, then the existing file will be overwritten.
   * 
   * @param file the file to save the controller's current tileset to.
   * @throws FileNotFoundException if the file cannot be accessed.
   * @throws IOException if there is an IO problem writing to the file.
   */
  public void saveTileset(File file) throws FileNotFoundException, IOException {
    // try with resources to create an output stream that does not append
    try (FileOutputStream output_stream = new FileOutputStream(file, false)) {
      output_stream.write(TilesetInterpreter.encodeTileset(current_tileset_));
      unsaved_changes_ = false;
    }  // output stream should auto-close since we use try with resources
  }
  
  /**
   * Returns whether or not there are unsaved changes to the tileset. This will
   * not change for changes which do not actually alter the contents of the
   * tileset, such as changing the width of the tileset.
   * 
   * @return true if there are unsaved changes to the tileset, false otherwise.
   */
  public boolean hasUnsavedChanges() { return unsaved_changes_; }
  
  /**
   * Gets the index for a color in the palette from a pixel in the tileset.
   * The given coordinates are based from (0, 0) at the top-right and correspond
   * to pixels in the tileset. This method is similar to
   * {@link Tileset#getPixelIndex(int, int, int)}, except that it takes the
   * height and width of the tileset into account.
   * 
   * @param x the horizontal component of the coordinates to get the index for.
   * @param y the vertical component of the coordinates to get the index for.
   * @return the index for a color in the palette from the pixel in the tileset
   *         corresponding to the given coordinates, or a negative number if the
   *         coordinates do not correspond to a pixel.
   */
  public int getPixelIndex(int x, int y) {
    if (!isPointInTileset(x, y)) {
      return -1;  // return -1 if outside of the (imaginary) bounding rectangle
    }
    
    // determine the tile and the insets into that tile
    int tile = getTileNumber(x, y);
    x %= Tileset.TILE_WIDTH;
    y %= Tileset.TILE_HEIGHT;
    
    return current_tileset_.getPixelIndex(tile, x, y);
  }
  
  /**
   * Gets the color of the pixel in the tileset at the specified coordinates
   * by referencing the given palette controller. The given coordinates are
   * based from (0, 0) at the top-right and correspond to pixels in the tileset.
   * 
   * @param x the horizontal component of the coordinates to get the color for.
   * @param y the vertical component of the coordinates to get the color for.
   * @param palette_controller the palette controller to reference.
   * @return the color corresponding to the specified coordinates in the
   *         tileset, or null if the coordinates do not correspond to a pixel.
   */
  public Color getPixelColor(int x, int y,
                             PaletteController palette_controller) {
    int pixel_index = getPixelIndex(x, y);
    if (pixel_index >= 0) {
      return palette_controller.getSubpaletteColor(pixel_index);
    } else {
      return null;
    }
  }
  
  /**
   * Changes the number of bits per pixel used for the current tileset. If the
   * number of bits per pixel is different from that of the current tileset,
   * then the current tileset will be reinterpreted to match the new number of
   * bits per pixel.
   * 
   * @param bpp the number of bits per pixel to be used. This should be between
   *        {@link PaletteController#MIN_BPP} and
   *        {@link PaletteController#MAX_BPP}.
   * @throws IllegalArgumentException if bpp is invalid.
   */
  public void changeBPP(int bpp) {
    if (bpp < Tileset.MIN_BPP) {
      throw new IllegalArgumentException(
        "Cannot set bpp to value less than " +
        Integer.toString(Tileset.MIN_BPP) + ".");
    } else if (bpp > Tileset.MAX_BPP) {
      throw new IllegalArgumentException(
        "Cannot set bpp to value more than " +
        Integer.toString(Tileset.MAX_BPP) + ".");
    }  // else, we have a legal bpp value
    
    if (bpp != getBPP()) {
      // prepare in case the # of bits changes (then we have a change to save)
      int old_num_bits = current_tileset_.getNumberOfTiles() *
                         current_tileset_.getBitsPerTile();
      
      Tileset reinterpreted_tileset_ = TilesetInterpreter.reinterpretTileset(
        current_tileset_, bpp, getBitplaneFormat());
      
      int new_num_bits = reinterpreted_tileset_.getNumberOfTiles() *
                         reinterpreted_tileset_.getBitsPerTile();
      // if the # of bits changed, there's different data (save before change)
      if (new_num_bits != old_num_bits) {
        saveForUndo();
        redo_states_.clear();
        unsaved_changes_ = true;
      }
      
      current_tileset_ = reinterpreted_tileset_;
    }
  }
  
  /**
   * Changes the bitplane format to be used by the current tileset. If the
   * bitplane format is different from that of the current tileset, then the
   * current tileset will be reinterpreted to match the new bitplane format.
   * 
   * @param bitplane_format the number denoting the bitplane format to be used.
   *        This should be one of {@link Tileset#BITPLANE_SERIAL},
   *        {@link Tileset#BITPLANE_PLANAR}, or
   *        {@link Tileset#BITPLANE_INTERTWINED}.
   * @throws IllegalArgumentException if bitplane_format is invalid.
   */
  public void changeBitplaneFormat(int bitplane_format) {
    if (bitplane_format != Tileset.BITPLANE_SERIAL &&
        bitplane_format != Tileset.BITPLANE_PLANAR &&
        bitplane_format != Tileset.BITPLANE_INTERTWINED) {
      throw new IllegalArgumentException(
        Integer.toString(bitplane_format) +
        " does not correspond to a valid bitplane format.");
    }  // else, the bitplane format is valid
    
    if (bitplane_format != getBitplaneFormat()) {
      current_tileset_ = TilesetInterpreter.reinterpretTileset(
        current_tileset_, getBPP(), bitplane_format);
    }
  }
  
  /**
   * Begins a brush stroke across the tileset at the specified coordinates using
   * the given index, and records that an active stroke has begun. If the
   * current tileset would be changed, then its previous unchanged state will be
   * saved for a possible undo and it will be recorded that there are now
   * unsaved changes. If invalid coordinates are provided, the stroke will still
   * begin, but the tileset will not be altered.
   * <p>
   * The other stroke methods, {@link #addToStroke(int, int)} and
   * {@link #endStroke(int, int)} will use the index given to this method for
   * the respective stroke.
   * 
   * @param x the horizontal component of the coordinates to start at.
   * @param y the vertical component of the coordinates to start at.
   * @param index the index to use for the stroke.
   */
  public void beginStroke(int x, int y, int index) {
    stroke_change_ = false;  // the current stroke has made no changes to start
    
    // only make a change to the tileset if the point is in range
    if (isPointInTileset(x, y)) { setPixelIndex(x, y, index); }
    
    last_stroke_x_ = x;
    last_stroke_y_ = y;
    stroke_index_ = index;
    stroke_active_ = true;
  }
  
  /**
   * Continues a brush stroke by drawing a line between the last coordinates and
   * the given coordinates. If the current tileset would be changed and has not
   * already been saved for an undo in the current stroke, then the previous
   * unchanged state of the tileset will be saved for a possible undo and it
   * will be recorded that there are now unsaved changes. If there is no active
   * stroke, this method will do nothing. Likewise, if the given coordinates are
   * the same as the last coordinates, nothing will happen.
   * <p>
   * The index used by this method will be the one given to
   * {@link #beginStroke(int, int, int)} for the respective stroke.
   * 
   * @param x the horizontal component of the coordinates to continue the stroke
   *          with.
   * @param y the vertical component of the coordinates to continue the stroke
   *          with.
   */
  public void addToStroke(int x, int y) {
    // draw only if the stroke is active AND the coordinates have changed
    if (stroke_active_ && (x != last_stroke_x_ || y != last_stroke_y_)) {
      drawStrokeLine(last_stroke_x_, last_stroke_y_, x, y, stroke_index_);
      
      last_stroke_x_ = x;
      last_stroke_y_ = y;
    }  // else, do nothing
  }
  
  /**
   * Ends a brush stroke across the tileset at the specified coordinates; a line
   * will be drawn between the last coordinates in the stroke and the given
   * coordinates. If the current tileset would be changed and has not
   * already been saved for an undo in the current stroke, then the previous
   * unchanged state of the tileset will be saved for a possible undo and it
   * will be recorded that there are now unsaved changes. If there is no active
   * stroke, then the tileset will not be altered. No line will be drawn if the
   * specified coordinates are the same as the last coordinates.
   * <p>
   * The index used by this method will be the one given to
   * {@link #beginStroke(int, int, int)} for the respective stroke.
   * 
   * @param x the horizontal component of the coordinates to end at.
   * @param y the vertical component of the coordinates to end at.
   */
  public void endStroke(int x, int y) {
    if (stroke_active_) {
      // draw the line if the coordinates are different from the last ones
      if (x != last_stroke_x_ || y != last_stroke_y_) {
        drawStrokeLine(last_stroke_x_, last_stroke_y_, x, y, stroke_index_);
      }
      
      stroke_active_ = false;
    }  // else, do nothing
  }
  
  /**
   * Returns whether it is possible to undo - that is, whether it is possible
   * to revert to a previous state for the tileset.
   * 
   * @return whether the tileset's state can be undone.
   */
  public boolean canUndo() { return undo_states_.size() > 0; }
  
  /**
   * Returns whether it is possible to redo - that is, whether it is possible
   * to restore the tileset to a previously undone states.
   * 
   * @return whether the tileset's state can be redone.
   */
  public boolean canRedo() { return redo_states_.size() > 0; }
  
  /**
   * Reverts the tileset to its previous state. This method can be called
   * multiple times to move back to further states so long as those states have
   * been recorded. If it is not possible to undo, this does nothing.
   */
  public void undo() {
    if (canUndo()) {
      // if we hit the cap for redos, pop the oldest one before proceeding
      if (redo_states_.size() >= MAX_REDOS) { redo_states_.removeLast(); }
      // save the current state for a possible redo and restore the latest undo
      redo_states_.addFirst(TilesetInterpreter.encodeTileset(current_tileset_));
      current_tileset_ = TilesetInterpreter.decodeBytes(
        undo_states_.removeFirst(), getBPP(), getBitplaneFormat());
    }  // else, there is nothing to undo...
  }
  
  /**
   * Restores the tileset to a previously undone state. This method can be
   * called multiple times to move to further states so long as those states
   * have been recorded. If it is not possible to redo, this does nothing.
   */
  public void redo() {
    if (canRedo()) {
      // if we hit the cap for undos, pop the oldest one before proceeding
      if (undo_states_.size() >= MAX_UNDOS) { undo_states_.removeLast(); }
      // push the current tileset to the undos and pop the first redo palette
      undo_states_.addFirst(TilesetInterpreter.encodeTileset(current_tileset_));
      current_tileset_ = TilesetInterpreter.decodeBytes(
        redo_states_.removeFirst(), getBPP(), getBitplaneFormat());
    }  // else, there is nothing to redo
  }
  
  // save for a possible undo and makes a copy for the new current tileset
  private void saveForUndo() {
    // if we hit the cap for undos, pop the oldest one before proceeding
    if (undo_states_.size() >= MAX_UNDOS) { undo_states_.removeLast(); }
    // make a copy of the tileset in its current state and save it
    undo_states_.addFirst(TilesetInterpreter.encodeTileset(current_tileset_));
  }
  
  // gets the index number for a tile for given coordinates in the tileset
  private int getTileNumber(int x, int y) {
    return tileset_width_ * (y / Tileset.TILE_HEIGHT) + x / Tileset.TILE_WIDTH;
  }
  
  // sets the index for the specified coordinates in the current tileset
  // this could save the state of the tileset if there is a new change
  private void setPixelIndex(int x, int y, int index) {
    // find the coordinates relative to the tile
    int tile = getTileNumber(x, y);
    x %= Tileset.TILE_WIDTH;
    y %= Tileset.TILE_HEIGHT;
    
    // only make a change if it would be meaningful
    if (index != current_tileset_.getPixelIndex(tile, x, y)) {
      // if the tileset's state has not yet been saved during the current stroke
      if (!stroke_change_) {  // then save the current state of the tileset
        saveForUndo();
        redo_states_.clear();
        stroke_change_ = true;
        unsaved_changes_ = true;
      }
      
      current_tileset_.setPixelIndex(tile, x, y, index);
    }
  }
  
  // determine whether a given point will be in the tileset (is is valid?)
  private boolean isPointInTileset(int x, int y) {
    return x >= 0 && y >= 0 &&
           x < getWidthInPixels() && y < getHeightInPixels() &&
           getTileNumber(x, y) < current_tileset_.getNumberOfTiles();
  }
  
  // draw a line on the tileset between the two specified coordinates
  // return true if changes are made to the tileset
  private void drawStrokeLine(int start_x, int start_y,
                              int end_x, int end_y, int index) {
    int delta_x = Math.abs(start_x - end_x) + 1;
    int delta_y = Math.abs(start_y - end_y) + 1;
    int rate, limit, accumulator = 0;
    int x = start_x;
    int y = start_y;
    int x_step, y_step;
    if (start_x <= end_x) { x_step = 1; } else { x_step = -1; }
    if (start_y <= end_y) { y_step = 1; } else { y_step = -1; }
    
    if (delta_x >= delta_y) {  // step along x
      rate = delta_y;
      limit = delta_x;
      
      while (x != end_x) {  // draws all but the last point in the line
        if (isPointInTileset(x, y)) { setPixelIndex(x, y, index); }
        
        x += x_step;
        accumulator += rate;
        if (accumulator >= limit) {
          y += y_step;
          accumulator -= limit;
        }
      }  // we still need to draw the endpoint after this
      
      if (isPointInTileset(x, y)) { setPixelIndex(x, y, index); }
    } else {  // step along y
      rate = delta_x;
      limit = delta_y;
      
      while (y != end_y) {  // draws all but the last point in the line
        if (isPointInTileset(x, y)) { setPixelIndex(x, y, index); }
        
        y += y_step;
        accumulator += rate;
        if (accumulator >= limit) {
          x += x_step;
          accumulator -= limit;
        }
      }  // we still need to draw the endpoint after this
      
      if (isPointInTileset(x, y)) { setPixelIndex(x, y, index); }
    }
  }
  
  /** The maximum number of states that will be recorded to be undone. */
  public static final int MAX_UNDOS = 30;
  /** The maximum number of states that will be recorded to be redone. */
  public static final int MAX_REDOS = 30;
  
  private int tileset_width_;   // number of tiles from left to right
  
  private boolean stroke_active_;
  private boolean stroke_change_;
  private int stroke_index_;
  private int last_stroke_x_;
  private int last_stroke_y_;
  
  private boolean unsaved_changes_;
  private Tileset current_tileset_;
  private final Deque<byte[]> undo_states_;
  private final Deque<byte[]> redo_states_;
}
