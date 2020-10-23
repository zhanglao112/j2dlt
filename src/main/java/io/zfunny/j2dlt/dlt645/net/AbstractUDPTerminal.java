package io.zfunny.j2dlt.dlt645.net;

import io.zfunny.j2dlt.dlt645.Dlt645;
import io.zfunny.j2dlt.dlt645.io.Dlt645UDPTransport;

import java.net.DatagramSocket;
import java.net.InetAddress;

public abstract class AbstractUDPTerminal {

    protected InetAddress address;
    protected Dlt645UDPTransport transport;
    protected boolean active;
    protected int port = Dlt645.DEFAULT_PORT;
    protected int timeout = Dlt645.DEFAULT_TIMEOUT;
    protected DatagramSocket socket;

    public InetAddress getAddress() {
        return address;
    }

    public synchronized int getPort() {
        return port;
    }

    protected synchronized void setPort(int port) {
        this.port = port;
    }

    public boolean isActive() {
        return active;
    }

    public synchronized void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public Dlt645UDPTransport getTransport() {
        return transport;
    }

    public abstract void activate() throws Exception;

    public abstract void deactivate();

    public abstract void sendMessage(byte[] msg) throws Exception;

    public abstract byte[] receiveMessage() throws Exception;
}
