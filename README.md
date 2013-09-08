#OLA to TPM2.net Converter

(c) by Michael Vogt 2013 / http://pixelinvaders.ch

##Main use case:

Grab DMX data from [OLA](http://www.opendmx.net/index.php/Open_Lighting_Architecture) and send it out via serial port using
the [TPM2Net](http://www.ledstyles.de/ftopic18969.html) protocol.

This enables to use your custom made lightning project together with OLA.


##Setup Teensy 3

Tested on a Teensy 3, *might* work on other hardware too, but untested. Keep an eye on the memory usage!

You need to have the [FastSPI LED2 Library](https://code.google.com/p/fastspi/downloads/list) installed. Grab the [TPM2Net Arduino firmware](https://github.com/neophob/PixelController/tree/develop/integration/ArduinoFw/tpm2serial) from my PixelController repo.


##Examples

Map DMX Universe 10 to offset 0, Universe 11 to offset 1 and Universe 12 to offset 2, send data to /dev/ttyACM0.

	./run.sh -u 10:0 -u 11:1 -u 12:2 -d /dev/ttyACM0 
	
The application grabs all Pixeldata from Universe 10 and sends it out using the TPM2Net protocol. My Arduino firmware will assign the pixeldata to the correct offset. One universe contains data up to 170 RGB pixels - so the Arduino firmware needs to add 170 for offset 1.

Limit to 10 fps (frames per second) and log verbose data.

	./run.sh -u 10:0 -u 11:1 -u 12:2 -d /dev/ttyACM0 -f 10 -v


##Setup RPI

Make sure you're using a hard float distribution and you installed a JRE.
Make sure your OLA instance is configured properly, read: the universe ID you want to map needs to exist or the application will not start.

## Install Daemon on RPI (run on boot)

Copy the `init.d/ola-to-tpm2net` file to `/etc/init.d/` (as root), then:

	pi@raspberrypi ~/test $ sudo update-rc.d ola-to-tpm2net defaults

	
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
	args="-u 10:0 -u 11:1 -u 12:2 -d /dev/ttyACM0 "
	application_dir="/home/pi/ola-to-tpm2net/"
	logfile="${application_dir}ola-to-tpm2net.log"

	
## Performance

See [OLA ticket 251](https://code.google.com/p/open-lighting/issues/detail?id=251).

I tested it on a RPi Model B using 20fps and 256 RGB Pixels. This application used 30-40% of the CPU.

Java Version:

	java version "1.8.0-ea"
	Java(TM) SE Runtime Environment (build 1.8.0-ea-b36e)
	Java HotSpot(TM) Client VM (build 25.0-b04, mixed mode)

## Compile yourself

Make sure you have maven and a JDK setup correctly. Enter

	# mvn initialize

to install all nessesary libraries, then

    # mvn package



