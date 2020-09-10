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
 */
public final class Tileset {
  /**
   * Creates a tileset with the given number of tiles, bits per pixel, and
   * tileset format. The tileset will have no pattern and only contain pixels
   * with an index of zero.
   * 
   * @param tiles the number of tiles for the created tileset. This should be
   *        between one and {@link #MAX_TILES}.
   * @param bpp the number of bits per pixel used to select a color from a
   *        palette.
   * @param tileset_format the number corresponding to the format of the
   *        tileset.
   * @throws IllegalArgumentException if the number of tiles is less than 1 or
   *         greater than {@link #MAX_TILES}, or if either bpp or tileset_format
   *         are invalid per {@link #isValidBPP(int)} and
   *         {@link #isValidTilesetFormat(int)}, respectively.
   */
  public Tileset(int tiles, int bpp, int tileset_format) {
    if (tiles < 1) {
      throw new IllegalArgumentException("Cannot have less than one tile.");
    } else if (tiles > MAX_TILES) {
      throw new IllegalArgumentException(
        "Cannot have more than " + Integer.toString(MAX_TILES) + " tiles.");
    } else if (!isValidBPP(bpp)) {
      throw new IllegalArgumentException("Invalid BPP value.");
    } else if (!isValidTilesetFormat(tileset_format)) {
      throw new IllegalArgumentException("Invalid tileset format value.");
    }  // else, all arguments should be valid
    
    tiles_ = tiles;
    bpp_ = bpp;
    tileset_format_ = tileset_format;
    pixel_indexes_ = new int[tiles][TILE_WIDTH][TILE_HEIGHT];
  }
  
  /**
   * Creates a distinct copy of a given tileset, but with the specified number
   * of tiles. The new tileset will have the same number of bits per pixel and
   * tileset format as the original tileset. The pattern from tileset will be
   * copied in its entirety unless there are too few tiles to copy to.
   * Otherwise, if the number of tiles is greater than the number of tiles in
   * the tileset to copy, the tileset will be copied entirely but the remaining
   * tiles will have no pattern.
   * 
   * @param tileset the tileset to be copied.
   * @param tiles the number of tiles that the new tileset should have.
   */
  public Tileset(Tileset tileset, int tiles) {
    // create a tileset with the desired # of tiles, and the same bpp & format
    this(tiles, tileset.getBPP(), tileset.getTilesetFormat());
    
    // copy to the new tileset until all is copied, or there is no more space
    int tile_limit = Math.min(tiles, tileset.getNumberOfTiles());
    for (int tile = 0; tile < tile_limit; tile++) {
      for (int x = 0; x < TILE_WIDTH; x++) {
        for (int y = 0; y < TILE_HEIGHT; y++) {
          setPixelIndex(tile, x, y, tileset.getPixelIndex(tile, x, y));
        }
      }
    }
  }
  
  /**
   * Creates a distinct copy of a given tileset. This new tileset has the same
   * number of tiles, bits per pixel, tileset format, and pattern as the
   * original tileset.
   * 
   * @param tileset the tileset to be copied.
   */
  public Tileset(Tileset tileset) {
    // create a copy that uses the same number of tiles as the original tileset
    this(tileset, tileset.getNumberOfTiles());
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
   * Gets the number denoting which tileset format should be used for the
   * tileset.
   * 
   * @return the number denoting the tileset's tileset format.
   */
  public int getTilesetFormat() { return tileset_format_; }
  
  /**
   * Gets the number of bits per tile needed for the tileset.
   * 
   * @return the number of bits per tile needed for the tileset.
   */
  public int getBitsPerTile() { return TILE_WIDTH * TILE_HEIGHT * bpp_; }
  
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
        Integer.toString(tiles_) + "."
      );
    } else if (x < 0 || x >= TILE_WIDTH || y < 0 || y >= TILE_HEIGHT) {
      throw new IllegalArgumentException(
        "Cannot set index for coordinates (" + Integer.toString(x) + ", " +
        Integer.toString(y) + ")."
      );
    } else if (index >= 1 << bpp_) {  // max index is 2^bpp_
      throw new IllegalArgumentException(
        Integer.toString(index) + " is an invalid index for " +
        Integer.toString(bpp_) + " BPP."
      );
    }  // else, the tile, coordinates, and new index are valid
    
    pixel_indexes_[tile][x][y] = index;
  }
  
  /**
   * Returns the number of bits required to store information for a single tile
   * using the given number of bits per pixel.
   * 
   * @param bpp the bits per pixel used for the tile. This should be between
   *        {@link #MIN_BPP} and {@link #MAX_BPP}.
   * @return the number of bits needed to store a single tile.
   * @throws IllegalArgumentException if bpp is invalid.
   */
  public static int bitsPerTile(int bpp) {
    if (!isValidBPP(bpp)) {
      throw new IllegalArgumentException("Invalid BPP value.");
    }  // else, bpp is valid
    
    return TILE_WIDTH * TILE_HEIGHT * bpp;
  }
  
  /**
   * Returns whether or not the given number of bits per pixel would be valid.
   * That is, whether the number is greater than or equal to {@link #MIN_BPP}
   * and less than or equal to {@link #MAX_BPP}.
   * 
   * @param bpp some number of bits per pixel.
   * @return true if bpp is a valid number of bits per pixel for a tileset,
   *         false otherwise.
   */
  public static boolean isValidBPP(int bpp) {
    return bpp >= MIN_BPP && bpp <= MAX_BPP;
  }
  
  /**
   * Returns whether or not the given number denotes a valid tileset format.
   * That is, whether the number denoting the tileset format is one of
   * {@link #SERIAL_FORMAT}, {@link #LINEAR_PLANAR_FORMAT},
   * {@link #LINEAR_INTERTWINED_FORMAT}, or {@link #PAIRED_INTERTWINED_FORMAT}.
   * 
   * @param tileset_format some number denoting the tileset format.
   * @return true if tileset_format corresponds to a valid tileset format,
   *         false otherwise.
   */
  public static boolean isValidTilesetFormat(int tileset_format) {
    switch (tileset_format) {
      case SERIAL_FORMAT:
        return true;
      case PLANAR_FORMAT:
        return true;
      case LINEAR_INTERTWINED_FORMAT:
        return true;
      case PAIRED_INTERTWINED_FORMAT:
        return true;
      default:
        return false;
    }
  }
  
  /** The width in pixels of a single tile. */
  public static final int TILE_WIDTH = 8;
  /** The height in pixels of a single tile. */
  public static final int TILE_HEIGHT = 8;
  /** The minimum number of bits per pixel. */
  public static final int MIN_BPP = 1;
  /** The maximum number of bits per pixel. */
  public static final int MAX_BPP = 8;
  /** The largest number of tiles that can be in a tileset. This should be the
   greatest number of tiles that can be used alongside {@link #MAX_BPP} without
   using more bits than the maximum value for a signed integer can hold. */
  public static final int MAX_TILES = Integer.MAX_VALUE / bitsPerTile(MAX_BPP);
  /** The number denoting a serial tileset format. */
  public static final int SERIAL_FORMAT = 0;
  /** The number denoting a planar tileset format. */
  public static final int PLANAR_FORMAT = 1;
  /** The number denoting an intertwined tileset format whose bitplanes have a
   linear ordering. */
  public static final int LINEAR_INTERTWINED_FORMAT = 2;
  /** The number denoting an intertwined tileset format whose bitplanes are
   ordered into pairs of two at most. */
  public static final int PAIRED_INTERTWINED_FORMAT = 3;
  
  private int[][][] pixel_indexes_;  // [tile][row][column]
  private final int tiles_;
  private final int bpp_;
  private final int tileset_format_;
}
