package io.zfunny.j2dlt.dlt645.net;

import io.zfunny.j2dlt.dlt645.Dlt645;
import io.zfunny.j2dlt.dlt645.io.AbstractDlt645Transport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;

public class UDPMasterConnection {

    private static final Logger logger = LoggerFactory.getLogger(UDPMasterConnection.class);

    private UDPMasterTerminal terminal;
    private int timeout = Dlt645.DEFAULT_TIMEOUT;
    private boolean connected;

    private InetAddress address;
    private int port = Dlt645.DEFAULT_PORT;

    public UDPMasterConnection(InetAddress adr) {
        address = adr;
    }

    public void connect() throws Exception {
        if (!connected) {
            terminal = new UDPMasterTerminal(address);
            terminal.setPort(port);
            terminal.setTimeout(timeout);
            terminal.activate();
            connected = true;
        }
    }

    public void close() {
        if (connected) {
            try {
                terminal.deactivate();
            } catch (Exception ex) {
                logger.debug("Exception occurred while closing UDPMasterConnection", ex);
            }
            connected = false;
        }
    }

    public AbstractDlt645Transport getDlt645Transport() {
        return terminal == null ? null : terminal.getTransport();
    }

    public AbstractUDPTerminal getTerminal() {
        return terminal;
    }

    public synchronized int getTimeout() {
        return timeout;
    }

    public synchronized void setTimeout(int timeout) {
        this.timeout = timeout;
        if (terminal != null) {
            terminal.setTimeout(timeout);
        }
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress adr) {
        address = adr;
    }

    public boolean isConnected() {
        return connected;
    }
}
