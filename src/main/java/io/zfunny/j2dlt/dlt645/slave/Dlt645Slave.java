package io.zfunny.j2dlt.dlt645.slave;

import io.zfunny.j2dlt.dlt645.Dlt645Exception;
import io.zfunny.j2dlt.dlt645.net.AbstractDlt645Listener;
import io.zfunny.j2dlt.dlt645.net.Dlt645SerialListener;
import io.zfunny.j2dlt.dlt645.net.Dlt645TCPListener;
import io.zfunny.j2dlt.dlt645.net.Dlt645UDPListener;
import io.zfunny.j2dlt.dlt645.procimg.ProcessImage;
import io.zfunny.j2dlt.dlt645.util.Dlt645Util;
import io.zfunny.j2dlt.dlt645.util.SerialParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class Dlt645Slave {
    private static final Logger logger = LoggerFactory.getLogger(Dlt645Slave.class);

    private final Dlt645SlaveType type;
    private final int port;
    private final SerialParameters serialParams;
    private final AbstractDlt645Listener listener;
    private boolean isRunning;
    private Thread listenerThread;

    private final Map<Long, ProcessImage> processImages = new HashMap<Long, ProcessImage>();

    /**
     * Creates a TCP Dlt645 slave
     *
     * @param port          Port to listen on if IP type
     * @param poolSize      Pool size for TCP slaves
     * @param useRtuOverTcp True if the RTU protocol should be used over TCP
     * @throws Dlt645Exception If a problem occurs e.g. port already in use
     */
    protected Dlt645Slave(int port, int poolSize, boolean useRtuOverTcp) throws Dlt645Exception {
        this(Dlt645SlaveType.TCP, null, port, poolSize, null, useRtuOverTcp, 0);
    }

    /**
     * Creates a TCP Dlt645 slave
     *
     * @param address       IP address to listen on
     * @param port          Port to listen on if IP type
     * @param poolSize      Pool size for TCP slaves
     * @param useRtuOverTcp True if the RTU protocol should be used over TCP
     * @throws Dlt645Exception If a problem occurs e.g. port already in use
     */
    protected Dlt645Slave(InetAddress address, int port, int poolSize, boolean useRtuOverTcp, int maxIdleSeconds) throws Dlt645Exception {
        this(Dlt645SlaveType.TCP, address, port, poolSize, null, useRtuOverTcp, maxIdleSeconds);
    }

    /**
     * Creates a UDP Dlt645 slave
     *
     * @param port          Port to listen on if IP type
     * @param useRtuOverTcp True if the RTU protocol should be used over TCP
     * @throws Dlt645Exception If a problem occurs e.g. port already in use
     */
    protected Dlt645Slave(int port, boolean useRtuOverTcp) throws Dlt645Exception {
        this(Dlt645SlaveType.UDP, null, port, 0, null, useRtuOverTcp, 0);
    }

    /**
     * Creates a UDP Dlt645 slave
     *
     * @param address       IP address to listen on
     * @param port          Port to listen on if IP type
     * @param useRtuOverTcp True if the RTU protocol should be used over TCP
     * @throws Dlt645Exception If a problem occurs e.g. port already in use
     */
    protected Dlt645Slave(InetAddress address, int port, boolean useRtuOverTcp) throws Dlt645Exception {
        this(Dlt645SlaveType.UDP, address, port, 0, null, useRtuOverTcp, 0);
    }

    /**
     * Creates a serial Dlt645 slave
     *
     * @param serialParams Serial parameters for serial type slaves
     * @throws Dlt645Exception If a problem occurs e.g. port already in use
     */
    protected Dlt645Slave(SerialParameters serialParams) throws Dlt645Exception {
        this(Dlt645SlaveType.SERIAL, null, 0, 0, serialParams, false, 0);
    }

    /**
     * Creates an appropriate type of listener
     *
     * @param type           Type of slave to create
     * @param address        IP address to listen on
     * @param port           Port to listen on if IP type
     * @param poolSize       Pool size for TCP slaves
     * @param serialParams   Serial parameters for serial type slaves
     * @param useRtuOverTcp  True if the RTU protocol should be used over TCP
     * @param maxIdleSeconds Maximum idle seconds for TCP connection
     */
    private Dlt645Slave(Dlt645SlaveType type, InetAddress address, int port, int poolSize, SerialParameters serialParams, boolean useRtuOverTcp, int maxIdleSeconds) {
        this.type = type == null ? Dlt645SlaveType.TCP : type;
        this.port = port;
        this.serialParams = serialParams;

        // Create the listener

        logger.debug("Creating {} listener", this.type);
        if (this.type.is(Dlt645SlaveType.UDP)) {
            listener = new Dlt645UDPListener();
        }
        else if (this.type.is(Dlt645SlaveType.TCP)) {
            Dlt645TCPListener tcpListener = new Dlt645TCPListener(poolSize, useRtuOverTcp);
            tcpListener.setMaxIdleSeconds(maxIdleSeconds);
            listener = tcpListener;
        }
        else {
            listener = new Dlt645SerialListener(serialParams);
        }

        listener.setAddress(address);
        listener.setPort(port);
        listener.setTimeout(0);
    }

    /**
     * Returns the type of this slave
     *
     * @return Type of slave
     */
    public Dlt645SlaveType getType() {
        return type;
    }

    /**
     * Returns the port that this IP slave is listening on
     *
     * @return Port being listened on if TCP or UDP
     */
    public int getPort() {
        return port;
    }

    /**
     * Returns the process image for the given Unit ID
     *
     * @param unitId Unit ID of the associated image
     * @return Process image
     */
    public ProcessImage getProcessImage(long unitId) {
        return processImages.get(unitId);
    }

    /**
     * Removes the process image for the given Unit ID
     *
     * @param unitId Unit ID of the associated image
     * @return Process image
     */
    public ProcessImage removeProcessImage(int unitId) {
        return processImages.remove(unitId);
    }

    /**
     * Adds a process image for the given Unit ID
     *
     * @param unitId       Unit ID to associate with this image
     * @param processImage Process image to add
     * @return Process image
     */
    public ProcessImage addProcessImage(long unitId, ProcessImage processImage) {
        return processImages.put(unitId, processImage);
    }

    /**
     * Returns the serial parameters of this slave if it is a Serial type
     *
     * @return Serial parameters
     */
    public SerialParameters getSerialParams() {
        return serialParams;
    }

    /**
     * Opens the listener to service requests
     *
     * @throws Dlt645Exception If we cannot listen
     */
    public void open() throws Dlt645Exception {

        // Start the listener if it isn' already running
        if (!isRunning) {
            try {
                listenerThread = new Thread(listener);
                listenerThread.start();

                // Need to check that there isn't an issue with the port or some other reason why we can't
                // actually start listening
                while (!listener.isListening() && listener.getError() == null) {
                    Dlt645Util.sleep(50);
                }
                if (!listener.isListening()) {
                    throw new Dlt645Exception(listener.getError());
                }
                isRunning = true;
            }
            catch (Exception x) {
                closeListener();
                throw new Dlt645Exception(x.getMessage());
            }
        }
    }

    /**
     * Convenience method for closing this port and removing it from the running list - simply
     * calls Dlt645SlaveFactory.close(this)
     */
    public void close() {
        Dlt645SlaveFactory.close(this);
    }

    /**
     * Returns the last error accrued by the listener
     *
     * @return Error if there is one
     */
    public String getError() {
        return listener != null ? listener.getError() : null;
    }

    /**
     * Returns the listener used for this port
     *
     * @return Listener
     */
    protected AbstractDlt645Listener getListener() {
        return listener;
    }

    /**
     * Closes the listener of this slave
     */
    @SuppressWarnings("deprecation")
    void closeListener() {
        if (listener != null && listener.isListening()) {
            listener.stop();

            // Wait until the listener says it has stopped, but don't wait forever
            int count = 0;
            while (listenerThread != null && listenerThread.isAlive() && count < 50) {
                Dlt645Util.sleep(100);
                count++;
            }
            // If the listener is still not stopped, kill the thread
            if (listenerThread != null && listenerThread.isAlive()) {
                listenerThread.stop();
            }
            listenerThread = null;
        }
        isRunning = false;
    }

    /**
     * Gets the name of the thread used by the listener
     *
     * @return Name of thread or null if not assigned
     */
    public String getThreadName() {
        return listener == null ? null : listener.getThreadName();
    }

    /**
     * Sets the name of the thread used by the listener
     *
     * @param threadName Name to use for the thread
     */
    public void setThreadName(String threadName) {
        if (listener != null) {
            listener.setThreadName(threadName);
        }
    }
}
