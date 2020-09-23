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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.Random;
import java.util.ArrayList;
import java.util.stream.Stream;
import java.nio.file.Files;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Unit tests for {@link TilesetInterpreter}.
 * 
 * @author Nicholas Lovdahl
 */
public class TilesetInterpreterTest {
  /** Loads and prepares resources used for tests. */
  public TilesetInterpreterTest() {
    byte[] test_pattern_data;
    Tileset test_pattern_tileset;
    
    // try with resources to create an input stream
    try (FileInputStream test_pattern_stream =
           new FileInputStream(TestValues.TEST_TILESET_PATTERN_PATH)) {
      test_pattern_data = test_pattern_stream.readAllBytes();
    } catch (Exception exception) {
      test_pattern_data = null;
    }  // input stream should auto-close since we use try with resources
    
    // try to decode the data into a tileset
    try {
      test_pattern_tileset = TilesetInterpreter.decodeBytes(
        test_pattern_data,
        TEST_PATTERN_TILESET_BPP, TEST_PATTERN_TILESET_FORMAT_VALUE
      );
    } catch (Exception exception) {
      test_pattern_tileset = null;
    }
    
    test_pattern_data_ = test_pattern_data;
    test_pattern_tileset_ = test_pattern_tileset;
  }
  
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
  
  /**
   * A series of regression tests which decodes test pattern data using the
   * given parameters and then encodes the resulting tileset. The indexes in
   * the decoded tileset and the data from the encoding are both compared to
   * expected values. The decoded tileset is also checked to see if it has the
   * expected number of bits per pixel and tileset format value.
   * 
   * @param bpp the number of bits per pixel to use for decoding.
   * @param format_value the tileset format value to use for decoding.
   */
  @ParameterizedTest(name = "Using BPP = {0} and format value = {1}")
  @MethodSource("combosOfBPPAndTilesetFormatValues")
  public void testDecodeEncodeRegression(int bpp, int format_value) {
    assertNotNull(test_pattern_data_,
                  "Unable to load test pattern data prior to test.");
    
    // this string will be used for naming / indentifying files for this test
    String base_name = "test_pattern_" + Integer.toString(bpp) + "_bpp_" +
                       Integer.toString(format_value) + "_format_value";
    
    // decode with the given parameters
    Tileset decoded_tileset =
      TilesetInterpreter.decodeBytes(test_pattern_data_, bpp, format_value);
    
    // check that the decoded tileset had the expected BPP and format
    assertAll(
      () -> assertEquals(bpp, decoded_tileset.getBPP(),
                         "Decoded tileset has the wrong BPP."),
      () -> assertEquals(format_value, decoded_tileset.getTilesetFormat(),
                         "Decoded tileset has the wrong format value.")
    );
    
    // write the actual index for the decoded tileset
    File decoded_output_file = new File(
      TestValues.ACTUAL_OUTPUT_PATH + base_name + "_indexes_decoded.dat"
    );
    try (DataOutputStream decoded_output_stream =
           new DataOutputStream(new FileOutputStream(decoded_output_file))) {
      for (int tile = 0;
           tile < decoded_tileset.getNumberOfTiles(); tile++) {
        for (int y = 0; y < Tileset.TILE_HEIGHT; y++) {
          for (int x = 0; x < Tileset.TILE_WIDTH; x++) {
            int index = decoded_tileset.getPixelIndex(tile, x, y);
            decoded_output_stream.writeInt(index);
          }
        }
      }
    } catch (Exception exception) {
      fail("Exception while writing decoded indexes. " + exception.toString());
    }
    // compare with the expected indexes for the decoded file
    File decoded_input_file = new File(
      TestValues.EXPECTED_OUTPUT_PATH + base_name + "_indexes.dat"
    );
    try {
      long mismatch_start = Files.mismatch(decoded_output_file.toPath(),
                                           decoded_input_file.toPath());
      assertEquals(-1, mismatch_start,
                   "Decoded indexes mismatch starting at " +
                   Long.toString(mismatch_start) + " bytes in.");
    } catch (IOException exception) {
      fail("Exception while comparing expected and actual decoded indexes. " +
           exception.toString());
    }
    
    // encode the tileset
    byte[] encoded_tileset_data =
      TilesetInterpreter.encodeTileset(decoded_tileset);
    
    // write the actual data for the encoded tileset
    File encoded_output_file = new File(
      TestValues.ACTUAL_OUTPUT_PATH + base_name + "_decoded.chr"
    );
    try (FileOutputStream encoded_output_stream =
           new FileOutputStream(encoded_output_file)) {
      encoded_output_stream.write(encoded_tileset_data);
    } catch (Exception exception) {
      fail("Exception while writing encoded tileset data. " +
           exception.toString());
    }
    
    // check that the encoded data has the expected number of bytes
    int original_num_bits = test_pattern_data_.length * 8;
    int expected_num_tiles = original_num_bits / Tileset.bitsPerTile(bpp);
    // if we have extra bits, but not enough for a whole tile, we expect padding
    if (original_num_bits % Tileset.bitsPerTile(bpp) != 0) {
      expected_num_tiles++;  // padding should only add a single tile
    }
    int expected_num_bits = expected_num_tiles * Tileset.bitsPerTile(bpp);
    int expected_num_bytes = expected_num_bits / 8;
    if (expected_num_bits % 8 != 0) { expected_num_bytes++; }
    
    assertEquals(expected_num_bytes, encoded_tileset_data.length,
                 "The encoded tileset is an unexpected size.");
    
    // then check that the encoded tileset matches alongside the original data
    for (int index = 0; index < test_pattern_data_.length; index++) {
      assertEquals(test_pattern_data_[index], encoded_tileset_data[index],
                   "Encoded tileset did not preserve original data. Mismatch " +
                   "found at " + Integer.toString(index) + " bytes in.");
    }
    // check that any extra data (from padding) is all zeroes
    for (int index = test_pattern_data_.length;
         index < encoded_tileset_data.length; index++) {
      assertEquals(0, encoded_tileset_data[index],
                   "Encoded tileset has non-zero padding bytes at " +
                   Integer.toString(index) + " bytes in.");
    }
  }
  
  /**
   * A series of regression tests which reinterpretes a decoded test pattern
   * tileset and then encodes the resulting tileset. The indexes in the
   * reinterpreted tileset and the data from the encoding are both compared to
   * expected values. The reinterpretted tileset is also checked to see if it
   * has the expected number of bits per pixel and tileset format value.
   * 
   * Since decoding, reinterpreting, and encoding should not change the
   * underlying data - with the exception of padding taking place - there is no
   * expected file to compare to; it is expected that the original test pattern
   * data will be preserved.
   * 
   * @param bpp the number of bits per pixel to use for reinterpretation.
   * @param format_value the tileset format value to use for reinterpretation.
   */
  @ParameterizedTest(name = "Using BPP = {0} and format value = {1}")
  @MethodSource("combosOfBPPAndTilesetFormatValues")
  public void testDecodeReinterpretEncodeRegression(int bpp, int format_value) {
    assertNotNull(test_pattern_tileset_,
                  "Unable to decode test pattern tileset prior to test.");
    
    // this string will be used for naming / indentifying files for this test
    String base_name = "test_pattern_" + Integer.toString(bpp) + "_bpp_" +
                       Integer.toString(format_value) + "_format_value";
    
    // reinterpret with the given parameters
    Tileset reinterpreted_tileset =
      TilesetInterpreter.reinterpretTileset(test_pattern_tileset_,
                                            bpp, format_value);
    
    // check that the reinterpreted tileset has the expected BPP and format
    assertAll(
      () -> assertEquals(bpp, reinterpreted_tileset.getBPP(),
                         "Reinterpreted tileset has the wrong BPP."),
      () -> assertEquals(format_value, reinterpreted_tileset.getTilesetFormat(),
                         "Reinterpreted tileset has the wrong format value.")
    );
    
    // write the actual indexes for the reinterpreted tileset
    File reint_output_file = new File(
      TestValues.ACTUAL_OUTPUT_PATH + base_name + "_indexes_reinterpreted.dat"
    );
    try (DataOutputStream reint_output_stream =
           new DataOutputStream(new FileOutputStream(reint_output_file))) {
      for (int tile = 0;
           tile < reinterpreted_tileset.getNumberOfTiles(); tile++) {
        for (int y = 0; y < Tileset.TILE_HEIGHT; y++) {
          for (int x = 0; x < Tileset.TILE_WIDTH; x++) {
            int index = reinterpreted_tileset.getPixelIndex(tile, x, y);
            reint_output_stream.writeInt(index);
          }
        }
      }
    } catch (Exception exception) {
      fail("Exception while writing reinterpreted indexes. " +
           exception.toString());
    }
    // compare with the expected indexes for the reinterpreted tileset
    File reint_input_file = new File(
      TestValues.EXPECTED_OUTPUT_PATH + base_name + "_indexes.dat"
    );
    try {
      long mismatch_start = Files.mismatch(reint_output_file.toPath(),
                                           reint_input_file.toPath());
      assertEquals(
        -1, mismatch_start,
        "Reinterpreted indexes mistmatch starting at " +
        Long.toString(mismatch_start) + " bytes in."
      );
    } catch (IOException exception) {
      fail("Exception while comparing expected and actual reinterpreted " +
           "indexes. " + exception.toString());
    }
    
    // encode the tileset
    byte[] encoded_tileset_data =
      TilesetInterpreter.encodeTileset(reinterpreted_tileset);
    
    // write the actual data for the encoded tileset
    File encoded_output_file = new File(
      TestValues.ACTUAL_OUTPUT_PATH + base_name + "_reinterpreted.chr"
    );
    try (FileOutputStream encoded_output_stream =
           new FileOutputStream(encoded_output_file)) {
      encoded_output_stream.write(encoded_tileset_data);
    } catch (Exception exception) {
      fail("Exception while writing encoded tileset data. " +
           exception.toString());
    }
    
    // check that the encoded data has the expected number of bytes
    int original_num_bits = test_pattern_data_.length * 8;
    int expected_num_tiles = original_num_bits / Tileset.bitsPerTile(bpp);
    // if we have extra bits, but not enough for a whole tile, we expect padding
    if (original_num_bits % Tileset.bitsPerTile(bpp) != 0) {
      expected_num_tiles++;  // padding should only add a single tile
    }
    int expected_num_bits = expected_num_tiles * Tileset.bitsPerTile(bpp);
    int expected_num_bytes = expected_num_bits / 8;
    if (expected_num_bits % 8 != 0) { expected_num_bytes++; }
    
    assertEquals(expected_num_bytes, encoded_tileset_data.length,
                 "The encoded tileset is an unexpected size.");
    
    // then check that the encoded tileset matches alongside the original data
    for (int index = 0; index < test_pattern_data_.length; index++) {
      assertEquals(test_pattern_data_[index], encoded_tileset_data[index],
                   "Encoded tileset did not preserve original data. Mismatch " +
                   "found at " + Integer.toString(index) + " bytes in.");
    }
    // check that any extra data (from padding) is all zeroes
    for (int index = test_pattern_data_.length;
         index < encoded_tileset_data.length; index++) {
      assertEquals(0, encoded_tileset_data[index],
                   "Encoded tileset has non-zero padding bytes at " +
                   Integer.toString(index) + " bytes in.");
    }
  }
  
  /**
   * Creates a stream of arguments formed by every combination of valid bits per
   * pixel and tileset format values. The first part of an argument is the
   * number of bits per pixel. The second part is the tileset format value.
   * 
   * @return a stream of arguments of combinations of bits per pixel and tileset
   *         format values.
   */
  private static Stream<Arguments> combosOfBPPAndTilesetFormatValues() {
    List<Arguments> combos =
      new ArrayList<>((Tileset.MAX_BPP - Tileset.MIN_BPP) *
                      TestValues.TILESET_FORMAT_VALUES.size());
    
    for (int bpp = Tileset.MIN_BPP; bpp <= Tileset.MAX_BPP; bpp++) {
      for (int format : TestValues.TILESET_FORMAT_VALUES) {
        combos.add(Arguments.of(bpp, format));
      }
    }
    
    return combos.stream();
  }
  
  /** The number of bits per pixel to use when decoding the test pattern. It is
   presumed that there will be no padding for the decoded test pattern tileset.
   This value was chosen with this requirement in mind. */
  private static final int TEST_PATTERN_TILESET_BPP = 8;
  /** The tileset format value to use when decoding the test pattern. */
  private static final int TEST_PATTERN_TILESET_FORMAT_VALUE =
    Tileset.SERIAL_FORMAT;
  
  private final byte[] test_pattern_data_;
  private final Tileset test_pattern_tileset_;
}