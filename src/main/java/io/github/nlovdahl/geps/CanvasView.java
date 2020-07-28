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
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import java.awt.Point;

/**
 * A view for the canvas, a subpart of the tileset through which the user makes
 * changes.
 * 
 * @author Nicholas Lovdahl
 * 
 * @see TilesetView
 */
public final class CanvasView extends JPanel implements Scrollable {
  public CanvasView(TilesetController tileset_controller,
                    PaletteController palette_controller, double scale_factor) {
    if (tileset_controller == null) {
      throw new NullPointerException(
        "Cannot create canvas view with null tileset controller.");
    } else if (palette_controller == null) {
      throw new NullPointerException(
        "Cannot create canvas view with null palette controller.");
    } else if (scale_factor <= 0) {
      throw new IllegalArgumentException(
        "Cannot have a scale factor less than or equal to zero.");
    } // else, we should have a good palette controller to pair with
    tileset_controller_ = tileset_controller;
    palette_controller_ = palette_controller;
    canvas_scale_factor_ = scale_factor;
    
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
      // change the cursor to crosshairs if it enters the canvas
      @Override
      public void mouseEntered(MouseEvent event) {
        setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
      }
      // return the cursor to default when it leaves the canvas
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
    
    redrawCanvasImage();
  }
  
  /**
   * Gets the scaling factor for the canvas.
   * 
   * @return the factor for the canvas.
   */
  public double getScaleFactor() { return canvas_scale_factor_; }
  
  /**
   * Sets the scaling factor for the canvas to the given value.
   * 
   * @param scale_factor the new factor to scale the canvas by.
   * @throws IllegalArgumentException if the new factor is less than or equal to
   *         zero.
   */
  public void setScaleFactor(double scale_factor) {
    if (scale_factor <= 0) {
      throw new IllegalArgumentException(
        "Scale factor must be greater than zero.");
    }  // else, the scale factor should be valud
    
    canvas_scale_factor_ = scale_factor;
  }
  
  @Override
  public void paintComponent(Graphics graphics) {
    super.paintComponent(graphics);
    
    redrawCanvasImage();
    graphics.drawImage(canvas_image_, 0, 0, null);
  }
  
  @Override
  public Dimension getPreferredScrollableViewportSize() {
    return canvas_image_size_;
  }
  
  @Override
  public int getScrollableBlockIncrement(Rectangle visibleRect,
                                         int orientation, int direction) {
    if (orientation == SwingConstants.VERTICAL) {
      return (int) canvas_scale_factor_ * Tileset.TILE_HEIGHT;
    } else {  // else, the orientation must be horizontal
      return (int) canvas_scale_factor_ * Tileset.TILE_WIDTH;
    }
  }
  
  @Override
  public boolean getScrollableTracksViewportHeight() { return false; }
  
  @Override
  public boolean getScrollableTracksViewportWidth() { return false; }
  
  @Override
  public int getScrollableUnitIncrement(Rectangle visibleRect,
                                        int orientation, int direction) {
    return (int) canvas_scale_factor_;
  }
  
  private void beginStroke(Point point) {
    // convert coordinates to pixels in the tileset and pass them on
    int mouse_x = point.x;
    int mouse_y = point.y;
    int tileset_x = (int) Math.rint(mouse_x / canvas_scale_factor_);
    int tileset_y = (int) Math.rint(mouse_y / canvas_scale_factor_);
    
    tileset_controller_.beginStroke(
      tileset_x, tileset_y,
      palette_controller_.getSelectedColorSubpaletteIndex());
    
    repaint();
  }
  
  private void addToStroke(Point point) {
    // convert coordinates to pixels in the tileset and pass them on
    int mouse_x = point.x;
    int mouse_y = point.y;
    int tileset_x = (int) Math.rint(mouse_x / canvas_scale_factor_);
    int tileset_y = (int) Math.rint(mouse_y / canvas_scale_factor_);
    
    tileset_controller_.addToStroke(tileset_x, tileset_y);
      
    repaint();
  }
  
  private void endStroke(Point point) {
    // convert coordinates to pixels in the tileset and pass them on
    int mouse_x = point.x;
    int mouse_y = point.y;
    int tileset_x = (int) Math.rint(mouse_x / canvas_scale_factor_);
    int tileset_y = (int) Math.rint(mouse_y / canvas_scale_factor_);
    
    tileset_controller_.endStroke(tileset_x, tileset_y);
    
    repaint();
    
    // fire a property change event just in case the state has been changed
    firePropertyChange(NEW_CANVAS_STATE, null, null);
  }
  
  private void redrawCanvasImage() {
    BufferedImage base_image = new BufferedImage(
      tileset_controller_.getWidthInPixels(),
      tileset_controller_.getHeightInPixels(),
      BufferedImage.TYPE_INT_ARGB);
    
    for (int x = 0; x < base_image.getWidth(); x++) {
      for (int y = 0; y < base_image.getHeight(); y++) {
        Color pixel_color = tileset_controller_.getPixelColor(
          x, y, palette_controller_);
        if (pixel_color != null) {  // use the color if coordinates are valid
          base_image.setRGB(x, y, pixel_color.getRGB());
        } else {  // else, draw a transparent color (no tileset here)
          base_image.setRGB(x, y, 0);
        }
      }
    }
    
    AffineTransform scaler = new AffineTransform();
    scaler.scale(canvas_scale_factor_, canvas_scale_factor_);
    AffineTransformOp scale_op = new AffineTransformOp(
      scaler, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
    
    canvas_image_ = scale_op.filter(base_image, null);
    canvas_image_size_ = new Dimension(canvas_image_.getWidth(),
                                       canvas_image_.getHeight());
    setPreferredSize(canvas_image_size_);
    revalidate();  // the new area will be 'dirty', repaint it all
  }
  
  /**
   * The string for a property change event which denotes a change in the state
   * of the canvas.
   */
  public static final String NEW_CANVAS_STATE = "canvasStateUpdate";
  
  private double canvas_scale_factor_;
  private BufferedImage canvas_image_;
  private Dimension canvas_image_size_;
  
  private final TilesetController tileset_controller_;
  private final PaletteController palette_controller_;
}
