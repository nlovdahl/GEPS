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
import javax.swing.JRadioButtonMenuItem;
import javax.swing.ButtonGroup;
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
    super("GEPS");
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
    int initial_tileset_width = 8;
    int initial_tileset_height = 8;
    int initial_bpp = 4;
    int initial_bitplane_format = Tileset.BITPLANE_PLANAR;
    double initial_canvas_scale_factor = 4.0;  // scale by x4
    
    palette_controller_ = new PaletteController(initial_bpp);
    tileset_controller_ = new TilesetController(
      initial_tileset_width, initial_tileset_height,
      initial_bpp, initial_bitplane_format);
    
    palette_view_ = new PaletteView(this, palette_controller_);
    tileset_view_ = new TilesetView(tileset_controller_);
    canvas_view_ = new CanvasView(
      tileset_controller_, palette_controller_, initial_canvas_scale_factor);  
    
    // initialize the menu bar
    JMenuBar menu_bar = new JMenuBar();
    
    JMenu file_menu = new JMenu("File");  // file menu initialization...
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
    JMenu canvas_zoom_submenu = new JMenu("Canvas Zoom");
    ButtonGroup canvas_zoom_item_group = new ButtonGroup();
    JRadioButtonMenuItem canvas_zoom_1_item = new JRadioButtonMenuItem("x1");
    canvas_zoom_submenu.add(canvas_zoom_1_item);
    canvas_zoom_item_group.add(canvas_zoom_1_item);
    canvas_zoom_1_item.addActionListener(this::CanvasZoomChangeAction);
    JRadioButtonMenuItem canvas_zoom_2_item = new JRadioButtonMenuItem("x2");
    canvas_zoom_submenu.add(canvas_zoom_2_item);
    canvas_zoom_item_group.add(canvas_zoom_2_item);
    canvas_zoom_2_item.addActionListener(this::CanvasZoomChangeAction);
    JRadioButtonMenuItem canvas_zoom_4_item = new JRadioButtonMenuItem("x4");
    canvas_zoom_submenu.add(canvas_zoom_4_item);
    canvas_zoom_item_group.add(canvas_zoom_4_item);
    canvas_zoom_4_item.addActionListener(this::CanvasZoomChangeAction);
    JRadioButtonMenuItem canvas_zoom_8_item = new JRadioButtonMenuItem("x8");
    canvas_zoom_submenu.add(canvas_zoom_8_item);
    canvas_zoom_item_group.add(canvas_zoom_8_item);
    canvas_zoom_8_item.addActionListener(this::CanvasZoomChangeAction);
    canvas_zoom_4_item.setSelected(true);  // begin with x4 zoom by default
    view_menu.add(canvas_zoom_submenu);
    
    JMenu format_menu = new JMenu("Format");  // format menu initialization...
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
    
    JMenu bitplane_format_submenu = new JMenu("Bitplane Format");
    ButtonGroup bitplane_format_item_group = new ButtonGroup();
    serial_bitplane_format_item_ = new JRadioButtonMenuItem("Serial");
    bitplane_format_submenu.add(serial_bitplane_format_item_);
    bitplane_format_item_group.add(serial_bitplane_format_item_);
    serial_bitplane_format_item_.addActionListener(
      this::BitplaneFormatChangeAction);
    planar_bitplane_format_item_ = new JRadioButtonMenuItem("Planar");
    bitplane_format_submenu.add(planar_bitplane_format_item_);
    bitplane_format_item_group.add(planar_bitplane_format_item_);
    planar_bitplane_format_item_.addActionListener(
      this::BitplaneFormatChangeAction);
    intertwined_bitplane_format_item_ = new JRadioButtonMenuItem("Intertwined");
    bitplane_format_submenu.add(intertwined_bitplane_format_item_);
    bitplane_format_item_group.add(intertwined_bitplane_format_item_);
    intertwined_bitplane_format_item_.addActionListener(
      this::BitplaneFormatChangeAction);
    format_menu.add(bitplane_format_submenu);
    
    menu_bar.add(file_menu);
    menu_bar.add(edit_menu);
    menu_bar.add(view_menu);
    menu_bar.add(format_menu);
    setJMenuBar(menu_bar);
    
    // setup the scroll panes for the tileset, canvas, and palette
    JScrollPane tileset_scroll = new JScrollPane(tileset_view_);
    JScrollPane canvas_scroll = new JScrollPane(canvas_view_);
    JScrollPane palette_scroll = new JScrollPane(palette_view_);
    
    // create the split panes that contain the views
    JSplitPane tileset_split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                              tileset_scroll, canvas_scroll);
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
    
    // update parts of the UI that are updated dynamically
    updateTilesetUndoRedoUI();
    updatePaletteUndoRedoUI();
    updateFormatUI();
    
    // arrange the layout of the frame's components within the frame
    setLayout(new BorderLayout());
    Container pane = getContentPane();
    pane.add(palette_split, BorderLayout.CENTER);
    
    pack();
    
    // setup property change listeners that need to interact with the UI
    canvas_view_.addPropertyChangeListener(CanvasView.NEW_CANVAS_STATE,
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
    System.exit(0);
  }
  
  private void OpenTilesetAction(ActionEvent event) {
    File chosen_file = snes_file_chooser_.chooseTilesetToOpen();
    
    if (chosen_file != null) {  // if the user chose a file
      long bytes_not_loaded = 0;
      try {
        bytes_not_loaded = tileset_controller_.loadTileset(chosen_file);
        
        updateTilesetUndoRedoUI();
        
        tileset_view_.repaint();  // repaint since things may have changed
        canvas_view_.repaint();
      } catch (FileNotFoundException file_exception) {
        JOptionPane.showMessageDialog(
          this, file_exception.getLocalizedMessage(),
          "File Not Found Error", JOptionPane.ERROR_MESSAGE);
      } catch (IOException io_exception) {
        JOptionPane.showMessageDialog(
          this, io_exception.getLocalizedMessage(),
          "IO Error", JOptionPane.ERROR_MESSAGE);
      }
      // if there were bytes in the file that were not loaded, warn the user
      if (bytes_not_loaded != 0) {
        JOptionPane.showMessageDialog(
          this, chosen_file.getName() + " was too large. " +
          Long.toString(bytes_not_loaded) + " bytes were not loaded.",
          "File Partially Loaded", JOptionPane.WARNING_MESSAGE);
      }
    }
  }
  
  private void SaveTilesetAction(ActionEvent event) {
    File referenced_file = tileset_controller_.getReferencedFile();
    if (referenced_file != null) {  // if there is a currently referenced file
      try {
        tileset_controller_.saveTileset(referenced_file);
        updateTilesetUndoRedoUI();
        
        tileset_view_.repaint();  // repaint since things may have changed
        canvas_view_.repaint();
      } catch (FileNotFoundException file_exception) {
        JOptionPane.showMessageDialog(
          this, file_exception.getLocalizedMessage(),
          "File Not Found Error", JOptionPane.ERROR_MESSAGE);
      } catch (IOException io_exception) {
        JOptionPane.showMessageDialog(
          this, io_exception.getLocalizedMessage(),
          "IO Error", JOptionPane.ERROR_MESSAGE);
      }
    } else {  // else, make this a save as action
      SaveTilesetAsAction(event);
    }
  }
  
  private void SaveTilesetAsAction(ActionEvent event) {
    File chosen_file = snes_file_chooser_.chooseTilesetToSave();
    if (chosen_file != null) {
      // check if the file exists (and if the user wants to overwrite it if so)
      if (chosen_file.exists()) {
        int result = JOptionPane.showConfirmDialog(
          this, chosen_file.getName() + " already exists.\n" +
          "Do you want to overwrite it?", "Overwrite File?",
          JOptionPane.YES_NO_CANCEL_OPTION);
        // return (do nothing) unless the user confirms they want to overwrite
        if (result != JOptionPane.YES_OPTION) { return; }
      }
      
      try {
        tileset_controller_.saveTileset(chosen_file);
        updateTilesetUndoRedoUI();
        
        tileset_view_.repaint();  // repaint since things may have changed
        canvas_view_.repaint();
      } catch (FileNotFoundException file_exception) {
        JOptionPane.showMessageDialog(
          this, file_exception.getLocalizedMessage(),
          "File Not Found Error", JOptionPane.ERROR_MESSAGE);
      } catch (IOException io_exception) {
        JOptionPane.showMessageDialog(
          this, io_exception.getLocalizedMessage(),
          "IO Error", JOptionPane.ERROR_MESSAGE);
      }
    }
  }
  
  private void OpenPaletteAction(ActionEvent event) {
    File chosen_file = snes_file_chooser_.choosePaletteToOpen();
    
    if (chosen_file != null) {  // if the user chose a file
      long bytes_not_loaded = 0;
      try {
        bytes_not_loaded = palette_controller_.loadPalette(chosen_file);
        
        updatePaletteUndoRedoUI();
        
        palette_view_.repaint();  // repaint since things may have changed
        tileset_view_.repaint();
        canvas_view_.repaint();
      } catch (FileNotFoundException file_exception) {
        JOptionPane.showMessageDialog(
          this, file_exception.getLocalizedMessage(),
          "File Not Found Error", JOptionPane.ERROR_MESSAGE);
      } catch (IOException io_exception) {
        JOptionPane.showMessageDialog(
          this, io_exception.getLocalizedMessage(),
          "IO Error", JOptionPane.ERROR_MESSAGE);
      }
      // if there were bytes in the file that were not loaded, warn the user
      if (bytes_not_loaded != 0) {
        JOptionPane.showMessageDialog(
          this, chosen_file.getName() + " was too large. " +
          Long.toString(bytes_not_loaded) + " bytes were not loaded.",
          "File Partially Loaded", JOptionPane.WARNING_MESSAGE);
      }
    }
  }
  
  private void SavePaletteAction(ActionEvent event) {
    File referenced_file = palette_controller_.getReferencedFile();
    if (referenced_file != null) {  // if there is a currently referenced file
      try {
        palette_controller_.savePalette(referenced_file);
        updatePaletteUndoRedoUI();
        
        palette_view_.repaint();  // repaint since things may have changed
        tileset_view_.repaint();
        canvas_view_.repaint();
      } catch (FileNotFoundException file_exception) {
        JOptionPane.showMessageDialog(
          this, file_exception.getLocalizedMessage(),
          "File Not Found Error", JOptionPane.ERROR_MESSAGE);
      } catch (IOException io_exception) {
        JOptionPane.showMessageDialog(
          this, io_exception.getLocalizedMessage(),
          "IO Error", JOptionPane.ERROR_MESSAGE);
      }
    } else {  // else, make this a save as action
      SavePaletteAsAction(event);
    }
  }
  
  private void SavePaletteAsAction(ActionEvent event) {
    File chosen_file = snes_file_chooser_.choosePaletteToSave();
    if (chosen_file != null) {
      // check if the file exists (and if the user wants to overwrite it if so)
      if (chosen_file.exists()) {
        int result = JOptionPane.showConfirmDialog(
          this, chosen_file.getName() + " already exists.\n" +
          "Do you want to overwrite it?", "Overwrite File?",
          JOptionPane.YES_NO_CANCEL_OPTION);
        // return (do nothing) unless the user confirms they want to overwrite
        if (result != JOptionPane.YES_OPTION) { return; }
      }
      
      try {
        palette_controller_.savePalette(chosen_file);
        updatePaletteUndoRedoUI();
        
        palette_view_.repaint();  // repaint since things may have changed
        tileset_view_.repaint();
        canvas_view_.repaint();
      } catch (FileNotFoundException file_exception) {
        JOptionPane.showMessageDialog(
          this, file_exception.getLocalizedMessage(),
          "File Not Found Error", JOptionPane.ERROR_MESSAGE);
      } catch (IOException io_exception) {
        JOptionPane.showMessageDialog(
          this, io_exception.getLocalizedMessage(),
          "IO Error", JOptionPane.ERROR_MESSAGE);
      }
    }
  }
  
  private void TilesetUndoAction(ActionEvent event) {
    if (tileset_controller_.canUndo()) {
      tileset_controller_.undo();
      updateTilesetUndoRedoUI();
      updateFormatUI();
      
      tileset_view_.repaint();  // repaint since things may have changed
      canvas_view_.repaint();
    }
  }
  
  private void TilesetRedoAction(ActionEvent event) {
    if (tileset_controller_.canRedo()) {
      tileset_controller_.redo();
      updateTilesetUndoRedoUI();
      updateFormatUI();
      
      tileset_view_.repaint();  // repaint since things may have changed
      canvas_view_.repaint();
    }
  }
  
  private void PaletteUndoAction(ActionEvent event) {
    if (palette_controller_.canUndo()) {
      palette_controller_.undo();
      updatePaletteUndoRedoUI();
      
      palette_view_.repaint();  // repaint since things may have changed
      canvas_view_.repaint();
      tileset_view_.repaint();
    }
  }
  
  private void PaletteRedoAction(ActionEvent event) {
    if (palette_controller_.canRedo()) {
      palette_controller_.redo();
      updatePaletteUndoRedoUI();
      
      palette_view_.repaint();  // repaint since things may have changed
      canvas_view_.repaint();
      tileset_view_.repaint();
    }
  }
  
  private void CanvasZoomChangeAction(ActionEvent event) {
    // parse the command, except the first character, which should be the number
    double scale_factor = Double.parseDouble(
      event.getActionCommand().substring(1));
    canvas_view_.setScaleFactor(scale_factor);
    canvas_view_.repaint();  // repaint the canvas with the new scaling factor
  }
  
  private void BPPChangeAction(ActionEvent event) {
    // split and parse the first part of the command, which should be the number
    int bpp = Integer.parseInt(event.getActionCommand().split(" ")[0]);
    // proceed only if the bpp is within a tolerable range
    if (bpp >= Tileset.MIN_BPP && bpp <= Tileset.MAX_BPP) {
      palette_controller_.setBPP(bpp);
      tileset_controller_.changeBPP(bpp);
      updateTilesetUndoRedoUI();
      updateFormatUI();
      
      palette_view_.repaint();  // repaint since things may have changed
      canvas_view_.repaint();
      tileset_view_.repaint();
    }
  }
  
  private void BitplaneFormatChangeAction(ActionEvent event) {
    // match the text of the chosen format to the corresponding number
    int bitplane_format;
    switch (event.getActionCommand()) {
      case "Serial":
        bitplane_format = Tileset.BITPLANE_SERIAL;
        break;
      case "Planar":
        bitplane_format = Tileset.BITPLANE_PLANAR;
        break;
      case "Intertwined":
        bitplane_format = Tileset.BITPLANE_INTERTWINED;
        break;
      default:
        throw new IllegalArgumentException(
          event.getActionCommand() +
          " is an unrecognized bitplane format action command.");
    }
    
    tileset_controller_.changeBitplaneFormat(bitplane_format);
    updateTilesetUndoRedoUI();
    updateFormatUI();
    
    canvas_view_.repaint();  // repaint since things may have changed
    tileset_view_.repaint();
  }
  
  private void TilesetStateChange(PropertyChangeEvent event) {
    updateTilesetUndoRedoUI();
  }
  
  private void PaletteStateChange(PropertyChangeEvent event) {
    updatePaletteUndoRedoUI();
  }
  
  private void PaletteSubpaletteChange(PropertyChangeEvent event) {
    canvas_view_.repaint();
    tileset_view_.repaint();
  }
  
  // methods to dynamically update the UI
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
    
    // match the current bitplane format to the right JRadioButtonMenuItem
    switch (tileset_controller_.getBitplaneFormat()) {
      case Tileset.BITPLANE_SERIAL:
        serial_bitplane_format_item_.setSelected(true);
        break;
      case Tileset.BITPLANE_PLANAR:
        planar_bitplane_format_item_.setSelected(true);
        break;
      case Tileset.BITPLANE_INTERTWINED:
        intertwined_bitplane_format_item_.setSelected(true);
        break;
      default:
        throw new RuntimeException(
          Integer.toString(tileset_controller_.getBitplaneFormat()) +
          " is an unrecognized bitplane format menu option.");
    }
  }
  
  // parts of the UI that need to be accessible to be updated dynamically
  private final JMenuItem tileset_undo_menu_item_;
  private final JMenuItem tileset_redo_menu_item_;
  private final JMenuItem palette_undo_menu_item_;
  private final JMenuItem palette_redo_menu_item_;
  private final JRadioButtonMenuItem one_bpp_item_;
  private final JRadioButtonMenuItem two_bpp_item_;
  private final JRadioButtonMenuItem three_bpp_item_;
  private final JRadioButtonMenuItem four_bpp_item_;
  private final JRadioButtonMenuItem eight_bpp_item_;
  private final JRadioButtonMenuItem serial_bitplane_format_item_;
  private final JRadioButtonMenuItem planar_bitplane_format_item_;
  private final JRadioButtonMenuItem intertwined_bitplane_format_item_;
  
  // the file chooser used to prompt the user to load and save files
  private final SNESFileChooser snes_file_chooser_;
  
  // controllers and views
  private final PaletteController palette_controller_;
  private final TilesetController tileset_controller_;
  
  private final PaletteView palette_view_;
  private final TilesetView tileset_view_;
  private final CanvasView canvas_view_;
}
