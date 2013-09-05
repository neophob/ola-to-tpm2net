package com.neophob.ola2uart;

import java.net.ConnectException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

import ola.OlaClient;
import ola.proto.Ola.DmxData;
import ola.proto.Ola.PluginListReply;
import ola.proto.Ola.UniverseInfoReply;

import com.neophob.ola2uart.log.MyFormatter;
import com.neophob.ola2uart.stat.StatisticHelper;
import com.neophob.ola2uart.tpm2.Tpm2Protocol;
import com.neophob.ola2uart.tpm2.Tpm2Serial;

/**
 * TODO: 
 * 	parameter
 * 	error handling (olad restart)
 *  finally
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

	private static final Logger LOG = Logger.getLogger(Runner.class.getName());
	 
	private static final String ERR_MSG_DEVICE = "-d requires an string value";
	private static final String ERR_MSG_MAPPING = "-u requires a DMX UNIVERSE to OFFSET mapping like 3:0";
	
	private static void displayHelp() {
		LOG.info("Usage:\t\tRunner -u 0:0 -u 1:1 -d /dev/tty.usbmodem.1234");
		LOG.info("");
		LOG.info("      \t\t -u define DMX Universe to offset mapping (can be used multiple times)");
		LOG.info("      \t\t -d usb device that recieve the data using the tpm2.net protocol");
		LOG.info("Make sure OLAD is running on 127.0.0.1:9010");
	}

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
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		LOG.setUseParentHandlers(false);
        MyFormatter formatter = new MyFormatter();
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(formatter);
        LOG.addHandler(handler);
        
		LOG.info("OLA-to-TPM2.net Daemon v"+VERSION+" by Michael Vogt / neophob.com");
		LOG.info("Read DMX universe from OLA and send the as TPM2 packet to the serial port");

		if (args.length < 3) {
        	displayHelp();
        	System.exit(1);
        }
		
		String serialDevice = "";
		Map<Integer, Integer> dmxToOffsetMap = new HashMap<Integer, Integer>();

		int i=0;
        while (i < args.length && args[i].startsWith("-")) {
        	String arg = args[i++];

        	if (arg.equals("-d")) {
        		if (i < args.length) { 
        			serialDevice = args[i++];
        			if (serialDevice.length()<3) {
            			LOG.severe(ERR_MSG_DEVICE);
            			System.exit(5);        				
        			}
        		} else {
        			LOG.severe(ERR_MSG_DEVICE);
        			System.exit(5);
        		}
        	}
        	
        	if (arg.equals("-u")) {
        		if (i < args.length) { 
        			String rawConfig = args[i++];
        			String[] data = rawConfig.split(":");
        			if (data==null || data.length<2) {
        				LOG.severe(ERR_MSG_MAPPING);
        				System.exit(5);
        			}
        			dmxToOffsetMap.put(Integer.parseInt(data[0]), Integer.parseInt(data[1]));
        		} else {
        			LOG.severe(ERR_MSG_MAPPING);
        			System.exit(5);
        		}
        	}        	
        }
        
        if (dmxToOffsetMap.size()==0) {
        	LOG.severe("No DMX/Offset Mapping information found! Exit now.");
        	System.exit(6);
        }

        LOG.info("Using Serial Device <"+serialDevice+">");
        LOG.info("Using DMX Universe to destional offset mapping: ");
        for (Map.Entry<Integer, Integer> e: dmxToOffsetMap.entrySet()) {
        	LOG.info("  Map DMX Universe "+e.getKey()+" to destination offset "+e.getValue());
        }
        
        LOG.finest("Init OLA Client");
		OlaClient olaClient=null;
		boolean oladConnectionAvailable = false;
		while (!oladConnectionAvailable) {
			try {
				olaClient = new OlaClient();	
			} catch (ConnectException e) {
				LOG.info("no olad server available, retry in 1s...");
				Thread.sleep(1000);
			}
			
		}
			
		PluginListReply replyPlugins = olaClient.getPlugins();        
		LOG.finest(replyPlugins.toString());        

		LOG.finest("Verify DMX Universe");
		for (Map.Entry<Integer,Integer> e: dmxToOffsetMap.entrySet()) {
			UniverseInfoReply u = olaClient.getUniverseInfo(e.getKey());
			LOG.finest(u.toString());
		}

		Tpm2Serial tpm2 = new Tpm2Serial(serialDevice, 115200);

		while (true) {
			//todo grab universe 0+1, send data to teensy
			//todo grab universe 2+3, send data to teensy

			if (!tpm2.connected()) {
				LOG.severe("SERIAL DISCONNECT! ");
				return;
			}
			
			int currentUniverse=0;

			try {
				
				for (Map.Entry<Integer,Integer> e: dmxToOffsetMap.entrySet()) {
					DmxData reply = olaClient.getDmx(e.getKey());
					short[] dmxData = olaClient.convertFromUnsigned(reply.getData());
					if (dmxData.length>2) {
						//send data via serial port
						tpm2.sendFrame((byte)currentUniverse, Tpm2Protocol.doProtocol(dmxData, currentUniverse, dmxToOffsetMap.size()));
						
						StatisticHelper.INSTANCE.incrementAndGetPacketsRecieved();
						currentUniverse++;

						Thread.sleep(5);
						while (tpm2.getPort().available() > 0) {
							System.out.println(tpm2.getPort().readString());
						} 

					} else { 
						LOG.info("no dmx data for universe "+e.getKey());
					}
				}

				
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

}
