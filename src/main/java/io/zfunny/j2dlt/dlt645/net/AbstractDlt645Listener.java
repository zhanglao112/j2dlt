package io.zfunny.j2dlt.dlt645.net;

import io.zfunny.j2dlt.dlt645.Dlt645;
import io.zfunny.j2dlt.dlt645.Dlt645Exception;
import io.zfunny.j2dlt.dlt645.Dlt645IOException;
import io.zfunny.j2dlt.dlt645.io.AbstractDlt645Transport;
import io.zfunny.j2dlt.dlt645.io.Dlt645RTUTransport;
import io.zfunny.j2dlt.dlt645.msg.Dlt645Request;
import io.zfunny.j2dlt.dlt645.msg.Dlt645Response;
import io.zfunny.j2dlt.dlt645.procimg.ProcessImage;
import io.zfunny.j2dlt.dlt645.slave.Dlt645Slave;
import io.zfunny.j2dlt.dlt645.slave.Dlt645SlaveFactory;
import io.zfunny.j2dlt.dlt645.util.Dlt645Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;

public abstract class AbstractDlt645Listener implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(AbstractDlt645Listener.class);
    protected int port = Dlt645.DEFAULT_PORT;
    protected boolean listening;
    protected InetAddress address;
    protected String error;
    protected int timeout = Dlt645.DEFAULT_TIMEOUT;
    protected String threadName;

    public abstract void stop();

    public void setPort(int port) {
        this.port = ((port > 0) ? port : Dlt645.DEFAULT_PORT);
    }

    public int getPort() {
        return port;
    }

    public void setAddress(InetAddress addr) {
        address = addr;
    }

    public InetAddress getAddress() {
        return address;
    }

    public boolean isListening() {
        return listening;
    }

    public void setListening(boolean b) {
        listening = b;
    }

    public String getError() {
        return error;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * Reads the request, checks it is valid and that the unit ID is ok
     * and sends back a response
     *
     * @param transport Transport to read request from
     * @param listener  Listener that the request was received by
     * @throws Dlt645IOException If there is an issue with the transport or transmission
     */
    void handleRequest(AbstractDlt645Transport transport, AbstractDlt645Listener listener) throws Dlt645IOException {

        // Get the request from the transport. It will be processed
        // using an associated process image

        if (transport == null) {
            throw new Dlt645IOException("No transport specified");
        }
        Dlt645Request request = transport.readRequest(listener);
        if (request == null) {
            throw new Dlt645IOException("Request for transport %s is invalid (null)", transport.getClass().getSimpleName());
        }
        Dlt645Response response;

        // Test if Process image exists for this Unit ID
        long uintIdLong = 0;
        try {
            uintIdLong = Dlt645Util.bytes2long(request.getUnitID());
        }catch (Exception e) {
            throw new Dlt645IOException("Invalid unitID");
        }
        ProcessImage spi = getProcessImage(uintIdLong);
        if (spi == null) {
            response = request.createExceptionResponse(Dlt645.ILLEGAL_ADDRESS_EXCEPTION);
            response.setAuxiliaryType(Dlt645Response.AuxiliaryMessageTypes.UNIT_ID_MISSMATCH);
        }
        else {
            response = request.createResponse(this);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Request:{}", request.getHexMessage());

            if (transport instanceof Dlt645RTUTransport && response.getAuxiliaryType() == Dlt645Response.AuxiliaryMessageTypes.UNIT_ID_MISSMATCH) {
                logger.debug("Not sending response because it was not meant for us.");
            }
            else {
                logger.debug("Response:{}", response.getHexMessage());
            }
        }

        // Write the response
        transport.writeResponse(response);
    }

    /**
     * Returns the related process image for this listener and Unit Id
     *
     * @param unitId Unit ID
     * @return Process image associated with this listener and Unit ID
     */
    public ProcessImage getProcessImage(long unitId) {
        Dlt645Slave slave = Dlt645SlaveFactory.getSlave(this);
        if (slave != null) {
            return slave.getProcessImage(unitId);
        }
        return null;
    }

    /**
     * Gets the name of the thread used by the listener
     *
     * @return Name of thread or null if not assigned
     */
    public String getThreadName() {
        return threadName;
    }

    /**
     * Sets the name of the thread used by the listener
     *
     * @param threadName Name to use for the thread
     */
    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }
}
