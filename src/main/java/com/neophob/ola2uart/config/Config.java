package com.neophob.ola2uart.config;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.neophob.ola2uart.output.IOutput;
import com.neophob.ola2uart.output.OutputNet;
import com.neophob.ola2uart.output.OutputSerial;

public class Config {
	
	private static final String ERR_MSG_DEVICE = "-d requires an string value";
	private static final String ERR_MSG_MAPPING = "-u requires a DMX UNIVERSE to OFFSET mapping like 3:0";	
	private static final int DEFAULT_FPS = 20;
	private static final Logger LOG = Logger.getLogger(Config.class.getName());
	
	private String serialDevice = "";
	private int netPort = -1;
	private IOutput output;
	private int fps = DEFAULT_FPS;
	private Map<Integer, Integer> dmxToOffsetMap = new HashMap<Integer, Integer>();
	
	private boolean olaWorkaround=false;
	private boolean debugOutput=false;
	private boolean verboseDebugOutput=false;
	
	public Config(String[] args) {
		//prevalidate
		if (args.length < 3) {
        	displayHelp();
        	System.exit(1);
        }

		this.parseArguments(args);
		
		//postvalidate
        if (dmxToOffsetMap.size()==0) {
        	LOG.severe("No DMX/Offset Mapping information found! Exit now.");
        	System.exit(6);
        }
        
        if (serialDevice.isEmpty() && netPort==-1) {
        	LOG.severe("No Output Port defined! Exit now.");
        	System.exit(7);        	
        }

        //init output
        if (netPort==-1) {
            output = new OutputSerial(serialDevice, debugOutput);        	
        } else {
            try {
				output = new OutputNet(netPort);
			} catch (Exception e) {
				LOG.severe("Failed to initialize net output!");
				e.printStackTrace();
				System.exit(10);
			}
        }
        
        LOG.info("Using DMX Universe to destional offset mapping: ");
        for (Map.Entry<Integer, Integer> e: dmxToOffsetMap.entrySet()) {
        	LOG.info("  Map DMX Universe "+e.getKey()+" to destination offset "+e.getValue());
        }

	}
	
	private void displayHelp() {
		System.out.println("Usage:\tRunner -u 0:0 -u 1:1 -d /dev/tty.usbmodem.1234 [-f 20]\n");
		System.out.println("      \t -u define DMX Universe to offset mapping (can be used multiple times)");
		System.out.println("      \t -d usb device that recieve the data using the tpm2.net protocol");
		System.out.println("      \t -f desired framerate (fps)");
		System.out.println("      \t -v enable verbose output");
		System.out.println("      \t -vv enable very verbose output");
		System.out.println("      \t -w enable workarround mode, only set all channels to black if the packet was send 3 times");
		System.out.println("Make sure OLAD is running on 127.0.0.1:9010");
	}

	private void parseArguments(String[] args) {
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

        	if (arg.equals("-p")) {
        		if (i < args.length) { 
        			netPort = Integer.parseInt(args[i++]);
        		} else {
        			LOG.severe("-p requires a integer value");
        			System.exit(5);
        		}
        	}

        	if (arg.equals("-f")) {
        		if (i < args.length) { 
        			fps = Integer.parseInt(args[i++]);
        		} else {
        			LOG.severe("-f requires a integer value");
        			System.exit(5);
        		}
        	}

        	if (arg.equals("-vv")) {
        		LOG.info("enable very verbose mode");
        		debugOutput = true;
        		verboseDebugOutput = true;
        	}

        	if (arg.equals("-v")) {
        		LOG.info("enable verbose mode");
        		debugOutput = true;
        	}
        	
        	if (arg.equals("-w")) {
        		LOG.info("enable ola workaround mode");
        		olaWorkaround = true;
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
	}

	public boolean isVerboseDebugOutput() {
		return verboseDebugOutput;
	}

	public IOutput getOutput() {
		return output;
	}

	public int getFps() {
		return fps;
	}

	public Map<Integer, Integer> getDmxToOffsetMap() {
		return dmxToOffsetMap;
	}

	public boolean isDebugOutput() {
		return debugOutput;
	}

	public boolean isOlaWorkaround() {
		return olaWorkaround;
	}
	
	
}
