# Cube CLI
This project provides a command line application to interface with a Cube 3D printer from 3D systems.
With this program, you can:
- Decode your .cube files to .gcode files to inspect them
- Encode those .gcode files back to .cube files
- Send .cube files to your Cube printer
- Send .gcode files (encoded on the fly) to your Cube printer
- Query your Cube printer on type, material, and (in case you used auto-detect) the values it broadcasts

You can specify where your Cube printer is by giving `--host` and `--port`.
If you do not give those, the program will auto-detect your Cube. (It does so by listening for UDP broadcast packets on port 55555 sent by the WiFly card.)

## Usage
Please use the `--help` flag to give a summary of the options.

The most essential options are:
- `--decode` (to decode a .cube file to .gcode)
- `--encode` (to encode a .gcode file to .cube)
- `--send` (to send a .cube file or a .gcode file to a printer)
- `--inquiry` (to query your printer for information)