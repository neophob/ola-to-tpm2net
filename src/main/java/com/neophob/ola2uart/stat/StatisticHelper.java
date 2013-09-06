package com.neophob.ola2uart.stat;

import java.util.concurrent.atomic.AtomicLong;


/**
 * singleton to track statistic, thread safe
 * 
 * @author michu
 *
 */
public enum StatisticHelper {

	INSTANCE;

	private AtomicLong packetsRecieved = new AtomicLong();
	private AtomicLong errors = new AtomicLong();
	private AtomicLong sendBytes = new AtomicLong();
	private AtomicLong frameCount = new AtomicLong();

	public long incrementAndGetFrameCount() {
		return frameCount.incrementAndGet();
	}

	public long incrementAndGetPacketsRecieved() {
		return packetsRecieved.incrementAndGet();
	}

	public long incrementAndGetError() {
		return errors.incrementAndGet();
	}

	public long updateSendBytes(long delta) {
		return sendBytes.addAndGet(delta);
	}
	
	public long getErrorCount() {
		return errors.get();
	}
	
	public long getPacketCount() {
		return packetsRecieved.get();
	}
	
	public long getSentBytes() {
		return sendBytes.get();
	}
	
	public long getFrameCount() {
		return frameCount.get();
	}

}
