package com.neophob.ola2uart.output;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author michu
 *
 */
public class OutputNet extends AbstractOutput {

	private static final Logger LOG = Logger.getLogger(OutputNet.class.getName());

	private DatagramPacket packet;
	private DatagramSocket dsocket;

	public OutputNet(int port) throws Exception {
		LOG.info("Initialize Network Device 127.0.0.1:"+port);

		packet = new DatagramPacket(new byte[0], 0, InetAddress.getByName("127.0.0.1"), port);
		dsocket = new DatagramSocket();
	}

	public void sendData(int ofs, byte[] data) {
		
		if (!didFrameChange(ofs, data)) {
			return;
		}
		
		packet.setData(data);
		packet.setLength(data.length);
		try {
			dsocket.send(packet);
		} catch (IOException e) {
			LOG.log(Level.WARNING, "failed to send network data.", e);				
		}

	}

}
