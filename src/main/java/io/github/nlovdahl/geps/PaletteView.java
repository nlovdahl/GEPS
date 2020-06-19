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

import javax.swing.JTable;
import javax.swing.JLabel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Component;

/**
 * The view for the palette.
 * 
 * @author Nicholas Lovdahl
 */
public final class PaletteView extends JTable {
  public PaletteView(PaletteController palette_controller, int bpp) {
    super(16, 16);  // create a table with 16 rows / 16 columns
    setRowSelectionAllowed(false);
    setColumnSelectionAllowed(false);
    setFillsViewportHeight(true);
    setPreferredScrollableViewportSize(getPreferredSize());
    setTableHeader(null);  // do not show a table header
    
    setDefaultRenderer(Object.class, new PaletteRenderer());
    
    if (palette_controller == null) {
      throw new NullPointerException(
        "Cannot create PaletteView with null PaletteController.");
    } else if (bpp < 1) {
      throw new IllegalArgumentException(
        "Cannot create PaletteView with less than 1 bit per pixel.");
    } else if (bpp > 8) {
      throw new IllegalArgumentException(
        "Cannot create PaletteView with more than 8 bits per pixel.");
    }  // else, our parameters are valid and we can proceed
    
    palette_controller_ = palette_controller;
    bpp_ = bpp;
  }
  
  public void setBPP(int bpp) {
    if (bpp < 1) {
      throw new IllegalArgumentException(
        "Cannot create PaletteView with less than 1 bit per pixel.");
    } else if (bpp > 8) {
      throw new IllegalArgumentException(
        "Cannot create PaletteView with more than 8 bits per pixel.");
    }  // else, our parameters are valid and we can proceed
    
    bpp_ = bpp;
  }
  
  private final class PaletteRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(
        JTable table, Object value, boolean is_selected, boolean has_focus,
        int row, int column) {
      JLabel cell = (JLabel) super.getTableCellRendererComponent(
          table, value, is_selected, has_focus, row, column);
      // the corresponding palette entry decides the appearance of the cell
      int index = row * 16 + column;
      cell.setBackground(palette_controller_.getColor(index));
      cell.setHorizontalAlignment(JLabel.CENTER);
      cell.setText(palette_controller_.getSNESColorCodeString(index));
      
      return cell;
    }
  }
  
  private final PaletteController palette_controller_;
  private int bpp_;
}
