package io.zfunny.j2dlt.dlt645.net;

import io.zfunny.j2dlt.dlt645.io.Dlt645UDPTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPMasterTerminal extends AbstractUDPTerminal {

    private static final Logger logger = LoggerFactory.getLogger(UDPMasterTerminal.class);

    UDPMasterTerminal(InetAddress addr) {
        address = addr;
    }

    public UDPMasterTerminal() {
    }

    @Override
    public synchronized void activate() throws Exception {
        if (!isActive()) {
            if (socket == null) {
                socket = new DatagramSocket();
            }
            logger.debug("UDPMasterTerminal::haveSocket():{}", socket);
            logger.debug("UDPMasterTerminal::raddr=:{}:rport:{}", address, port);

            socket.setReceiveBufferSize(1024);
            socket.setSendBufferSize(1024);
            socket.setSoTimeout(timeout);

            transport = new Dlt645UDPTransport(this);
            active = true;
        }
        logger.debug("UDPMasterTerminal::activated");
    }

    @Override
    public synchronized void deactivate() {
        try {
            logger.debug("UDPMasterTerminal::deactivate()");
            if (socket != null) {
                socket.close();
            }
            transport = null;
            active = false;
        } catch (Exception ex) {
            logger.error("Error closing socket", ex);
        }
    }

    @Override
    public void sendMessage(byte[] msg) throws Exception {
        DatagramPacket req = new DatagramPacket(msg, msg.length, address, port);
        socket.send(req);
    }

    @Override
    public byte[] receiveMessage() throws Exception {

        // The longest possible DatagramPacket is 256 bytes (Modbus message
        // limit) plus the 6 byte header.
        byte[] buffer = new byte[262];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.setSoTimeout(timeout);
        socket.receive(packet);
        return buffer;
    }
}
