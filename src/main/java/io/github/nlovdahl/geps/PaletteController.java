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
 * The controller for the palette. The controller handles making changes to the
 * palette's data model.
 * 
 * @author Nicholas Lovdahl
 * 
 * @see Palette
 * @see PaletteView
 */
public final class PaletteController {
  public PaletteController() {
    palette_ = new Palette();
  }
  
  public Color getColor(int index) { return palette_.getColor(index); }
  
  public void setColor(int index, int r, int g, int b) {
    palette_.setColor(index, r, g, b);
  }
  
  public void setColor(int index, Color color) {
    palette_.setColor(index, color);
  }
  
  public int getSNESColorCode(int index) {
    return Palette.getSNESColorCode(palette_.getColor(index));
  }
  
  public String getSNESColorCodeString(int index) {
    return Palette.getSNESColorCodeString(palette_.getColor(index));
  }
  
  private Palette palette_;
}
