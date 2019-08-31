/*
 * Copyright 2013 Licel LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bakaoh.smartcard.simulator;

import com.licel.jcardsim.utils.AIDUtil;
import im.status.keycard.KeycardApplet;
import javacard.framework.AID;

import com.licel.jcardsim.base.CardManager;
import com.licel.jcardsim.base.Simulator;

import java.io.IOException;

public class VSmartCard {

    private Simulator sim;
    private VSmartCardTCPProtocol driverProtocol;

    public VSmartCard(String host, int port) throws IOException {
        driverProtocol = new VSmartCardTCPProtocol();
        driverProtocol.connect(host, port);
        sim = new Simulator();

        // Install KeycardApplet
        AID appletAID = AIDUtil.create(new byte[]{(byte) 0xA0, 0x00, 0x00, 0x08, 0x04, 0x00, 0x01, 0x01, 0x01});
        byte[] aid = new byte[]{0x09, (byte) 0xA0, 0x00, 0x00, 0x08, 0x04, 0x00, 0x01, 0x01, 0x01};
        sim.installApplet(appletAID, KeycardApplet.class, aid, (short) 0, (byte) aid.length);
    }

    public void start() throws IOException {
        final IOThread ioThread = new IOThread(sim, driverProtocol);
        ShutDownHook hook = new ShutDownHook(ioThread);
        Runtime.getRuntime().addShutdownHook(hook);
        ioThread.start();
    }

    static class ShutDownHook extends Thread {

        IOThread ioThread;

        public ShutDownHook(IOThread ioThread) {
            this.ioThread = ioThread;
        }

        public void run() {
            ioThread.isRunning = false;
            System.out.println("Shutdown connections");
            ioThread.driverProtocol.disconnect();
        }
    }

    static class IOThread extends Thread {

        VSmartCardTCPProtocol driverProtocol;
        Simulator sim;
        boolean isRunning;

        public IOThread(Simulator sim, VSmartCardTCPProtocol driverProtocol) {
            this.sim = sim;
            this.driverProtocol = driverProtocol;
            isRunning = true;
        }

        private void hexDump(byte[] apdu) {
            for (int i = 0; i < apdu.length; i += 8) {
                System.out.printf("%04X:  ", i);
                for (int j = i; j < i + 4; ++j) {
                    if (j >= apdu.length)
                        break;
                    System.out.printf("%02X ", apdu[j]);
                }
                System.out.printf(" ");
                for (int j = i + 4; j < i + 8; ++j) {
                    if (j >= apdu.length)
                        break;
                    System.out.printf("%02X ", apdu[j]);
                }
                System.out.printf("\n");
            }
        }

        @Override
        public void run() {
            while (isRunning) {
                try {
                    int cmd = driverProtocol.readCommand();
                    switch (cmd) {
                        case VSmartCardTCPProtocol.POWER_ON:
                        case VSmartCardTCPProtocol.RESET:
                            sim.reset();
                            break;
                        case VSmartCardTCPProtocol.GET_ATR:
                            driverProtocol.writeData(sim.getATR());
                            break;
                        case VSmartCardTCPProtocol.APDU:
                            final byte[] apdu = driverProtocol.readData();
                            System.out.println("== APDU");
                            hexDump(apdu);
                            final byte[] reply = CardManager.dispatchApdu(sim, apdu);
                            System.out.println("== Reply APDU");
                            hexDump(reply);
                            driverProtocol.writeData(reply);
                            break;
                    }
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            }
        }
    }

}
