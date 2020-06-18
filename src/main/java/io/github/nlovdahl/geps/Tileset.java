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

/**
 * The data model for a tileset. Tileset contains references which are meant to
 * correspond to color entries in a {@link Palette}. A tileset itself is divided
 * into a discrete number of 8x8 tiles.
 * 
 * @author Nicholas Lovdahl
 * 
 * @see TilesetController
 * @see TilesetView
 * @see CanvasView
 */
public final class Tileset {
  /**
   * Creates a default tileset. This default tileset will have 64 8x8 tiles with
   * no pattern.
   */
  public Tileset() { this(64, 4); }
  
  /**
   * Creates a tileset with the given number of tiles and bits per pixel. The
   * contents of the tileset are a default (no pattern). A tileset must have at
   * least one tile and between 0 and 8 bits per pixel.
   * 
   * @param tiles the number of 8x8 tiles for the created tileset.
   * @param bpp the bits per pixel used to select a color in the palette.
   * @throws IllegalArgumentException if the number of tiles is less than 1 or
   *         if the bits per pixel is not between 0 and 8.
   */
  public Tileset(int tiles, int bpp) {
    if (tiles < 1) {
      throw new IllegalArgumentException(
        "Number of tiles cannot be less than 1.");
    } else if (bpp < 1) {
      throw new IllegalArgumentException(
        "Cannot create tileset with less than 1 bit per pixel.");
    } else if (bpp > 8) {
      throw new IllegalArgumentException(
        "Cannot create tileset with more than 8 bits per pixel.");
    }  // else, our parameters are valid and we can proceed
    
    tiles_ = tiles;
    bpp_ = bpp;
    pixel_indexes_ = new int[tiles][TILE_WIDTH][TILE_HEIGHT];
  }
  
  /**
   * Creates a distinct copy of a given tileset. This new tileset has the same
   * number of tiles, bits per pixel, and pattern as the original tileset.
   * 
   * @param tileset the tileset to be copied.
   */
  public Tileset(Tileset tileset) {
    this(tileset.getNumberOfTiles(), tileset.getBPP());
    
    for (int tile = 0; tile < tiles_; tile++) {
      for (int x = 0; x < TILE_WIDTH; x++) {
        for (int y = 0; y < TILE_HEIGHT; y++) {
          setPixel(tile, x, y, tileset.getPixel(tile, x, y));
        }
      }
    }
  }
  
  public int getNumberOfTiles() { return tiles_; }
  
  public int getBPP() { return bpp_; }
  
  public int getPixel(int tile, int x, int y) {
    return pixel_indexes_[tile][x][y];
  }
  
  public void setPixel(int tile, int x, int y, int index) {
    pixel_indexes_[tile][x][y] = index % (1 << bpp_);  // index % 2^bpp
  }
  
  public static final int TILE_WIDTH = 8;
  public static final int TILE_HEIGHT = 8;
  
  private int[][][] pixel_indexes_;  // [tile][row][column]
  private final int tiles_;
  private final int bpp_;
}
