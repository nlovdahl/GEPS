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

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Collectors;

/**
 * A various collection of values and lists which can be used in many tests.
 * Such things which have use across multiple groups of tests should go here.
 * 
 * @author Nicholas Lovdahl
 */
public class TestValues {
  /** A list that should contain of all of the values used to denote distinct
   tileset formats. */
  public static final List<Integer> TILESET_FORMAT_VALUES = List.of(
    Tileset.SERIAL_FORMAT, Tileset.PLANAR_FORMAT,
    Tileset.LINEAR_INTERTWINED_FORMAT, Tileset.PAIRED_INTERTWINED_FORMAT
  );
  
  /** A valid tileset size (the number of tiles) which can be used for testing
   when the tileset size itself is not being tested and can be constant). */
  public static final int TEST_TILESET_SIZE = 16;
  /** A valid number of bits per pixel which can be used for testing when the
   number of bits per pixel itself is not being tested and can be constant. */
  public static final int TEST_BPP = (Tileset.MIN_BPP + Tileset.MAX_BPP) / 2;
  /** A valid tileset format value which can be used for testing when the format
   value itself is not being tested and can be constant. */
  public static final int TEST_TILESET_FORMAT_VALUE =
    Tileset.PAIRED_INTERTWINED_FORMAT;
  
  /** A list of both valid and invalid tileset sizes (the number of tiles). */
  public static final List<Integer> TEST_TILESET_SIZES = List.of(
    -1, 0, 1, TEST_TILESET_SIZE, TEST_TILESET_SIZE * 2, Tileset.MAX_TILES + 1
  );
  /** A list of both valid and invalid numbers of bits per pixel. */
  public static final List<Integer> TEST_BPPS =
    IntStream.rangeClosed(
      Tileset.MIN_BPP - 1, Tileset.MAX_BPP + 1
    ).boxed().collect(Collectors.toList());
  /** A list of both valid and invalid tileset format values. */
  public static final List<Integer> TEST_TILESET_FORMAT_VALUES =
    IntStream.rangeClosed(
      Collections.min(TILESET_FORMAT_VALUES) - 1,
      Collections.max(TILESET_FORMAT_VALUES) + 1
    ).boxed().collect(Collectors.toList());
  
  /** A list of both valid and valid tileset numbers (the number denoting a
   particular tile in a tileset). */
  public static final List<Integer> TEST_TILE_NUMS = List.of(
    // lesser than valid range (invalid)
    -TEST_TILESET_SIZE, -TEST_TILESET_SIZE / 2, -TEST_TILESET_SIZE / 4, 0,
    // valid range
    1, TEST_TILESET_SIZE / 4, TEST_TILESET_SIZE / 2, TEST_TILESET_SIZE - 1,
    // greater than valid range (invalid)
    TEST_TILESET_SIZE, TEST_TILESET_SIZE * 2, TEST_TILESET_SIZE * 4
  );
  /** A list of both valid and invalid x-coordinates (the horizontal coordinate
   of some pixel in a tile). */
  public static final List<Integer> TEST_X_COORDS = List.of(
    // lesser than valid range (invalid)
    -Tileset.TILE_WIDTH, -Tileset.TILE_WIDTH / 2, Tileset.TILE_WIDTH / 4, 0,
    // valid range
    1, Tileset.TILE_WIDTH / 4, Tileset.TILE_WIDTH / 2, Tileset.TILE_WIDTH - 1,
    // greater than valid range (invalid)
    Tileset.TILE_WIDTH, Tileset.TILE_WIDTH * 2, Tileset.TILE_WIDTH * 4
  );
  /** A list of both valid and invalid y-coordinates (the vertical coordinate of
   some pixel in a tile). */
  public static final List<Integer> TEST_Y_COORDS = List.of(
    // lesser than valid range (invalid)
    -Tileset.TILE_HEIGHT, - Tileset.TILE_HEIGHT / 2, Tileset.TILE_HEIGHT / 4, 0,
    // valid range
    1, Tileset.TILE_HEIGHT / 4, Tileset.TILE_HEIGHT / 2,
    Tileset.TILE_HEIGHT - 1,
    // greater than valid range (invalid)
    Tileset.TILE_HEIGHT, Tileset.TILE_HEIGHT * 2, Tileset.TILE_HEIGHT * 4
  );
}
