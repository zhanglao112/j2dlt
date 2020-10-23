package io.zfunny.j2dlt.dlt645.facade;

import io.zfunny.j2dlt.dlt645.Dlt645;
import io.zfunny.j2dlt.dlt645.io.AbstractDlt645Transport;
import io.zfunny.j2dlt.dlt645.io.Dlt645SerialTransaction;
import io.zfunny.j2dlt.dlt645.net.AbstractSerialConnection;
import io.zfunny.j2dlt.dlt645.net.SerialConnection;
import io.zfunny.j2dlt.dlt645.util.SerialParameters;

/**
 * Modbus/Serial Master facade.
 *
 * @author Dieter Wimberger
 * @author John Charlton
 * @author Steve O'Hara (4NG)
 * @version 2.0 (March 2016)
 */
public class Dlt645SerialMaster extends AbstractDlt645Master {

    private final AbstractSerialConnection connection;
    private final int transDelay;

    /**
     * Constructs a new master facade instance for communication
     * with a given slave.
     *
     * @param param SerialParameters specifies the serial port parameters to use
     *              to communicate with the slave device network.
     */
    public Dlt645SerialMaster(SerialParameters param) {
        this(param, Dlt645.DEFAULT_TIMEOUT, Dlt645.DEFAULT_TRANSMIT_DELAY);
    }

    /**
     * Constructs a new master facade instance for communication
     * with a given slave.
     *
     * @param param   SerialParameters specifies the serial port parameters to use
     *                to communicate with the slave device network.
     * @param timeout Receive timeout in milliseconds
     */
    public Dlt645SerialMaster(SerialParameters param, int timeout) {
        this(param, timeout, Dlt645.DEFAULT_TRANSMIT_DELAY);
    }

    /**
     * Constructs a new master facade instance for communication
     * with a given slave.
     *
     * @param param      SerialParameters specifies the serial port parameters to use
     *                   to communicate with the slave device network.
     * @param timeout    Receive timeout in milliseconds
     * @param transDelay The transmission delay to use between frames (milliseconds)
     */
    public Dlt645SerialMaster(SerialParameters param, int timeout, int transDelay) {
        try {
            this.transDelay = transDelay > -1 ? transDelay : Dlt645.DEFAULT_TRANSMIT_DELAY;
            connection = new SerialConnection(param);
            connection.setTimeout(timeout);
            this.timeout = timeout;
        }
        catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public AbstractSerialConnection getConnection() {
        return connection;
    }

    /**
     * Connects this <tt>ModbusSerialMaster</tt> with the slave.
     *
     * @throws Exception if the connection cannot be established.
     */
    @Override
    public synchronized void connect() throws Exception {
        if (connection != null && !connection.isOpen()) {
            connection.open();
            transaction = connection.getDlt645Transport().createTransaction();
            ((Dlt645SerialTransaction) transaction).setTransDelayMS(transDelay);
            setTransaction(transaction);
        }
    }

    /**
     * Disconnects this <tt>ModbusSerialMaster</tt> from the slave.
     */
    @Override
    public synchronized void disconnect() {
        if (connection != null && connection.isOpen()) {
            connection.close();
            transaction = null;
            setTransaction(null);
        }
    }

    @Override
    public synchronized void setTimeout(int timeout) {
        super.setTimeout(timeout);
        if (connection != null) {
            connection.setTimeout(timeout);
        }
    }

    @Override
    public AbstractDlt645Transport getTransport() {
        return connection == null ? null : connection.getDlt645Transport();
    }

    @Override
    public boolean isConnected() {
        return connection != null && connection.isOpen();
    }

}
