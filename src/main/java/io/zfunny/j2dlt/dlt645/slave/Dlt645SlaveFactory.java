package io.zfunny.j2dlt.dlt645.slave;

import io.zfunny.j2dlt.dlt645.Dlt645Exception;
import io.zfunny.j2dlt.dlt645.net.AbstractDlt645Listener;
import io.zfunny.j2dlt.dlt645.util.Dlt645Util;
import io.zfunny.j2dlt.dlt645.util.SerialParameters;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This is a factory class that allows users to easily create and manage slaves.<br>
 * Each slave is uniquely identified by the port it is listening on, irrespective of if
 * the socket type (TCP, UDP or Serial)
 *
 * @author Steve O'Hara (4NG)
 * @version 2.0 (March 2016)
 */
public class Dlt645SlaveFactory {

    private static final Map<String, Dlt645Slave> slaves = new HashMap<String, Dlt645Slave>();

    /**
     * Prevent instantiation
     */
    private Dlt645SlaveFactory() {}

    /**
     * Creates a TCP Dlt645 slave or returns the one already allocated to this port
     *
     * @param port     Port to listen on
     * @param poolSize Pool size of listener threads
     * @return new or existing TCP Dlt645 slave associated with the port
     * @throws Dlt645Exception If a problem occurs e.g. port already in use
     */
    public static synchronized Dlt645Slave createTCPSlave(int port, int poolSize) throws Dlt645Exception {
        return createTCPSlave(port, poolSize, false);
    }

    /**
     * Creates a TCP Dlt645 slave or returns the one already allocated to this port
     *
     * @param port          Port to listen on
     * @param poolSize      Pool size of listener threads
     * @param useRtuOverTcp True if the RTU protocol should be used over TCP
     * @return new or existing TCP Dlt645 slave associated with the port
     * @throws Dlt645Exception If a problem occurs e.g. port already in use
     */
    public static synchronized Dlt645Slave createTCPSlave(int port, int poolSize, boolean useRtuOverTcp) throws Dlt645Exception {
        return createTCPSlave(null, port, poolSize, useRtuOverTcp);
    }

    /**
     * Creates a TCP Dlt645 slave or returns the one already allocated to this port
     *
     * @param address       IP address to listen on
     * @param port          Port to listen on
     * @param poolSize      Pool size of listener threads
     * @param useRtuOverTcp True if the RTU protocol should be used over TCP
     * @return new or existing TCP Dlt645 slave associated with the port
     * @throws Dlt645Exception If a problem occurs e.g. port already in use
     */
    public static synchronized Dlt645Slave createTCPSlave(InetAddress address, int port, int poolSize, boolean useRtuOverTcp) throws Dlt645Exception {
        return createTCPSlave(address, port, poolSize, useRtuOverTcp, 0);
    }

    /**
     * Creates a TCP Dlt645 slave or returns the one already allocated to this port
     *
     * @param address        IP address to listen on
     * @param port           Port to listen on
     * @param poolSize       Pool size of listener threads
     * @param useRtuOverTcp  True if the RTU protocol should be used over TCP
     * @param maxIdleSeconds Maximum idle seconds for TCP connection
     * @return new or existing TCP Dlt645 slave associated with the port
     * @throws Dlt645Exception If a problem occurs e.g. port already in use
     */
    public static synchronized Dlt645Slave createTCPSlave(InetAddress address, int port, int poolSize, boolean useRtuOverTcp, int maxIdleSeconds) throws Dlt645Exception {
        String key = Dlt645SlaveType.TCP.getKey(port);
        if (slaves.containsKey(key)) {
            return slaves.get(key);
        }
        else {
            Dlt645Slave slave = new Dlt645Slave(address, port, poolSize, useRtuOverTcp, maxIdleSeconds);
            slaves.put(key, slave);
            return slave;
        }
    }

    /**
     * Creates a UDP Dlt645 slave or returns the one already allocated to this port
     *
     * @param port Port to listen on
     * @return new or existing UDP Dlt645 slave associated with the port
     * @throws Dlt645Exception If a problem occurs e.g. port already in use
     */
    public static synchronized Dlt645Slave createUDPSlave(int port) throws Dlt645Exception {
        return createUDPSlave(null, port);
    }

    /**
     * Creates a UDP Dlt645 slave or returns the one already allocated to this port
     *
     * @param address IP address to listen on
     * @param port    Port to listen on
     * @return new or existing UDP Dlt645 slave associated with the port
     * @throws Dlt645Exception If a problem occurs e.g. port already in use
     */
    public static synchronized Dlt645Slave createUDPSlave(InetAddress address, int port) throws Dlt645Exception {
        String key = Dlt645SlaveType.UDP.getKey(port);
        if (slaves.containsKey(key)) {
            return slaves.get(key);
        }
        else {
            Dlt645Slave slave = new Dlt645Slave(address, port, false);
            slaves.put(key, slave);
            return slave;
        }
    }

    /**
     * Creates a serial Dlt645 slave or returns the one already allocated to this port
     *
     * @param serialParams Serial parameters for serial type slaves
     * @return new or existing Serial Dlt645 slave associated with the port
     * @throws Dlt645Exception If a problem occurs e.g. port already in use
     */
    public static synchronized Dlt645Slave createSerialSlave(SerialParameters serialParams) throws Dlt645Exception {
        Dlt645Slave slave = null;
        if (serialParams == null) {
            throw new Dlt645Exception("Serial parameters are null");
        }
        else if (Dlt645Util.isBlank(serialParams.getPortName())) {
            throw new Dlt645Exception("Serial port name is empty");
        }

        // If we have a slave already assigned to this port
        if (slaves.containsKey(serialParams.getPortName())) {
            slave = slaves.get(serialParams.getPortName());

            // Check if any of the parameters have changed
            if (!serialParams.toString().equals(slave.getSerialParams().toString())) {
                close(slave);
                slave = null;
            }
        }

        // If we don;t have a slave, create one
        if (slave == null) {
            slave = new Dlt645Slave(serialParams);
            slaves.put(serialParams.getPortName(), slave);
            return slave;
        }
        return slave;
    }

    /**
     * Closes this slave and removes it from the running list
     *
     * @param slave Slave to remove
     */
    public static void close(Dlt645Slave slave) {
        if (slave != null) {
            slave.closeListener();
            slaves.remove(slave.getType().getKey(slave.getPort()));
        }
    }

    /**
     * Closes all slaves and removes them from the running list
     */
    public static void close() {
        for (Dlt645Slave slave : new ArrayList<Dlt645Slave>(slaves.values())) {
            slave.close();
        }
    }

    /**
     * Returns the running slave listening on the given IP port
     *
     * @param port Port to check for running slave
     * @return Null or Dlt645Slave
     */
    public static Dlt645Slave getSlave(int port) {
        return slaves.get(port + "");
    }

    /**
     * Returns the running slave listening on the given serial port
     *
     * @param port Port to check for running slave
     * @return Null or Dlt645Slave
     */
    public static Dlt645Slave getSlave(String port) {
        return Dlt645Util.isBlank(port) ? null : slaves.get(port);
    }

    /**
     * Returns the running slave that utilises the give listener
     *
     * @param listener Listener used for this slave
     * @return Null or Dlt645Slave
     */
    public static synchronized Dlt645Slave getSlave(AbstractDlt645Listener listener) {
        for (Dlt645Slave slave : slaves.values()) {
            if (slave.getListener().equals(listener)) {
                return slave;
            }
        }
        return null;
    }

}
