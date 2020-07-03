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
 * The data model for a tileset. A tileset can be thought of as a collection of
 * tiles where a tile would consist of references which are meant to correspond
 * to color entries in a {@link Palette}. Together, a tileset and a palette
 * could be used to form an image.
 * 
 * @author Nicholas Lovdahl
 * 
 * @see TilesetController
 * @see TilesetView
 * @see CanvasView
 */
public final class Tileset {
  /** Creates a tileset with a default number of tiles and bits per pixel. */
  public Tileset() { this(64, 4); }
  
  /**
   * Creates a tileset with the given number of tiles and bits per pixel. The
   * contents of the tileset are a default (no pattern). A tileset must have at
   * least one tile a number of bits per pixel between
   * {@link PaletteController#MAX_BPP} and {@link PaletteController#MIN_BPP}.
   * 
   * @param tiles the number of 8x8 tiles for the created tileset.
   * @param bpp the bits per pixel used to select a color in the palette.
   * @throws IllegalArgumentException if the number of tiles is less than 1 or
   *         if the bits per pixel is not between
   *         {@link PaletteController#MAX_BPP} and
   *         {@link PaletteController#MIN_BPP}.
   */
  public Tileset(int tiles, int bpp) {
    if (tiles < 1) {
      throw new IllegalArgumentException("Cannot have less than one tile.");
    } else if (bpp < PaletteController.MIN_BPP) {
      throw new IllegalArgumentException(
        "Cannot set bpp to value less than " +
        Integer.toString(PaletteController.MIN_BPP) + ".");
    } else if (bpp > PaletteController.MAX_BPP) {
      throw new IllegalArgumentException(
        "Cannot set bpp to value more than " +
        Integer.toString(PaletteController.MAX_BPP) + ".");
    }  // else, we have legal values
    
    tiles_ = tiles;
    bpp_ = bpp;
    pixel_indexes_ = new int[tiles][TILE_WIDTH][TILE_HEIGHT];
  }
  
  /**
   * Creates a distinct copy of a given tileset. This new tileset has the same
   * number of tiles, bits per pixel, and pattern as the original tileset. This
   * new tileset is a deep copy; it does not use references to the data in the
   * tileset to be copied but instead sets aside separate data for itself which
   * is identical.
   * 
   * @param tileset the tileset to be copied.
   */
  public Tileset(Tileset tileset) {
    this(tileset.getNumberOfTiles(), tileset.getBPP());
    
    for (int tile = 0; tile < tiles_; tile++) {
      for (int x = 0; x < TILE_WIDTH; x++) {
        for (int y = 0; y < TILE_HEIGHT; y++) {
          setPixelIndex(tile, x, y, tileset.getPixelIndex(tile, x, y));
        }
      }
    }
  }
  
  /**
   * Gets the number of tiles in the tileset.
   * 
   * @return the number of tiles in the tileset.
   */
  public int getNumberOfTiles() { return tiles_; }
  
  /**
   * Gets the number of bits per pixel used for color selection by the tileset.
   * 
   * @return the number of bits per pixel used by the tileset.
   */
  public int getBPP() { return bpp_; }
  
  /**
   * Gets the index for the given coordinates in a specified tile in the
   * tileset. This index is meant to be used alongside a {@link Palette} to get
   * a corresponding color.
   * 
   * @param tile the number for the tile to get the index from.
   * @param x the horizontal component of the coordinates of the pixel to get
   *          the index for.
   * @param y the vertical component of the coordinates of the pixel to get the
   *          index for.
   * @return the index for the pixel at the given coordinates and tile, or a
   *         negative number if either the tile or coordinates are not valid.
   */
  public int getPixelIndex(int tile, int x, int y) {
    if (x < 0 || x >= TILE_WIDTH || y < 0 || y >= TILE_HEIGHT ||
        tile < 0 || tile >= tiles_) {
      return -1;
    } else {
      return pixel_indexes_[tile][x][y];
    }
  }
  
  /**
   * Sets the index for the given coordinates in a specified tile in the
   * tileset. This index is meant to be used alongside a {@link Palette} to get
   * a corresponding color.
   * 
   * @param tile the number for the tile to set the index of.
   * @param x the horizontal component of the coordinates of the pixel to set
   *          the index for.
   * @param y the vertical component of the coordinates of the pixel to set the
   *          index for.
   * @param index the new value for the index.
   * @throws IllegalArgumentException if the tile or the coordinates are not
   *         valid, or if the index is greater than would be allowed for the
   *         tileset's number of bits per pixel.
   */
  public void setPixelIndex(int tile, int x, int y, int index) {
    if (tile < 0 || tile >= tiles_) {
      throw new IllegalArgumentException(
        "Cannot set index for tile " + Integer.toString(tile) + " out of " +
        Integer.toString(tiles_) + ".");
    } else if (x < 0 || x >= TILE_WIDTH || y < 0 || y >= TILE_HEIGHT) {
      throw new IllegalArgumentException(
        "Cannot set index for coordinates (" + Integer.toString(x) + ", " +
        Integer.toString(y) + ").");
    } else if (index >= 1 << bpp_) {  // max index is 2^bpp_
      throw new IllegalArgumentException(
        "Cannot set index to be " + Integer.toString(index) +
        ". Max index is " + Integer.toString(1 << bpp_) + ".");
    }  // else, the tile, coordinates, and new index are valid
    
    pixel_indexes_[tile][x][y] = index;
  }
  
  /** The width in pixels of a single tile. */
  public static final int TILE_WIDTH = 8;
  /** The height in pixels of a single tile. */
  public static final int TILE_HEIGHT = 8;
  
  private int[][][] pixel_indexes_;  // [tile][row][column]
  private final int tiles_;
  private final int bpp_;
}
