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
 * A collection of methods which can be used to transform and create tilesets.
 * A tileset can be reinterpreted into a different format, or a tileset could be
 * encoded into binary data or decoded from binary data.
 * 
 * @author Nicholas Lovdahl
 * 
 * @see Tileset
 */
public final class TilesetInterpreter {
  /**
   * Takes an existing tileset and reinterprets it to create a new tileset with
   * the specified number of bits per pixel and tileset format. If both the
   * number of bits per pixel and tileset format are the same as those of the
   * given tileset, then a new tileset which is a deep copy of the tileset will
   * be returned.
   * 
   * @param tileset the tileset to be reinterpreted.
   * @param bpp the number of bits per pixel that the tileset should be
   *        reinterpreted with.
   * @param tileset_format the tileset format that the tileset should be
   *        reinterpreted with.
   * @return a new tileset with the specified number of bits per pixel and
   *         tileset format.
   * @throws NullPointerException if the given tileset is null.
   * @throws IllegalArgumentException if bpp or tileset_format are invalid per
   *         {@link Tileset#isValidBPP(int)} and
   *         {@link Tileset#isValidTilesetFormat(int)}, respectively.
   */
  public static Tileset reinterpretTileset(Tileset tileset,
                                           int bpp, int tileset_format) {
    if (tileset == null) {
      throw new NullPointerException("Cannot reinterpret a null tileset.");
    } else if (!Tileset.isValidBPP(bpp)) {
      throw new IllegalArgumentException("Invalid BPP value.");
    } else if (!Tileset.isValidTilesetFormat(tileset_format)) {
      throw new IllegalArgumentException("Invalid tileset format.");
    }  // else, we should have valid arguments
    
    // return a deep copy of the tileset if it already matches the given specs
    if (tileset.getBPP() == bpp &&
        tileset.getTilesetFormat() == tileset_format) {
      return new Tileset(tileset);
    } else {  // else, reinterpret the tileset
      return decodeBytes(encodeTileset(tileset), bpp, tileset_format);
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
      throw new NullPointerException("Cannot encode a null tileset.");
    }  // else, the tileset should be valid
    
    TilesetEncoderDecoder encoder;
    int bpp = tileset.getBPP();
    // select the appropriate encoder based on the tileset format
    switch (tileset.getTilesetFormat()) {
      case Tileset.SERIAL_FORMAT:
        encoder = new SerialFormatEncoderDecoder(bpp);
        break;
      case Tileset.PLANAR_FORMAT:
        encoder = new PlanarFormatEncoderDecoder(bpp);
        break;
      case Tileset.LINEAR_INTERTWINED_FORMAT:
        encoder = new LinearIntertwinedFormatEncoderDecoder(bpp);
        break;
      case Tileset.PAIRED_INTERTWINED_FORMAT:
        encoder = new PairedIntertwinedFormatEncoderDecoder(bpp);
        break;
      default:
        throw new RuntimeException("Unexpected tileset format.");
    }
    
    return encoder.encode(tileset);
  }
  
  /**
   * Takes an array of bytes and interprets them to create a tileset with the
   * specified number of bits per pixel and tileset format. The given tileset
   * data can be reconstructed using
   * {@link #encodeTileset(io.github.nlovdahl.geps.Tileset)}.
   * 
   * If there are extra bytes which cannot be decoded into a whole tile, then
   * the tileset returned will be padded with enough extra data so that there is
   * a whole number of tiles. The padded part of the tileset will have no
   * pattern such that if the tileset is encoded again, then any extra bytes
   * will be zeroes.
   * 
   * @param tileset_data the bytes to be interpreted.
   * @param bpp the number of bits per pixel used for interpretation.
   * @param tileset_format the tileset format used to interpretation.
   * @return the resulting tileset interpreted from the given bytes, using the
   *         specified number of bits per pixel, and tileset format.
   * @throws NullPointerException if tileset_data is null.
   * @throws IllegalArgumentException if tileset_data has a length of less than
   *         one, or if bpp or tileset_format are invalid per
   *         {@link Tileset#isValidBPP(int)} and
   *         {@link Tileset#isValidTilesetFormat(int)}, respectively.
   */
  public static Tileset decodeBytes(byte[] tileset_data,
                                    int bpp, int tileset_format) {
    if (tileset_data == null) {
      throw new NullPointerException("Cannot interpret null byte array.");
    } else if (tileset_data.length < 1) {
      throw new IllegalArgumentException("Cannot interpret empty byte array.");
    } else if (!Tileset.isValidBPP(bpp)) {
      throw new IllegalArgumentException("Invalid BPP value.");
    } else if (!Tileset.isValidTilesetFormat(tileset_format)) {
      throw new IllegalArgumentException("Invalid tileset format value.");
    }  // else, we should have valid arguments
    
    TilesetEncoderDecoder decoder;
    // select the appropriate decoder based on the tileset format
    switch (tileset_format) {
      case Tileset.SERIAL_FORMAT:
        decoder = new SerialFormatEncoderDecoder(bpp);
        break;
      case Tileset.PLANAR_FORMAT:
        decoder = new PlanarFormatEncoderDecoder(bpp);
        break;
      case Tileset.LINEAR_INTERTWINED_FORMAT:
        decoder = new LinearIntertwinedFormatEncoderDecoder(bpp);
        break;
      case Tileset.PAIRED_INTERTWINED_FORMAT:
        decoder = new PairedIntertwinedFormatEncoderDecoder(bpp);
        break;
      default:
        throw new RuntimeException("Unexpected tileset format.");
    }
    
    return decoder.decode(tileset_data);
  }
  
  /** A template class for both encoding and decoding tilesets. */
  private static abstract class TilesetEncoderDecoder {
    /**
     * Sets the number of bits per pixel and tileset format to be used, as well
     * as the initial values for the tile, x, y, and bitplane when encoding or
     * decoding.
     * 
     * @param bpp the number of bits per pixel of the tileset or tilesets to be
     *        encoded or decoded.
     * @param tileset_format the tileset format of the tileset or tilesets to be
     *        encoded or decoded.
     * @param initial_tile the initial tile when encoding or decoding.
     * @param initial_x the initial x value in a tile when encoding or decoding.
     * @param initial_y the initial y value in a tile when encoding or decoding.
     * @param initial_bitplane the initial bitplane when encoding or decoding.
     */
    public TilesetEncoderDecoder(
        int bpp, int tileset_format,
        int initial_tile, int initial_x, int initial_y, int initial_bitplane) {
      bpp_ = bpp;
      tileset_format_ = tileset_format;
      initial_tile_ = initial_tile;
      initial_x_ = initial_x;
      initial_y_ = initial_y;
      initial_bitplane_ = initial_bitplane;
    }
    
    /**
     * Encodes the given tileset into an array of bytes.
     * 
     * @param tileset the tileset to encode.
     * @return the array of bytes resulting from the encoding of the tileset.
     */
    public byte[] encode(Tileset tileset) {
      // the number of tiles is limited such that num_bits will fit in an int
      int num_bits = tileset.getNumberOfTiles() * Tileset.bitsPerTile(bpp_);
      int num_bytes = num_bits / 8;
      // add an extra byte if there will be remainder bits (but not a full byte)
      if (num_bits % 8 != 0) { num_bytes++; }
      
      byte[] tileset_data = new byte[num_bytes];
      
      initializeCoordinates();
      // begin encoding each bit we need to
      for (int bit = 0; bit < num_bits; bit++) {
        // if the bit for the chosen bitplane in the pixel index is 1...
        if ((tileset.getPixelIndex(tile_, x_, y_) & (1 << bitplane_)) != 0) {
          tileset_data[bit / 8] |= (128 >> (bit % 8));  // ... then encode a 1
        }
        
        incrementCoordinates();
      }
      
      return tileset_data;
    }
    
    /**
     * Decodes the given array of bytes into a tileset.
     * 
     * @param tileset_data the array of bytes to decode.
     * @return the tileset resulting from the decoding of the given data.
     */
    public Tileset decode(byte[] tileset_data) {
      // the number of tiles is limited such that num_bits will fit in an int
      int num_bits = tileset_data.length * 8;
      int bits_per_tile = Tileset.bitsPerTile(bpp_);
      int num_tiles = num_bits / bits_per_tile;
      // add an extra tile if there will be remainder bits (but not a full tile)
      if (num_bits % bits_per_tile != 0) { num_tiles++; }
      
      Tileset tileset = new Tileset(num_tiles, bpp_, tileset_format_);
      
      initializeCoordinates();
      
      // begin decoding each bit we need to
      for (int bit = 0; bit < num_bits; bit++) {
        // if the current bit is a 1...
        if ((tileset_data[bit / 8] & (128 >> (bit % 8))) != 0) {
          // ... then set the corresponding bit in the tileset
          int new_index = tileset.getPixelIndex(tile_, x_, y_) |
                          (1 << bitplane_);
          tileset.setPixelIndex(tile_, x_, y_, new_index);
        }  // else, there should be a 0 by default (so we can just skip it)
        
        incrementCoordinates();
      }
      
      return tileset;
    }
    
    /**
     * Advances the coordinates (tile, x, y, and bitplane) for the next bit.
     * This is the only method that needs to be completed by an extending class.
     */
    protected abstract void incrementCoordinates();
    
    private void initializeCoordinates() {
      tile_ = initial_tile_;
      x_ = initial_x_;
      y_ = initial_y_;
      bitplane_ = initial_bitplane_;
    }
    
    // coordinates used during encoding and decoding
    protected int tile_;
    protected int x_;
    protected int y_;
    protected int bitplane_;
    // bpp and tileset format
    protected final int bpp_;
    protected final int tileset_format_;
    // initial coordinate values
    protected final int initial_tile_;
    protected final int initial_x_;
    protected final int initial_y_;
    protected final int initial_bitplane_;
  }
  
  /** A class that can encode and decode tilesets using the
   {@link Tileset#SERIAL_FORMAT} tileset format. */
  private static final class SerialFormatEncoderDecoder
      extends TilesetEncoderDecoder {
    /**
     * Performs the initial setup needed before encoding or decoding tilesets
     * using the {@link Tileset#SERIAL_FORMAT} tileset format and the given
     * number of bits per pixel.
     * 
     * @param bpp the number of bits per pixel of the tileset or tilesets to be
     *        encoded or decoded.
     */
    public SerialFormatEncoderDecoder(int bpp) {
      // start at tile = 0, x = 0, y = 0, bitplane = bpp - 1 (most significant)
      super(bpp, Tileset.SERIAL_FORMAT, 0, 0, 0, bpp - 1);
    }
    
    @Override
    protected void incrementCoordinates() {
      // bitplane (MSB to LSB) -> x -> y -> tile
      bitplane_--;
      if (bitplane_ < 0) { bitplane_ = initial_bitplane_; x_++; }
      if (x_ >= Tileset.TILE_WIDTH) { x_ = 0; y_++; }
      if (y_ >= Tileset.TILE_HEIGHT) { y_ = 0; tile_++; }
    }
  }
  
  /** A class that can encode and decode tilesets using the
   {@link Tileset#PLANAR_FORMAT} tileset format. */
  private static final class PlanarFormatEncoderDecoder
      extends TilesetEncoderDecoder {
    /**
     * Performs the initial setup needed before encoding or decoding tilesets
     * using the {@link Tileset#PLANAR_FORMAT} tileset format and the given
     * number of bits per pixel.
     * 
     * @param bpp the number of bits per pixel of the tileset or tilesets to be
     *        encoded or decoded.
     */
    public PlanarFormatEncoderDecoder(int bpp) {
      // start at tile = 0, x = 0, y = 0, bitplane = 0 (least significant)
      super(bpp, Tileset.PLANAR_FORMAT, 0, 0, 0, 0);
    }
    
    @Override
    protected void incrementCoordinates() {
      // x -> y -> bitplane(LSB to MSB) -> tile
      x_++;
      if (x_ >= Tileset.TILE_WIDTH) { x_ = 0; y_++; }
      if (y_ >= Tileset.TILE_HEIGHT) { y_ = 0; bitplane_++; }
      if (bitplane_ >= bpp_) { bitplane_ = 0; tile_++; }
    }
  }
  
  /** A class that can encode and decode tilesets using the
   {@link Tileset#LINEAR_INTERTWINED_FORMAT} tileset format. */
  private static final class LinearIntertwinedFormatEncoderDecoder
      extends TilesetEncoderDecoder {
    /**
     * Performs the initial setup needed before encoding or decoding tilesets
     * using the {@link Tileset#LINEAR_INTERTWINED_FORMAT} tileset format and
     * the given number of bits per pixel.
     * 
     * @param bpp the number of bits per pixel of the tileset or tilesets to be
     *        encoded or decoded.
     */
    public LinearIntertwinedFormatEncoderDecoder(int bpp) {
      // start at tile = 0, x = 0, y = 0, bitplane = 0 (least significant)
      super(bpp, Tileset.LINEAR_INTERTWINED_FORMAT, 0, 0, 0, 0);
    }
    
    @Override
    protected void incrementCoordinates() {
      // x -> bitplane (LSB to MSB) -> y -> tile
      x_++;
      if (x_ >= Tileset.TILE_WIDTH) { x_ = 0; bitplane_++; }
      if (bitplane_ >= bpp_) { bitplane_ = 0; y_++; }
      if (y_ >= Tileset.TILE_HEIGHT) { y_ = 0; tile_++; }
    }
  }
  
  /** A class that can encode and decode tilesets using the
   {@link Tileset#PAIRED_INTERTWINED_FORMAT} tileset format. */
  private static final class PairedIntertwinedFormatEncoderDecoder
      extends TilesetEncoderDecoder {
    /**
     * Performs the initial setup needed before encoding or decoding tilesets
     * using the {@link Tileset#PAIRED_INTERTWINED_FORMAT} tileset format and
     * the given number of bits per pixel.
     * 
     * @param bpp the number of bits per pixel of the tileset or tilesets to be
     *        encoded or decoded.
     */
    public PairedIntertwinedFormatEncoderDecoder(int bpp) {
      // start at tile = 0, x = 0, y = 0, bitplane = 0 (least significant)
      super(bpp, Tileset.PAIRED_INTERTWINED_FORMAT, 0, 0, 0, 0);
    }
    
    @Override
    protected void incrementCoordinates() {
      // x -> bitplane (LSB to MSB, within pair) -> y -> bitplane pair -> tile
      x_++;
      if (x_ >= Tileset.TILE_WIDTH) {
        x_ = 0;
        
        if (bitplane_ % 2 == 0) {
          if (bitplane_ == bpp_ - 1) {
            y_++;
          } else {
            bitplane_++;
          }
        } else {
          bitplane_--;
          y_++;
        }
      }
      if (y_ >= Tileset.TILE_HEIGHT) {
        y_ = 0;
        bitplane_ += 2;
      }
      if (bitplane_ >= bpp_) { bitplane_ = 0; tile_++; }
    }
  }
}
