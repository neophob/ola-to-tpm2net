package com.neophob.ola2uart.ola;

import java.net.ConnectException;
import java.util.logging.Logger;

import ola.OlaClient;

public abstract class OlaHelper {

	private static final Logger LOG = Logger.getLogger(OlaHelper.class.getName());
	
	public static OlaClient connectToOlad() throws Exception {
        LOG.finest("Init OLA Client");
		OlaClient olaClient=null;
		boolean oladConnectionAvailable = false;
		while (!oladConnectionAvailable) {
			try {
				olaClient = new OlaClient();
				oladConnectionAvailable = true;
			} catch (ConnectException e) {
				LOG.info("no olad server available, retry in 1s...");
				Thread.sleep(1000);
			}			
		}
		
		return olaClient;
	}

}
