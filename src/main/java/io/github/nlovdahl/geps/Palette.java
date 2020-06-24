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
 * The data model for a palette. A palette is composed of a 256 color entries
 * which can be used by a {@link Tileset} to determine what colors that tileset
 * should have.
 * 
 * @author Nicholas Lovdahl
 * 
 * @see PaletteController
 * @see PaletteView
 */
public final class Palette {
  /** Creates a new palette with default colors as its entries. */
  public Palette() {
    colors_ = new Color[PALETTE_MAX_SIZE];
    for (int index = 0; index < PALETTE_MAX_SIZE; index++) {
      colors_[index] = new Color(255, 0, 0);
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
   * The color code consists of the last (least significant) 16 bits
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
   * @param color color the color for which to return a color code string for.
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
  
  /** The maximum number of entries that a palette can have. */
  public static final int PALETTE_MAX_SIZE = 256;
  
  private final Color[] colors_;
}
