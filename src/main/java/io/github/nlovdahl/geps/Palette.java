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

import java.awt.Color;

/**
 * The data model for a palette. A palette is composed of an explicit number of
 * color entries which can be used by a {@link Tileset} to determine what colors
 * that tileset should have. Together, a tileset and a palette could be used to
 * form an image.
 * 
 * @author Nicholas Lovdahl
 * 
 * @see PaletteController
 * @see PaletteView
 */
public final class Palette {
  /** Creates a palette with a default selection of colors as its entries. */
  public Palette() {
    colors_ = new Color[PALETTE_MAX_SIZE];
    
    // fill in some grayscale colors
    shade_fill(0, 16, 255, 255, 255);
    setColor(0, 0, 128, 0);  // use a green background to contrast black
    // fill in different shades of the rainbow (combos of channels)
    shade_fill(16,  16, 255, 0,   0);
    shade_fill(32,  16, 255, 128, 0);
    shade_fill(48,  16, 255, 255, 0);
    shade_fill(64,  16, 128, 255, 0);
    shade_fill(80,  16, 0,   255, 0);
    shade_fill(96,  16, 0,   255, 128);
    shade_fill(112, 16, 0,   255, 255);
    shade_fill(128, 16, 0,   128, 255);
    shade_fill(144, 16, 0,   0,   255);
    shade_fill(160, 16, 128, 0,   255);
    shade_fill(176, 16, 255, 0,   255);
    shade_fill(192, 16, 255, 0,   128);
    // fill in the remaining entries in the palette with black
    for (int index = 208; index < colors_.length; index++) {
      setColor(index, 0, 0, 0);
    }
  }
  
  /**
   * Creates a new Palette which is a copy of another given palette. This new
   * palette is a deep copy; it does not use references to the colors of the
   * palette to be copied but instead creates new entries for colors with the
   * same values as the colors in the palette to be copied.
   * 
   * @param palette the palette to be copied.
   */
  public Palette(Palette palette) {
    colors_ = new Color[PALETTE_MAX_SIZE];
    for (int index = 0; index < PALETTE_MAX_SIZE; index++) {
      Color color = palette.getColor(index);
      colors_[index] = new Color(color.getRed(),
                                 color.getGreen(),
                                 color.getBlue());
    }
  }
  
  /**
   * Returns the color in the specified index in the palette.
   * 
   * @param index the index in the palette of the color to return. 
   * @return the color in the specified index in the palette.
   */
  public Color getColor(int index) { return colors_[index]; }
  
  /**
   * Sets the color for a specified index in the palette based on the given RGB
   * components.
   * 
   * @param index the index in the palette of the color to set.
   * @param r the value for the red component of the color.
   * @param g the value for the green component of the color.
   * @param b the value for the blue component of the color.
   */
  public void setColor(int index, int r, int g, int b) {
    colors_[index] = clampColor(r, g, b);
  }
  
  /**
   * Sets the color for a specified index in the palette based on the given
   * color.
   * 
   * @param index the index in the palette of the color to set.
   * @param color the color whose values will be used to set the color entry in
   *              the palette.
   * @throws NullPointerException if the given color is null.
   */
  public void setColor(int index, Color color) {
    if (color == null) {
      throw new NullPointerException("Cannot set null as color in palette.");
    }  // else, we can proceed
    colors_[index] = clampColor(color);
  }
  
  /**
   * Returns an integer containing a color code that can be given to the SNES.
   * The color code consists of the last (least significant) 16 bits.
   * 
   * @param color the color for which to return a color code for.
   * @return an integer containing a color code that can be given to the SNES.
   * @throws NullPointerException if the given color is null.
   */
  public static int getSNESColorCode(Color color) {
    if (color == null) {
      throw new NullPointerException("Cannot get SNES color code for null.");
    }
    
    int r = color.getRed();
    int g = color.getGreen();
    int b = color.getBlue();
    int snes_code = 0;
    // shift and pack the RGB components into 0RRRRRGG GGGBBBBB
    snes_code = snes_code | (r >> 3);
    snes_code = snes_code << 5;
    snes_code = snes_code | (g >> 3);
    snes_code = snes_code << 5;
    snes_code = snes_code | (b >> 3);
    
    return snes_code;
  }
  
  /**
   * Returns a string with the color code that can be given to the SNES in a
   * hexadecimal format.
   * 
   * @param color color the color for which to return a color code string.
   * @return a hex string corresponding to the color code to give to the SNES.
   * @throws NullPointerException if the given color is null.
   */
  public static String getSNESColorCodeString(Color color) {
    if (color == null) {
      throw new NullPointerException("Cannot get SNES hex string for null.");
    }
    // 0x + a 4 digit hex string, padding with leading zeroes if needed
    return "0x" + String.format("%04x", getSNESColorCode(color)).toUpperCase();
  }
  
  /**
   * Takes a color and clamps it to a range that can be handled by the SNES.
   * A regular color (Java's Color) has 8 bits for each color channel while a
   * color code for the SNES has only 5 bits. The color given is clamped by
   * setting its 3 least significant bits to zero - the most significant 5 bits
   * are left as they are.
   * 
   * @param r the value for the red component of the color to be clamped.
   * @param g the value for the green component of the color to be clamped.
   * @param b the value for the blue component of the color to be clamped.
   * @return the resulting clamped color.
   */
  public static Color clampColor(int r, int g, int b) {
    r &= 0xF8;
    g &= 0xF8;
    b &= 0xF8;
    
    return new Color(r, g, b);
  }
  
  /**
   * Takes a color and clamps it to a range that can be handled by the SNES.
   * A regular color (Java's Color) has 8 bits for each color channel while a
   * color code for the SNES has only 5 bits. The color given is clamped by
   * setting its 3 least significant bits to zero - the most significant 5 bits
   * are left as they are.
   * 
   * @param color the color to be clamped.
   * @return the resulting clamped color.
   * @throws NullPointerException if the given color is null.
   */
  public static Color clampColor(Color color) {
    if (color == null) {
      throw new NullPointerException("Cannot get contrast of null.");
    }
    
    return clampColor(color.getRed(), color.getGreen(), color.getBlue());
  }
  
  /**
   * Takes a color and returns either black or white, depending on which of
   * those two colors should contrast best with the given color. This
   * determination is based off of the luma of the given color.
   * 
   * @param color the color to find get a contrast for.
   * @return a color, either black or white, which should contrast with the
   *         given color.
   * @throws NullPointerException if the given color is null.
   */
  public static Color contrastColor(Color color) {
    if (color == null) {
      throw new NullPointerException("Cannot get contrast of null.");
    }
    
    float luma = 0.2126f * (color.getRed() / 255f) +
                 0.7152f * (color.getGreen() / 255f) +
                 0.0722f * (color.getBlue() / 255f);
    
    if (luma > 0.5f) {
      return Color.BLACK;
    } else {
      return Color.WHITE;
    }
  }
  
  /**
   * Fills in a span of the palette from the given start index to the end index
   * with shades of a given color. The first entry is a dark (but not black) color,
   * followed by the given color, the given color at half intensity, black, then
   * increasingly more intense shades of the given color.
   * <p>
   * It is assumed that start_index is less than or equal to end_index and that
   * r, g, and b are valid color channel values.
   * 
   * @param start_index the index of the first entry to be filled.
   * @param length the number of entries, including start_index, to be used.
   * @param r the red channel's value for the color whose shade will be used.
   * @param g the green channel's value for the color whose shade will be used.
   * @param b the blue channel's value for the color whose shade will be used.
   */
  private void shade_fill(int start_index, int length, int r, int g, int b) {
    int index = start_index;
    int end_index = start_index + length - 1;
    int r_step = (int) Math.rint(r / length);
    int g_step = (int) Math.rint(g / length);
    int b_step = (int) Math.rint(b / length);
    
    if (index <= end_index) {
      setColor(index, 64, 64, 64);
      index++;
    }
    if (index <= end_index) {
      setColor(index, r, g, b);
      index++;
    }
    if (index <= end_index) {
      setColor(index, r / 2, g / 2, b / 2);
      index++;
    }
    if (index <= end_index) {
      setColor(index, 0, 0, 0);
      index++;
    }
    for (index = end_index; index >= start_index + 4; index--) {
      r -= r_step;
      g -= g_step;
      b -= b_step;
      setColor(index, r, g, b);
    }
  }
  
  /** The maximum number of entries that a palette can have. */
  public static final int PALETTE_MAX_SIZE = 256;
  
  private final Color[] colors_;
}
