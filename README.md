# GEPS
![Release Build](https://github.com/nlovdahl/GEPS/workflows/Release%20Build/badge.svg)
![Development Build](https://github.com/nlovdahl/GEPS/workflows/Development%20Build/badge.svg)

GEPS is a Graphics Editing Program for SNES homebrew development. GEPS can create, load, edit, and save tilesets and palettes that can be used on SNES hardware. GEPS supports multiple tileset formats (serial, planar, and intertwined) and a varying number of bits per pixel (1, 2, 3, 4, and 8). Tilesets can be viewed and edited using different subpalettes (depending on the number of bits per pixel being used).

For the full changelog for GEPS, see `CHANGELOG.md`.

## Installation
GEPS can be built using Maven. You can [download](https://maven.apache.org/download.cgi) and [install Maven](http://maven.apache.org/install.html) if you don't already have it. Once Maven is installed, you can use the following commands:

*	Use `mvn test` to run unit tests.
*	Use `mvn package` to compile a package in the current repository. This should produce a runnable JAR file.
*	Use `mvn clean` to clean the target folder, removing files produced by any previous build.
*	Use `mvn clean package` to clean and then rebuild the package. This will compile a package from scratch.

## Licensing
GEPS is licensed under the GNU General Public License - either version 3 or, at your discretion, any later version. You can read `LICENSE` or visit https://www.gnu.org/licenses/gpl-3.0.html for the full license text.
