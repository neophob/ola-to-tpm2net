package com.neophob.ola2uart.output;

import java.util.logging.Logger;

import com.neophob.ola2uart.output.tpm2.NoSerialPortFoundException;
import com.neophob.ola2uart.output.tpm2.Tpm2Serial;

/**
 * 
 * @author michu
 *
 */
public class OutputSerial implements IOutput {

	private static final Logger LOG = Logger.getLogger(OutputSerial.class.getName());
	
	private Tpm2Serial tpm2 = null;
	private boolean debugOutput;
	

	public OutputSerial(String serialDevice, boolean debugOutput) {
		this.debugOutput = debugOutput;
		LOG.info("Initialize Serial Device <"+serialDevice+">");
		try {
			tpm2 = new Tpm2Serial(serialDevice, 115200);
		} catch (NoSerialPortFoundException e) {
			LOG.severe("Failed to open serial port!");
		}

	}

	public void sendData(int ofs, byte[] data) {
		if (!tpm2.connected()) {
			LOG.severe("SERIAL DISCONNECT! ");
			return;
		}

		tpm2.sendFrame((byte)ofs, data);
		
		if (debugOutput) {
			while (tpm2.getPort().available() > 0) {			
				System.out.println(tpm2.getPort().readString());
			} 			
		}

	}

}
