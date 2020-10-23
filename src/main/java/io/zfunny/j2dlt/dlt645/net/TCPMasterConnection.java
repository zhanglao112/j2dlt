package io.zfunny.j2dlt.dlt645.net;

import io.zfunny.j2dlt.dlt645.Dlt645;
import io.zfunny.j2dlt.dlt645.io.AbstractDlt645Transport;
import io.zfunny.j2dlt.dlt645.io.Dlt645RTUTCPTransport;
import io.zfunny.j2dlt.dlt645.io.Dlt645TCPTransport;
import io.zfunny.j2dlt.dlt645.util.Dlt645Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class TCPMasterConnection {

    private static final Logger logger = LoggerFactory.getLogger(TCPMasterConnection.class);

    private Socket socket;
    private int timeout = Dlt645.DEFAULT_TIMEOUT;
    private boolean connected;

    private InetAddress address;
    private int port = Dlt645.DEFAULT_PORT;

    private Dlt645TCPTransport transport;

    private boolean useRtuOverTcp = false;

    /**
     * useUrgentData - sent a byte of urgent data when testing the TCP
     * connection.
     */
    private boolean useUrgentData = false;

    public TCPMasterConnection(InetAddress adr) {
        address = adr;
    }

    private void prepareTransport(boolean useRtuOverTcp) throws IOException {

        // If we don't have a transport, or the transport type has changed
        if (transport == null || (this.useRtuOverTcp != useRtuOverTcp)) {

            // Save the flag to tell us which transport type to use
            this.useRtuOverTcp = useRtuOverTcp;

            // Select the correct transport
            if (useRtuOverTcp) {
                logger.trace("prepareTransport() -> using RTU over TCP transport.");
                transport = new Dlt645RTUTCPTransport(socket);
                transport.setMaster(this);
            } else {
                logger.trace("prepareTransport() -> using standard TCP transport.");
                transport = new Dlt645TCPTransport(socket);
                transport.setMaster(this);
            }
        } else {
            logger.trace("prepareTransport() -> using custom transport: {}", transport.getClass().getSimpleName());
            transport.setSocket(socket);
        }
        transport.setTimeout(timeout);
    }

    public void connect() throws Exception {
        connect(useRtuOverTcp);
    }

    public void connect(boolean useRtuOverTcp) throws Exception {
        if (!isConnected()) {
            logger.debug("connect()");

            socket = new Socket();
            socket.setReuseAddress(true);
            socket.setSoLinger(true, 1);
            socket.setKeepAlive(true);
            setTimeout(timeout);

            socket.connect(new InetSocketAddress(address, port), timeout);

            prepareTransport(useRtuOverTcp);
            connected = true;
        }
    }

    public synchronized boolean isConnected() {
        if (connected && socket != null) {
            if (!socket.isConnected() || socket.isClosed() || socket.isInputShutdown() || socket.isOutputShutdown()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    logger.error("Socket exception", e);
                } finally {
                    connected = false;
                }
            } else {
                if (useUrgentData) {
                    try {
                        socket.sendUrgentData(0);
                        Dlt645Util.sleep(6);
                    } catch (IOException e) {
                        connected = false;
                        try {
                            socket.close();
                        } catch (IOException e1) {

                        }
                    }
                }
            }
        }
        return connected;
    }

    public void close() {
        if (connected) {
            try {
                transport.close();
            } catch (IOException ex) {
                logger.debug("close()", ex);
            } finally {
                connected = false;
            }
        }
    }

    public AbstractDlt645Transport getDlt645Transport() {
        return transport;
    }

    public void setDlt645Transport(Dlt645TCPTransport trans) {
        transport = trans;
    }

    public synchronized int getTimeout() {
        return timeout;
    }

    public synchronized void setTimeout(int timeout) {
        try {
            this.timeout = timeout;
            if (socket != null) {
                socket.setSoTimeout(timeout);
            }
        } catch (IOException ex) {
            logger.warn("Could not set timeout to value {}", timeout, ex);
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

    public void setAddress(InetAddress addr) {
        address = addr;
    }

    public boolean getUseUrgentData() {
        return useUrgentData;
    }

    public void setUseUrgentData(boolean useUrgentData) {
        this.useUrgentData = useUrgentData;
    }

    public boolean isUseRtuOverTcp() {
        return useRtuOverTcp;
    }

    public void setUseRtuOverTcp(boolean useRtuOverTcp) throws Exception {
        this.useRtuOverTcp = useRtuOverTcp;
        if (isConnected()) {
            prepareTransport(useRtuOverTcp);
        }
    }
}
