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

/**
 * The controller for the tileset. The controller handles making changes to the
 * tileset's data model. Unlike {@link Tileset}, the tileset controller has an
 * explicit sense of shape - the controller allows tilesets to be used as if
 * they had a width and height even though the tileset itself is essentially
 * amorphous.
 * 
 * @author Nicholas Lovdahl
 * 
 * @see Tileset
 * @see TilesetView
 * @see CanvasView
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
    tileset_height_ = height;
    
    current_tileset_ = new Tileset(width * height, bpp, bitplane_format);
    undo_states_ = new LinkedList<>();
    redo_states_ = new LinkedList<>();
    
    stroke_active_ = false;
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
  public int getHeightInTiles() { return tileset_height_; }
  
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
    return tileset_height_ * Tileset.TILE_HEIGHT;
  }
  
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
    if (x < 0 || y < 0 || x >= tileset_width_ * Tileset.TILE_WIDTH ||
        y >= tileset_height_ * Tileset.TILE_HEIGHT) {
      return -1;  // return -1 if outside of the (imaginary) bounding rectangle
    }
    // determine the tile and the insets into that tile
    int tile = getTileNumber(x, y);
    if (tile >= current_tileset_.getNumberOfTiles()) {
      return -1;  // return -1 if the tile will be outside of the tileset
    }  // else, find the coordinates relative to the tile
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
   * then the current state of the tileset to be saved for a possible undo and
   * the current tileset will be reinterpreted to match the new number of bits
   * per pixel.
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
      saveForUndo();
      redo_states_.clear();
      current_tileset_ = TilesetInterpreter.reinterpretTileset(
        current_tileset_, bpp, getBitplaneFormat());
      // change the tileset height to accomodate possible change in # of tiles
      tileset_height_ = current_tileset_.getNumberOfTiles() / tileset_width_;
      if (current_tileset_.getNumberOfTiles() % tileset_width_ != 0) {
        tileset_height_++;
      }
    }
  }
  
  /**
   * Changes the bitplane format to be used by the current tileset. If the
   * bitplane format is different from that of the current tileset, then the
   * current state of the tileset to be saved for a possible undo and the
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
      saveForUndo();
      redo_states_.clear();
      current_tileset_ = TilesetInterpreter.reinterpretTileset(
        current_tileset_, getBPP(), bitplane_format);
      // change the tileset height to accomodate possible change in # of tiles
      tileset_height_ = current_tileset_.getNumberOfTiles() / tileset_width_;
      if (current_tileset_.getNumberOfTiles() % tileset_width_ != 0) {
        tileset_height_++;
      }
    }
  }
  
  /**
   * Begins a brush stroke across the tileset at the specified coordinates using
   * the given index, and records that an active stroke has begun. This method
   * will also save the state of the tileset before changing it for a possible
   * undo. If invalid coordinates are provided, the stroke will still begin and
   * the tileset's state will be saved, but the tileset will not be altered.
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
    saveForUndo();
    redo_states_.clear();
    
    // only make a change to the tileset if the point is in range
    if (isPointInTileset(x, y)) { setPixelIndex(x, y, index); }
    
    last_stroke_x_ = x;
    last_stroke_y_ = y;
    stroke_index_ = index;
    stroke_active_ = true;
  }
  
  /**
   * Continues a brush stroke by drawing a line between the last coordinates and
   * the given coordinates. If there is no active stroke, this method will do
   * nothing. Likewise, if the given coordinates are the same as the last
   * coordinates, nothing will happen.
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
   * coordinates. If there is no active stroke, then the tileset will not be
   * altered. No line will be drawn if the specified coordinates are the same as
   * the last coordinates.
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
      redo_states_.addFirst(current_tileset_);
      current_tileset_ = undo_states_.removeFirst();
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
      // push the current palette to the undos and pop the first redo palette
      undo_states_.addFirst(current_tileset_);
      current_tileset_ = redo_states_.removeFirst();
    }  // else, there is nothing to redo
  }
  
  // save for a possible undo and makes a copy for the new current tileset
  private void saveForUndo() {
    // if we hit the cap for undos, pop the oldest one before proceeding
    if (undo_states_.size() >= MAX_UNDOS) { undo_states_.removeLast(); }
    // make a copy of the palette in its current state and save it
    undo_states_.addFirst(new Tileset(current_tileset_));
  }
  
  // gets the index number for a tile for given coordinates in the tileset
  private int getTileNumber(int x, int y) {
    return tileset_width_ * (y / Tileset.TILE_HEIGHT) + x / Tileset.TILE_WIDTH;
  }
  
  /**
   * Sets the index for the given coordinates in a specified tile in the
   * tileset. The given coordinates are based from (0, 0) at the top-right and
   * correspond to pixels in the tileset. This method is similar to
   * {@link Tileset#setPixelIndex(int, int, int, int)}, except that it takes the
   * height and width of the tileset into account.
   * 
   * @param x the horizontal component of the coordinates to set the index for.
   * @param y the vertical component of the coordinates to set the index for.
   * @param index the new value for the index.
   * @throws IllegalArgumentException if the given coordinates are outside of
   *         the bounds of the tileset, or if
   *         {@link Tileset#setPixelIndex(int, int, int, int)} finds the
   *         arguments invalid.
   */
  private void setPixelIndex(int x, int y, int index) {
    // check that the coordinates, tile, and index are valid, respectively
    if (x < 0 || y < 0 || x >= tileset_width_ * Tileset.TILE_WIDTH ||
        y >= tileset_height_ * Tileset.TILE_HEIGHT) {
      throw new IllegalArgumentException(
        "Cannot set index for coordinates (" + Integer.toString(x) + ", " +
        Integer.toString(y) + ").");
    } // else, the coordinates, the tile, and new index are valid
    
    // find the coordinates relative to the tile
    int tile = getTileNumber(x, y);
    x %= Tileset.TILE_WIDTH;
    y %= Tileset.TILE_HEIGHT;
    
    current_tileset_.setPixelIndex(tile, x, y, index);
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
  private int tileset_height_;  // number of tiles from top to bottom
  
  private boolean stroke_active_;
  private int stroke_index_;
  private int last_stroke_x_;
  private int last_stroke_y_;
  
  private Tileset current_tileset_;
  private final Deque<Tileset> undo_states_;
  private final Deque<Tileset> redo_states_;
}
