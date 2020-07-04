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
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;

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
    if (tileset_controller == null) {
      throw new NullPointerException(
        "Cannot create canvas view with null tileset controller.");
    }  // else, we should have a good palette controller to pair with
    tileset_controller_ = tileset_controller;
    redrawCanvasImage();
  }
  
  @Override
  public void paintComponent(Graphics graphics) {
    super.paintComponent(graphics);
    
    redrawCanvasImage();
    graphics.drawImage(canvas_image_, 0, 0, null);
  }
  
  private void redrawCanvasImage() {
    BufferedImage base_image = new BufferedImage(
      tileset_controller_.getTilesetWidth() * Tileset.TILE_WIDTH,
      tileset_controller_.getTilesetHeight() * Tileset.TILE_HEIGHT,
      BufferedImage.TYPE_INT_ARGB);
    
    for (int x = 0; x < base_image.getWidth(); x++) {
      for (int y = 0; y < base_image.getHeight(); y++) {
        Color pixel_color = tileset_controller_.getPixelColor(x, y);
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
    canvas_image_ = scale_op.filter(base_image, canvas_image_);
    
    setPreferredSize(new Dimension(canvas_image_.getWidth(), canvas_image_.getHeight()));
  }
  
  private double canvas_scale_factor_ = 4.0;
  private BufferedImage canvas_image_;
  
  private final TilesetController tileset_controller_;
}
