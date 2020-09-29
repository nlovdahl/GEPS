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

import java.awt.Component;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JButton;
import javax.swing.border.TitledBorder;
import javax.swing.BoxLayout;
import javax.swing.Box;
import java.awt.event.ActionEvent;
import java.util.Properties;
import java.util.stream.Collectors;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * A dialog window which presents the user with some information about the
 * program and the program's license.
 * 
 * @author Nicholas Lovdahl
 */
public final class AboutWindow extends JDialog {
  /**
   * Creates a dialog which contains information about the program and its
   * licensing. This dialog blocks input to the parent while it is on screen.
   * 
   * @param parent the frame which owns this dialog.
   */
  public AboutWindow(JFrame parent) {
    // create dialog that blocks input and uses the parent's look & feel
    super(parent, "GEPS - About", true, parent.getGraphicsConfiguration());
    
    // load project properties and license text
    Properties properties = new Properties();
    InputStream properties_stream =
      getClass().getResourceAsStream("geps.properties");
    InputStream license_stream = getClass().getResourceAsStream("license.txt");
    // get a reader for the license stream if possible
    BufferedReader license_reader = null;
    if (license_stream != null) {
      license_reader =
        new BufferedReader(new InputStreamReader(license_stream));
    }
    
    // start with default messages in case of a problem
    String version_text = "GEPS Version: Unknown";
    String license_text = "There was a problem loading the license.";
    
    try {
      // load the version and license text if we can
      if (properties_stream != null) {
        properties.load(properties_stream);
        version_text = "GEPS Version: " +
                       properties.getProperty("version", "Unknown");
      }
      
      if (license_reader != null) {
        license_text = license_reader.lines().collect(Collectors.joining("\n"));
      }
    } catch (IOException io_exception) {
      JOptionPane.showMessageDialog(
        parent, io_exception.getLocalizedMessage(),
        "IO Error", JOptionPane.ERROR_MESSAGE);
    } finally {
      try {    // no matter what, try to close the resources we used
        if (properties_stream != null) { properties_stream.close(); }
        
        if (license_reader != null) {
          license_reader.close();  // this should close the stream too
        } else if (license_stream != null) {
          license_stream.close();  // else, close the stream itself if it exists
        }
      } catch (IOException close_exception) {
        JOptionPane.showMessageDialog(
          parent, close_exception.getLocalizedMessage(),
          "IO Error", JOptionPane.ERROR_MESSAGE);
      }
    }
    
    // set up the contents
    JLabel version_label = new JLabel(version_text);
    version_label.setAlignmentX(Component.CENTER_ALIGNMENT);
    
    JTextArea license_text_area = new JTextArea(10, 50);  // size in rows & cols
    license_text_area.setEditable(false);
    license_text_area.setLineWrap(true);
    license_text_area.setWrapStyleWord(true);
    license_text_area.setText(license_text);
    // setting the carat to 0 makes the scrollpane focus on the start
    license_text_area.setCaretPosition(0);
    JScrollPane license_scroll_pane = new JScrollPane(license_text_area);
    TitledBorder scroll_border = new TitledBorder("GNU General Public License");
    scroll_border.setTitleJustification(TitledBorder.CENTER);
    license_scroll_pane.setBorder(scroll_border);
    
    JButton close_button = new JButton("Close");
    close_button.setAlignmentX(JButton.CENTER_ALIGNMENT);
    // upon clicking the button, dispose of the dialog
    close_button.addActionListener((ActionEvent event) -> dispose());
    
    // layout the contents
    setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
    add(Box.createRigidArea(new java.awt.Dimension(0, 5)));
    add(version_label);
    add(Box.createRigidArea(new java.awt.Dimension(0, 10)));
    add(license_scroll_pane);
    add(close_button);
    add(Box.createRigidArea(new java.awt.Dimension(0, 5)));
    
    setLocationByPlatform(true);
    
    pack();
  }
}
