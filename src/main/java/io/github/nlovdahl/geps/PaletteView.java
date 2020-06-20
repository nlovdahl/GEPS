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
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.Point;

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
    
    addMouseListener(new MouseListener() {
      @Override
      public void mouseClicked(MouseEvent event) {
        // if it is a right-click (button 3) or control is pressed
        if (event.getButton() == MouseEvent.BUTTON3 || event.isControlDown()) {
          chooseColor(event.getPoint());
        }
      }
      // do nothing for other situations
      @Override public void mousePressed(MouseEvent e) {}
      @Override public void mouseReleased(MouseEvent e) {}
      @Override public void mouseEntered(MouseEvent e) {}
      @Override public void mouseExited(MouseEvent e) {}
    });
    
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
  
  // disallow the user from editing the cells in the table
  @Override
  public boolean isCellEditable(int row, int column) { return false; }
  
  /**
   * Takes a point and allows the user to choose a different color for the cell
   * at that point. This corresponds to changing the color entry in the palette.
   * After this, the table is repainted to reflect this change.
   * 
   * @param point the selected point which should correspond to the cell / color
   *              entry being chosen.
   */
  private void chooseColor(Point point) {
    int index = 16 * rowAtPoint(point) + columnAtPoint(point);
    palette_controller_.setColor(index, 0, 255, 0);
    repaint();
  }
  
  // control how the cells in the table are shown
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
