package io.zfunny.j2dlt.dlt645.net;

import io.zfunny.j2dlt.dlt645.Dlt645IOException;
import io.zfunny.j2dlt.dlt645.io.AbstractDlt645Transport;
import io.zfunny.j2dlt.dlt645.io.Dlt645SerialTransport;
import io.zfunny.j2dlt.dlt645.util.SerialParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that implements a ModbusSerialListener.<br>
 * If listening, it accepts incoming requests passing them on to be handled.
 *
 * @author Dieter Wimberger
 * @author Julie Haugh Code cleanup in prep to refactor with ModbusListener
 *         interface
 * @author Steve O'Hara (4NG)
 * @version 2.0 (March 2016)
 */
public class Dlt645SerialListener extends AbstractDlt645Listener {

    private static final Logger logger = LoggerFactory.getLogger(Dlt645SerialListener.class);
    private final AbstractSerialConnection serialCon;

    /**
     * Constructs a new <tt>ModbusSerialListener</tt> instance.
     *
     * @param params a <tt>SerialParameters</tt> instance.
     */
    public Dlt645SerialListener(SerialParameters params) {
        serialCon = new SerialConnection(params);
    }

    /**
     * Constructs a new <tt>ModbusSerialListener</tt> instance specifying the serial connection interface
     *
     * @param serialCon Serial connection to use
     */
    public Dlt645SerialListener(AbstractSerialConnection serialCon) {
        this.serialCon = serialCon;
    }

    @Override
    public void setTimeout(int timeout) {
        super.setTimeout(timeout);
        if (serialCon != null && listening) {
            Dlt645SerialTransport transport = (Dlt645SerialTransport)serialCon.getDlt645Transport();
            if (transport != null) {
                transport.setTimeout(timeout);
            }
        }
    }

    @Override
    public void run() {

        // Set a suitable thread name
        if (threadName == null || threadName.isEmpty()) {
            threadName = String.format("Modbus Serial Listener [port:%s]", serialCon.getDescriptivePortName());
        }
        Thread.currentThread().setName(threadName);

        try {
            serialCon.open();
        }
        // Catch any fatal errors and set the listening flag to false to indicate an error
        catch (Exception e) {
            error = String.format("Cannot start Serial listener on port %s - %s", serialCon.getPortName(), e.getMessage());
            listening = false;
            return;
        }

        listening = true;
        try {
            AbstractDlt645Transport transport = serialCon.getDlt645Transport();
            while (listening) {
                safeHandleRequest(transport);
            }
        }
        catch (Exception e) {
            logger.error("Exception occurred while handling request.", e);
        }
        finally {
            listening = false;
            serialCon.close();
        }
    }

    /**
     * Handles the request and swallows any exceptions
     *
     * @param transport Transport to use
     */
    private void safeHandleRequest(AbstractDlt645Transport transport) {
        try {
            handleRequest(transport, this);
        }
        catch (Dlt645IOException ex) {
            logger.debug(ex.getMessage());
        }
    }

    @Override
    public void stop() {
        if (serialCon != null) {
            serialCon.close();
        }
        listening = false;
    }

}
