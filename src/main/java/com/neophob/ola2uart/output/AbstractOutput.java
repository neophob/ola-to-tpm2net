package com.neophob.ola2uart.output;

import java.util.HashMap;
import java.util.Map;
import java.util.zip.Adler32;

import com.neophob.ola2uart.output.IOutput;

public abstract class AbstractOutput implements IOutput {

	private static Adler32 adler = new Adler32();
	
	private Map<Integer, Long> lastDataMap = new HashMap<Integer, Long>();
	
	protected boolean didFrameChange(int ofs, byte data[]) {
		adler.reset();
		adler.update(data);
		long l = adler.getValue();

		if (!lastDataMap.containsKey(ofs)) {
			//first run
			lastDataMap.put(ofs, l);
			return true;
		}

		if (lastDataMap.get(ofs) == l) {
			//last frame was equal current frame, do not send it!
			//log.log(Level.INFO, "do not send frame to {0}", addr);
			return false;
		}
		//update new hash
		lastDataMap.put(ofs, l);
		return true;
	}

	public abstract void sendData(int ofs, byte[] data);
}
