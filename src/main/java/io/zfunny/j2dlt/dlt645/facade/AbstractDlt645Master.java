package io.zfunny.j2dlt.dlt645.facade;

import io.zfunny.j2dlt.dlt645.Dlt645;
import io.zfunny.j2dlt.dlt645.Dlt645Exception;
import io.zfunny.j2dlt.dlt645.io.AbstractDlt645Transport;
import io.zfunny.j2dlt.dlt645.io.Dlt645Transaction;
import io.zfunny.j2dlt.dlt645.msg.Dlt645Response;

/**
 * Modbus/TCP Master facade - common methods for all the facade implementations
 * The emphasis is in making callas to Modbus devices as simple as possible
 * for the most common Function Codes.
 * This class makes sure that no NPE is raised and that the methods are thread-safe.
 *
 * @author Steve O'Hara (4NG)
 * @version 2.0 (March 2016)
 */
public abstract class AbstractDlt645Master {

    private static final int DEFAULT_UNIT_ID = 1;

    protected Dlt645Transaction transaction;
    protected int timeout = Dlt645.DEFAULT_TIMEOUT;

    /**
     * Sets the transaction to use
     *
     * @param transaction Transaction to use
     */
    protected synchronized void setTransaction(Dlt645Transaction transaction) {
        this.transaction = transaction;
    }

    /**
     * Connects this <tt>ModbusTCPMaster</tt> with the slave.
     *
     * @throws Exception if the connection cannot be established.
     */
    public abstract void connect() throws Exception;

    /**
     * Disconnects this <tt>ModbusTCPMaster</tt> from the slave.
     */
    public abstract void disconnect();

    /**
     * Reads the response from the transaction
     * If there is no response, then it throws an error
     *
     * @return Modbus response
     *
     * @throws ModbusException If response is null
     */
    private Dlt645Response getAndCheckResponse() throws Dlt645Exception {
        Dlt645Response res = transaction.getResponse();
        if (res == null) {
            throw new Dlt645Exception("No response");
        }
        return res;
    }

    /**
     * Checks to make sure there is a transaction to use
     *
     * @throws ModbusException If transaction is null
     */
    private void checkTransaction() throws Dlt645Exception {
        if (transaction == null) {
            throw new Dlt645Exception("No transaction created, probably not connected");
        }
    }

    /**
     * Returns the receive timeout in milliseconds
     *
     * @return Timeout in milliseconds
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * Sets the receive timeout
     *
     * @param timeout Timeout in milliseconds
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * Set the amount of retries for opening
     * the connection for executing the transaction.
     *
     * @param retries the amount of retries as <tt>int</tt>.
     */
    public synchronized void setRetries(int retries) {
        if (transaction != null) {
            transaction.setRetries(retries);
        }
    }

    /**
     * Sets the flag that controls whether the
     * validity of a transaction will be checked.
     *
     * @param b true if checking validity, false otherwise.
     */
    public synchronized void setCheckingValidity(boolean b) {
        if (transaction != null) {
            transaction.setCheckingValidity(b);
        }
    }

    /**
     * Returns the transport being used by the
     *
     * @return ModbusTransport
     */
    public abstract AbstractDlt645Transport getTransport();

    /**
     * Returns true if the master is connected
     *
     * @return True if connected
     */
    public abstract boolean isConnected();

}
