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
  /**
   * Creates a new palette with default colors as its entries.
   */
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
   * Returns an integer containing a color code that can be given to the SNES.
   * The color code consists of the last (least significant) 16 bits
   * 
   * @param index the index in the palette of the color to return a SNES color
   *              code for.
   * @return an integer containing a color code that can be given to the SNES.
   */
  public int getSNESColorCode(int index) {
    int r = colors_[index].getRed();
    int g = colors_[index].getGreen();
    int b = colors_[index].getBlue();
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
   * @param index the index in the palette of the color to return a SNES color
   *              code for.
   * @return a hex string corresponding to the color code to give to the SNES.
   */
  public String getSNESColorCodeString(int index) {
    return "0x" + Integer.toHexString(getSNESColorCode(index));
  }
  
  /**
   * Sets the color for a specified index in the palette based on the given RGB
   * components.
   * 
   * @param index the index in the Palette of the color to set.
   * @param r the value for the red component of the color.
   * @param g the value for the green component of the color.
   * @param b the value for the blue component of the color.
   */
  public void setColor(int index, int r, int g, int b) {
    colors_[index] = new Color(r, g, b);
  }
  
  public static final int PALETTE_MAX_SIZE = 256;
  
  Color[] colors_;
}
