package com.neophob.ola2uart;

import java.util.HashMap;
import java.util.Map;

import ola.OlaClient;
import ola.proto.Ola.DmxData;
import ola.proto.Ola.PluginListReply;
import ola.proto.Ola.UniverseInfoReply;

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

	private final static String DEFAULT_HOST = "localhost";

	private final static int DEFAULT_PORT = 9010;

	private OlaClient client;	

	private static void displayHelp() {
		System.out.println("Usage:\t\tRunner [-p port] [-b bus] [-d delay] [-t i2c_targets]\n");
		System.out.println("Example:\tRunner -p 65506 -b 1 -d 10 -t 4:5:6:7");
		System.out.println("\t");
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
		System.out.println("OLA-to-UART Server v"+VERSION+" by Michael Vogt / neophob.com");
		System.out.println("Read DMX universe from OLA and send the as TPM2 packet to the serial port");

		/*        if (args.length < 2) {
        	displayHelp();
        	System.exit(1);
        }*/

		Map<Integer, Integer> dmxToOffsetMap = new HashMap<Integer, Integer>();
		dmxToOffsetMap.put(0, 0);	//dmx universe 1 map to offset 0
		dmxToOffsetMap.put(1, 1);
		dmxToOffsetMap.put(2, 1);
		dmxToOffsetMap.put(3, 1);

		/*int i=0;
        while (i < args.length && args[i].startsWith("-")) {
        	String arg = args[i++];

        	if (arg.equals("-p")) {
        		if (i < args.length) { 
        			port = Integer.parseInt(args[i++]);
        		} else {
                    System.err.println("-port requires an integer value");
        		}
        	}
        }*/

		OlaClient olaClient = new OlaClient();

		PluginListReply replyPlugins = olaClient.getPlugins();        
		System.out.println(replyPlugins);        

		for (Map.Entry<Integer,Integer> e: dmxToOffsetMap.entrySet()) {
			UniverseInfoReply u = olaClient.getUniverseInfo(e.getKey());
			System.out.println(u);
		}

		Tpm2Serial tpm2 = new Tpm2Serial("/dev/tty.usbmodem5781", 115200);

		while (true) {
			//todo grab universe 0+1, send data to teensy
			//todo grab universe 2+3, send data to teensy

			if (!tpm2.connected()) {
				System.out.println("SERIAL DISCONNECT!");
				return;
			}
			
			int currentUniverse=0;

			try {
				
				for (Map.Entry<Integer,Integer> e: dmxToOffsetMap.entrySet()) {
					DmxData reply = olaClient.getDmx(e.getKey());
					short[] dmxData = olaClient.convertFromUnsigned(reply.getData());
					if (dmxData.length>2) {
						tpm2.sendFrame((byte)currentUniverse, Tpm2Protocol.doProtocol(dmxData, currentUniverse, dmxToOffsetMap.size()));
						
						StatisticHelper.INSTANCE.incrementAndGetPacketsRecieved();
						currentUniverse++;

						Thread.sleep(5);
						while (tpm2.getPort().available() > 0) {
							System.out.println(tpm2.getPort().readString());
						} 

					} else { 
						System.out.println("no dmx data for universe "+e.getKey());
					}
				}

				
			} catch (NullPointerException e) {
				// happens if olad is restarted (for example), wait and retry!
				System.out.println("NPE! wait");	
				Thread.sleep(1000);
				
				try {
					olaClient = new OlaClient();
				} catch (Exception e2) {
					System.out.println("Failed to reinit OlaClient");
				}
			}

		}
	}

}
