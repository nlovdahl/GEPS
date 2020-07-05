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
  public TilesetController(PaletteController palette_controller) {
    if (palette_controller == null) {
      throw new NullPointerException(
        "Cannot create tileset controller with null palette controller.");
    }  // else, we should have a good palette controller to pair with
    palette_controller_ = palette_controller;
    
    tileset_width_ = 8;
    tileset_height_ = 8;
    
    current_tileset_ = new Tileset();
    undo_states_ = new LinkedList<>();
    redo_states_ = new LinkedList<>();
  }
  
  public int getTilesetWidth() { return tileset_width_; }
  
  public int getTilesetHeight() { return tileset_height_; }
  
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
  public void setPixelIndex(int x, int y, int index) {
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
  
  /**
   * Gets the color of the pixel in the tileset at the specified coordinates.
   * The given coordinates are based from (0, 0) at the top-right and correspond
   * to pixels in the tileset.
   * 
   * @param x the horizontal component of the coordinates to get the color for.
   * @param y the vertical component of the coordinates to get the color for.
   * @return the color corresponding to the specified coordinates in the
   *         tileset, or null if the coordinates do not correspond to a pixel.
   */
  public Color getPixelColor(int x, int y) {
    int pixel_index = getPixelIndex(x, y);
    if (pixel_index >= 0) {
      return palette_controller_.getSelectionColor(pixel_index);
    } else {
      return null;
    }
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
  
  /** The maximum number of states that will be recorded to be undone. */
  public static final int MAX_UNDOS = 30;
  /** The maximum number of states that will be recorded to be redone. */
  public static final int MAX_REDOS = 30;
  
  private int tileset_width_;   // number of tiles from left to right
  private int tileset_height_;  // number of tiles from top to bottom
  
  private Tileset current_tileset_;
  private final PaletteController palette_controller_;
  private final Deque<Tileset> undo_states_;
  private final Deque<Tileset> redo_states_;
}
