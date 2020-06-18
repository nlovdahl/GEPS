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

/**
 * The controller for the tileset. The controller handles making changes to the
 * tileset's data model.
 * 
 * @author Nicholas Lovdahl
 * 
 * @see Tileset
 * @see TilesetView
 * @see CanvasView
 */
public final class TilesetController {
  public TilesetController() {
    tileset_ = new Tileset();
  }
  
  public int getBPP() { return tileset_.getBPP(); }
  
  private Tileset tileset_;
}
