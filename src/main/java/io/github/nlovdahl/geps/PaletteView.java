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
import javax.swing.JFrame;
import javax.swing.BorderFactory;
import java.awt.Color;
import java.awt.Font;
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
  public PaletteView(JFrame parent_frame,
                     PaletteController palette_controller) {
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
        // else, if it is a left (button 1) double-click
        } else if (event.getButton() == MouseEvent.BUTTON1 &&
                   event.getClickCount() == 2) {
          changeSelection(event.getPoint());
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
    }  // else, our parameters are valid and we can proceed
    
    palette_controller_ = palette_controller;
    color_chooser_ = new SNESColorChooser(parent_frame);
  }
  
  // disallow the user from editing the cells in the table
  @Override
  public boolean isCellEditable(int row, int column) { return false; }
  
  /**
   * Takes a point and allows the user to choose a different color for the cell
   * at that point. This corresponds to changing the color entry in the palette.
   * After this, the altered cell is repainted to reflect this change.
   * 
   * @param point the selected point which should correspond to the cell / color
   *              entry being chosen.
   */
  private void chooseColor(Point point) {
    // get the current color
    int row = rowAtPoint(point);
    int column = columnAtPoint(point);
    int index = 16 * row + column;
    Color chosen_color = palette_controller_.getColor(index);
    // allow the user to select a color, starting with the current color
    chosen_color = color_chooser_.chooseColor(chosen_color);
    
    if (chosen_color != null) {  // if the user made a choice (not null)
      // update the palette controller and repaint the appropriate cell
      palette_controller_.setColor(index, chosen_color);
      repaint(getCellRect(row, column, true));  // only repaint the one cell
    }
  }
  
  /**
   * Takes a point and changes the selection from the palette to include the
   * cell at that point. After this, the entire table is repainted to reflect
   * the change (even if there isn't any).
   * 
   * @param point the selected point which should correspond to the cell / color
   *              entry being chosen.
   */
  private void changeSelection(Point point) {
    int index = 16 * rowAtPoint(point) + columnAtPoint(point);
    palette_controller_.setSelection(index);
    repaint();  // repaint the entire table
  }
  
  // control how the cells in the table are shown
  private final class PaletteRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(
        JTable table, Object value, boolean is_selected, boolean has_focus,
        int row, int column) {
      JLabel cell = (JLabel) super.getTableCellRendererComponent(
          table, value, is_selected, has_focus, row, column);
      // retrieve the corresponding color from the palette controller
      int index = row * 16 + column;
      Color cell_color = palette_controller_.getColor(index);
      Color contrast_color = Palette.contrastColor(cell_color);
      
      // the corresponding palette entry decides the appearance of the cell
      cell.setBackground(cell_color);
      cell.setForeground(contrast_color);
      cell.setHorizontalAlignment(JLabel.CENTER);
      cell.setText(Palette.getSNESColorCodeString(cell_color));
      
      // whether there is a border is decided by the selection and position
      if (palette_controller_.isIndexInSelection(index)) {
        int top, left, bottom, right;
        // set the size of each part of the border...
        if (row == 0 || palette_controller_.isIndexInSelection(index - 16)) {
          top = 0;
        } else {
          top = THICK_BORDER_SIZE;
        }
        if (column == 0 || palette_controller_.isIndexInSelection(index - 1)) {
          left = 0;
        } else {
          left = THICK_BORDER_SIZE;
        }
        if (row == 15 || palette_controller_.isIndexInSelection(index + 16)) {
          bottom = 0;
        } else {
          bottom = THICK_BORDER_SIZE;
        }
        if (column == 15 || palette_controller_.isIndexInSelection(index + 1)) {
          right = 0;
        } else {
          right = THICK_BORDER_SIZE;
        }
        // draw a border between selected and non-selected, except on the edges
        cell.setBorder(BorderFactory.createMatteBorder(top, left, bottom, right,
                                                       contrast_color));
      }
      
      return cell;
    }
    
    private static final int THICK_BORDER_SIZE = 2;
  }
  
  private final PaletteController palette_controller_;
  private final SNESColorChooser color_chooser_;
}
