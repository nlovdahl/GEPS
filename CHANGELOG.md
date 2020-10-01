# Changelog

## v0.2.0 (2020-09-30)
This update adds in some basic quality of life features related to the tileset view as well as a bugfix for filenames.

### Features
* The tileset view can now show gridlines for pixels and / or tiles. Each of these gridlines can be toggled on or off in the view menu.
* The option to change the width of the tileset has been changed to an option to change the maximum width of the tileset - the maximum width of the tileset will not be changed by adding or removing tiles.
* Added keyboard shortcuts to create, open, save, undo, and redo both tilesets and palettes.

### Bug Fixes
* A base extension, .chr for tilesets and .pal for palettes, will now be added to filenames when saved instead of "null".

## v0.1.0 (2020-09-01)
This is the first preliminy version of GEPS. It has some basic features realted to editing tilesets and palettes for the SNES, although these features have not all been extensively tested and are subject to substantial change.

### Features
* Loading and saving tileset files with various tileset formats and numbers of bits per pixel.
* Loading and saving palette files with various numbers of bits per pixel.
* Editing tilesets and palettes.
* Reinterpreting tilesets for different tileset formats and numbers of bits per pixel.
* Selecting different subpalettes from the palette.
* Undo and redo states for both tilesets and palettes.
* Increasing or decreasing the number of tiles in the tileset.
* Changing the zoom level of the tileset view.
* Changing the width of the tileset for the tileset view.
