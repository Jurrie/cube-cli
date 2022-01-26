package org.jurr.cube3d.cubecli;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.beust.jcommander.Parameter;

public final class Settings {
	public static final Settings INSTANCE = new Settings();

	@Parameter(names = { "-d", "--decode" }, description = "Decode a .cube file to a .gcode file")
	private String fileToDecode;

	@Parameter(names = { "-e", "--encode" }, description = "Encode a .gcode file to a .cube file")
	private String fileToEncode;

	@Parameter(names = { "-s", "--send" }, description = "Send a .cube or .gcode file to a Cube printer")
	private String fileToSend;

	@Parameter(names = { "-h", "--help" }, description = "Show this help message", help = true)
	private boolean help;

	@Parameter(names = {
			"--host" }, description = "Host or IP of the Cube you want to send to (turns off autodetection)")
	private String host;

	@Parameter(names = { "-i", "--inquiry" }, description = "Retrieve information from a Cube printer")
	private boolean inquiry;

	@Parameter(names = { "-o",
			"--output" }, description = "Output file (for encode and decode operation). If not given, use standard output.")
	private String outputFile;

	@Parameter(names = { "-p", "--port" }, description = "Port number of the Cube you want to send to")
	private int port = 2000;

	@Parameter(names = { "--trace", "-t", "--debug" }, description = "Trace output (implies -v)")
	private boolean traceOutput;

	@Parameter(names = { "--log", "-l", "--verbose", "-v" }, description = "Verbose output")
	private boolean verboseOutput;

	private Settings() {
	}

	public Path getFileToDecode() {
		return Paths.get(fileToDecode);
	}

	public Path getFileToEncode() {
		return Paths.get(fileToEncode);
	}

	public Path getFileToSend() {
		return Paths.get(fileToSend);
	}

	public String getHost() {
		return host;
	}

	public Path getOutputFile() {
		return Paths.get(outputFile);
	}

	public int getPort() {
		return port;
	}

	public boolean isAutodetectEnabled() {
		return host == null;
	}

	public boolean isDecodeFile() {
		return fileToDecode != null;
	}

	public boolean isEncodeFile() {
		return fileToEncode != null;
	}

	public boolean isHelp() {
		return help;
	}

	public boolean isInquiry() {
		return inquiry;
	}

	public boolean isSendFile() {
		return fileToSend != null;
	}

	public boolean isTraceOutput() {
		return traceOutput;
	}

	public boolean isUseStandardOutput() {
		return outputFile == null;
	}

	public boolean isVerboseOutput() {
		return verboseOutput || traceOutput;
	}

	public void setHost(final String host) {
		this.host = host;
	}

	public void setPort(final int port) {
		this.port = port;
	}
}