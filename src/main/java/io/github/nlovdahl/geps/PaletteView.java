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
    setFocusable(false);
    setFillsViewportHeight(true);
    setPreferredScrollableViewportSize(getPreferredSize());
    setTableHeader(null);  // do not show a table header
    
    setDefaultRenderer(Object.class, new PaletteRenderer());
    
    addMouseListener(new MouseListener() {
      @Override public void mousePressed(MouseEvent event) {
        // if it is a right-click (button 3) or control is pressed
        if (event.getButton() == MouseEvent.BUTTON3 || event.isControlDown()) {
          chooseColor(event.getPoint());
        // else, if it is a left-click (button 1)
        } else if (event.getButton() == MouseEvent.BUTTON1) {
          changeSelection(event.getPoint());
        }
      }
      // do nothing for other situations
      @Override public void mouseClicked(MouseEvent event) {}
      @Override public void mouseReleased(MouseEvent event) {}
      @Override public void mouseEntered(MouseEvent event) {}
      @Override public void mouseExited(MouseEvent event) {}
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
    int chosen_index = 16 * row + column;
    Color old_color = palette_controller_.getColor(chosen_index);
    // allow the user to select a color, starting with the current color
    Color new_color = color_chooser_.chooseColor(old_color);
    
    // if the user made a choice (not null) and it is different from the old one
    if (new_color != null && old_color.getRGB() != new_color.getRGB()) {
      // update the palette controller and repaint the appropriate cell
      palette_controller_.setColor(chosen_index, new_color);
      repaint(getCellRect(row, column, true));  // only repaint the one cell
      
      // record that the state of the palette will have changed
      firePropertyChange(NEW_PALETTE_STATE, null, null);
      // if the change was within the subpalette, record that too
      if (palette_controller_.isIndexInSubpalette(chosen_index)) {
        int selection_index = palette_controller_.getSubpaletteStartIndex();
        // use null to represent no new subpalette (new contents only)
        firePropertyChange(NEW_PALETTE_SUBPALETTE, selection_index, null);
      }
    }
  }
  
  /**
   * Takes a point and changes the subpalette and selected color based on the
   * corresponding palette entry. After this, the entire table is repainted to
   * reflect the change (even if there isn't any).
   * 
   * @param point the selected point which should correspond to the cell / color
   *              entry being chosen.
   */
  private void changeSelection(Point point) {
    int old_index = palette_controller_.getSubpaletteStartIndex();
    int chosen_index = 16 * rowAtPoint(point) + columnAtPoint(point);
    palette_controller_.setSubpalette(chosen_index);
    palette_controller_.setSelectedColor(chosen_index);
    int new_index = palette_controller_.getSubpaletteStartIndex();
    
    // repaint the entire table and record the subpalette change
    repaint();
    firePropertyChange(NEW_PALETTE_SUBPALETTE, old_index, new_index);
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
      
      // borders are decided by the subpalette, selected color, and position
      if (palette_controller_.isIndexInSubpalette(index)) {
        if (index == palette_controller_.getSelectedColorIndex()) {
          // draw a solid border if it corresponds to the selected color
          cell.setBorder(BorderFactory.createDashedBorder(
            contrast_color, BORDER_THICKNESS, BORDER_LENGTH, BORDER_SPACING,
            false));
        } else {  // else, draw around the current subpalette (except the edges)
          int top = 0, left = 0, bottom = 0, right = 0;
          // set the border size (to exist) if needed...
          if (row != 0 &&
              !palette_controller_.isIndexInSubpalette(index - 16)) {
            top = BORDER_THICKNESS;
          }
          if (column != 0 &&
              !palette_controller_.isIndexInSubpalette(index - 1)) {
            left = BORDER_THICKNESS;
          }
          if (row != 15 &&
              !palette_controller_.isIndexInSubpalette(index + 16)) {
            bottom = BORDER_THICKNESS;
          }
          if (column != 15 &&
              !palette_controller_.isIndexInSubpalette(index + 1)) {
            right = BORDER_THICKNESS;
          }
          
          cell.setBorder(BorderFactory.createMatteBorder(
            top, left, bottom, right, contrast_color));
        }
      }
      
      return cell;
    }
    
    private static final int BORDER_THICKNESS = 2;
    private static final int BORDER_LENGTH = 3;
    private static final int BORDER_SPACING = 2;
  }
  
  /**
   * The string for a property change event which denotes a change in the state
   * of the palette.
   */
  public static final String NEW_PALETTE_STATE = "paletteStateUpdate";
  /**
   * The string for a property change event which denotes either a change in at
   * least one of the colors in the current subpalette or a change in the
   * subpalette itself.
   */
  public static final String NEW_PALETTE_SUBPALETTE = "paletteSubpaletteUpdate";
  
  private final PaletteController palette_controller_;
  private final SNESColorChooser color_chooser_;
}
