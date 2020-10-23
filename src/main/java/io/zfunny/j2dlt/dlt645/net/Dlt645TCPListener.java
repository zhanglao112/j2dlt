package io.zfunny.j2dlt.dlt645.net;

import io.zfunny.j2dlt.dlt645.util.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;

/**
 * Class that implements a Dlt645TCPListener.
 * <p>
 * If listening, it accepts incoming requests passing them on to be handled.
 * If not listening, silently drops the requests.
 *
 * @author Dieter Wimberger
 * @author Julie Haugh
 * @author Steve O'Hara (4NG)
 * @version 2.0 (March 2016)
 */
public class Dlt645TCPListener extends AbstractDlt645Listener {

    private static final Logger logger = LoggerFactory.getLogger(Dlt645TCPListener.class);

    private ServerSocket serverSocket = null;
    private final ThreadPool threadPool;
    private Thread listener;
    private final boolean useRtuOverTcp;
    private int maxIdleSeconds;

    /**
     * Constructs a Dlt645TCPListener instance.<br>
     *
     * @param poolsize the size of the <tt>ThreadPool</tt> used to handle incoming
     *                 requests.
     * @param addr     the interface to use for listening.
     */
    public Dlt645TCPListener(int poolsize, InetAddress addr) {
        this(poolsize, addr, false);
    }

    /**
     * Constructs a Dlt645TCPListener instance.<br>
     *
     * @param poolsize      the size of the <tt>ThreadPool</tt> used to handle incoming
     *                      requests.
     * @param addr          the interface to use for listening.
     * @param useRtuOverTcp True if the RTU protocol should be used over TCP
     */
    public Dlt645TCPListener(int poolsize, InetAddress addr, boolean useRtuOverTcp) {
        threadPool = new ThreadPool(poolsize);
        address = addr;
        this.useRtuOverTcp = useRtuOverTcp;
        maxIdleSeconds = 0;
    }

    /**
     * /**
     * Constructs a Dlt645TCPListener instance.  This interface is created
     * to listen on the wildcard address (0.0.0.0), which will accept TCP packets
     * on all available adapters/interfaces
     *
     * @param poolsize the size of the <tt>ThreadPool</tt> used to handle incoming
     *                 requests.
     */
    public Dlt645TCPListener(int poolsize) {
        this(poolsize, false);
    }

    /**
     * /**
     * Constructs a Dlt645TCPListener instance.  This interface is created
     * to listen on the wildcard address (0.0.0.0), which will accept TCP packets
     * on all available adapters/interfaces
     *
     * @param poolsize      the size of the <tt>ThreadPool</tt> used to handle incoming
     *                      requests.
     * @param useRtuOverTcp True if the RTU protocol should be used over TCP
     */
    public Dlt645TCPListener(int poolsize, boolean useRtuOverTcp) {
        threadPool = new ThreadPool(poolsize);
        try {
            address = InetAddress.getByAddress(new byte[]{0, 0, 0, 0});
        }
        catch (UnknownHostException ex) {
            // Can't happen -- size is fixed.
        }
        this.useRtuOverTcp = useRtuOverTcp;
        maxIdleSeconds = 0;
    }

    /**
     * Sets a maximum time a connection can be idle, i.e. has no input/output.
     * This is important for detecting "hanging" slave connection.
     *
     * Details: TCP connection is "immortal" in established state (problems will
     * only be detected when any data are being actively sent in any direction).
     * When a slave is performing socket.read() to get the next request it may not
     * (and often won't) receive any notification that a peer host is down (powered
     * off, for example) and hang forever.
     * Such a connection will occupy thread in a thread pool and eventually the
     * pool will be exhausted
     *
     * Settings this option will force closing the connection after
     * <code>maxIdleSeconds</code> of total silence.
     * This option is disabled by default (maxIdleSeconds == 0)
     *
     * @param maxIdleSeconds    0 to disable watchdog, or a positive number to set it.
     */
    public void setMaxIdleSeconds(int maxIdleSeconds) {
        if(maxIdleSeconds < 0) {
            throw new IllegalArgumentException("maxIdleSeconds must be >= 0: " + maxIdleSeconds);
        }
        this.maxIdleSeconds = maxIdleSeconds;
    }

    @Override
    public void setTimeout(int timeout) {
        super.setTimeout(timeout);
        if (serverSocket != null && listening) {
            try {
                serverSocket.setSoTimeout(timeout);
            }
            catch (SocketException e) {
                logger.error("Cannot set socket timeout", e);
            }
        }
    }

    @Override
    public void run() {

        // Set a suitable thread name
        if (threadName == null || threadName.isEmpty()) {
            threadName = String.format("Dlt645 TCP Listener [port:%d]", port);
        }
        Thread.currentThread().setName(threadName);

        try {
            /*
             * A server socket is opened with a connectivity queue of a size
             * specified in int floodProtection. Concurrent login handling under
             * normal circumstances should be alright, denial of service
             * attacks via massive parallel program logins can probably be
             * prevented.
             */
            int floodProtection = 100;
            serverSocket = new ServerSocket(port, floodProtection, address);
            serverSocket.setSoTimeout(timeout);
            logger.debug("Listening to {} (Port {})", serverSocket, port);
        }

        // Catch any fatal errors and set the listening flag to false to indicate an error
        catch (Exception e) {
            error = String.format("Cannot start TCP listener on port %d - %s", port, e.getMessage());
            listening = false;
            return;
        }

        listener = Thread.currentThread();
        listening = true;
        try {

            // Initialise the message handling pool
            threadPool.initPool(threadName);

            // Infinite loop, taking care of resources in case of a lot of
            // parallel logins
            while (listening) {
                Socket incoming;
                try {
                    incoming = serverSocket.accept();
                }
                catch (SocketTimeoutException e) {
                    continue;
                }
                logger.debug("Making new connection {}", incoming);
                if (listening) {
                    TCPSlaveConnection slave = new TCPSlaveConnection(incoming, useRtuOverTcp);
                    slave.setTimeout(timeout);
                    threadPool.execute(new TCPConnectionHandler(this, slave, maxIdleSeconds));
                }
                else {
                    incoming.close();
                }
            }
        }
        catch (IOException e) {
            error = String.format("Problem starting listener - %s", e.getMessage());
        }
        finally {
            threadPool.close();
        }
    }

    @Override
    public void stop() {
        listening = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
            if (listener != null) {
                listener.join();
            }
            if (threadPool != null) {
                threadPool.close();
            }
        }
        catch (Exception ex) {
            logger.error("Error while stopping Dlt645TCPListener", ex);
        }
    }

}
