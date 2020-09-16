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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.awt.Color;
import java.util.List;

/**
 * Unit tests for {@link Palette}. Note that some of these tests will use colors
 * which could be replaced with static colors from {@link java.awt.Color}, the
 * use of constructors with a color code was chosen instead to avoid dependency
 * on particular values being used to define such colors.
 * 
 * @author Nicholas Lovdahl
 */
public class PaletteTest {
  /**
   * The {@link Palette#clampColor(int, int, int)} method should set all of the
   * bits in the alpha channel and clear the last three bits of each color
   * channel of the given color.
   */
  @Test
  public void testClampColorWithRGB() {
    for (Color test_color : SELECT_COLORS) {
      Color expected_color =
        new Color((test_color.getRGB() | 0xFF000000) & 0xFFF8F8F8, true);
      Color actual_color = Palette.clampColor(test_color.getRed(),
                                              test_color.getGreen(),
                                              test_color.getBlue());
      
      assertAll(
        () -> assertEquals(expected_color.getRed(), actual_color.getRed(),
                           "Failure to clamp red channel."),
        () -> assertEquals(expected_color.getGreen(), actual_color.getGreen(),
                           "Failure to clamp green channel."),
        () -> assertEquals(expected_color.getBlue(), actual_color.getBlue(),
                           "Failure to clamp blue channel."),
        () -> assertEquals(expected_color.getAlpha(), actual_color.getAlpha(),
                           "Failure to clamp alpha channel.")
      );
    }
  }
  
  /**
   * The {@link Palette#clampColor(java.awt.Color)} method should set all of the
   * bits in the alpha channel and clear the last three bits of each color
   * channel of the given color.
   */
  @Test
  public void testClampColorWithColor() {
    for (Color test_color : SELECT_COLORS) {
      Color expected_color =
        new Color((test_color.getRGB() | 0xFF000000) & 0xFFF8F8F8, true);
      Color actual_color = Palette.clampColor(test_color);
      
      assertAll(
        () -> assertEquals(expected_color.getRed(), actual_color.getRed(),
                           "Failure to clamp red channel."),
        () -> assertEquals(expected_color.getGreen(), actual_color.getGreen(),
                           "Failure to clamp green channel."),
        () -> assertEquals(expected_color.getBlue(), actual_color.getBlue(),
                           "Failure to clamp blue channel."),
        () -> assertEquals(expected_color.getAlpha(), actual_color.getAlpha(),
                           "Failure to clamp alpha channel.")
      );
    }
  }
  
  /**
   * {@link Palette#clampColor(java.awt.Color)} should reject null as an
   * argument and throw the appropriate exception.
   */
  @Test
  public void testClampColorNullColorArgument() {
    assertThrows(NullPointerException.class, () -> Palette.clampColor(null),
                 "No NullPointerException thrown for null color argument.");
  }
  
  /**
   * The {@link Palette#getColor(int)} method should accept valid indexes as
   * arguments, but throw an exception for invalid indexes.
   */
  @Test
  public void testGetColorIndexes() {
    Palette palette = new Palette();
    
    for (int index : SELECT_PALETTE_INDEXES) {
      // if the index should be valid...
      if (index >= 0 && index < Palette.PALETTE_MAX_SIZE) {
        assertDoesNotThrow(
          () -> palette.getColor(index),
          "An exception was thrown for index " + Integer.toString(index) + "."
        );
      } else {  // else, the index is invalid and we expect an exception
        assertThrows(
          ArrayIndexOutOfBoundsException.class,
          () -> palette.getColor(index),
          "No ArrayIndexOutOfBoundsException was thrown for index " +
          Integer.toString(index) + "."
        );
      }
    }
  }
  
  /**
   * The {@link Palette#setColor(int, int, int, int)} method should accept valid
   * indexes as arguments, but throw an exception for invalid indexes. This
   * test does not evaluate whether the method works correctly with respect to
   * the values to be set to.
   */
  @Test
  public void testSetColorIndexesWithRGB() {
    Palette palette = new Palette();
    
    for (int index : SELECT_PALETTE_INDEXES) {
      // if the index should be valid...
      if (index >= 0 && index < Palette.PALETTE_MAX_SIZE) {
        assertDoesNotThrow(
          () -> palette.setColor(index, 0, 0, 0),
          "An exception was thrown for index " + Integer.toString(index) + "."
        );
      } else {  // else, the index is invalid and we expect an exception
        assertThrows(
          ArrayIndexOutOfBoundsException.class,
          () -> palette.setColor(index, 0, 0, 0),
          "No ArrayIndexOutOfBoundsException was thrown for index " +
          Integer.toString(index) + "."
        );
      }
    }
  }
  
  /**
   * The {@link Palette#setColor(int, java.awt.Color)} method should accept
   * valid indexes as arguments, but throw an exception for invalid indexes.
   * This test does not evaluate whether the method works correctly with respect
   * to the values to be set to.
   */
  @Test
  public void testSetColorIndexesWithColor() {
    Palette palette = new Palette();
    
    for (int index : SELECT_PALETTE_INDEXES) {
      // if the index should be valid...
      if (index >= 0 && index < Palette.PALETTE_MAX_SIZE) {
        assertDoesNotThrow(
          () -> palette.setColor(index, Color.BLACK),
          "An exception was thrown for index " + Integer.toString(index) + "."
        );
      } else {  // else, the index is invalid and we expect an exception
        assertThrows(
          ArrayIndexOutOfBoundsException.class,
          () -> palette.setColor(index, Color.BLACK),
          "No ArrayIndexOutOfBoundsException was thrown for index " +
          Integer.toString(index) + "."
        );
      }
    }
  }
  
  /**
   * {@link Palette#setColor(int, java.awt.Color)} should reject null as a
   * arguments and throw the appropriate exception.
   */
  @Test
  public void testSetColorNullColorArgument() {
    Palette palette = new Palette();
    
    assertThrows(NullPointerException.class, () -> palette.setColor(0, null),
                 "No NullPointerException thrown for null color argument.");
  }
  
  /**
   * The changes to the colors in the palette by the
   * {@link Palette#setColor(int, int, int, int) } method should be clamped, and
   * the {@link Palette#getColor(int)} method should provide the expected
   * results for corresponding indexes.
   */
  @Test
  public void testGetAndSetColorWithRGB() {
    Palette palette = new Palette();
    
    // go through the both the entire palette and all of the test colors
    int limit = Math.max(SELECT_COLORS.size(), Palette.PALETTE_MAX_SIZE);
    for (int index = 0; index < limit; index++) {
      int test_index = index % Palette.PALETTE_MAX_SIZE;
      Color test_color = SELECT_COLORS.get(index % SELECT_COLORS.size());
      Color expected_color = Palette.clampColor(test_color);
      
      palette.setColor(test_index, test_color.getRed(), test_color.getGreen(),
                       test_color.getBlue());
      assertEquals(
        expected_color, palette.getColor(test_index),
        "Get and set mismatch at index " + Integer.toString(index) + "."
      );
    }
  }
  
  /**
   * The changes to the colors in the palette by the
   * {@link Palette#setColor(int, java.awt.Color)} method should be clamped, and
   * the {@link Palette#getColor(int)} method should provide the expected
   * results for corresponding indexes.
   */
  @Test
  public void testGetAndSetColorWithColor() {
    Palette palette = new Palette();
    
    // go through the both the entire palette and all of the test colors
    int limit = Math.max(SELECT_COLORS.size(), Palette.PALETTE_MAX_SIZE);
    for (int index = 0; index < limit; index++) {
      int test_index = index % Palette.PALETTE_MAX_SIZE;
      Color test_color = SELECT_COLORS.get(index % SELECT_COLORS.size());
      Color expected_color = Palette.clampColor(test_color);
      
      palette.setColor(test_index , test_color);
      assertEquals(
        expected_color, palette.getColor(test_index),
        "Get and set mismatch at index " + Integer.toString(index) + "."
      );
    }
  }
  
  /**
   * A newly constructed palette should have some default colors (what these
   * colors are is not important); no colors in a new palette should be null and
   * each color should be the same as if it were clamped. That is, all entries
   * in a new palette should be non-null, clamped values.
   */
  @Test
  public void testPaletteConstructorColors() {
    Palette palette = new Palette();
    for (int index = 0; index < Palette.PALETTE_MAX_SIZE; index++) {
      assertNotNull(palette.getColor(index),
                    "Null found at index " + Integer.toString(index) + ".");
      assertEquals(Palette.clampColor(palette.getColor(index)),
                   palette.getColor(index),
                   "Unclamped value at index " + Integer.toString(index) + ".");
    }
  }
  
  /**
   * When the copy constructor is used, providing null for the palette to be
   * copied should throw the appropriate exception.
   */
  @Test
  public void testPaletteCopyConstructorNullPaletteArgument() {
    assertThrows(NullPointerException.class, () -> new Palette(null),
                 "No NullPointerException thrown for null palette argument.");
  }
  
  /**
   * When the copy constructor is used, the resulting palette should have
   * all the same colors in the respective indexes.
   */
  @Test
  public void testPaletteCopyConstructorIdentical() {
    // create and fill out a starting palette
    Palette starting_palette = new Palette();
    for (int index = 0; index < Palette.PALETTE_MAX_SIZE; index++) {
      starting_palette.setColor(
        index, SELECT_COLORS.get(index % SELECT_COLORS.size())
      );
    }
    
    // create another palette and check that it has the same colors
    Palette copied_palette = new Palette(starting_palette);
    for (int index = 0; index < Palette.PALETTE_MAX_SIZE; index++) {
      Color expected_color = starting_palette.getColor(index);
      Color actual_color = copied_palette.getColor(index);
      assertEquals(expected_color, actual_color,
                   "Copied palette has different color at index " +
                   Integer.toString(index) + ".");
    }
  }
  
  /**
   * Palettes created with the copy constructor should be deep copies - that is,
   * changes made to either palette should not be reflected in the other; the
   * palettes should be and have distinct objects.
   */
  @Test
  public void testPaletteCopyConstructorDeepCopy() {
    // create and fill out a starting palette, then make a copy
    Palette starting_palette = new Palette();
    for (int index = 0; index < Palette.PALETTE_MAX_SIZE; index++) {
      starting_palette.setColor(
        index, SELECT_COLORS.get(index % SELECT_COLORS.size())
      );
    }
    Palette copied_palette = new Palette(starting_palette);
    
    // change the copied palette (just shift the colors)
    for (int index = 0; index < Palette.PALETTE_MAX_SIZE; index++) {
      copied_palette.setColor(
        index, SELECT_COLORS.get((index + 1) % SELECT_COLORS.size())
      );
    }
    // check that the starting palette has remained the same
    for (int index = 0; index < Palette.PALETTE_MAX_SIZE; index++) {
      Color expected_color = Palette.clampColor(
        SELECT_COLORS.get(index % SELECT_COLORS.size())
      );
      Color actual_color = starting_palette.getColor(index);
      assertEquals(expected_color, actual_color,
                   "Copied palette altered by change in starting palette at " +
                   "index " + Integer.toString(index) + ".");
    }
    
    // change the starting palette
    for (int index = 0; index < Palette.PALETTE_MAX_SIZE; index++) {
      starting_palette.setColor(
        index, SELECT_COLORS.get((index + 2) % SELECT_COLORS.size()));
    }
    // check that the copied palette has remained the same
    for (int index = 0; index < Palette.PALETTE_MAX_SIZE; index++) {
      Color expected_color = Palette.clampColor(
        SELECT_COLORS.get((index + 1) % SELECT_COLORS.size()));
      Color actual_color = copied_palette.getColor(index);
      assertEquals(expected_color, actual_color,
                   "Starting palette altered by change in copied palette at " +
                   "index " + Integer.toString(index) + ".");
    }
  }
  
  /**
   * The {@link Palette#getSNESColorCode(java.awt.Color)} method should reject
   * null as an argument and throw the appropriate exception.
   */
  @Test
  public void testGetSNESColorCodeNullColorArgument() {
    assertThrows(NullPointerException.class,
                 () -> Palette.getSNESColorCode(null),
                 "No NullPointerException thrown for null color argument.");
  }
  
  /**
   * The {@link Palette#getSNESColorCode(java.awt.Color)} method should give a
   * clamped version of the color that is then packed into 0BBBBBGGGGGRRRRR
   * (or 5B5G5R) format.
   */
  @Test
  public void testGetSNESColorCode() {
    assertAll(
      () -> assertEquals(0x0000, Palette.getSNESColorCode(new Color(0x000000))),
      () -> assertEquals(0x7FFF, Palette.getSNESColorCode(new Color(0xFFFFFF))),
      () -> assertEquals(0x001F, Palette.getSNESColorCode(new Color(0xFF0000))),
      () -> assertEquals(0x03E0, Palette.getSNESColorCode(new Color(0x00FF00))),
      () -> assertEquals(0x7C00, Palette.getSNESColorCode(new Color(0x0000FF))),
      () -> assertEquals(0x7FFF, Palette.getSNESColorCode(new Color(0xF8F8F8))),
      () -> assertEquals(0x001F, Palette.getSNESColorCode(new Color(0xF80000))),
      () -> assertEquals(0x03E0, Palette.getSNESColorCode(new Color(0x00F800))),
      () -> assertEquals(0x7C00, Palette.getSNESColorCode(new Color(0x0000F8))),
      () -> assertEquals(0x1D83, Palette.getSNESColorCode(new Color(0x1F6639))),
      () -> assertEquals(0x7BB9, Palette.getSNESColorCode(new Color(0xC9EDF3))),
      () -> assertEquals(0x4DF6, Palette.getSNESColorCode(new Color(0xB57C9F)))
    );
  }
  
  /**
   * The {@link Palette#getSNESColorCodeString(java.awt.Color)} method should
   * provide the number that would be given by the
   * {@link Palette#getSNESColorCode(java.awt.Color)} method, but as a String in
   * a hexadecimal format. This format should be of the form (0xHHHH) where the
   * H's are hexadecimal numbers.
   */
  @Test
  public void testGetSNESColorCodeString() {
    assertAll(
      () -> assertEquals("0x0000",
                         Palette.getSNESColorCodeString(new Color(0x000000))),
      () -> assertEquals("0x7FFF",
                         Palette.getSNESColorCodeString(new Color(0xFFFFFF))),
      () -> assertEquals("0x001F",
                         Palette.getSNESColorCodeString(new Color(0xFF0000))),
      () -> assertEquals("0x03E0",
                         Palette.getSNESColorCodeString(new Color(0x00FF00))),
      () -> assertEquals("0x7C00",
                         Palette.getSNESColorCodeString(new Color(0x0000FF))),
      () -> assertEquals("0x7FFF",
                         Palette.getSNESColorCodeString(new Color(0xF8F8F8))),
      () -> assertEquals("0x001F",
                         Palette.getSNESColorCodeString(new Color(0xF80000))),
      () -> assertEquals("0x03E0",
                         Palette.getSNESColorCodeString(new Color(0x00F800))),
      () -> assertEquals("0x7C00",
                         Palette.getSNESColorCodeString(new Color(0x0000F8))),
      () -> assertEquals("0x1D83",
                         Palette.getSNESColorCodeString(new Color(0x1F6639))),
      () -> assertEquals("0x7BB9",
                         Palette.getSNESColorCodeString(new Color(0xC9EDF3))),
      () -> assertEquals("0x4DF6",
                         Palette.getSNESColorCodeString(new Color(0xB57C9F)))
    );
  }
  
  /**
   * The {@link Palette#getSNESColorCodeString(java.awt.Color)} method should
   * reject null as an argument and throw the appropriate exception.
   */
  @Test
  public void testGetSNESColorCodeStringNullColorArgument() {
    assertThrows(NullPointerException.class,
                 () -> Palette.getSNESColorCodeString(null),
                 "No NullPointerException thrown for null color argument.");
  }
  
  /**
   * The {@link Palette#contrastColor(java.awt.Color)} method should return
   * {@link Color#WHITE} when given colors that are 'dark' and
   * {@link Color#BLACK} when given colors that are 'bright'. The boundaries for
   * this can be subjective, so only extremes are tested.
   */
  @Test
  public void testConstrastColor() {
    assertAll(
      // 'bright' colors which should have black to contrast with
      () -> assertEquals(Color.BLACK,
                         Palette.contrastColor(new Color(0xFFFFFF)),
                         "Expected black contrast with white."),
      () -> assertEquals(Color.BLACK,
                         Palette.contrastColor(new Color(0xC0C0C0)),
                         "Expected black contrast with light gray."),
      () -> assertEquals(Color.BLACK,
                         Palette.contrastColor(new Color(0xFFFF00)),
                         "Expected black contrast with yellow."),
      () -> assertEquals(Color.BLACK,
                         Palette.contrastColor(new Color(0x00FF00)),
                         "Expected black contrast with green."),
      () -> assertEquals(Color.BLACK,
                         Palette.contrastColor(new Color(0x00FFFF)),
                         "Expected black contrast with cyan."),
      // 'dark' colors which should have white to contrast with
      () -> assertEquals(Color.WHITE,
                         Palette.contrastColor(new Color(0x000000)),
                         "Expected white contrast with black."),
      () -> assertEquals(Color.WHITE,
                         Palette.contrastColor(new Color(0x404040)),
                         "Expected white contrast with dark gray."),
      () -> assertEquals(Color.WHITE,
                         Palette.contrastColor(new Color(0xFF0000)),
                         "Expected white contrast with red."),
      () -> assertEquals(Color.WHITE,
                         Palette.contrastColor(new Color(0xFF00FF)),
                         "Expected white contrast with magenta."),
      () -> assertEquals(Color.WHITE,
                         Palette.contrastColor(new Color(0x0000FF)),
                         "Expected white contrast with blue.")
    );
  }
  
  /**
   * The {@link Palette#contrastColor(java.awt.Color)} method should reject null 
   * as an argument and throw the appropriate exception.
   */
  public void testContrastColorNullColorArgument() {
    assertThrows(NullPointerException.class,
                 () -> Palette.contrastColor(null),
                 "No NullPointerException thrown for null color argument.");
  }
  
  /**
   * {@link Palette#PALETTE_MAX_SIZE} should be a power of two and should also
   * be at least large enough to store all of the colors that could be addressed
   * using {@link Tileset#MAX_BPP} bits per pixel.
   */
  @Test
  public void testMaxPaletteSize() {
    assertTrue(Palette.PALETTE_MAX_SIZE > 0,
               "PALETTE_MAX_SIZE should be at least 0.");
    assertTrue(Integer.bitCount(Palette.PALETTE_MAX_SIZE) == 1,
               "PALETTE_MAX_SIZE should be a power of 2.");
    int min_palette_max_size = 1 << Tileset.MAX_BPP;
    assertTrue(Palette.PALETTE_MAX_SIZE >= min_palette_max_size,
               "PALETTE_MAX_SIZE needs to be able to hold at least " +
               Integer.toString(min_palette_max_size) + " entries.");
  }
  
  /** A list of both valid and invalid palette indexes. */
  private static final List<Integer> SELECT_PALETTE_INDEXES = List.of(
    -1, 0, 1, Palette.PALETTE_MAX_SIZE / 2, Palette.PALETTE_MAX_SIZE - 1,
    Palette.PALETTE_MAX_SIZE
  );
  /** Colors used for testing. The selection of these colors impacts the quality
   of the coverage of the unit tests. */
  private static final List<Color> SELECT_COLORS = List.of(
    // simple colors
    Color.BLACK, Color.DARK_GRAY, Color.GRAY, Color.LIGHT_GRAY, Color.WHITE,
    Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.BLUE, Color.CYAN,
    Color.MAGENTA, Color.PINK,
    // colors that should stay the same when clamped
    new Color(0xD0C820), new Color(0x70C828), new Color(0x9030D0),
    new Color(0xB800F8), new Color(0x001030), new Color(0x3078D0),
    new Color(0x080868), new Color(0xF06088), new Color(0xD018B0),
    new Color(0xA01878), new Color(0x90F8F0), new Color(0x905088),
    new Color(0xC038A8), new Color(0x488050), new Color(0xA07810),
    // colors that should change when clamped (but are completely opaque)
    new Color(0x2FB29A), new Color(0x6E158C), new Color(0x11EE3D),
    new Color(0x7533DA), new Color(0xBFEF1D), new Color(0x43FE7D),
    new Color(0x46B58C), new Color(0xC993B7), new Color(0x17FC76),
    new Color(0x96EB7D), new Color(0x939405), new Color(0x0B5A9C),
    new Color(0x85971B), new Color(0x0B914E), new Color(0x96A3CC),
    // colors with transparency (should be completely opaque when clamped)
    new Color(0x3EBEB77E, true), new Color(0x2FECB4C3, true),
    new Color(0xE91D6303, true), new Color(0x12DC9759, true),
    new Color(0x95C73586, true), new Color(0xA63DDEF9, true),
    new Color(0xF66E2C86, true), new Color(0xC36E7554, true),
    new Color(0x99210D9E, true), new Color(0x44FE1AB3, true),
    new Color(0x2C34332D, true), new Color(0x2A12B4AA, true),
    new Color(0xB32FB56A, true), new Color(0x25016D99, true),
    new Color(0x9BE4A4C1, true)
  );
}
