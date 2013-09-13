#OLA to TPM2.net Converter

(c) by Michael Vogt 2013 / http://pixelinvaders.ch

##Main use case:

Grab DMX data from [OLA](http://www.opendmx.net/index.php/Open_Lighting_Architecture) and send it out via serial port using
the [TPM2Net](http://www.ledstyles.de/ftopic18969.html) protocol using a microcontroller like a Teensy 3.0.

This enables to use your custom made lightning project together with OLA. This application must run on the same machine as OLA is running.

TL;DR, needed Hardware:

* Raspberry Pi
* Teensy 3

Needed Software:

* Java
* OLA


##Examples

Map DMX Universe 10 to offset 0, Universe 11 to offset 1 and Universe 12 to offset 2, send data to /dev/ttyACM0.

	./run.sh -u 10:0 -u 11:1 -u 12:2 -d /dev/ttyACM0 
	
The application grabs all Pixeldata from Universe 10,11 and 12 and sends it out using the TPM2Net protocol. My Arduino firmware will assign the pixeldata to the correct offset. One universe contains data up to 170 RGB pixels - so the Arduino firmware needs to add 170 for offset 1.

Antother example with *debug* output:
Limit to 10 fps (frames per second) and log verbose data.

	./run.sh -u 10:0 -u 11:1 -u 12:2 -d /dev/ttyACM0 -f 10 -v


## Installation

###Setup RPI

Make sure you're using a hard float distribution and you installed a JRE.
Make sure your OLA instance is configured properly, read: the universe ID you want to map needs to exist or the application will not start.

#### Install Oracle JDK

Go to the [JVM download site](http://jdk8.java.net/download.html) and download the `Linux ARMv6/7 VFP, HardFP ABI` JDK. Example:

```
pi@STREAMER ~ $ sudo bash
<enter pw>
root@STREAMER ~ $ wget --no-check-certificate http://www.java.net/download/jdk8/archive/b106/binaries/jdk-8-ea-b106-linux-arm-vfp-hflt-04_sep_2013.tar.gz
...
root@STREAMER ~ $ gunzip jdk-8-ea-b106-linux-arm-vfp-hflt-04_sep_2013.tar.gz 
root@STREAMER ~ $ tar -xf jdk-8-ea-b106-linux-arm-vfp-hflt-04_sep_2013.tar 
root@STREAMER ~ $ mkdir -p /opt/java
root@STREAMER ~ $ mv ./jdk1.8.0 /opt/java/
root@STREAMER:/home/pi# update-alternatives --install "/usr/bin/java" "java" "/opt/java/jdk1.8.0/bin/java" 1
root@STREAMER:/home/pi# sudo update-alternatives --set java /opt/java/jdk1.8.0/bin/java
root@STREAMER:/home/pi# exit
pi@STREAMER ~ $ java -version
java version "1.8.0-ea"
Java(TM) SE Runtime Environment (build 1.8.0-ea-b106)
Java HotSpot(TM) Client VM (build 25.0-b48, mixed mode) 
```

#### Verify you can access the Microcontroller
Connect the Teensy 3 Board via USB port.

```
pi@STREAMER ~ $ ls /dev/ttyAC*
<--snip-->
crw------- 1 root root      4,  9 Jan  1  1970 /dev/tty9
crw-rw---T 1 root dialout 204, 64 Jan  1  1970 /dev/ttyACM0
```
If the device `/dev/ttyACM0` does not exist, make sure the `cdc_acm` module is loaded. 
Hint: Some OLA distributions blacklist this device, check if you have a module called `/etc/modprobe.d/eurolite-dmx.conf` and remove it.

#### Install Daemon on RPI (run on boot)
Copy the release to your homedirectory of your RPi and unpack it:

```
anotherhost-> scp ola-to-serial-1.0.zip pi@192.168.1.2:/home/pi
pi@192.168.1.2's password: 
ola-to-serial-1.0.zip                                                                                                            100%  911KB 910.6KB/s   00:00    
[4 ~/Downloads]
```
Now switch to your RPi session and unzip the package:

```
pi@STREAMER ~ $ unzip ola-to-serial-1.0.zip 
Archive:  ola-to-serial-1.0.zip
   creating: ola-to-serial-1.0/
   creating: ola-to-serial-1.0/init.d/
  inflating: ola-to-serial-1.0/init.d/ola-to-tpm2net  
   creating: ola-to-serial-1.0/lib/
  inflating: ola-to-serial-1.0/lib/librxtxSerial.jnilib  
  inflating: ola-to-serial-1.0/lib/librxtxSerial.so  
  inflating: ola-to-serial-1.0/lib/ola-java-client-0.0.1.jar  
  inflating: ola-to-serial-1.0/lib/ola-to-serial-1.0.jar  
  inflating: ola-to-serial-1.0/lib/protobuf-java-2.4.1.jar  
  inflating: ola-to-serial-1.0/lib/rxtx-2.2.jar  
  inflating: ola-to-serial-1.0/run.sh
pi@STREAMER ~ $ cd ola-to-serial-1.0/
pi@STREAMER ~/ola-to-serial-1.0 $
```

Copy the init script file and enable it:

```
pi@STREAMER ~/ola-to-serial-1.0 $ sudo /bin/cp /home/pi/ola-to-serial-1.0/init.d/ola-to-tpm2net /etc/init.d/
pi@STREAMER ~/ola-to-serial-1.0 $ sudo update-rc.d ola-to-tpm2net defaults
update-rc.d: using dependency based boot sequencing
update-rc.d: warning: default stop runlevel arguments (0 1 6) do not match ola-to-tpm2net Default-Stop values (none)
```
	
Test if the daemon can started:

```
pi@STREAMER ~/ola-to-serial-1.0 $ sudo /etc/init.d/ola-to-tpm2net start
pi@STREAMER ~/ola-to-serial-1.0 $ ps aux | grep ola-to-serial
root     10756  0.0  0.2   1748   528 pts/2    S    00:04   0:00 /bin/sh /home/pi/ola-to-serial-1.0//run.sh -u 10:0 -u 11:1 -u 12:2 -d /dev/ttyACM0
pi       10774  0.0  0.3   4080   852 pts/2    S+   00:05   0:00 grep --color=auto ola-to-serial

```
Now reboot the RPi and verify the daemon is running after the reboot.

###Setup Teensy 3

Tested on a Teensy 3, *might* work on other hardware too, but untested. Keep an eye on the memory usage!

You need to have the [FastSPI LED2 Library](https://code.google.com/p/fastspi/downloads/list) installed (I used RC2). Grab the TPM2Net Arduino firmware from the `arduino` directory. Start the Arduino IDE and upload the firmware. The firmware supports up to 512 RGB pixels out of the box - if you need more, adjust the `#define NUM_LEDS 512` definition.

	
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



