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
    
    palette_controller_ = new PaletteController();
    tileset_controller_ = new TilesetController();
    
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
    
    menu_bar.add(file_menu);
    setJMenuBar(menu_bar);
    
    // initialize the views for the tileset, canvas, and palette
    TilesetView tileset_view = new TilesetView(tileset_controller_);
    JScrollPane tileset_scroll = new JScrollPane(tileset_view);
    CanvasView canvas_view = new CanvasView(tileset_controller_);
    JScrollPane canvas_scroll = new JScrollPane(canvas_view);
    PaletteView palette_view = new PaletteView(palette_controller_,
                                                tileset_controller_.getBPP());
    JScrollPane palette_scroll = new JScrollPane(palette_view);
    
    // create the split panes that contain the views
    JSplitPane tileset_canvas_split =
      new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                     canvas_scroll, tileset_scroll);
    tileset_canvas_split.setResizeWeight(0.2);
    tileset_canvas_split.setContinuousLayout(true);
    
    JSplitPane tileset_canvas_palette_split =
      new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                     tileset_canvas_split, palette_scroll);
    tileset_canvas_palette_split.setResizeWeight(1);
    tileset_canvas_palette_split.setContinuousLayout(true);
    
    // arrange the layout of the frame's components within the frame
    setLayout(new BorderLayout());
    Container pane = getContentPane();
    pane.add(tileset_canvas_palette_split, BorderLayout.CENTER);
    
    pack();
  }
  
  /**
   * Handles the event for when the user wants to exit the program.
   * 
   * @param event 
   */
  private void ExitAction(ActionEvent event) {
    System.exit(0);
  }
  
  private final PaletteController palette_controller_;
  private final TilesetController tileset_controller_;
}
