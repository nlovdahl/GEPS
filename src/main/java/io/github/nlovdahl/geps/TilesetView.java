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

import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import java.awt.Rectangle;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import java.awt.Point;

/**
 * A view of the tileset through which the user can make changes. The view is
 * scrollable with a unit increment the size of one pixel in the tileset and a
 * block increment the size of an entire tile.
 * 
 * @author Nicholas Lovdahl
 */
public final class TilesetView extends JPanel implements Scrollable {
  /**
   * Creates a view for the tileset using the tileset and palette from the given
   * controllers. This new view is scaled by the scale factor given, although
   * this can be changed later.
   * 
   * @param tileset_controller the controller for the tileset to use.
   * @param palette_controller the controller for the palette to use.
   * @param scale_factor the factor to scale the tileset by.
   * @throws NullPointerException if tileset_controller or palette_controller
   *         are null.
   * @throws IllegalArgumentException if scale_factor is less than one.
   */
  public TilesetView(TilesetController tileset_controller,
                    PaletteController palette_controller, int scale_factor) {
    if (tileset_controller == null) {
      throw new NullPointerException("Cannot use a null tileset controller.");
    } else if (palette_controller == null) {
      throw new NullPointerException("Cannot use a null palette controller.");
    } else if (scale_factor < 1) {
      throw new IllegalArgumentException("Cannot use scaler less than one.");
    } // else, we should have a good palette controller to pair with
    
    tileset_controller_ = tileset_controller;
    palette_controller_ = palette_controller;
    scale_factor_ = scale_factor;
    
    checkDimensions();  // this will setup the base image and dimensions
    
    // setup listeners for when the user interacts with the tileset view
    addMouseListener(new MouseListener() {
      @Override public void mousePressed(MouseEvent event) {
        // if the left mouse button (button 1) is pressed
        if (event.getButton() == MouseEvent.BUTTON1) {
          beginStroke(event.getPoint());
        }
      }
      @Override public void mouseReleased(MouseEvent event) {
        // if the left mouse button (button 1) is released
        if (event.getButton() == MouseEvent.BUTTON1) {
          endStroke(event.getPoint());
        }
      }
      // change the cursor to crosshairs if it enters the view
      @Override
      public void mouseEntered(MouseEvent event) {
        setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
      }
      // return the cursor to default when it leaves the view
      @Override
      public void mouseExited(MouseEvent event) {
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      }
      // do nothing for other situations
      @Override public void mouseClicked(MouseEvent event) {}
    });
    
    addMouseMotionListener(new MouseMotionListener() {
      @Override public void mouseDragged(MouseEvent event) {
        addToStroke(event.getPoint());
      }
      // do nothing for other situations
      @Override public void mouseMoved(MouseEvent event) {}
    });
  }
  
  /**
   * Gets the scaling factor for the tileset view.
   * 
   * @return the scaling factor.
   */
  public int getScaleFactor() { return scale_factor_; }
  
  /**
   * Sets the scaling factor for the tileset view to the given value.
   * 
   * @param scale_factor the new factor to scale the tileset by.
   * @throws IllegalArgumentException if scale_factor is less than one.
   */
  public void setScaleFactor(int scale_factor) {
    if (scale_factor < 1) {
      throw new IllegalArgumentException("Scale factor must be at least one.");
    }  // else, the scale factor should be valid
    
    scale_factor_ = scale_factor;
  }
  
  @Override
  public void paintComponent(Graphics graphics) {
    super.paintComponent(graphics);
    
    checkDimensions();  // make sure the base image is the right size
    
    // update the area of the base image which will be contained in the clip
    Rectangle clipArea = graphics.getClipBounds();
    if (clipArea != null) {  // if there is a clip area
      /* overshoot the tileset area corresponding to the clip to avoid missing
      any straddled tileset pixels */
      int tileset_x_min = Math.max(clipArea.x / scale_factor_ - 1, 0);
      int tileset_y_min = Math.max(clipArea.y / scale_factor_ - 1, 0);
      int tileset_x_max =
        Math.min((clipArea.x + clipArea.width) / scale_factor_ + 1,
                 tileset_controller_.getWidthInPixels());
      int tileset_y_max =
        Math.min((clipArea.y + clipArea.height) / scale_factor_ + 1,
                 tileset_controller_.getHeightInPixels());
      
      for (int y = tileset_y_min; y < tileset_y_max; y++) {
        for (int x = tileset_x_min; x < tileset_x_max; x++) {
          Color pixel_color = tileset_controller_.getPixelColor(
                                x, y, palette_controller_);
          if (pixel_color != null) {  // use the color if coordinates are valid
            base_image_.setRGB(x, y, pixel_color.getRGB());
          } else {  // else, draw a transparent color (no tileset here)
            base_image_.setRGB(x, y, 0);
          }
        }
      }
    }
    
    // finally, draw the image while scaling it on the fly
    graphics.drawImage(base_image_, 0, 0, scaled_image_size_.width,
                       scaled_image_size_.height, getBackground(), null);
  }
  
  @Override
  public Dimension getPreferredScrollableViewportSize() {
    return scaled_image_size_;
  }
  
  @Override
  public int getScrollableBlockIncrement(Rectangle visibleRect,
                                         int orientation, int direction) {
    if (orientation == SwingConstants.VERTICAL) {
      return scale_factor_ * Tileset.TILE_HEIGHT;
    } else {  // else, the orientation must be horizontal
      return scale_factor_ * Tileset.TILE_WIDTH;
    }
  }
  
  @Override
  public boolean getScrollableTracksViewportHeight() { return false; }
  
  @Override
  public boolean getScrollableTracksViewportWidth() { return false; }
  
  @Override
  public int getScrollableUnitIncrement(Rectangle visibleRect,
                                        int orientation, int direction) {
    return scale_factor_;
  }
  
  private void beginStroke(Point point) {
    // convert coordinates to pixels in the tileset and pass them on
    int mouse_x = point.x;
    int mouse_y = point.y;
    int tileset_x = mouse_x / scale_factor_;
    int tileset_y = mouse_y / scale_factor_;
    
    tileset_controller_.beginStroke(
      tileset_x, tileset_y,
      palette_controller_.getSelectedColorSubpaletteIndex());
    
    // just paint the one pixel by setting the last tileset coordinates first
    last_tileset_x_ = tileset_x;
    last_tileset_y_ = tileset_y;
    repaintTilesetArea(tileset_x, tileset_y);
  }
  
  private void addToStroke(Point point) {
    // convert coordinates to pixels in the tileset and pass them on
    int mouse_x = point.x;
    int mouse_y = point.y;
    int tileset_x = mouse_x / scale_factor_;
    int tileset_y = mouse_y / scale_factor_;
    
    tileset_controller_.addToStroke(tileset_x, tileset_y);
    
    repaintTilesetArea(tileset_x, tileset_y);
    
    last_tileset_x_ = tileset_x;
    last_tileset_y_ = tileset_y;
  }
  
  private void endStroke(Point point) {
    // convert coordinates to pixels in the tileset and pass them on
    int mouse_x = point.x;
    int mouse_y = point.y;
    int tileset_x = mouse_x / scale_factor_;
    int tileset_y = mouse_y / scale_factor_;
    
    tileset_controller_.endStroke(tileset_x, tileset_y);
    
    repaintTilesetArea(tileset_x, tileset_y);
    
    last_tileset_x_ = tileset_x;
    last_tileset_y_ = tileset_y;
    
    // fire a property change event just in case the state has been changed
    firePropertyChange(NEW_TILESET_STATE, null, null);
  }
  
  /**
   * Repaints an area of the base image corresponding to the rectangular area
   * between the given tileset coordinates and the last ones. This method does
   * not change the last tileset coordinates used.
   * 
   * @param new_x the horizontal component of the new tileset coordinates.
   * @param new_y the vertical component of the new tileset coordinates.
   */
  private void repaintTilesetArea(int new_x, int new_y) {
    int scaled_image_x_min =
      Math.min(new_x, last_tileset_x_) * scale_factor_ - 1;
    int scaled_image_y_min =
      Math.min(new_y, last_tileset_y_) * scale_factor_ - 1;
    int scaled_image_x_max =
      (Math.max(new_x, last_tileset_x_) + 1) * scale_factor_ + 1;
    int scaled_image_y_max =
      (Math.max(new_y, last_tileset_y_) + 1) * scale_factor_ + 1;
    
    repaint(scaled_image_x_min, scaled_image_y_min,
            scaled_image_x_max - scaled_image_x_min,
            scaled_image_y_max - scaled_image_y_min);
  }
  
  private void checkDimensions() {
    if (base_image_ == null ||
        tileset_controller_.getWidthInPixels() != base_image_.getWidth() ||
        tileset_controller_.getHeightInPixels() != base_image_.getHeight()) {
      base_image_ = new BufferedImage(tileset_controller_.getWidthInPixels(),
                                      tileset_controller_.getHeightInPixels(),
                                      BufferedImage.TYPE_INT_ARGB);
      // if this is triggered, then the dimensions (up next) will be changed too
    }
    
    int correct_width =
      tileset_controller_.getWidthInPixels() * scale_factor_;
    int correct_height =
      tileset_controller_.getHeightInPixels() * scale_factor_;
    
    if (scaled_image_size_ == null ||
        correct_width != scaled_image_size_.width ||
        correct_height != scaled_image_size_.height) {
      scaled_image_size_ = new Dimension(correct_width, correct_height);
      // correct the size of the view and declare it 'dirty' (repaint it all)
      setPreferredSize(scaled_image_size_);
      revalidate();
    }
  }
  
  /** The string for a property change event which denotes a change in the state
   of the tileset. */
  public static final String NEW_TILESET_STATE = "tilesetStateUpdate";
  
  private int last_tileset_x_;
  private int last_tileset_y_;
  
  private BufferedImage base_image_;
  private int scale_factor_;
  private Dimension scaled_image_size_;
  
  private final TilesetController tileset_controller_;
  private final PaletteController palette_controller_;
}
