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

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.List;
import java.util.Random;

/**
 * Unit tests for {@link TilesetInterpreter}.
 * 
 * @author Nicholas Lovdahl
 */
public class TilesetInterpreterTest {
  /**
   * When the {@link
   * TilesetInterpreter#reinterpretTileset(io.github.nlovdahl.geps.Tileset,
   * int, int)} method is asked to reinterpret a tileset using the same number
   * of bits per pixel and tileset format as the tileset already has, then the
   * tileset returned should have an identical pattern to the original tileset.
   */
  @Test
  public void testReinterpretTilesetIdenticalForSameProperties() {
    Random random = new Random();  // used to generate random indexes
    
    for (int tileset_size : TestValues.SELECT_VALID_TILESET_SIZES) {
      // test the full range of combinations of BPPs and tileset format values
      for (int format : TestValues.TILESET_FORMAT_VALUES) {
        for (int bpp = Tileset.MIN_BPP; bpp <= Tileset.MAX_BPP; bpp++) {
          int index_bound = 1 << bpp;  // 2^bpp
          
          // create a tileset to copy and set its indexes to hold random values
          Tileset tileset = new Tileset(tileset_size, bpp, format);
          for (int tile = 0; tile < tileset_size; tile++) {
            for (int y = 0; y < Tileset.TILE_HEIGHT; y++) {
              for (int x = 0; x < Tileset.TILE_WIDTH; x++) {
                tileset.setPixelIndex(tile, x, y, random.nextInt(index_bound));
              }
            }
          }
          
          Tileset reinterpreted_tileset =
            TilesetInterpreter.reinterpretTileset(tileset, bpp, format);
          
          // check that the reinterpreted tileset has all the same properties
          assertAll(
            () -> assertEquals(
                    tileset_size, reinterpreted_tileset.getNumberOfTiles(),
                    "Reinterpreted tileset has an unexpected size."
                  ),
            () -> assertEquals(
                    tileset.getBPP(), reinterpreted_tileset.getBPP(),
                    "Reinterpreted tileset has an unexpected BPP."
                  ),
            () -> assertEquals(
                    tileset.getTilesetFormat(),
                    reinterpreted_tileset.getTilesetFormat(),
                    "Reinterpreted tileset has an unexpected tileset format."
                  )
          );
          // check that the pattern of the reinterpreted tileset is the same too
          for (int tile = 0; tile < tileset_size; tile++) {
            for (int y = 0; y < Tileset.TILE_HEIGHT; y++) {
              for (int x = 0; x < Tileset.TILE_WIDTH; x++) {
                assertEquals(
                  tileset.getPixelIndex(tile, x, y),
                  reinterpreted_tileset.getPixelIndex(tile, x, y),
                  "The reinterpreted tileset has a different index at " +
                  "coordinates (tile = " + Integer.toString(tile) + ", x = " +
                  Integer.toString(x) + ", y = " + Integer.toString(y) + ")."
                );
              }
            }
          }
        }
      }
    }
  }
  
  /**
   * When the {@link
   * TilesetInterpreter#reinterpretTileset(io.github.nlovdahl.geps.Tileset,
   * int, int)} method is asked to reinterpret a tileset using the same number
   * of bits per pixel and tileset format as the tileset already has, then a
   * deep copy of the original tileset should be returned. That is, changes to
   * either the original or the copied tileset are not reflected in the other.
   */
  @Test
  public void testReinterpretTilesetDeepCopyForSameProperties() {
    Random random = new Random();  // used to generate random indexes
    
    // test the full range of combinations of BPPs and tileset format values
    for (int format : TestValues.TILESET_FORMAT_VALUES) {
      for (int bpp = Tileset.MIN_BPP; bpp <= Tileset.MAX_BPP; bpp++) {
        int index_bound = 1 << bpp;  // 2^bpp
        int[][][] original_indexes =  // [tile][row][column]
          new int[TestValues.TEST_TILESET_SIZE]
                 [Tileset.TILE_HEIGHT][Tileset.TILE_WIDTH];
        
        // fill a tileset with random indexes - keep a copy of these indexes
        Tileset tileset = new Tileset(TestValues.TEST_TILESET_SIZE,
                                      bpp, format);
        for (int tile = 0; tile < TestValues.TEST_TILESET_SIZE; tile++) {
          for (int y = 0; y < Tileset.TILE_HEIGHT; y++) {
            for (int x = 0; x < Tileset.TILE_WIDTH; x++) {
              int index = random.nextInt(index_bound);
              original_indexes[tile][y][x] = index;
              tileset.setPixelIndex(tile, x, y, index);
            }
          }
        }
        // get the reinterpreted tileset with the same properties
        Tileset reinterpreted_tileset =
          TilesetInterpreter.reinterpretTileset(tileset, bpp, format);
        
        // change the reinterpreted tileset (just shift the indexes)
        for (int tile = 0; tile < TestValues.TEST_TILESET_SIZE; tile++) {
          for (int y = 0; y < Tileset.TILE_HEIGHT; y++) {
            for (int x = 0; x < Tileset.TILE_WIDTH; x++) {
              reinterpreted_tileset.setPixelIndex(
                tile, x, y,
                (reinterpreted_tileset.getPixelIndex(tile, x, y) + 1) %
                index_bound
              );
            }
          }
        }
        // check to see if the orignal tileset has been changed
        for (int tile = 0; tile < TestValues.TEST_TILESET_SIZE; tile++) {
          for (int y = 0; y < Tileset.TILE_HEIGHT; y++) {
            for (int x = 0; x < Tileset.TILE_WIDTH; x++) {
              assertEquals(
                original_indexes[tile][y][x],
                tileset.getPixelIndex(tile, x, y),
                "The original tileset has a changed index at coordinates " +
                "(tile = " + Integer.toString(tile) + ", x = " +
                Integer.toString(x) + ", y = " + Integer.toString(y) + ")."
              );
            }
          }
        }
        
        reinterpreted_tileset =  // reset reinterpreted tileset
          TilesetInterpreter.reinterpretTileset(tileset, bpp, format);
        
        // change the original tileset (just shifting indexes again)
        for (int tile = 0; tile < TestValues.TEST_TILESET_SIZE; tile++) {
          for (int y = 0; y < Tileset.TILE_HEIGHT; y++) {
            for (int x = 0; x < Tileset.TILE_WIDTH; x++) {
              tileset.setPixelIndex(
                tile, x, y,
                (tileset.getPixelIndex(tile, x, y) + 1) % index_bound
              );
            }
          }
        }
        // check to see if the copied tileset has been changed
        for (int tile = 0; tile < TestValues.TEST_TILESET_SIZE; tile++) {
          for (int y = 0; y < Tileset.TILE_HEIGHT; y++) {
            for (int x = 0; x < Tileset.TILE_WIDTH; x++) {
              assertEquals(
                original_indexes[tile][y][x],
                reinterpreted_tileset.getPixelIndex(tile, x, y),
                "The reinterpreted tileset has a changed index at " +
                "coordinates (tile = " + Integer.toString(tile) + ", x = " +
                Integer.toString(x) + ", y = " + Integer.toString(y) + ")."
              );
            }
          }
        }
      }
    }
  }
  
  /**
   * The {@link
   * TilesetInterpreter#reinterpretTileset(io.github.nlovdahl.geps.Tileset,
   * int, int)} method should accept valid number of bits per pixel and tileset
   * format values, but throw the appropriate exception for invalid values.
   */
  @Test
  public void testReinterpretTilesetArguments() {
    // the tileset that will be used for testing (no pattern)
    Tileset tileset = new Tileset(
      TestValues.TEST_TILESET_SIZE, TestValues.TEST_BPP,
      TestValues.TEST_TILESET_FORMAT_VALUE
    );
    
    for (int bpp : TestValues.TEST_BPPS) {
      for (int format : TestValues.TEST_TILESET_FORMAT_VALUES) {
        // if the arguments are all valid...
        if (bpp >= Tileset.MIN_BPP && bpp <= Tileset.MAX_BPP &&
            TestValues.TILESET_FORMAT_VALUES.contains(format)) {
          assertDoesNotThrow(
            () -> TilesetInterpreter.reinterpretTileset(tileset, bpp, format),
            "An exception was thrown when reinterpreting a tileset going " +
            "from " + Integer.toString(tileset.getBPP()) + " to " +
            Integer.toString(bpp) + " BPP and a tileset format value of " +
            Integer.toString(tileset.getTilesetFormat()) + " to " +
            Integer.toString(format) + "."
          );
        } else {  // else, we expect an exception for invalid arguments
          assertThrows(
            IllegalArgumentException.class,
            () -> TilesetInterpreter.reinterpretTileset(tileset, bpp, format),
            "No IllegalArgumentException was thrown when reinterpreting a " +
            "tileset going from " + Integer.toString(tileset.getBPP()) +
            " to " + Integer.toString(bpp) + " BPP and a tileset format " +
            "value of " + Integer.toString(tileset.getTilesetFormat()) +
            " to " + Integer.toString(format) + "."
          );
        }
      }
    }
  }
  
  /**
   * The {@link
   * TilesetInterpreter#reinterpretTileset(io.github.nlovdahl.geps.Tileset,
   * int, int)} method should throw the appropriate exception if given a null
   * value for a tileset to reinterpret.
   */
  @Test
  public void testReinterpretTilesetNullTilesetArgument() {
    assertThrows(
      NullPointerException.class,
      () -> TilesetInterpreter.reinterpretTileset(
              null, TestValues.TEST_BPP, TestValues.TEST_TILESET_FORMAT_VALUE
            ),
      "No NullPointerException was thrown for a null tileset argument."
    );
  }
  
  /**
   * The
   * {@link TilesetInterpreter#encodeTileset(io.github.nlovdahl.geps.Tileset)}
   * method should throw the appropriate exception if given a null value for a
   * tileset to encode.
   */
  @Test
  public void testEncodeTilesetNullTilesetArgument() {
    assertThrows(
      NullPointerException.class,
      () -> TilesetInterpreter.encodeTileset(null),
      "No NullPointerException was thrown for a null argument."
    );
  }
  
  /**
   * When the {@link TilesetInterpreter#decodeBytes(byte[], int, int)} method
   * is not given enough bytes to decode a single tile it should pad the tileset
   * with extra data to fill a tile. This padded part of the tile should have no
   * pattern such that when the tileset is encoded again, it is the same as when
   * it was decoded, but now with extra bytes which are all zeroes.
   */
  @Test
  public void testDecodePaddingSingleByte() {
    byte[] single_byte = { -1 };  // expect this to be 0xFF
    
    // test all BPP and format combinations
    for (int bpp = Tileset.MIN_BPP; bpp <= Tileset.MAX_BPP; bpp++) {
      int bits_per_tile = Tileset.bitsPerTile(bpp);
      // skip a bpp if partial tiles will be impossible
      if (bits_per_tile <= 8 && (bits_per_tile % 2) == 0) { break; }
      
      for (int format : TestValues.TILESET_FORMAT_VALUES) {
        // decode then encode to see what we get (make sure we get something)
        Tileset padded_tileset =
          TilesetInterpreter.decodeBytes(single_byte, bpp, format);
        assertNotNull(
          padded_tileset,
          "No tileset was returned by decoding. Using " +
          Integer.toString(bpp) + " BPP and a tileset format value of " +
          Integer.toString(format) + "."
        );
        byte[] padded_data = TilesetInterpreter.encodeTileset(padded_tileset);
        assertNotNull(
          padded_data,
          "No data was returned by encoding. Using " + Integer.toString(bpp) +
          " BPP and a tileset format value of " + Integer.toString(format) + "."
        );
        // also see that we have more data as a result of padding
        assertTrue(
          padded_data.length > 1,
          "No padding data was added. Using " + Integer.toString(bpp) +
          " BPP and a tileset format value of " + Integer.toString(format) + "."
        );
        
        // check that the first byte is the same
        assertEquals(
          -1, padded_data[0],
          "The single byte was not preserved. Using " + Integer.toString(bpp) +
          " BPP and a tileset format value of " + Integer.toString(format) + "."
        );
        // then check that any remaining bytes are all zeroes
        for (int index = 1; index < padded_data.length; index++) {
          assertEquals(
            0, padded_data[index],
            "Padded bytes are non-zero at " + Integer.toString(index) +
            " bytes in. Using " + Integer.toString(bpp) +
            " BPP and a tileset format value of " + Integer.toString(format) +
            "."
          );
        }
      }
    }
  }
  
  /**
   * When the {@link TilesetInterpreter#decodeBytes(byte[], int, int)} method
   * has extra bytes - not enough bytes for another tile to be decoded - then it
   * should pad the tileset with extra data. This padded part of the tileset
   * should have no patten such that when the tileset is encoded again, it is
   * the same as when it was decoded, but now with extra bytes which are all
   * zeroes.
   */
  @Test
  public void testDecodePaddingTilesetWithPartialTile() {
    // test all BPP and format combinations
    for (int bpp = Tileset.MIN_BPP; bpp <= Tileset.MAX_BPP; bpp++) {
      int bits_per_tile = Tileset.bitsPerTile(bpp);
      // skip a bpp if partial tiles will be impossible
      if (bits_per_tile <= 8 && (bits_per_tile % 2) == 0) { break; }
      
      // find a number of tiles that will fit precisely into an array of bytes
      // bytes = lcm(bpt, 8) = (8 * bpt) / (8 * gcd(bpt, 8)) = bpt / gcd(bpt, 8)
      int gcd = 8;  // look through 8's divisors (all powers of 2)
      while (gcd > 1) {
        if (bits_per_tile % gcd == 0) { break; }  // if we find gcd, break
        gcd = gcd >> 1;  // otherwise, check the next divisor of 8
      }  // we will have the gcd by the time we exit this loop
      byte[] tileset_data = new byte[(bits_per_tile / gcd) + 1];  // +extra byte
      
      // fill the data with all ones (-1 should be 0xFF)
      for (int index = 0; index < tileset_data.length; index++) {
        tileset_data[index] = -1;
      }  // now we have data which will need padding
      
      for (int format : TestValues.TILESET_FORMAT_VALUES) {
        // decode then encode to see what we get (make sure we get something)
        Tileset padded_tileset =
          TilesetInterpreter.decodeBytes(tileset_data, bpp, format);
        assertNotNull(
          padded_tileset,
          "No tileset was returned by decoding. Using " +
          Integer.toString(bpp) + " BPP and a tileset format value of " +
          Integer.toString(format) + "."
        );
        byte[] padded_data = TilesetInterpreter.encodeTileset(padded_tileset);
        assertNotNull(
          padded_data,
          "No data was returned by encoding. Using " + Integer.toString(bpp) +
          " BPP and a tileset format value of " + Integer.toString(format) + "."
        );
        // also see that we have more data as a result of padding
        assertTrue(
          padded_data.length > tileset_data.length,
          "No padding data was added. Using " + Integer.toString(bpp) +
          " BPP and a tileset format value of " + Integer.toString(format) + "."
        );
        
        // check that all of the original bytes are the same
        for (int index = 0; index < tileset_data.length; index++) {
          assertEquals(
            -1, padded_data[index],
            "The original tileset data was not preserved. Mismatch found " +
            Integer.toString(index) + " bytes in. Using " +
            Integer.toString(bpp) + " BPP and a tileset format value of " +
            Integer.toString(format) + "."
          );
        }
        // then check that any remaining bytes are all zeroes
        for (int index = tileset_data.length;
                 index < padded_data.length; index++) {
          assertEquals(
            0, padded_data[index],
            "Padded bytes are non-zero at " + Integer.toString(index) +
            " bytes in. Using " + Integer.toString(bpp) +
            " BPP and a tileset format value of " + Integer.toString(format) +
            "."
          );
        }
      }
    }
  }
  
  /**
   * The {@link TilesetInterpreter#decodeBytes(byte[], int, int)} method should
   * accept valid values for the number of bits per pixel and tileset format
   * values, but throw the appropriate exception for invalid values.
   */
  @Test
  public void testDecodeTilesetArguments() {
    // a list of test tileset data to test with (no interesting patterns here)
    List<byte[]> test_tileset_data = List.of(
      new byte[0], new byte[1], new byte[256], new byte[1024]
    );
    
    for (byte[] tileset_data : test_tileset_data) {
      for (int bpp : TestValues.TEST_BPPS) {
        for (int format : TestValues.TEST_TILESET_FORMAT_VALUES) {
          // if the arguments are all valid...
          if (tileset_data.length > 0 &&
              bpp >= Tileset.MIN_BPP && bpp <= Tileset.MAX_BPP &&
              TestValues.TILESET_FORMAT_VALUES.contains(format)) {
            assertDoesNotThrow(
              () -> TilesetInterpreter.decodeBytes(tileset_data, bpp, format),
              "An exception was thrown decoding a tileset with " +
              Integer.toString(tileset_data.length) + " bytes, " +
              Integer.toString(bpp) + " BPP, and a format value of " +
              Integer.toString(format) + "."
            );
          } else {  // else, we expect an exception to be thrown
            assertThrows(
              IllegalArgumentException.class,
              () -> TilesetInterpreter.decodeBytes(tileset_data, bpp, format),
              "No IllegalArgumentException was thrown decoding a tileset " +
              "with " + tileset_data.length + " bytes, " +
              Integer.toString(bpp) + " BPP, and a format value of " +
              Integer.toString(format) + "."
            );
          }
        }
      }
    }
  }
  
  /**
   * The {@link TilesetInterpreter#decodeBytes(byte[], int, int)} method should
   * throw the appropriate exception if given a null value for the tileset data
   * to decode.
   */
  @Test
  public void testDecodeTilesetNullTilesetDataArgument() {
    assertThrows(
      NullPointerException.class,
      () -> TilesetInterpreter.decodeBytes(
              null, TestValues.TEST_BPP, TestValues.TEST_TILESET_FORMAT_VALUE
            ),
      "No NullPointerException was thrown for a null tileset argument."
    );
  }
}