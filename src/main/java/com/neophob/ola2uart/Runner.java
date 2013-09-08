package com.neophob.ola2uart;

import java.util.Map;
import java.util.logging.Logger;

import ola.OlaClient;
import ola.proto.Ola.DmxData;
import ola.proto.Ola.PluginListReply;
import ola.proto.Ola.UniverseInfoReply;

import com.neophob.ola2uart.config.Config;
import com.neophob.ola2uart.ola.OlaHelper;
import com.neophob.ola2uart.output.tpm2.Tpm2Protocol;
import com.neophob.ola2uart.stat.StatisticHelper;

/**
 *  
 * hint, do not use register for dmx:
 * 	The problem is that all the calls are blocking, so streaming updates 
 * 	from the server aren't implemented. Calling registerForDMX will 
 * 	probably break things as the olad server will start sending updates to 
 * 	the Java client, but the Java client won't try to read from the socket 
 * 	until another API call is made. 
 * 
 * https://groups.google.com/forum/#!searchin/open-lighting/registerForDmx/open-lighting/XhmJ_YlnJGk/YLdY3maYnE8J
 * 
 * 
 * @author michu
 *
 */
public class Runner {

	private static final String VERSION = "0.1";

	private static final long WARNING_NO_DMX_DATA_TIMEOUT = 60000;
	private static final long STATISTIC_OUTPUT_TIMEOUT = 30000;

	private static final Logger LOG = Logger.getLogger(Runner.class.getName());

	private static Config cfg = null;
	
	private static long lastErrorMessage=0;
	private static long lastDebugOutput=0;		
	

	/**
	 * 
	 * https://code.google.com/p/open-lighting/source/browse/java/src/test/java/ola/OlaClientTest.java#95
	 * 
	 * options needed:
	 * - which dmx universe should be read
	 * - how which offsetshould be sent out
	 * - host
	 * 
	 * example:
	 *  read universe 1,2,3,4
	 *  send out as offset 0,1,2,3
	 * 
	 * -> parameter DMXUNIVERSE:PANEL_OFFSET
	 * 
	 * 
	 * @param args
	 */
	public static void main(String[] args) throws Exception {		
		LOG.info("OLA-to-TPM2.net Daemon v"+VERSION+" by Michael Vogt / neophob.com");
		LOG.info("Read DMX universe from OLA and send the as TPM2 packet to the serial port");

		cfg = new Config(args);

		OlaClient olaClient=OlaHelper.connectToOlad();

		PluginListReply replyPlugins = olaClient.getPlugins();        
		LOG.finest(replyPlugins.toString());        

		LOG.info("Verify DMX Universe");
		for (Map.Entry<Integer,Integer> e: cfg.getDmxToOffsetMap().entrySet()) {
			UniverseInfoReply u = olaClient.getUniverseInfo(e.getKey());
			try {
				LOG.finest(u.toString());
			} catch (NullPointerException npe) {
				LOG.severe("Universe "+e.getKey()+" does not exist, check your OLA configuration!");
				System.exit(7);
			}
		}

		Framerate framerate = new Framerate(cfg.getFps());

		LOG.info("enter main loop...");
		while (true) {

			try {
				eventLoop(olaClient, framerate);
			} catch (NullPointerException e) {
				// happens if olad is restarted (for example), wait and retry!
				LOG.severe("NullPointer detected, sleep 1s and try to reconnect ...");	
				Thread.sleep(1000);

				try {
					olaClient = new OlaClient();
				} catch (Exception e2) {
					LOG.info("... failed to reinit OlaClient");
				}
			}

		}
	}

	/**
	 * 
	 * @param olaClient
	 * @param framerate
	 */
	private static void eventLoop(OlaClient olaClient, Framerate framerate) {
		int currentUniverse=0;				
		for (Map.Entry<Integer,Integer> e: cfg.getDmxToOffsetMap().entrySet()) {
			long t1 = System.currentTimeMillis();
			DmxData reply = olaClient.getDmx(e.getKey());					
			long t2 = System.currentTimeMillis()-t1;
			short[] dmxData = olaClient.convertFromUnsigned(reply.getData());					

			if (cfg.isDebugOutput()) {
				debugln("Time to get data from universe "+e.getKey()+": "+t2+"ms");
			}

			if (dmxData.length>0) {
				debug(StatisticHelper.INSTANCE.getFrameCount()+" send "+dmxData.length+" bytes to universe "+currentUniverse);

				byte[] data = Tpm2Protocol.doProtocol(dmxData, currentUniverse, cfg.getDmxToOffsetMap().size());
				cfg.getOutput().sendData(currentUniverse, data);

				StatisticHelper.INSTANCE.incrementAndGetPacketsRecieved();
				StatisticHelper.INSTANCE.updateSendBytes(dmxData.length);
				currentUniverse++;

				debugln("... done");
				if (System.currentTimeMillis()-lastDebugOutput > STATISTIC_OUTPUT_TIMEOUT) {
					LOG.info(" sent "+StatisticHelper.INSTANCE.getPacketCount()+" packages ("+(StatisticHelper.INSTANCE.getSentBytes()/1024)+" kb), errors: "
							+StatisticHelper.INSTANCE.getErrorCount());						
					lastDebugOutput = System.currentTimeMillis();
				}

			} else {
				debugln("no data found for universe "+e.getKey());
				if (System.currentTimeMillis()-lastErrorMessage > WARNING_NO_DMX_DATA_TIMEOUT) {
					LOG.info("no dmx data for universe "+e.getKey());						
					lastErrorMessage = System.currentTimeMillis();
				}
			}
		}

		long cnt = StatisticHelper.INSTANCE.incrementAndGetFrameCount();
		framerate.waitForFps(cnt);		
	}

	/**
	 * 
	 * @param s
	 */
	private static void debugln(String s) {
		if (cfg.isDebugOutput()) {
			System.out.println(s);
		}		
	}

	/**
	 * 
	 * @param s
	 */
	private static void debug(String s) {
		if (cfg.isDebugOutput()) {
			System.out.print(s);
		}		
	}

}
