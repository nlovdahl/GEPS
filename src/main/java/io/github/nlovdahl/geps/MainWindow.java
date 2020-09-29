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
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.ButtonGroup;
import javax.swing.JSeparator;

import javax.swing.SpinnerNumberModel;
import javax.swing.JSpinner;

import javax.swing.WindowConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import java.awt.Container;
import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

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
    
    // initialize the file chooser (this is the parent)
    snes_file_chooser_ = new SNESFileChooser(this);
    
    // initialize the controllers and views
    int initial_tileset_width = 4;
    int initial_tileset_height = 4;
    int initial_bpp = 4;
    int initial_bitplane_format = Tileset.PAIRED_INTERTWINED_FORMAT;
    int initial_tileset_scale_factor = 8;  // x8 scaler
    boolean draw_pixel_grid = true;
    boolean draw_tile_grid = true;
    
    palette_controller_ = new PaletteController(initial_bpp);
    tileset_controller_ = new TilesetController(
      initial_tileset_width, initial_tileset_height,
      initial_bpp, initial_bitplane_format);
    
    palette_view_ = new PaletteView(this, palette_controller_);
    tileset_view_ = new TilesetView(
      tileset_controller_, palette_controller_, initial_tileset_scale_factor,
      draw_pixel_grid, draw_tile_grid);  
    
    // initialize the menu bar
    JMenuBar menu_bar = new JMenuBar();
    
    JMenu file_menu = new JMenu("File");  // file menu initialization...
    JMenuItem new_tileset_menu_item = new JMenuItem("New Tileset");
    new_tileset_menu_item.addActionListener(this::NewTilesetAction);
    file_menu.add(new_tileset_menu_item);
    JMenuItem open_tileset_menu_item = new JMenuItem("Open Tileset...");
    open_tileset_menu_item.addActionListener(this::OpenTilesetAction);
    file_menu.add(open_tileset_menu_item);
    JMenuItem save_tileset_menu_item = new JMenuItem("Save Tileset");
    save_tileset_menu_item.addActionListener(this::SaveTilesetAction);
    file_menu.add(save_tileset_menu_item);
    JMenuItem save_tileset_as_menu_item = new JMenuItem("Save Tileset As...");
    save_tileset_as_menu_item.addActionListener(this::SaveTilesetAsAction);
    file_menu.add(save_tileset_as_menu_item);
    file_menu.add(new JSeparator());
    JMenuItem new_palette_menu_item = new JMenuItem("New Palette");
    new_palette_menu_item.addActionListener(this::NewPaletteAction);
    file_menu.add(new_palette_menu_item);
    JMenuItem open_palette_menu_item = new JMenuItem("Open Palette...");
    open_palette_menu_item.addActionListener(this::OpenPaletteAction);
    file_menu.add(open_palette_menu_item);
    JMenuItem save_palette_menu_item = new JMenuItem("Save Palette");
    save_palette_menu_item.addActionListener(this::SavePaletteAction);
    file_menu.add(save_palette_menu_item);
    JMenuItem save_palette_as_menu_item = new JMenuItem("Save Palette As...");
    save_palette_as_menu_item.addActionListener(this::SavePaletteAsAction);
    file_menu.add(save_palette_as_menu_item);
    file_menu.add(new JSeparator());
    JMenuItem exit_menu_item = new JMenuItem("Exit");
    exit_menu_item.addActionListener(this::ExitAction);
    file_menu.add(exit_menu_item);
    
    JMenu edit_menu = new JMenu("Edit");  // edit menu initialization...
    tileset_undo_menu_item_ = new JMenuItem("Undo (Tileset)");
    tileset_undo_menu_item_.addActionListener(this::TilesetUndoAction);
    edit_menu.add(tileset_undo_menu_item_);
    tileset_redo_menu_item_ = new JMenuItem("Redo (Tileset)");
    tileset_redo_menu_item_.addActionListener(this::TilesetRedoAction);
    edit_menu.add(tileset_redo_menu_item_);
    edit_menu.add(new JSeparator());
    palette_undo_menu_item_ = new JMenuItem("Undo (Palette)");
    palette_undo_menu_item_.addActionListener(this::PaletteUndoAction);
    edit_menu.add(palette_undo_menu_item_);
    palette_redo_menu_item_ = new JMenuItem("Redo (Palette)");
    palette_redo_menu_item_.addActionListener(this::PaletteRedoAction);
    edit_menu.add(palette_redo_menu_item_);
    
    JMenu view_menu = new JMenu("View");  // view menu initialization...
    draw_pixel_grid_menu_item_ = new JCheckBoxMenuItem("Show Pixel Gridlines");
    draw_pixel_grid_menu_item_.addActionListener(
      this::DrawPixelGridChangeAction
    );
    draw_pixel_grid_menu_item_.setSelected(draw_pixel_grid);
    view_menu.add(draw_pixel_grid_menu_item_);
    draw_tile_grid_menu_item_ = new JCheckBoxMenuItem("Show Tile Gridlines");
    draw_tile_grid_menu_item_.addActionListener(this::DrawTileGridChangeAction);
    draw_tile_grid_menu_item_.setSelected(draw_tile_grid);
    view_menu.add(draw_tile_grid_menu_item_);
    JMenuItem tileset_width_menu_item = new JMenuItem("Tileset Width...");
    tileset_width_menu_item.addActionListener(this::TilesetWidthChangeAction);
    view_menu.add(tileset_width_menu_item);
    JMenu tileset_zoom_submenu = new JMenu("Tileset Zoom");
    ButtonGroup tileset_zoom_item_group = new ButtonGroup();
    JRadioButtonMenuItem tileset_zoom_1_item = new JRadioButtonMenuItem("x1");
    tileset_zoom_submenu.add(tileset_zoom_1_item);
    tileset_zoom_item_group.add(tileset_zoom_1_item);
    tileset_zoom_1_item.addActionListener(this::TilesetZoomChangeAction);
    JRadioButtonMenuItem tileset_zoom_2_item = new JRadioButtonMenuItem("x2");
    tileset_zoom_submenu.add(tileset_zoom_2_item);
    tileset_zoom_item_group.add(tileset_zoom_2_item);
    tileset_zoom_2_item.addActionListener(this::TilesetZoomChangeAction);
    JRadioButtonMenuItem tileset_zoom_4_item = new JRadioButtonMenuItem("x4");
    tileset_zoom_submenu.add(tileset_zoom_4_item);
    tileset_zoom_item_group.add(tileset_zoom_4_item);
    tileset_zoom_4_item.addActionListener(this::TilesetZoomChangeAction);
    JRadioButtonMenuItem tileset_zoom_8_item = new JRadioButtonMenuItem("x8");
    tileset_zoom_submenu.add(tileset_zoom_8_item);
    tileset_zoom_item_group.add(tileset_zoom_8_item);
    tileset_zoom_8_item.addActionListener(this::TilesetZoomChangeAction);
    JRadioButtonMenuItem tileset_zoom_16_item = new JRadioButtonMenuItem("x16");
    tileset_zoom_submenu.add(tileset_zoom_16_item);
    tileset_zoom_item_group.add(tileset_zoom_16_item);
    tileset_zoom_16_item.addActionListener(this::TilesetZoomChangeAction);
    JRadioButtonMenuItem tileset_zoom_32_item = new JRadioButtonMenuItem("x32");
    tileset_zoom_submenu.add(tileset_zoom_32_item);
    tileset_zoom_item_group.add(tileset_zoom_32_item);
    tileset_zoom_32_item.addActionListener(this::TilesetZoomChangeAction);
    tileset_zoom_8_item.setSelected(true);  // begin with x8 zoom by default
    view_menu.add(tileset_zoom_submenu);
    
    JMenu format_menu = new JMenu("Format");  // format menu initialization...
    JMenuItem tileset_size_menu_item = new JMenuItem("Tileset Size...");
    tileset_size_menu_item.addActionListener(this::TilesetSizeChangeAction);
    format_menu.add(tileset_size_menu_item);
    JMenu bpp_submenu = new JMenu("Bits per Pixel");
    ButtonGroup bpp_item_group = new ButtonGroup();
    one_bpp_item_ = new JRadioButtonMenuItem("1 BPP");
    bpp_submenu.add(one_bpp_item_);
    bpp_item_group.add(one_bpp_item_);
    one_bpp_item_.addActionListener(this::BPPChangeAction);
    two_bpp_item_ = new JRadioButtonMenuItem("2 BPP");
    bpp_submenu.add(two_bpp_item_);
    bpp_item_group.add(two_bpp_item_);
    two_bpp_item_.addActionListener(this::BPPChangeAction);
    three_bpp_item_ = new JRadioButtonMenuItem("3 BPP");
    bpp_submenu.add(three_bpp_item_);
    bpp_item_group.add(three_bpp_item_);
    three_bpp_item_.addActionListener(this::BPPChangeAction);
    four_bpp_item_ = new JRadioButtonMenuItem("4 BPP");
    bpp_submenu.add(four_bpp_item_);
    bpp_item_group.add(four_bpp_item_);
    four_bpp_item_.addActionListener(this::BPPChangeAction);
    eight_bpp_item_ = new JRadioButtonMenuItem("8 BPP");
    bpp_submenu.add(eight_bpp_item_);
    bpp_item_group.add(eight_bpp_item_);
    eight_bpp_item_.addActionListener(this::BPPChangeAction);
    format_menu.add(bpp_submenu);
    
    JMenu tileset_format_submenu = new JMenu("Tileset Format");
    ButtonGroup tileset_format_item_group = new ButtonGroup();
    serial_tileset_format_item_ =
      new JRadioButtonMenuItem(SERIAL_FORMAT_STRING);
    tileset_format_submenu.add(serial_tileset_format_item_);
    tileset_format_item_group.add(serial_tileset_format_item_);
    serial_tileset_format_item_.addActionListener(
      this::TilesetFormatChangeAction);
    planar_tileset_format_item_ =
      new JRadioButtonMenuItem(PLANAR_FORMAT_STRING);
    tileset_format_submenu.add(planar_tileset_format_item_);
    tileset_format_item_group.add(planar_tileset_format_item_);
    planar_tileset_format_item_.addActionListener(
      this::TilesetFormatChangeAction);
    linear_intertwined_tileset_format_item_ =
      new JRadioButtonMenuItem(LINEAR_INTERTWINED_FORMAT_STRING);
    tileset_format_submenu.add(linear_intertwined_tileset_format_item_);
    tileset_format_item_group.add(linear_intertwined_tileset_format_item_);
    linear_intertwined_tileset_format_item_.addActionListener(
      this::TilesetFormatChangeAction);
    paired_intertwined_tileset_format_item_ =
      new JRadioButtonMenuItem(PAIRED_INTERTWINED_FORMAT_STRING);
    tileset_format_submenu.add(paired_intertwined_tileset_format_item_);
    tileset_format_item_group.add(paired_intertwined_tileset_format_item_);
    paired_intertwined_tileset_format_item_.addActionListener(
      this::TilesetFormatChangeAction);
    format_menu.add(tileset_format_submenu);
    
    JMenu help_menu = new JMenu("Help");
    JMenuItem about_menu_item = new JMenuItem("About");
    about_menu_item.addActionListener(this::AboutAction);
    help_menu.add(about_menu_item);
    
    menu_bar.add(file_menu);
    menu_bar.add(edit_menu);
    menu_bar.add(view_menu);
    menu_bar.add(format_menu);
    menu_bar.add(help_menu);
    setJMenuBar(menu_bar);
    
    // setup the scroll panes for the tileset, and palette
    JScrollPane tileset_scroll = new JScrollPane(tileset_view_);
    JScrollPane palette_scroll = new JScrollPane(palette_view_);
    
    // create a split pane to contain the views
    JSplitPane view_split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                           tileset_scroll, palette_scroll);
    view_split.setResizeWeight(1);
    view_split.setContinuousLayout(true);
    
    // control the divider's location by fixing its location if moved too far
    view_split.addPropertyChangeListener("dividerLocation",
                                         (PropertyChangeEvent event) -> {
      int location = (Integer) event.getNewValue();  // location div moved to
      int min = view_split.getHeight() -          // size for palette view
                palette_view_.getPreferredSize().height -
                2 * view_split.getDividerSize();
      if (location < min) { view_split.setDividerLocation(min); }
    });
    
    // update parts of the UI that are updated dynamically
    updateTitle();
    updateTilesetUndoRedoUI();
    updatePaletteUndoRedoUI();
    updateFormatUI();
    
    // arrange the layout of the frame's components within the frame
    setLayout(new BorderLayout());
    Container pane = getContentPane();
    pane.add(view_split, BorderLayout.CENTER);
    
    pack();
    
    // setup property change listeners that need to interact with the UI
    tileset_view_.addPropertyChangeListener(TilesetView.NEW_TILESET_STATE,
                                            this::TilesetStateChange);
    palette_view_.addPropertyChangeListener(PaletteView.NEW_PALETTE_STATE,
                                            (this::PaletteStateChange));
    palette_view_.addPropertyChangeListener(PaletteView.NEW_PALETTE_SUBPALETTE,
                                            (this::PaletteSubpaletteChange));
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
  
  // methods to handle actions and property change events
  private void ExitAction(ActionEvent event) {
    if (tileset_controller_.hasUnsavedChanges()) {  // handle an unsaved tileset
      int result = JOptionPane.showConfirmDialog(
        this, snes_file_chooser_.getReferencedTilesetFileShortName() +
        " has unsaved changes.\nWould you like to save them?", "Save Changes?",
        JOptionPane.YES_NO_CANCEL_OPTION);
      // save the tileset if the user wants, stop the exit on cancel or closure
      if (result == JOptionPane.YES_OPTION) {
        SaveTilesetAction(event);
      } else if (result != JOptionPane.NO_OPTION) {  // cancel or closure option
        return;
      }
    }
    
    if (palette_controller_.hasUnsavedChanges()) {  // handle an unsaved palette
      int result = JOptionPane.showConfirmDialog(
        this, snes_file_chooser_.getReferencedPaletteFileShortName() +
        " has unsaved changes.\nWould you like to save them?", "Save Changes?",
        JOptionPane.YES_NO_CANCEL_OPTION);
      // save the palette if the user wants, stop the exit on cancel or closure
      if (result == JOptionPane.YES_OPTION) {
        SavePaletteAction(event);
      } else if (result != JOptionPane.NO_OPTION) {
        return;
      }
    }
    
    System.exit(0);  // if we reach this point, we are fine to exit the program
  }
  
  private void NewTilesetAction(ActionEvent event) {
    if (tileset_controller_.hasUnsavedChanges()) {  // handle an unsaved tileset
      int result = JOptionPane.showConfirmDialog(
        this, snes_file_chooser_.getReferencedTilesetFileShortName() +
        " has unsaved changes.\nWould you like to save them?", "Save Changes?",
        JOptionPane.YES_NO_CANCEL_OPTION);
      // save the tileset if the user wants, stop the exit on cancel or closure
      if (result == JOptionPane.YES_OPTION) {
        SaveTilesetAction(event);
      } else if (result != JOptionPane.NO_OPTION) {  // cancel or closure option
        return;
      }
    }
    
    // now reset the current tileset
    tileset_controller_.resetTileset();
    snes_file_chooser_.resetReferencedTilesetFile();
    updateTitle();
    updateTilesetUndoRedoUI();
    updateFormatUI();
    tileset_view_.repaint();
  }
  
  private void OpenTilesetAction(ActionEvent event) {
    if (tileset_controller_.hasUnsavedChanges()) {  // handle an unsaved tileset
      int result = JOptionPane.showConfirmDialog(
        this, snes_file_chooser_.getReferencedTilesetFileShortName() +
        " has unsaved changes.\nWould you like to save them?", "Save Changes?",
        JOptionPane.YES_NO_CANCEL_OPTION);
      // save the tileset if the user wants, stop the exit on cancel or closure
      if (result == JOptionPane.YES_OPTION) {
        SaveTilesetAction(event);
      } else if (result != JOptionPane.NO_OPTION) {  // cancel or closure option
        return;
      }
    }
    
    // let the user select the tileset, then load it
    File selected_file = snes_file_chooser_.selectTilesetFileToOpen();
    if (selected_file != null) {  // if user made a selection (not null)
      try {
        long unloaded_bytes = tileset_controller_.loadTileset(selected_file);
        if (unloaded_bytes > 0) {
          JOptionPane.showMessageDialog(
            this, snes_file_chooser_.getReferencedTilesetFileShortName() +
            " was only partially loaded.\n" + Long.toString(unloaded_bytes) +
            " bytes were not loaded.", "File Partially Loaded",
            JOptionPane.INFORMATION_MESSAGE);
        }
        
        updateTitle();
        updateTilesetUndoRedoUI();
        tileset_view_.repaint();
      } catch (FileNotFoundException file_exception) {
        JOptionPane.showMessageDialog(
          this, file_exception.toString(),
          "File Not Found Exception", JOptionPane.ERROR_MESSAGE);
      } catch (IOException io_exception) {
        JOptionPane.showMessageDialog(
          this, io_exception.toString(),
          "IO Exception", JOptionPane.ERROR_MESSAGE);
      }
    }
  }
  
  private void SaveTilesetAction(ActionEvent event) {
    // if there is no referenced tileset file to save to, make this a save as
    File tileset_file = snes_file_chooser_.getReferencedTilesetFile();
    if (tileset_file == null) {  // if there is no current referenced file
      SaveTilesetAsAction(event);
    } else {  // just save to the file
      try {
        tileset_controller_.saveTileset(tileset_file);
        updateTitle();
      } catch (FileNotFoundException file_exception) {
        JOptionPane.showMessageDialog(
          this, file_exception.toString(),
          "File Not Found Exception", JOptionPane.ERROR_MESSAGE);
      } catch (IOException io_exception) {
        JOptionPane.showMessageDialog(
          this, io_exception.toString(),
          "IO Exception", JOptionPane.ERROR_MESSAGE);
      }
    }
  }
  
  private void SaveTilesetAsAction(ActionEvent event) {
    File tileset_file = snes_file_chooser_.selectTilesetFileToSave();
    if (tileset_file != null) {  // if the user made a selection (not null)
      SaveTilesetAction(event);  // then proceed to save the tileset
    }
  }
  
  private void NewPaletteAction(ActionEvent event) {
    if (palette_controller_.hasUnsavedChanges()) {  // handle an unsaved palette
      int result = JOptionPane.showConfirmDialog(
        this, snes_file_chooser_.getReferencedPaletteFileShortName() +
        " has unsaved changes.\nWould you like to save them?", "Save Changes?",
        JOptionPane.YES_NO_CANCEL_OPTION);
      // save the palette if the user wants, stop the exit on cancel or closure
      if (result == JOptionPane.YES_OPTION) {
        SavePaletteAction(event);
      } else if (result != JOptionPane.NO_OPTION) {
        return;
      }
    }
    
    // now reset the current palette
    palette_controller_.resetPalette();
    snes_file_chooser_.resetReferencedPaletteFile();
    updateTitle();
    updatePaletteUndoRedoUI();
    palette_view_.repaint();
    tileset_view_.repaint();
  }
  
  private void OpenPaletteAction(ActionEvent event) {
    if (palette_controller_.hasUnsavedChanges()) {  // handle an unsaved palette
      int result = JOptionPane.showConfirmDialog(
        this, snes_file_chooser_.getReferencedPaletteFileShortName() +
        " has unsaved changes.\nWould you like to save them?", "Save Changes?",
        JOptionPane.YES_NO_CANCEL_OPTION);
      // save the palette if the user wants, stop the exit on cancel or closure
      if (result == JOptionPane.YES_OPTION) {
        SavePaletteAction(event);
      } else if (result != JOptionPane.NO_OPTION) {
        return;
      }
    }
    
    // let the user select the tileset, then load it
    File selected_file = snes_file_chooser_.selectPaletteFileToOpen();
    if (selected_file != null) {  // if user made a selection (not null)
      try {
        long unloaded_bytes = palette_controller_.loadPalette(selected_file);
        if (unloaded_bytes > 0) {
          JOptionPane.showMessageDialog(
            this, snes_file_chooser_.getReferencedPaletteFileShortName() +
            " was only partially loaded.\n" + Long.toString(unloaded_bytes) +
            " bytes were not loaded.", "File Partially Loaded",
            JOptionPane.INFORMATION_MESSAGE);
        }
        
        updateTitle();
        updatePaletteUndoRedoUI();
        tileset_view_.repaint();  // repaint since things may have changed
        palette_view_.repaint();
      } catch (FileNotFoundException file_exception) {
        JOptionPane.showMessageDialog(
          this, file_exception.toString(),
          "File Not Found Exception", JOptionPane.ERROR_MESSAGE);
      } catch (IOException io_exception) {
        JOptionPane.showMessageDialog(
          this, io_exception.toString(),
          "IO Exception", JOptionPane.ERROR_MESSAGE);
      }
    }
  }
  
  private void SavePaletteAction(ActionEvent event) {
    // if there is no referenced palette file to save to, make this a save as
    File palette_file = snes_file_chooser_.getReferencedPaletteFile();
    if (palette_file == null) {  // if there is no current referenced file
      SavePaletteAsAction(event);
    } else {  // just save to the file
      try {
        palette_controller_.savePalette(palette_file);
        updateTitle();
      } catch (FileNotFoundException file_exception) {
        JOptionPane.showMessageDialog(
          this, file_exception.toString(),
          "File Not Found Exception", JOptionPane.ERROR_MESSAGE);
      } catch (IOException io_exception) {
        JOptionPane.showMessageDialog(
          this, io_exception.toString(),
          "IO Exception", JOptionPane.ERROR_MESSAGE);
      }
    }
  }
  
  private void SavePaletteAsAction(ActionEvent event) {
    File palette_file = snes_file_chooser_.selectPaletteFileToSave();
    if (palette_file != null) {  // if the user made a selection (not null)
      SavePaletteAction(event);  // then proceed to save the tileset
    }
  }
  
  private void TilesetUndoAction(ActionEvent event) {
    if (tileset_controller_.canUndo()) {
      tileset_controller_.undo();
      updateTilesetUndoRedoUI();
      tileset_view_.repaint();  // repaint since things may have changed
    }
  }
  
  private void TilesetRedoAction(ActionEvent event) {
    if (tileset_controller_.canRedo()) {
      tileset_controller_.redo();
      updateTilesetUndoRedoUI();
      tileset_view_.repaint();  // repaint since things may have changed
    }
  }
  
  private void PaletteUndoAction(ActionEvent event) {
    if (palette_controller_.canUndo()) {
      palette_controller_.undo();
      updatePaletteUndoRedoUI();
      tileset_view_.repaint();  // repaint since things may have changed
      palette_view_.repaint();
    }
  }
  
  private void PaletteRedoAction(ActionEvent event) {
    if (palette_controller_.canRedo()) {
      palette_controller_.redo();
      updatePaletteUndoRedoUI();
      tileset_view_.repaint();  // repaint since things may have changed
      palette_view_.repaint();
    }
  }
  
  private void DrawPixelGridChangeAction(ActionEvent event) {
    tileset_view_.setDrawPixelGrid(draw_pixel_grid_menu_item_.isSelected());
  }
  
  private void DrawTileGridChangeAction(ActionEvent event) {
    tileset_view_.setDrawTileGrid(draw_tile_grid_menu_item_.isSelected());
  }
  
  private void TilesetWidthChangeAction(ActionEvent event) {
    // create a s[inner that starts with the current tileset width
    SpinnerNumberModel tileset_width_spinner_model =
      new SpinnerNumberModel(tileset_controller_.getWidthInTiles(),
                             1, Tileset.MAX_TILES, 1);  // min, max, step
    JSpinner tileset_width_spinner = new JSpinner(tileset_width_spinner_model);
    int result = JOptionPane.showOptionDialog(
      this, tileset_width_spinner, "Select New Tileset Width",
      JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
      null, null, null);  // default icon and no options (we use the spinner)
    
    // change the tileset width if the user made a choice, do nothing otherwise
    if (result == JOptionPane.OK_OPTION) {
      int new_tileset_width = (int) tileset_width_spinner.getValue();
      // only do something if the number is different
      if (new_tileset_width != tileset_controller_.getWidthInTiles()) {
        tileset_controller_.setTilesetWidth(new_tileset_width);
        tileset_view_.repaint();
      }
    }
  }
  
  private void TilesetZoomChangeAction(ActionEvent event) {
    // parse the command, except the first character, which should be the number
    int scale_factor = Integer.parseInt(event.getActionCommand().substring(1));
    tileset_view_.setScaleFactor(scale_factor);
  }
  
  private void TilesetSizeChangeAction(ActionEvent event) {
    // create a spinner that starts with the current number of tiles
    SpinnerNumberModel tileset_size_spinner_model =
      new SpinnerNumberModel(tileset_controller_.getNumberOfTiles(),
                             1, Tileset.MAX_TILES, 1);  // min, max, step
    JSpinner tileset_size_spinner = new JSpinner(tileset_size_spinner_model);
    int result = JOptionPane.showOptionDialog(
      this, tileset_size_spinner, "Select New Tileset Size",
      JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
      null, null, null);  // default icon and no options (we use the spinner)
    
    // change the tileset's size if the user made a choice, do nothing otherwise
    if (result == JOptionPane.OK_OPTION) {
      int new_tileset_size = (int) tileset_size_spinner.getValue();
      // only do something if the number is different
      if (new_tileset_size != tileset_controller_.getNumberOfTiles()) {
        tileset_controller_.changeNumberOfTiles(new_tileset_size);
        updateTitle();
        updateTilesetUndoRedoUI();
        tileset_view_.repaint();  // repaint since things may have changed
      }
    }
  }
  
  private void BPPChangeAction(ActionEvent event) {
    // split and parse the first part of the command, which should be the number
    int bpp = Integer.parseInt(event.getActionCommand().split(" ")[0]);
    // proceed only if the bpp is within a tolerable range
    if (bpp >= Tileset.MIN_BPP && bpp <= Tileset.MAX_BPP) {
      tileset_controller_.changeBPP(bpp);
      palette_controller_.setBPP(bpp);
      updateTitle();
      updateTilesetUndoRedoUI();
      updateFormatUI();
      tileset_view_.repaint();  // repaint since things may have changed
      palette_view_.repaint();
    }
  }
  
  private void TilesetFormatChangeAction(ActionEvent event) {
    // match the text of the chosen format to the corresponding number
    int tileset_format;
    switch (event.getActionCommand()) {
      case SERIAL_FORMAT_STRING:
        tileset_format = Tileset.SERIAL_FORMAT;
        break;
      case PLANAR_FORMAT_STRING:
        tileset_format = Tileset.PLANAR_FORMAT;
        break;
      case LINEAR_INTERTWINED_FORMAT_STRING:
        tileset_format = Tileset.LINEAR_INTERTWINED_FORMAT;
        break;
      case PAIRED_INTERTWINED_FORMAT_STRING:
        tileset_format = Tileset.PAIRED_INTERTWINED_FORMAT;
        break;
      default:
        throw new IllegalArgumentException(
          event.getActionCommand() +
          " is an unrecognized tileset format action command.");
    }
    
    tileset_controller_.changeTilesetFormat(tileset_format);
    updateTitle();
    updateFormatUI();
    tileset_view_.repaint();  // repaint since things may have changed
  }
  
  private void AboutAction(ActionEvent event) {
    new AboutWindow(this).setVisible(true);
  }
  
  private void TilesetStateChange(PropertyChangeEvent event) {
    updateTitle();
    updateTilesetUndoRedoUI();
  }
  
  private void PaletteStateChange(PropertyChangeEvent event) {
    updateTitle();
    updatePaletteUndoRedoUI();
  }
  
  private void PaletteSubpaletteChange(PropertyChangeEvent event) {
    tileset_view_.repaint();
  }
  
  // methods to dynamically update the UI
  private void updateTitle() {
    String tileset_filename =
      snes_file_chooser_.getReferencedTilesetFileShortName();
    if (tileset_controller_.hasUnsavedChanges()) {
      tileset_filename = "*" + tileset_filename;  // add a * for unsaved changes
    }
    String palette_filename =
      snes_file_chooser_.getReferencedPaletteFileShortName();
    if (palette_controller_.hasUnsavedChanges()) {
      palette_filename = "*" + palette_filename;  // add a * for unsaved changes
    }
    
    setTitle("GEPS - " + tileset_filename + " | " + palette_filename);
  }
  
  private void updatePaletteUndoRedoUI() {
    palette_undo_menu_item_.setEnabled(palette_controller_.canUndo());
    palette_redo_menu_item_.setEnabled(palette_controller_.canRedo());
  }
  
  private void updateTilesetUndoRedoUI() {
    tileset_undo_menu_item_.setEnabled(tileset_controller_.canUndo());
    tileset_redo_menu_item_.setEnabled(tileset_controller_.canRedo());
  }
  
  private void updateFormatUI() {
    // match the current bpp to the right JRadioButtonMenuItem
    switch (tileset_controller_.getBPP()) {
      case 1:
        one_bpp_item_.setSelected(true);
        break;
      case 2:
        two_bpp_item_.setSelected(true);
        break;
      case 3:
        three_bpp_item_.setSelected(true);
        break;
      case 4:
        four_bpp_item_.setSelected(true);
        break;
      case 8:
        eight_bpp_item_.setSelected(true);
        break;
      default:
        throw new RuntimeException(
          Integer.toString(tileset_controller_.getBPP()) +
          " is an unrecognized bpp menu option.");
    }
    
    // match the current tileset format to the right JRadioButtonMenuItem
    switch (tileset_controller_.getTilesetFormat()) {
      case Tileset.SERIAL_FORMAT:
        serial_tileset_format_item_.setSelected(true);
        break;
      case Tileset.PLANAR_FORMAT:
        planar_tileset_format_item_.setSelected(true);
        break;
      case Tileset.LINEAR_INTERTWINED_FORMAT:
        linear_intertwined_tileset_format_item_.setSelected(true);
        break;
      case Tileset.PAIRED_INTERTWINED_FORMAT:
        paired_intertwined_tileset_format_item_.setSelected(true);
        break;
      default:
        throw new RuntimeException(
          Integer.toString(tileset_controller_.getTilesetFormat()) +
          " is an unrecognized tileset format menu option.");
    }
  }
  
  // strings (that the user sees in the UI) used to denote tileset formats
  private static final String SERIAL_FORMAT_STRING = "Serial";
  private static final String PLANAR_FORMAT_STRING = "Planar";
  private static final String LINEAR_INTERTWINED_FORMAT_STRING =
    "Intertwined (Linear)";
  private static final String PAIRED_INTERTWINED_FORMAT_STRING =
    "Intertwined (Paired)";
  
  // parts of the UI that need to be accessible and / or be updated dynamically
  private final JMenuItem tileset_undo_menu_item_;
  private final JMenuItem tileset_redo_menu_item_;
  private final JMenuItem palette_undo_menu_item_;
  private final JMenuItem palette_redo_menu_item_;
  private final JCheckBoxMenuItem draw_pixel_grid_menu_item_;
  private final JCheckBoxMenuItem draw_tile_grid_menu_item_;
  private final JRadioButtonMenuItem one_bpp_item_;
  private final JRadioButtonMenuItem two_bpp_item_;
  private final JRadioButtonMenuItem three_bpp_item_;
  private final JRadioButtonMenuItem four_bpp_item_;
  private final JRadioButtonMenuItem eight_bpp_item_;
  private final JRadioButtonMenuItem serial_tileset_format_item_;
  private final JRadioButtonMenuItem planar_tileset_format_item_;
  private final JRadioButtonMenuItem linear_intertwined_tileset_format_item_;
  private final JRadioButtonMenuItem paired_intertwined_tileset_format_item_;
  
  // the file chooser used to prompt the user to load and save files
  private final SNESFileChooser snes_file_chooser_;
  
  // controllers and views
  private final PaletteController palette_controller_;
  private final TilesetController tileset_controller_;
  
  private final PaletteView palette_view_;
  private final TilesetView tileset_view_;
}
