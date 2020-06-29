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

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.WindowConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.beans.PropertyChangeEvent;
import java.awt.Container;
import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;

/**
 * The primary interface through which a user interacts with GEPS. It houses the
 * various views and controls for the user to load, edit, and save tilesets and
 * palettes, among other functionalities.
 * 
 * @author Nicholas Lovdahl
 */
public final class MainWindow extends JFrame {
  /**
   * The constructor for MainWindow is responsible for initializing its
   * components (the views and controls) and connecting certain events actions
   * to the appropriate methods.
   */
  public MainWindow() {
    super("GEPS");
    
    palette_controller_ = new PaletteController(4);
    tileset_controller_ = new TilesetController();
    
    palette_view_ = new PaletteView(this, palette_controller_);
    tileset_view_ = new TilesetView(tileset_controller_);
    canvas_view_ = new CanvasView(tileset_controller_);
    
    InitMainWindow();
  }
  
  /**
   * Creates an instance of MainWindow and begins a thread to handle event
   * dispatches. Prior to the creation of the window, this method will attempt
   * to set the look and feel of the UI to match the system. The user is
   * notified if this cannot be done.
   */
  public static void Run() {
    try {  // try to set the look and feel to match the system's
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | IllegalAccessException |
             InstantiationException | UnsupportedLookAndFeelException e) {
      JOptionPane.showMessageDialog(null,
        "Failed to set system look and feel. Using default look and feel.\n" +
        e.getClass().getCanonicalName());  // give the name of the exception
    }
    
    new MainWindow().setVisible(true);
  }
  
  private void InitMainWindow() {
    // set properties for the frame / main window itself
    setLocationByPlatform(true);
    
    // on 'closing', call on the action to exit instead of closing by default
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent event) {
        ActionEvent e = new ActionEvent(event.getSource(), event.getID(), null);
        ExitAction(e);
      }
    });
    
    // initialize the menu bar
    JMenuBar menu_bar = new JMenuBar();
    
    JMenu file_menu = new JMenu("File");  // file menu initialization...
    JMenuItem exit_menu_item = new JMenuItem("Exit");
    exit_menu_item.addActionListener(this::ExitAction);
    file_menu.add(exit_menu_item);
    
    JMenu edit_menu = new JMenu("Edit");  // edit menu initialization...
    JMenuItem palette_undo_item = new JMenuItem("Undo Palette");
    palette_undo_item.addActionListener(this::PaletteUndoAction);
    edit_menu.add(palette_undo_item);
    JMenuItem palette_redo_item = new JMenuItem("Redo Palette");
    palette_redo_item.addActionListener(this::PaletteRedoAction);
    edit_menu.add(palette_redo_item);
    
    menu_bar.add(file_menu);
    menu_bar.add(edit_menu);
    setJMenuBar(menu_bar);
    
    // initialize the views for the tileset, canvas, and palette
    JScrollPane tileset_scroll = new JScrollPane(tileset_view_);
    JScrollPane canvas_scroll = new JScrollPane(canvas_view_);
    JScrollPane palette_scroll = new JScrollPane(palette_view_);
    
    // create the split panes that contain the views
    JSplitPane tileset_split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                              canvas_scroll, tileset_scroll);
    tileset_split.setResizeWeight(0.2);
    tileset_split.setContinuousLayout(true);
    
    JSplitPane palette_split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                              tileset_split, palette_scroll);
    palette_split.setResizeWeight(1);
    palette_split.setContinuousLayout(true);
    
    // control the divider's location by fixing its location if moved too far
    palette_split.addPropertyChangeListener("dividerLocation",
                                            (PropertyChangeEvent event) -> {
      int location = (Integer) event.getNewValue();  // location div moved to
      int min = palette_split.getHeight() -          // size for palette view
              palette_view_.getPreferredSize().height -
              2 * palette_split.getDividerSize();
      if (location < min) { palette_split.setDividerLocation(min); }
    });
    
    // arrange the layout of the frame's components within the frame
    setLayout(new BorderLayout());
    Container pane = getContentPane();
    pane.add(palette_split, BorderLayout.CENTER);
    
    pack();
  }
  
  private void ExitAction(ActionEvent event) {
    System.exit(0);
  }
  
  private void PaletteUndoAction(ActionEvent event) {
    if (palette_controller_.canUndo()) {
      palette_controller_.undo();
      palette_view_.repaint();  // repaint the palette since it may have changed
    }
  }
  
  private void PaletteRedoAction(ActionEvent event) {
    if (palette_controller_.canRedo()) {
      palette_controller_.redo();
      palette_view_.repaint();  // repaint the palette since it may have changed
    }
  }
  
  private final PaletteController palette_controller_;
  private final TilesetController tileset_controller_;
  
  private final PaletteView palette_view_;
  private final TilesetView tileset_view_;
  private final CanvasView canvas_view_;
}
