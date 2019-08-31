# Keycard Simulator

A simulator that make testing and debugging [status-keycard](https://github.com/status-im/status-keycard) easier. Without the need of real card or card reader, it lowers the barrier to entry for newcomers.

[Why do i need a keycard simulator?](https://medium.com/@batatsar/a-status-keycard-simulator-543f2989bb41)

[![asciicast](https://asciinema.org/a/QedYAAWwsGrlZi68yWmfMCyUa.svg)](https://asciinema.org/a/QedYAAWwsGrlZi68yWmfMCyUa?speed=2&autoplay=1)

## Usage

Install [Virtual Smart Card](http://frankmorgner.github.io/vsmartcard/virtualsmartcard/README.html)

Clone this repo and fetch submodules

```
$ git clone https://github.com/bakaoh/keycard-simulator.git
$ cd keycard-simulator
$ git submodule init
$ git submodule update
```

Run Keycard Simulator

```
$ ./gradlew run
```

Try it with [keycard-cli](https://github.com/status-im/keycard-cli)
```
$ keycard info -l debug
```

## Install Virtual Smart Card on Ubuntu

Install linux dependency

```
$ sudo apt-get install pcscd libpcsclite-dev
```

Download and build the virtual smart card and its reader driver

```
$ git clone https://github.com/frankmorgner/vsmartcard.git
$ cd vsmartcard/virtualsmartcard
$ autoreconf --verbose --install && ./configure && sudo make install
```

Restart pcscd to load the new reader driver
```
$ sudo /etc/init.d/pcscd restart
```

Check if the vpcd port is listening
```
$ sudo netstat -ntpla | grep 35963
```