package org.jurr.cube3d.cubecli.sender.wifly;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

/*
 * Source: WiFly user manual, page 52:
 *
 * The format of the packet is 110 bytes of data.
 * 0-5		MAC address of AP that we are Associated with (for location)
 * 6		Channel we are on
 * 7		RSSI
 * 8-9		local TCP port# (for connecting into the Wifly device)
 * 10-13	RTC value (MSB first to LSB last)
 * 14-15	Battery Voltage on Pin 20 in millivolts (2755 for example)
 * 16-17	value of the GPIO pins
 * 18-31	ASCII time
 * 32-59	Version string with date code
 * 60-91	Programmable Device ID string (set option deviceid <string>)
 * 92-93	Boot time in milliseconds
 * 94-109	Voltage readings of Sensors 0 thru 7 (enabled with "set opt format <mask>")
 */

public class BroadcastMessage {
	protected static final int UDP_PACKET_SIZE = 110;

	protected static BroadcastMessage fromDatagramPacket(final DatagramPacket datagramPacket) {
		final var result = new BroadcastMessage();
		result.address = datagramPacket.getAddress();
		result.port = datagramPacket.getPort();

		final var data = datagramPacket.getData();
		result.accesspointMacAddress = new int[] { data[0] & 0xff, data[1] & 0xff, data[2] & 0xff, data[3] & 0xff,
				data[4] & 0xff, data[5] & 0xff };
		result.wifiChannel = data[6];
		result.receivedSignalStrengthIndicator = data[7] & 0xff;
		result.callbackPort = (data[8] & 0xff) << 8 | data[9] & 0xff;
		result.rtc = (data[10] & 0xff) << 24 | (data[11] & 0xff) << 16 | (data[12] & 0xff) << 8 | data[13] & 0xff;
		result.batteryVoltage = (data[14] & 0xff) << 8 | data[15] & 0xff;

		// TODO: Bytes 16 and 17

		final var time = new byte[13];
		System.arraycopy(data, 18, time, 0, time.length);
		result.time = new String(time, StandardCharsets.ISO_8859_1);

		final var version = new byte[26];
		System.arraycopy(data, 32, version, 0, version.length);
		result.version = new String(version, StandardCharsets.ISO_8859_1);

		final var deviceId = new byte[32];
		System.arraycopy(data, 60, deviceId, 0, deviceId.length);
		result.deviceId = new String(deviceId, StandardCharsets.ISO_8859_1);

		result.bootTime = (data[92] & 0xff) << 8 | data[93] & 0xff;

		// TODO: Bytes 94-109

		return result;
	}

	private int[] accesspointMacAddress;
	private InetAddress address;
	private int batteryVoltage;
	private int bootTime;
	private int callbackPort;
	private String deviceId;
	private int port;
	private int receivedSignalStrengthIndicator;
	private long rtc;
	private String time;
	private String version;
	private byte wifiChannel;

	public int[] getAccesspointMacAddress() {
		return accesspointMacAddress;
	}

	public InetAddress getAddress() {
		return address;
	}

	public int getBatteryVoltage() {
		return batteryVoltage;
	}

	public int getBootTime() {
		return bootTime;
	}

	public int getCallbackPort() {
		return callbackPort;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public int getPort() {
		return port;
	}

	public int getReceivedSignalStrengthIndicator() {
		return receivedSignalStrengthIndicator;
	}

	public long getRtc() {
		return rtc;
	}

	public String getTime() {
		return time;
	}

	public String getVersion() {
		return version;
	}

	public byte getWifiChannel() {
		return wifiChannel;
	}
}