#OLA to TPM2.net Converter

(c) by Michael Vogt 2013 / http://neophob.com

##Main use case:

Grab DMX data from [OLA](http://www.opendmx.net/index.php/Open_Lighting_Architecture) and send it out via serial port using
the TPM2net protocol.

This enables to use your custom made lightning project together with OLA.


##Setup RPI

Make sure you're using a hard float distribution and you installed a JRE.
Make sure your OLA instance is configured properly, read: the universe ID you want to map needs to exist or the application will not start.

##Setup Teensy 3

Tested on a Teensy 3, *might* work on other hardware too, but untested. Keep an eye on the memory usage!

You need to have the [FastSPI LED2 Library installed](https://code.google.com/p/fastspi/downloads/list)


##Examples

Listen on port 65506, use i2c bus 1 (common for rpi model b rev002) and send data to i2c address nr. 4:

	pi@raspberrypi ~/udp2i2c $ ./run.sh -p 65506 -b 1 -d 5 -t 4 
	UDP-to-I2c Server v0.1 by Michael Vogt / neophob.com
	Bridge a TPM2Net UDP packet to I2C
	Listening on port 65506, using i2c bus 1, i2c target address:
	    4 

TODO

## Install Daemon on RPI (run on boot)

Copy the `init.d/ola-to-tpm2net` file to `/etc/init.d/` (as root), then:

	pi@raspberrypi ~/test $ sudo update-rc.d ola-to-tpm2net defaults
	update-rc.d: using dependency based boot sequencing
	insserv: warning: script 'K01ola-to-tpm2net' missing LSB tags and overrides
	insserv: warning: script 'ola-to-tpm2net' missing LSB tags and overrides

	
Check in which runlevels the daemon is started:

	pi@raspberrypi ~/test $ find /etc/rc?.d | grep ola-to-tpm2net
	/etc/rc0.d/K01ola-to-tpm2net
	/etc/rc1.d/K01ola-to-tpm2net
	/etc/rc2.d/S02ola-to-tpm2net
	/etc/rc3.d/S02ola-to-tpm2net
	/etc/rc4.d/S02ola-to-tpm2net
	/etc/rc5.d/S02ola-to-tpm2net
	/etc/rc6.d/K01ola-to-tpm2net
	
Open the init script `/etc/init.d/ola-to-tpm2net` and make sure the following entries are correct:

	JAVA_HOME="/usr/local/java/"
TODO
	args="-p 65506 -b 0 -d 5 -t 5:7:6:9:8:4"
	application_dir="/home/pi/ola-to-tpm2net/"
	logfile="${application_dir}ola-to-tpm2net.log"

Hint: for RPI r001 (256MB ram) you need to use the I2C bus 0 (-b 0) while on the RPI r002 (512MB ram) you need to use the I2C buf 1 (-b 1).
	
## Performance

TODO 

Test with one I2C output and 130 frames/s running on a RPI rev002. 
UDP2I2C use less than 8% CPU and about 4% of memory. 

Java Version:

	java version "1.8.0-ea"
	Java(TM) SE Runtime Environment (build 1.8.0-ea-b36e)
	Java HotSpot(TM) Client VM (build 25.0-b04, mixed mode)

## Compile yourself

Make sure you have maven and a JDK setup correctly. enter
	# mvn initialize
to install all nessesary libraries. 



