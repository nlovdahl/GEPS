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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.List;
import java.util.Random;

/**
 * Unit tests for {@link Tileset}.
 * 
 * @author Nicholas Lovdahl
 */
public class TilesetTest {
  /**
   * If the {@link Tileset#bitsPerTile(int)} method is given an invalid number
   * of bits per pixel per {@link Tileset#isValidBPP(int)}, then it should
   * throw the appropriate exception.
   */
  @Test
  public void testBitsPerTileInvalidBPPs() {
    // a list of invalid BPPs to test with
    List<Integer> invalid_bpps = List.of(
      Tileset.MIN_BPP - 10, Tileset.MIN_BPP - 1,
      Tileset.MAX_BPP + 1, Tileset.MAX_BPP + 10
    );
    
    for (int bpp : invalid_bpps) {
      assertThrows(
        IllegalArgumentException.class,
        () -> Tileset.bitsPerTile(bpp),
        "No IllegalArgumentException was thrown for invalid BPP " +
        Integer.toString(bpp) + "."
      );
    }
  }
  
  /**
   * The {@link Tileset#bitsPerTile(int)} method should return the number of
   * bits needed to store a whole tile. This should be equal to the product of
   * the number of bits per pixel and the number of pixels in a tile.
   */
  @Test
  public void testBitsPerTile() {
    int pixels_per_tile = Tileset.TILE_WIDTH * Tileset.TILE_HEIGHT;
    // test the full range of valid BPPs
    for (int bpp = Tileset.MIN_BPP; bpp <= Tileset.MAX_BPP; bpp++) {
      assertEquals(bpp * pixels_per_tile, Tileset.bitsPerTile(bpp),
                   "Mismatch at " + Integer.toString(bpp) + " BPP.");
    }
  }
  
  /**
   * The {@link Tileset#isValidBPP(int)} method should return true for values
   * between {@link Tileset#MIN_BPP} and {@link Tileset#MAX_BPP} (inclusive) and
   * false otherwise.
   */
  @Test
  public void testIsValidBPP() {
    for (int bpp : TestValues.TEST_BPPS) {
      if (bpp >= Tileset.MIN_BPP && bpp <= Tileset.MAX_BPP) {
        assertTrue(Tileset.isValidBPP(bpp),
                   Integer.toString(bpp) + " should be a valid BPP.");
      } else {
        assertFalse(Tileset.isValidBPP(bpp),
                    Integer.toString(bpp) + " should be not be a valid BPP.");
      }
    }
  }
  
  /**
   * The {@link Tileset#isValidTilesetFormat(int)} method should return true
   * for values which correspond to a tileset format and false otherwise.
   */
  @Test
  public void testIsValidTilesetFormat() {
    for (int format : TestValues.TEST_TILESET_FORMAT_VALUES) {
      // if the value denotes a tileset format, then it should be valid
      if (TestValues.TILESET_FORMAT_VALUES.contains(format)) {
        assertTrue(Tileset.isValidTilesetFormat(format),
                   Integer.toString(format) + " should be valid.");
      } else {  // else, the value should not be a valid tileset format
        assertFalse(Tileset.isValidTilesetFormat(format),
                    Integer.toString(format) + " should be not be valid.");
      }
    }
  }
  
  /**
   * The {@link Tileset#getNumberOfTiles()} method should return the number of
   * tiles that was given to the tileset constructor. If this test fails, it
   * might also indicate that there is a problem with the tileset's constructor.
   */
  @Test
  public void testGetNumberOfTiles() {
    for (int tileset_size : TestValues.SELECT_VALID_TILESET_SIZES) {
      Tileset tileset = new Tileset(tileset_size, TestValues.TEST_BPP,
                                    TestValues.TEST_TILESET_FORMAT_VALUE);
      
      assertEquals(tileset_size, tileset.getNumberOfTiles(),
                   "The returned number of tiles does not match what was " +
                   "given to the constructor.");
    }
  }
  
  /**
   * The {@link Tileset#getBPP()} method should return the number of bits per
   * pixel that was given to the tileset constructor. If this test fails, it
   * might also indicate that there is a problem with the tileset's constructor.
   */
  @Test
  public void testGetBPP() {
    // test the full range of valid BPPs
    for (int bpp = Tileset.MIN_BPP; bpp <= Tileset.MAX_BPP; bpp++) {
      Tileset tileset = new Tileset(TestValues.TEST_TILESET_SIZE, bpp,
                                    TestValues.TEST_TILESET_FORMAT_VALUE);
      
      assertEquals(
        bpp, tileset.getBPP(),
        "The returned BPP does not match what was given to the constructor."
      );
    }
  }
  
  /**
   * The {@link Tileset#getTilesetFormat()} method should return the number
   * denoting the tileset format to be used that was given to the constructor.
   * If this test fails, it might also indicate that there is a problem with the
   * tileset's constructor.
   */
  @Test
  public void testGetTilesetFormat() {
    for (int format : TestValues.TILESET_FORMAT_VALUES) {
      Tileset tileset = new Tileset(TestValues.TEST_TILESET_SIZE,
                                    TestValues.TEST_BPP, format);
      
      assertEquals(format, tileset.getTilesetFormat(),
                   "The returned tileset format value does not match what " +
                   "was given to the constructor.");
    }
  }
  
  /**
   * The {@link Tileset#getPixelIndex(int, int, int)} method should accept valid
   * coordinates as arguments but return -1 for invalid coordinates. This
   * test checks that the index returned is within a possible range of indexes,
   * but does not actually test the correctness of the index returned.
   */
  @Test
  public void testGetPixelIndexCoordinates() {
    // create a tileset to test with
    Tileset tileset = new Tileset(
      TestValues.TEST_TILESET_SIZE, TestValues.TEST_BPP,
      TestValues.TEST_TILESET_FORMAT_VALUE
    );
    
    for (int tile : TestValues.TEST_TILE_NUMS) {
      for (int y : TestValues.TEST_Y_COORDS) {
        for (int x : TestValues.TEST_X_COORDS) {
          int index = tileset.getPixelIndex(tile, x, y);
          // if the coordinates should be valid...
          if (isValidCoordinates(TestValues.TEST_TILESET_SIZE, tile, x, y)) {
            // check that we got an index in the range possible for bpp's value
            assertTrue(
              index >= 0 && index < (1 << TestValues.TEST_BPP),
              "An impossible index for " +
              Integer.toString(tileset.getBPP()) + " BPP, " +
              Integer.toString(index) + ", was returned for coordinates " +
              "(tile = " + Integer.toString(tile) + ", x = " +
              Integer.toString(x) + ", y = " + Integer.toString(y) + ")."
            );
          } else {  // else, the coordinates should be invalid (so we expect -1)
            assertEquals(
              -1, index,
              Integer.toString(index) + " was returned for invalid " +
              "coordinates (tile = " + Integer.toString(tile) + ", x = " +
              Integer.toString(x) + ", y = " + Integer.toString(y) + ")."
            );
          }
        }
      }
    }
  }
  
  /**
   * The {@link Tileset#setPixelIndex(int, int, int, int)} method should accept
   * valid coordinates but throw an exception for invalid coordinates. This
   * test does not evaluate whether the method works correctly with respect to
   * the values to be set to.
   */
  @Test
  public void testSetPixelIndexCoordinates() {
    // create a tileset to test with
    Tileset tileset = new Tileset(
      TestValues.TEST_TILESET_SIZE, TestValues.TEST_BPP,
      TestValues.TEST_TILESET_FORMAT_VALUE
    );
    
    for (int tile : TestValues.TEST_TILE_NUMS) {
      for (int y : TestValues.TEST_Y_COORDS) {
        for (int x : TestValues.TEST_X_COORDS) {
          // if the coordinates should be valid...
          if (isValidCoordinates(TestValues.TEST_TILESET_SIZE, tile, x, y)) {
            // check that we got an index in the range possible for bpp's value
            assertDoesNotThrow(
              () -> tileset.setPixelIndex(tile, x, y, 0),
              "An exception was thrown for coordinates (tile = " +
              Integer.toString(tile) + ", x = " + Integer.toString(x) +
              ", y = " + Integer.toString(y) + ")."
            );
          } else {  // else, the coordinates should be invalid (exception)
            assertThrows(
              IllegalArgumentException.class,
              () -> tileset.setPixelIndex(tile, x, y, 0),
              "No IllegalArgumentException was thrown for invalid " + 
              "coordinates (tile = " + Integer.toString(tile) + ", x = " +
              Integer.toString(x) + ", y = " + Integer.toString(y) + ")."
            );
          }
        }
      }
    }
  }
  
  /**
   * The {@link Tileset#setPixelIndex(int, int, int, int)} method should accept
   * valid index appropriate to the number of bits per pixel used by the
   * tileset but throw an exception for an invalid index.
   */
  @Test
  public void testSetPixelIndexIndexes() {
    for (int bpp : SELECT_VALID_BPPS) {
      // create a tileset with the specified bpp to test with
      Tileset tileset = new Tileset(TestValues.TEST_TILESET_SIZE, bpp,
                                    TestValues.TEST_TILESET_FORMAT_VALUE);
      // create a list of indexes to test with for the bpp
      int max_index = (1 << bpp) - 1;
      List<Integer> indexes = List.of(
        -1, 0, 1, max_index / 2, max_index - 1, max_index, max_index + 1
      );
      
      for (int tile : TestValues.TEST_TILE_NUMS) {
        for (int y : TestValues.TEST_Y_COORDS) {
          for (int x : TestValues.TEST_X_COORDS) {
            // only test valid coordinates
            if (isValidCoordinates(TestValues.TEST_TILESET_SIZE, tile, x, y)) {
              for (int index : indexes) {
                // if the index should be valid...
                if (index >= 0 && index <= max_index) {
                  assertDoesNotThrow(
                    () -> tileset.setPixelIndex(tile, x, y, index),
                    "Exception thrown for valid index " +
                    Integer.toString(index) + " at coordinates (tile = " +
                    Integer.toString(tile) + ", x = " + Integer.toString(x) +
                    ", y = " + Integer.toString(y) + ")."
                  );
                } else {  // else, the index should be invalid (exception)
                  assertThrows(
                    IllegalArgumentException.class,
                    () -> tileset.setPixelIndex(tile, x, y, index),
                    "No IllegalArgumentException thrown for invalid index " +
                    Integer.toString(index) + " at coordinates (tile = " +
                    Integer.toString(tile) + ", x = " + Integer.toString(x) +
                    ", y = " + Integer.toString(y) + ")."
                  );
                }
              }
            }
          }
        }
      }
    }
  }
  
  /**
   * Changes made to the indexes in the tileset by the
   * {@link Tileset#setPixelIndex(int, int, int, int)} method should match with
   * the indexes gotten by the {@link Tileset#getPixelIndex(int, int, int)}
   * method for corresponding coordinates.
   */
  @Test
  public void testGetAndSetPixelIndexes() {
    for (int bpp : SELECT_VALID_BPPS) {
      // create a tileset with the specified bpp to test with
      Tileset tileset = new Tileset(TestValues.TEST_TILESET_SIZE, bpp,
                                    TestValues.TEST_TILESET_FORMAT_VALUE);
      // create a list of indexes to test with for the bpp
      int max_index = (1 << bpp) - 1;
      List<Integer> indexes = List.of(
        // indexes of new tilesets should be all 0s already - don't start with 0
        1, max_index / 2, max_index, 0, max_index - 1
      );
      
      for (int tile : TestValues.TEST_TILE_NUMS) {
        for (int y : TestValues.TEST_Y_COORDS) {
          for (int x : TestValues.TEST_X_COORDS) {
            // only test valid coordinates
            if (isValidCoordinates(TestValues.TEST_TILESET_SIZE, tile, x, y)) {
              for (int index : indexes) {
                tileset.setPixelIndex(tile, x, y, index);
                assertEquals(
                  index,
                  tileset.getPixelIndex(tile, x, y),
                  "Get and set mismatch at coordinates (tile = " +
                  Integer.toString(tile) + ", x = " + Integer.toString(x) +
                  ", y = " + Integer.toString(y) + ").");
              }
            }
          }
        }
      }
    }
  }
  
  /**
   * When creating a brand new tileset, the constructor should accept valid
   * arguments (number of tiles, bits per pixel, and tileset format), but throw
   * the appropriate exception for invalid arguments. A brand new tileset should
   * have the same number of tiles, bits per pixel, and tileset format value as
   * was given to the constructor that made it.
   */
  @Test
  public void testTilesetConstructorArguments() {
    for (int tileset_size : TestValues.TEST_TILESET_SIZES) {
      for (int bpp : TestValues.TEST_BPPS) {
        for (int format : TestValues.TEST_TILESET_FORMAT_VALUES) {
          // if the arguments are all valid...
          if (tileset_size > 0 && tileset_size <= Tileset.MAX_TILES &&
              bpp >= Tileset.MIN_BPP && bpp <= Tileset.MAX_BPP &&
              TestValues.TILESET_FORMAT_VALUES.contains(format)) {
            assertDoesNotThrow(
              () -> new Tileset(tileset_size, bpp, format),
              "An exception was thrown making a tileset with " +
              Integer.toString(tileset_size) + " tiles, " +
              Integer.toString(bpp) + " BPP, and a tileset format value of " +
              Integer.toString(format) + "."
            );
          } else {  // else, we should expect an exception
            assertThrows(
              IllegalArgumentException.class,
              () -> new Tileset(tileset_size, bpp, format),
              "No IllegalArgumentException was thrown making a tileset with " +
              Integer.toString(tileset_size) + " tiles, " +
              Integer.toString(bpp) + " BPP, and a tileset format value of " +
              Integer.toString(format) + "."
            );
          }
        }
      }
    }
  }
  
  /**
   * When creating a brand new tileset, the new tileset should have no pattern
   * and all of its pixel indexes should be zero.
   */
  @Test
  public void testTilesetConstructorInitialPixelIndexes() {
    // test the full range of combinations of BPPs and tileset format values
    for (int tileset_format : TestValues.TILESET_FORMAT_VALUES) {
      for (int bpp : SELECT_VALID_BPPS) {
        // create a new tileset with the BPP and format to check
        Tileset tileset = new Tileset(TestValues.TEST_TILESET_SIZE, bpp,
                                      tileset_format);
        // check each pixel in the tileset
        for (int tile = 0; tile < TestValues.TEST_TILESET_SIZE; tile++) {
          for (int y = 0; y < Tileset.TILE_HEIGHT; y++) {
            for (int x = 0; x < Tileset.TILE_WIDTH; x++) {
              assertEquals(
                0, tileset.getPixelIndex(tile, x, y),
                "Nonzero pixel index for " + Integer.toString(bpp)
                + " BPP tileset at coordinates (tile = " +
                Integer.toString(tile) + ", x = " + Integer.toString(x) +
                ", y = " + Integer.toString(y) + ")."
              );
            }
          }
        }
      }
    }
  }
  
  /**
   * The sized copy constructor should accept valid arguments for the size of
   * the tileset and create a tileset with the proper number of tiles, but throw
   * the appropriate exception if the size would create an invalid tileset.
   */
  @Test
  public void testTilesetSizedCopyConstructorSizes() {
    // create a tileset to test the sized copy constructor with
    Tileset tileset = new Tileset(
      TestValues.TEST_TILESET_SIZE, TestValues.TEST_BPP,
      TestValues.TEST_TILESET_FORMAT_VALUE
    );
    
    for (int tileset_size : TestValues.TEST_TILESET_SIZES) {
      // if the tileset size should be a valid argument...
      if (tileset_size > 0 && tileset_size <= Tileset.MAX_TILES) {
        assertEquals(
          tileset_size, new Tileset(tileset, tileset_size).getNumberOfTiles(),
          "The copied tileset has the wrong number of tiles."
        );
      } else {  // else, the size should be invalid and we expect an exception
        assertThrows(
          IllegalArgumentException.class,
          () -> new Tileset(tileset, tileset_size),
          "No IllegalArgumentException was thrown making a copy with " +
          Integer.toString(tileset_size) + " tiles."
        );
      }
    }
  }
  
  /**
   * The tileset produced by the sized copy constructor should have the same
   * pattern from tileset in its entirety unless there are too few tiles to copy
   * to. Otherwise, if the number of tiles is greater than the number of tiles
   * in the tileset to copy, the tileset will be copied entirely but the
   * remaining tiles will have no pattern.
   */
  @Test
  public void testTilesetSizedCopyConstructorIdentical() {
    Random random = new Random();  // used to generate random indexes
    
    // test the full range of combinations of BPPs and tileset format values
    for (int format : TestValues.TILESET_FORMAT_VALUES) {
      for (int bpp = Tileset.MIN_BPP; bpp <= Tileset.MAX_BPP; bpp++) {
        int index_bound = 1 << bpp;
        
        // create a tileset to copy and set its indexes to hold random values
        Tileset tileset = new Tileset(TestValues.TEST_TILESET_SIZE, bpp,
                                      format);
        for (int tile = 0; tile < TestValues.TEST_TILESET_SIZE; tile++) {
          for (int y = 0; y < Tileset.TILE_HEIGHT; y++) {
            for (int x = 0; x < Tileset.TILE_WIDTH; x++) {
              tileset.setPixelIndex(tile, x, y, random.nextInt(index_bound));
            }
          }
        }
        
        // test various sizes for the copy constructors
        for (int tileset_size : TestValues.SELECT_VALID_TILESET_SIZES) {
          Tileset copied_tileset = new Tileset(tileset, tileset_size);
          
          // check that the tilesets have the same bpp and tileset format
          assertAll(
            () -> assertEquals(tileset.getBPP(), copied_tileset.getBPP(),
                               "Copied tileset has a different BPP."),
            () -> assertEquals(tileset.getTilesetFormat(),
                               copied_tileset.getTilesetFormat(),
                               "Copied tileset has a different tileset format.")
          );
          
          for (int tile = 0; tile < copied_tileset.getNumberOfTiles(); tile++) {
            // if the tiles should be copies then see that they are copies
            if (tile < tileset.getNumberOfTiles()) {
              for (int y = 0; y < Tileset.TILE_HEIGHT; y++) {
                for (int x = 0; x < Tileset.TILE_WIDTH; x++) {
                  assertEquals(
                    tileset.getPixelIndex(tile, x, y),
                    copied_tileset.getPixelIndex(tile, x, y),
                    "The original and copied tilesets mismatch at " +
                    "coordinates (tile = " + Integer.toString(tile) + ", x = " +
                    Integer.toString(x) + ", y = " + Integer.toString(y) + ")."
                  );
                }
              }
            } else {  // else, make sure the indexes are all zeroes
              for (int y = 0; y < Tileset.TILE_HEIGHT; y++) {
                for (int x = 0; x < Tileset.TILE_WIDTH; x++) {
                  assertEquals(
                    0, copied_tileset.getPixelIndex(tile, x, y),
                    "Nonzero pixel index at coordinates (tile = " +
                    Integer.toString(tile) + ", x = " + Integer.toString(x) +
                    ", y = " + Integer.toString(y) + ")."
                  );
                }
              }
            }
          }
        }
      }
    }
  }
  
  /**
   * Tilesets created with the sized copy constructor should be deep copies -
   * that is, changes made to either tileset should not be reflected in the
   * other; the tilesets should be and have distinct objects.
   */
  @Test
  public void testTilesetSizedCopyConstructorDeepCopy() {
    Random random = new Random();  // used to generate random indexes
    
    // test the full range of combinations of BPPs and tileset format values
    for (int format : TestValues.TILESET_FORMAT_VALUES) {
      for (int bpp = Tileset.MIN_BPP; bpp <= Tileset.MAX_BPP; bpp++) {
        int index_bound = 1 << bpp;
        int[][][] original_indexes =  // [tile][row][column]
          new int[TestValues.TEST_TILESET_SIZE]
                 [Tileset.TILE_HEIGHT][Tileset.TILE_WIDTH];
        
        // fill a tileset with random indexes - keep a copy of these indexes
        Tileset tileset = new Tileset(TestValues.TEST_TILESET_SIZE, bpp,
                                      format);
        for (int tile = 0; tile < TestValues.TEST_TILESET_SIZE; tile++) {
          for (int y = 0; y < Tileset.TILE_HEIGHT; y++) {
            for (int x = 0; x < Tileset.TILE_WIDTH; x++) {
              int index = random.nextInt(index_bound);
              original_indexes[tile][y][x] = index;
              tileset.setPixelIndex(tile, x, y, index);
            }
          }
        }
        // use the sized copy constructor to create an identical copy
        Tileset copied_tileset =
          new Tileset(tileset, TestValues.TEST_TILESET_SIZE);
        
        // change the copied tileset (just shift the indexes)
        for (int tile = 0; tile < TestValues.TEST_TILESET_SIZE; tile++) {
          for (int y = 0; y < Tileset.TILE_HEIGHT; y++) {
            for (int x = 0; x < Tileset.TILE_WIDTH; x++) {
              copied_tileset.setPixelIndex(
                tile, x, y,
                (copied_tileset.getPixelIndex(tile, x, y) + 1) % index_bound
              );
            }
          }
        }
        // check to see if the original tileset has been changed
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
        
        copied_tileset =  // reset copy
          new Tileset(tileset, TestValues.TEST_TILESET_SIZE);
        
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
          for (int y = 0; y < Tileset.TILE_WIDTH; y++) {
            for (int x = 0; x < Tileset.TILE_WIDTH; x++) {
              assertEquals(
                original_indexes[tile][y][x],
                copied_tileset.getPixelIndex(tile, x, y),
                "The copied tileset has a changed index at coordinates " +
                "(tile = " + Integer.toString(tile) + ", x = " +
                Integer.toString(x) + ", y = " + Integer.toString(y) + ")."
              );
            }
          }
        }
      }
    }
  }
  
  /**
   * When the sized copy constructor is used, providing null for the tileset to
   * be copied should throw the appropriate exception.
   */
  @Test
  public void testTilesetSizedCopyConstructorNullTilesetArgument() {
    assertThrows(NullPointerException.class,
                 () -> new Tileset(null, TestValues.TEST_TILESET_SIZE),
                 "No NullPointerException thrown for null argument.");
  }
  
  /**
   * When the copy constructor is used, the resulting tileset should have
   * the same pattern, number of tiles, number of bits per pixel, and tileset
   * format value as the original tileset.
   */
  @Test
  public void testTilesetCopyConstructorIdentical() {
    Random random = new Random();  // used to generate random indexes
    
    for (int tileset_size : TestValues.SELECT_VALID_TILESET_SIZES) {
      // test the full range of combinations of BPPs and tileset format values
      for (int format : TestValues.TILESET_FORMAT_VALUES) {
        for (int bpp = Tileset.MIN_BPP; bpp <= Tileset.MAX_BPP; bpp++) {
          int index_bound = 1 << bpp;

          // create a tileset to copy and set its indexes to hold random values
          Tileset tileset = new Tileset(tileset_size, bpp, format);
          for (int tile = 0; tile < tileset_size; tile++) {
            for (int y = 0; y < Tileset.TILE_HEIGHT; y++) {
              for (int x = 0; x < Tileset.TILE_WIDTH; x++) {
                tileset.setPixelIndex(tile, x, y, random.nextInt(index_bound));
              }
            }
          }
          
          Tileset copied_tileset = new Tileset(tileset);  // create a copy
          
          // check that the copied tileset has all the same properties
          assertAll(
            () -> assertEquals(tileset_size, copied_tileset.getNumberOfTiles(),
                               "Copied tileset is a different size."),
            () -> assertEquals(tileset.getBPP(), copied_tileset.getBPP(),
                               "Copied tileset has a different BPP."),
            () -> assertEquals(tileset.getTilesetFormat(),
                               copied_tileset.getTilesetFormat(),
                               "Copied tileset has a different tileset format.")
          );
          // check that the pattern of the copied tileset is also the same
          for (int tile = 0; tile < tileset_size; tile++) {
            for (int y = 0; y < Tileset.TILE_HEIGHT; y++) {
              for (int x = 0; x < Tileset.TILE_WIDTH; x++) {
                assertEquals(
                  tileset.getPixelIndex(tile, x, y),
                  copied_tileset.getPixelIndex(tile, x, y),
                  "The copied tileset has a different index at coordinates " +
                  "(tile = " + Integer.toString(tile) + ", x = " +
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
   * Tilesets created with the sized copy constructor should be deep copies -
   * that is, changes made to either tileset should not be reflected in the
   * other; the tilesets should be and have distinct objects.
   */
  @Test
  public void testTilesetCopyConstructorDeepCopy() {
    Random random = new Random();  // used to generate random indexes
    
    // test the full range of combinations of BPPs and tileset format values
    for (int format : TestValues.TILESET_FORMAT_VALUES) {
      for (int bpp = Tileset.MIN_BPP; bpp <= Tileset.MAX_BPP; bpp++) {
        int index_bound = 1 << bpp;
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
        // use the copy constructor to create an identical copy
        Tileset copied_tileset = new Tileset(tileset);
        
        // change the copied tileset (just shift the indexes)
        for (int tile = 0; tile < TestValues.TEST_TILESET_SIZE; tile++) {
          for (int y = 0; y < Tileset.TILE_HEIGHT; y++) {
            for (int x = 0; x < Tileset.TILE_WIDTH; x++) {
              copied_tileset.setPixelIndex(
                tile, x, y,
                (copied_tileset.getPixelIndex(tile, x, y) + 1) % index_bound
              );
            }
          }
        }
        // check to see if the original tileset has been changed
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
        
        copied_tileset = new Tileset(tileset);  // reset copy
        
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
          for (int y = 0; y < Tileset.TILE_WIDTH; y++) {
            for (int x = 0; x < Tileset.TILE_WIDTH; x++) {
              assertEquals(
                original_indexes[tile][y][x],
                copied_tileset.getPixelIndex(tile, x, y),
                "The copied tileset has a changed index at coordinates " +
                "(tile = " + Integer.toString(tile) + ", x = " +
                Integer.toString(x) + ", y = " + Integer.toString(y) + ")."
              );
            }
          }
        }
      }
    }
  }
  
  /**
   * When the copy constructor is used, providing null for the tileset to be
   * copied should throw the appropriate exception.
   */
  @Test
  public void testTilesetCopyConstructorNullTilesetArgument() {
    assertThrows(NullPointerException.class, () -> new Tileset(null),
                 "No NullPointerException thrown for null argument.");
  }
  
  /** {@link Tileset#TILE_WIDTH} should be at least one. */
  @Test
  public void testTileWidth() {
    assertTrue(Tileset.TILE_WIDTH >= 1, "Tile width is less than one.");
  }
  
  /** {@link Tileset#TILE_HEIGHT} should be at least one. */
  @Test
  public void testTileHeight() {
    assertTrue(Tileset.TILE_HEIGHT >= 1, "Tile height is less than one.");
  }
  
  /**
   * {@link Tileset#MIN_BPP} and {@link Tileset#MAX_BPP} should both be at least
   * one, and the minimum should be less than or equal to the maximum.
   */
  @Test
  public void testMinAndMaxBPP() {
    assertTrue(Tileset.MIN_BPP >= 1, "The minimum BPP is less than one.");
    assertTrue(Tileset.MAX_BPP >= 1, "The maximum BPP is less than one.");
    assertTrue(Tileset.MIN_BPP <= Tileset.MAX_BPP,
               "The minimum BPP is greater than the maximum BPP.");
  }
  
  /**
   * {@link Tileset#MAX_TILES} should be the greatest number of tiles that can
   * be used alongside {@link #MAX_BPP} without using more bits than the maximum
   * value for a signed integer can hold.
   */
  @Test
  public void testMaxTiles() {
    int correct_max =  // divide by the greatest number of bits for a tile
      Integer.MAX_VALUE / (Tileset.TILE_WIDTH * Tileset.TILE_HEIGHT *
                           Tileset.MAX_BPP);
    assertTrue(Tileset.MAX_TILES == correct_max);
  }
  
  /** Each of the tileset format values should be distinct. */
  @Test
  public void testTilesetFormatValues() {
    // count the number of distinct values and see if there are fewer elements
    long distinct_values =
      TestValues.TILESET_FORMAT_VALUES.stream().distinct().count();
    assertEquals(TestValues.TILESET_FORMAT_VALUES.size(), distinct_values,
                 "Only " + Long.toString(distinct_values) + " out of " +
                 Integer.toString(TestValues.TILESET_FORMAT_VALUES.size()) +
                 " tileset format values are distinct.");
  }
  
  /**
   * Returns whether or not the given coordinates would be valid for a tileset.
   * 
   * @param tileset_size the number of tiles in the tileset.
   * @param tile the tile number within the tileset.
   * @param x the x coordinate for the pixel within the specified tile.
   * @param y the y coordinate for the pixel within the specified tile.
   * @return true if the coordinates should be valid and false otherwise.
   */
  private static boolean isValidCoordinates(int tileset_size,
                                            int tile, int x, int y) {
    return tile >= 0 && tile < tileset_size &&
           x >= 0 && x < Tileset.TILE_WIDTH &&
           y >= 0 && y < Tileset.TILE_HEIGHT;
  }
  
  /** A list of some valid numbers of bits per pixels for these unit tests.
   These values should be relevant for testing - especially boundaries. */
  private static final List<Integer> SELECT_VALID_BPPS = List.of(
    Tileset.MIN_BPP, (Tileset.MIN_BPP + Tileset.MAX_BPP) / 2, Tileset.MAX_BPP
  );
}