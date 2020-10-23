package io.zfunny.j2dlt.dlt645.net;

import io.zfunny.j2dlt.dlt645.Dlt645;
import io.zfunny.j2dlt.dlt645.io.AbstractDlt645Transport;
import io.zfunny.j2dlt.dlt645.io.Dlt645RTUTCPTransport;
import io.zfunny.j2dlt.dlt645.io.Dlt645TCPTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Class that implements a TCPSlaveConnection.
 *
 * @author Dieter Wimberger
 * @author Steve O'Hara (4NG)
 * @version 2.0 (March 2016)
 */
public class TCPSlaveConnection {

    private static final Logger logger = LoggerFactory.getLogger(TCPSlaveConnection.class);

    // instance attributes
    private Socket socket;
    private int timeout = Dlt645.DEFAULT_TIMEOUT;
    private boolean connected;
    private Dlt645TCPTransport transport;

    /**
     * Constructs a <tt>TCPSlaveConnection</tt> instance using a given socket
     * instance.
     *
     * @param socket the socket instance to be used for communication.
     */
    public TCPSlaveConnection(Socket socket) {
        this(socket, false);
    }

    /**
     * Constructs a <tt>TCPSlaveConnection</tt> instance using a given socket
     * instance.
     *
     * @param socket        the socket instance to be used for communication.
     * @param useRtuOverTcp True if the RTU protocol should be used over TCP
     */
    public TCPSlaveConnection(Socket socket, boolean useRtuOverTcp) {
        try {
            setSocket(socket, useRtuOverTcp);
        }
        catch (IOException ex) {
            logger.debug("TCPSlaveConnection::Socket invalid");

            throw new IllegalStateException("Socket invalid", ex);
        }
    }

    /**
     * Closes this <tt>TCPSlaveConnection</tt>.
     */
    public void close() {
        if (connected) {
            try {
                transport.close();
                socket.close();
            }
            catch (IOException ex) {
                logger.warn("Could not close socket", ex);
            }
            connected = false;
        }
    }

    /**
     * Returns the <tt>ModbusTransport</tt> associated with this
     * <tt>TCPMasterConnection</tt>.
     *
     * @return the connection's <tt>ModbusTransport</tt>.
     */
    public AbstractDlt645Transport getDlt645Transport() {
        return transport;
    }

    /**
     * @return last activity timestamp of a connection
     * @see ModbusTCPTransport#getLastActivityTimestamp()
     * @see System#nanoTime()
     */
    public long getLastActivityTimestamp() {
        return transport.getLastActivityTimestamp();
    }

    /**
     * Prepares the associated <tt>ModbusTransport</tt> of this
     * <tt>TCPMasterConnection</tt> for use.
     *
     * @param socket        the socket to be used for communication.
     * @param useRtuOverTcp True if the RTU protocol should be used over TCP
     * @throws IOException if an I/O related error occurs.
     */
    private void setSocket(Socket socket, boolean useRtuOverTcp) throws IOException {
        this.socket = socket;

        if (transport == null) {
            if (useRtuOverTcp) {
                logger.trace("setSocket() -> using RTU over TCP transport.");
                transport = new Dlt645RTUTCPTransport(socket);
            }
            else {
                logger.trace("setSocket() -> using standard TCP transport.");
                transport = new Dlt645TCPTransport(socket);
            }
        }
        else {
            transport.setSocket(socket);
        }

        connected = true;
    }

    /**
     * Returns the timeout for this <tt>TCPSlaveConnection</tt>.
     *
     * @return the timeout as <tt>int</tt>.
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * Sets the timeout for this <tt>TCPSlaveConnection</tt>.
     *
     * @param timeout the timeout in milliseconds as <tt>int</tt>.
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;

        try {
            socket.setSoTimeout(timeout);
        }
        catch (IOException ex) {
            logger.warn("Could not set timeout to {}", timeout, ex);
        }
    }

    /**
     * Returns the destination port of this <tt>TCPSlaveConnection</tt>.
     *
     * @return the port number as <tt>int</tt>.
     */
    public int getPort() {
        return socket.getLocalPort();
    }

    /**
     * Returns the destination <tt>InetAddress</tt> of this
     * <tt>TCPSlaveConnection</tt>.
     *
     * @return the destination address as <tt>InetAddress</tt>.
     */
    public InetAddress getAddress() {
        return socket.getLocalAddress();
    }

    /**
     * Tests if this <tt>TCPSlaveConnection</tt> is connected.
     *
     * @return <tt>true</tt> if connected, <tt>false</tt> otherwise.
     */
    public boolean isConnected() {
        return connected;
    }
}
