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
  public Palette() {
    colors_ = new Color[PALETTE_MAX_SIZE];
    for (int index = 0; index < PALETTE_MAX_SIZE; index++) {
      colors_[index] = new Color(255, 0, 0);
    }
  }
  
  public Palette(Palette palette) {
    colors_ = new Color[PALETTE_MAX_SIZE];
    for (int index = 0; index < PALETTE_MAX_SIZE; index++) {
      Color color = palette.getColor(index);
      colors_[index] = new Color(color.getRed(),
                                 color.getGreen(),
                                 color.getBlue());
    }
  }
  
  public Color getColor(int index) { return colors_[index]; }
  
  public void setColor(int color, int r, int g, int b) {
    colors_[color] = new Color(r, g, b);
  }
  
  public static final int PALETTE_MAX_SIZE = 256;
  
  Color[] colors_;
}
