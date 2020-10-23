package io.zfunny.j2dlt.dlt645.io;

import io.zfunny.j2dlt.dlt645.Dlt645;
import io.zfunny.j2dlt.dlt645.Dlt645Exception;
import io.zfunny.j2dlt.dlt645.Dlt645IOException;
import io.zfunny.j2dlt.dlt645.Dlt645SlaveException;
import io.zfunny.j2dlt.dlt645.msg.Dlt645Request;
import io.zfunny.j2dlt.dlt645.msg.ExceptionResponse;
import io.zfunny.j2dlt.dlt645.net.AbstractSerialConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Dlt645SerialTransaction extends Dlt645Transaction {

    private static final Logger logger = LoggerFactory.getLogger(Dlt645SerialTransaction.class);

    private int transDelayMS = Dlt645.DEFAULT_TRANSMIT_DELAY;
    private long lastTransactionTimestamp = 0;

    public Dlt645SerialTransaction() {
    }

    public Dlt645SerialTransaction(Dlt645Request request) {
        setRequest(request);
    }

    public Dlt645SerialTransaction(AbstractSerialConnection conn) {
        setSerialConnection(conn);
    }

    public synchronized void setSerialConnection(AbstractSerialConnection con) {
        transport = con.getDlt645Transport();
    }

    public synchronized void setTransport(Dlt645SerialTransport transport) {
        this.transport = transport;
    }

    public int getTransDelayMS() {
        return transDelayMS;
    }

    public void setTransDelayMS(int newTransDelayMS) {
        this.transDelayMS = newTransDelayMS;
    }

    private void assertExecutable() throws Dlt645Exception {
        if (request == null || transport == null) {
            throw new Dlt645Exception("Assertion failed, transaction not executable");
        }
    }

    @Override
    public void execute() throws Dlt645Exception {
        assertExecutable();

        int tries = 0;
        boolean finished = false;
        do {
            try {
                ((Dlt645SerialTransport)transport).waitBetweenFrames(transDelayMS, lastTransactionTimestamp);

                synchronized (this) {
                    transport.writeRequest(request);
                    response = transport.readResponse();
                    finished = true;
                }
            } catch (Dlt645IOException e) {
                if (++tries >= retries) {
                    throw e;
                }
                logger.debug("Execute try {} error: {}", tries, e.getMessage());
            }
        } while (!finished);

        if (response instanceof ExceptionResponse) {
            throw new Dlt645SlaveException(((ExceptionResponse) response).getExceptionCode());
        }

        if (isCheckingValidity()) {
            checkValidity();
        }

        lastTransactionTimestamp = System.nanoTime();
    }
}
