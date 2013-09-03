package com.neophob.ola2uart;

import java.util.HashMap;
import java.util.Map;

import ola.OlaClient;
import ola.proto.Ola.DmxData;
import ola.proto.Ola.PluginListReply;
import ola.proto.Ola.RegisterAction;

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
        dmxToOffsetMap.put(1, 0);	//dmx universe 1 map to offset 0
        dmxToOffsetMap.put(2, 1);
        
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
        System.out.println("Try to connect to OLA");
     /*   try {
            //olaClient.connect("192.168.225.13", DEFAULT_PORT);
            olaClient.connect(DEFAULT_HOST, DEFAULT_PORT);
        } catch (Exception e) {
        	System.out.println("failed to connect to the olad!");
        	System.exit(1);
        }
       */
        PluginListReply replyPlugins = olaClient.getPlugins();
        
        System.out.println(replyPlugins);        
        //System.out.println("Universe 0 name: "+olaClient.getUniverseInfo(0).getUniverse(0).getName());
        
        //DmxData reply = olaClient.getDmx(0);
        //short[] dmxData = olaClient.convertFromUnsigned(reply.getData());
        System.out.println("register for dmx universe 0");
        olaClient.registerForDmx(0, RegisterAction.REGISTER);
        
        DmxData reply = olaClient.getDmx(0);
        short[] dmxData = olaClient.convertFromUnsigned(reply.getData());
        if (dmxData.length>2) {
            System.out.println(dmxData[0]);
            System.out.println(dmxData[1]);
            System.out.println(dmxData[2]);        	
        } else { 
        	System.out.println("no dmx data");
        }
	}

}
