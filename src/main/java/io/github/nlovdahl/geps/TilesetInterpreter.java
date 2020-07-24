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
 * A collection of methods which can be used to create new tilesets. This is
 * useful for interpreting raw data to turn into a tileset, or to reinterpret a
 * tileset using a different format.
 * 
 * @author Nicholas Lovdahl
 * 
 * @see Tileset
 */
public final class TilesetInterpreter {
  /**
   * Takes an existing tileset and reinterprets it to create a new tileset with
   * the specified number of bits per pixel and bitplane format. If both the
   * number of bits per pixel and bitplane format are the same as those of the
   * given tileset, then a new tileset which is a deep copy of the tileset will
   * be returned.
   * 
   * @param tileset the tileset to be reinterpreted.
   * @param bpp the number of bits per pixel that the tileset should be
   *        reinterpreted with. This should be between {@link Tileset#MIN_BPP}
   *        and {@link Tileset#MAX_BPP}.
   * @param bitplane_format the bitplane format that the tileset should be
   *        reinterpreted with. This should be one of
   *        {@link Tileset#BITPLANE_SERIAL}, {@link Tileset#BITPLANE_PLANAR}, or
   *        {@link Tileset#BITPLANE_INTERTWINED}.
   * @return a new tileset with the specified number of bits per pixel and
   *         bitplane format.
   * @throws NullPointerException if the given tileset is null.
   * @throws IllegalArgumentException if bpp or bitplane_format is invalid.
   */
  public static Tileset reinterpretTileset(Tileset tileset,
                                           int bpp, int bitplane_format) {
    if (tileset == null) {
      throw new NullPointerException("Cannot reinterpret a null tileset.");
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
    
    // return a deep copy of the tileset if it already matches the given specs
    if (tileset.getBPP() == bpp &&
        tileset.getBitplaneFormat() == bitplane_format) {
      return new Tileset(tileset);
    } else {  // else, reinterpret the tileset
      return decodeBytes(encodeTileset(tileset), bpp, bitplane_format);
    }
  }
  
  /**
   * Creates an array of bytes from the given tileset. This data corresponds to
   * the given tileset and can be reconstructed using
   * {@link TilesetInterpreter#decodeBytes(byte[], int, int)}.
   * 
   * @param tileset the tileset to create the bytes from.
   * @return an array of bytes containing data corresponding to the tileset.
   * @throws NullPointerException if the given tileset is null.
   */
  public static byte[] encodeTileset(Tileset tileset) {
    if (tileset == null) {
      throw new NullPointerException("Cannot create data from null tileset.");
    }  // else, the tileset should be valid
    
    // the number of tiles is limited so that tileset_bits will fit in an int
    int num_bits = tileset.getNumberOfTiles() *
                   Tileset.bitsPerTile(tileset.getBPP());
    int num_bytes = num_bits / 8;
    // add an extra byte if there will be remainder bits (but not a full byte)
    if (num_bits % 8 != 0) { num_bytes++; }
    
    byte[] tileset_data = new byte[num_bytes];
    
    // set the initial pixel coordinates and bitplane
    int tile = 0, x = 0, y = 0, bitplane = 0;
    // serial encoding starts with the MSB, unlike planar and intertwined
    if (tileset.getBitplaneFormat() == Tileset.BITPLANE_SERIAL) {
      bitplane = tileset.getBPP() - 1;
    }
    
    // begin encoding each bit that we need to
    for (int bit = 0; bit < num_bits; bit++) {
      // if the bit for the chosen bitplane in the pixel index is 1, encode a 1
      if ((tileset.getPixelIndex(tile, x, y) & (1 << bitplane)) != 0) {
        tileset_data[bit / 8] |= (1 << (bit % 8));
      }  // else, there should be a 0 by default already (so just skip it)
      
      // advance the pixel coordinates and bitplane based on the bitplane format
      switch (tileset.getBitplaneFormat()) {
        // for serial: bitplane (MSB to LSB) -> x -> y -> tile
        case Tileset.BITPLANE_SERIAL:
          bitplane--;
          if (bitplane < 0) { bitplane = tileset.getBPP() - 1; x++; }
          if (x >= Tileset.TILE_WIDTH) { x = 0; y++; }
          if (y >= Tileset.TILE_HEIGHT) { y = 0; tile++; }
          break;
        // for planar: x -> y -> bitplane (LSB to MSB) -> tile
        case Tileset.BITPLANE_PLANAR:
          x++;
          if (x >= Tileset.TILE_WIDTH) { x = 0; y++; }
          if (y >= Tileset.TILE_HEIGHT) { y = 0; bitplane++; }
          if (bitplane >= tileset.getBPP()) { bitplane = 0; tile++; }
          break;
        // for intertwined: x -> bitplane (LSB to MSB) -> y -> tile
        case Tileset.BITPLANE_INTERTWINED:
          x++;
          if (x >= Tileset.TILE_WIDTH) { x = 0; bitplane++; }
          if (bitplane >= tileset.getBPP()) { bitplane = 0; y++; }
          if (y >= Tileset.TILE_HEIGHT) { y = 0; tile++; }
          break;
      }
    }
    
    return tileset_data;
  }
  
  /**
   * Takes an array of bytes and interprets them to create a tileset with the
   * specified number of bits per pixel and bitplane format.
   * 
   * @param tileset_data the bytes to be interpreted.
   * @param bpp the number of bits per pixel used for interpretation. This
   *        should be between {@link Tileset#MIN_BPP} and
   *        {@link Tileset#MAX_BPP}.
   * @param bitplane_format the bitplane format used to interpretation. This
   *        should be one of {@link Tileset#BITPLANE_SERIAL},
   *        {@link Tileset#BITPLANE_PLANAR}, or
   *        {@link Tileset#BITPLANE_INTERTWINED}.
   * @return the resulting tileset interpreted from the given bytes, bits per
   *         pixel, and bitplane format.
   * @throws NullPointerException if tileset_bytes is null.
   * @throws IllegalArgumentException if tileset_bytes has a length of less than
   *         one, or if bpp or bitplane_format are invalid.
   */
  public static Tileset decodeBytes(byte[] tileset_data,
                                    int bpp, int bitplane_format) {
    if (tileset_data == null) {
      throw new NullPointerException("Cannot interpret null byte array.");
    } else if (tileset_data.length < 1) {
      throw new IllegalArgumentException("Cannot interpret empty byte array.");
    } else if (bpp < Tileset.MIN_BPP) {
      throw new IllegalArgumentException(
        "Cannot use bpp to value less than " +
        Integer.toString(Tileset.MIN_BPP) + ".");
    } else if (bpp > Tileset.MAX_BPP) {
      throw new IllegalArgumentException(
        "Cannot use bpp to value more than " +
        Integer.toString(Tileset.MAX_BPP) + ".");
    } else if (bitplane_format != Tileset.BITPLANE_SERIAL &&
               bitplane_format != Tileset.BITPLANE_PLANAR &&
               bitplane_format != Tileset.BITPLANE_INTERTWINED) {
      throw new IllegalArgumentException(
        Integer.toString(bitplane_format) +
        " does not correspond to a valid bitplane format.");
    }  // else, we have legal values
    
    // the number of tiles is limited so that tileset_bits will fit in an int
    int num_bits = tileset_data.length * 8;
    int bits_per_tile = Tileset.bitsPerTile(bpp);
    int num_tiles = num_bits / bits_per_tile;
    // add an extra tile if there will be remainder bits (but not a full tile)
    if (num_bits % bits_per_tile != 0) { num_tiles++; }
    
    Tileset tileset = new Tileset(num_tiles, bpp, bitplane_format);
    
    // set the initial pixel coordinates and bitplane
    int tile = 0, x = 0, y = 0, bitplane = 0;
    // serial encoding starts with the MSB, unlike planar and intertwined
    if (tileset.getBitplaneFormat() == Tileset.BITPLANE_SERIAL) {
      bitplane = tileset.getBPP() - 1;
    }
    
    // begin decoding each bit we need to
    for (int bit = 0; bit < num_bits; bit++) {
      // if the current bit is 1, set the corresponding bit in the tileset
      if ((tileset_data[bit / 8] & (1 << (bit % 8))) != 0) {
        int new_index = tileset.getPixelIndex(tile, x, y) | (1 << bitplane);
        tileset.setPixelIndex(tile, x, y, new_index);
      }  // else, there should be a 0 by default already (so just skip it)
      
      // advance the pixel coordinates and bitplane based on the bitplane format
      switch (tileset.getBitplaneFormat()) {
        // for serial: bitplane (MSB to LSB) -> x -> y -> tile
        case Tileset.BITPLANE_SERIAL:
          bitplane--;
          if (bitplane < 0) { bitplane = tileset.getBPP() - 1; x++; }
          if (x >= Tileset.TILE_WIDTH) { x = 0; y++; }
          if (y >= Tileset.TILE_HEIGHT) { y = 0; tile++; }
          break;
        // for planar: x -> y -> bitplane (LSB to MSB) -> tile
        case Tileset.BITPLANE_PLANAR:
          x++;
          if (x >= Tileset.TILE_WIDTH) { x = 0; y++; }
          if (y >= Tileset.TILE_HEIGHT) { y = 0; bitplane++; }
          if (bitplane >= tileset.getBPP()) { bitplane = 0; tile++; }
          break;
        // for intertwined: x -> bitplane (LSB to MSB) -> y -> tile
        case Tileset.BITPLANE_INTERTWINED:
          x++;
          if (x >= Tileset.TILE_WIDTH) { x = 0; bitplane++; }
          if (bitplane >= tileset.getBPP()) { bitplane = 0; y++; }
          if (y >= Tileset.TILE_HEIGHT) { y = 0; tile++; }
          break;
      }
    }
    
    return tileset;
  }
}
