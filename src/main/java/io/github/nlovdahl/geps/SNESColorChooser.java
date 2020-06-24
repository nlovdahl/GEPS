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

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JColorChooser;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;
import javax.swing.BorderFactory;
import javax.swing.event.ChangeEvent;
import javax.swing.WindowConstants;

/**
 * A color chooser interface which allows a user to select a color. This color
 * will correspond to one supported by the SNES (5 bits for each of the RGB
 * channels). While the dialog is shown to allow the user to select a color,
 * input to the parent frame and its components should be blocked.
 * 
 * @author Nicholas Lovdahl
 */
public final class SNESColorChooser extends JDialog {
  /**
   * Creates a dialog for the custom color chooser using the given parent frame.
   * Setting up the color chooser and connecting events (like pressing a button)
   * to their appropriate methods is done here.
   * 
   * @param parent_frame the frame which ultimately owns this dialog.
   */
  public SNESColorChooser(JFrame parent_frame) {
    // create a dialog window that blocks input
    super(parent_frame, "Edit Color", true,
          parent_frame.getGraphicsConfiguration());
    // treat closing the dialog like clicking on the cancel button
    setLocationByPlatform(true);
    setResizable(false);
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent event) {
        ActionEvent e = new ActionEvent(
            event.getSource(), event.getID(), CANCEL_OPTION);
        buttonAction(e);
      }
    });
    
    // create a custom color chooser
    color_chooser_ = new JColorChooser();
    color_chooser_.setBorder(BorderFactory.createTitledBorder("Choose Color"));
    // setup a custom preview for the chooser
    color_chooser_.setPreviewPanel(new JPanel());  // disables preview
    new_color_label_ = new JLabel();
    new_color_label_.setHorizontalAlignment(JLabel.CENTER);
    new_color_label_.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
    new_color_label_.setOpaque(true);
    initial_color_label_ = new JLabel();
    initial_color_label_.setHorizontalAlignment(JLabel.CENTER);
    initial_color_label_.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
    initial_color_label_.setOpaque(true);
    // change the new color label when selections in the color chooser change
    color_chooser_.getSelectionModel().addChangeListener(
      (ChangeEvent event) -> { setNewColorLabel(color_chooser_.getColor());
    });
    
    // create buttons to control this dialog and the chooser
    JButton select_button = new JButton(SELECT_OPTION);
    select_button.addActionListener(
      (ActionEvent event) -> { buttonAction(event); }
    );
    JButton cancel_button = new JButton(CANCEL_OPTION);
    cancel_button.addActionListener(
      (ActionEvent event) -> { buttonAction(event); }
    );
    
    // layout the chooser and buttons
    setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    // add the labels for the new color and the initial color (previews)
    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    add(new_color_label_, c);
    c.gridx = 1;
    add(initial_color_label_, c);
    // the color chooser should fill middle cells and resize both hor. / ver.
    c.gridx = 0;
    c.gridy = 1;
    c.gridwidth = 2;
    c.fill = GridBagConstraints.BOTH;
    add(color_chooser_, c);
    // add the buttons next
    c.gridy = 2;
    c.gridwidth = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    add(select_button, c);
    c.gridx = 1;
    add(cancel_button, c);
    
    pack();
  }
  
  /**
   * Provides a dialog for a user to select a color with a customized color
   * chooser. An initial color for the color chooser may be specified. The user
   * may select a color or abort the selection process - the selected color or
   * null will be returned, respectively.
   * 
   * @param initial_color the initial color which should be reflected by the
   *                      current selection of the color chooser.
   * @return the color selected by the user, or null if no color was selected.
   */
  public Color chooseColor(Color initial_color) {
    // set initial color as given (use some default if null)
    if (initial_color == null) { initial_color = new Color(0, 0, 0); }
    color_chooser_.setColor(initial_color);
    setInitialColorLabel(initial_color);
    
    setVisible(true);  // show dialog and block until selection or closure...
    
    if (option_.equals(SELECT_OPTION)) {  // if color was selected, return it
      return color_chooser_.getColor();
    } else {  // else, return null to represent that no choice was made
      return null;
    }
  }
  
  // set the option based on the command and close the dialog (will unblock)
  private void buttonAction(ActionEvent event) {
    option_ = event.getActionCommand();
    dispose();
  }
  
  // change the text and background corresponding the the new color
  private void setNewColorLabel(Color color) {
    new_color_label_.setText("New Color: " +
                             Palette.getSNESColorCodeString(color));
    new_color_label_.setBackground(color);
    new_color_label_.setForeground(Palette.contrastColor(color));
    pack();  // dialog may need to be resized
  }
  
  // change the text and background corresponding to the initial color
  private void setInitialColorLabel(Color color) {
    initial_color_label_.setText("Old Color: " +
                                 Palette.getSNESColorCodeString(color));
    initial_color_label_.setBackground(color);
    initial_color_label_.setForeground(Palette.contrastColor(color));
    pack();  // dialog may need to be resized
  }
  
  private String option_;
  private final JColorChooser color_chooser_;
  private final JLabel new_color_label_;
  private final JLabel initial_color_label_;
  
  private static final String CANCEL_OPTION = "Cancel";
  private static final String SELECT_OPTION = "Select";
}
