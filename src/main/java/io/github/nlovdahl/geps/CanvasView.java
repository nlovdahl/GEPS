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
import javax.swing.JPanel;

/**
 * A view for the canvas, a subpart of the tileset through which the user makes
 * changes.
 * 
 * @author Nicholas Lovdahl
 * 
 * @see TilesetView
 */
public final class CanvasView extends JPanel {
  public CanvasView(TilesetController tileset_controller) {
    tileset_controller_ = tileset_controller;
    setBackground(Color.YELLOW);
  }
  
  private final TilesetController tileset_controller_;
}
