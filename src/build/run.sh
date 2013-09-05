#!/bin/sh
cd "$(dirname "$0")"
java -cp ./lib/*:./lib/ola-java-client-0.0.1.jar:./lib/RXTXcomm.jar:./lib/protobuf-java-2.4.1.jar -Djava.library.path=./lib com.neophob.ola2uart.Runner $@
