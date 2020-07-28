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
 * A collection of methods which can be used to create palettes. A palette can
 * be either encoded into binary data, or decoded from binary data.
 * 
 * @author Nicholas Lovdahl
 * 
 * @see Palette
 */
public class PaletteInterpreter {
  /**
   * Creates an array of bytes from the given palette. This data corresponds to
   * the given palette and can be reconstructed using
   * {@link PaletteInterpreter#decodeBytes(byte[])}.
   * 
   * @param palette the palette to create the bytes from.
   * @return an array of bytes containing data corresponding to the palette.
   * @throws NullPointerException if palette is null.
   */
  public static byte[] encodePalette(Palette palette) {
    if (palette == null) {
      throw new NullPointerException("Cannot create data from null palette.");
    }  // else, the palette should be valid
    
    // get space for 2 bytes per entry in the palette
    byte[] palette_data = new byte[2 * Palette.PALETTE_MAX_SIZE];
    
    for (int entry = 0; entry < Palette.PALETTE_MAX_SIZE; entry++) {
      int color_code = Palette.getSNESColorCode(palette.getColor(entry));
      byte lower_byte = (byte) color_code;
      byte upper_byte = (byte) (color_code >> 8);
      
      // SNES is little-endian, so store the lower byte first, then the upper
      palette_data[2 * entry] = lower_byte;
      palette_data[2 * entry + 1] = upper_byte;
    }
    
    return palette_data;
  }
  
  /**
   * Takes an array of bytes and interprets them to create a palette. If there
   * is not enough data to fill an entire palette, the data will be padded with
   * enough extra zeroes to complete a palette.
   * 
   * @param palette_data the bytes to be interpreted.
   * @return the resulting palette interpreted from the given bytes.
   * @throws NullPointerException if palette_data is null.
   */
  public static Palette decodeBytes(byte[] palette_data) {
    if (palette_data == null) {
      throw new NullPointerException("Cannot interpret null byte array.");
    }  // else, the data should be valid
    
    // resize the array if it does not have enough data
    if (palette_data.length < Palette.PALETTE_MAX_SIZE) {
      byte[] padded_data = new byte[Palette.PALETTE_MAX_SIZE];
      System.arraycopy(palette_data, 0, padded_data, 0, palette_data.length);
      palette_data = padded_data;
    }
    
    Palette palette = new Palette();
    
    for (int entry = 0; entry < Palette.PALETTE_MAX_SIZE; entry++) {
      // SNES is little-endian, so we get the lower byte first, then the upper
      int lower = palette_data[2 * entry] & 0xFF;
      int upper = palette_data[2 * entry + 1] & 0xFF;
      int color_code = (upper << 8) | lower;
      
      // extract the values for the color channels next (remember, it's BGR555)
      int r = (color_code & 0x1F) << 3;
      color_code = color_code >> 5;
      int g = (color_code & 0x1F) << 3;
      color_code = color_code >> 5;
      int b = (color_code & 0x1F) << 3;
      
      palette.setColor(entry, r, g, b);
    }
    
    return palette;
  }
}
