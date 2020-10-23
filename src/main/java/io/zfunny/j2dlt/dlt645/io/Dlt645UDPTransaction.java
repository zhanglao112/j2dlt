package io.zfunny.j2dlt.dlt645.io;

import io.zfunny.j2dlt.dlt645.Dlt645;
import io.zfunny.j2dlt.dlt645.Dlt645Exception;
import io.zfunny.j2dlt.dlt645.Dlt645IOException;
import io.zfunny.j2dlt.dlt645.Dlt645SlaveException;
import io.zfunny.j2dlt.dlt645.msg.Dlt645Request;
import io.zfunny.j2dlt.dlt645.msg.ExceptionResponse;
import io.zfunny.j2dlt.dlt645.net.AbstractUDPTerminal;
import io.zfunny.j2dlt.dlt645.net.UDPMasterConnection;
import io.zfunny.j2dlt.dlt645.util.Dlt645Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Dlt645UDPTransaction extends Dlt645Transaction {

    private static final Logger logger = LoggerFactory.getLogger(Dlt645UDPTransaction.class);

    private AbstractUDPTerminal terminal;

    public Dlt645UDPTransaction() {
    }

    public Dlt645UDPTransaction(Dlt645Request request) {
        setRequest(request);
    }

    public Dlt645UDPTransaction(AbstractUDPTerminal terminal) {
        setTerminal(terminal);
    }

    public Dlt645UDPTransaction(UDPMasterConnection con) {
        setTerminal(con.getTerminal());
    }

    public void setTerminal(AbstractUDPTerminal terminal) {
        this.terminal = terminal;
        if (terminal.isActive()) {
            transport = terminal.getTransport();
        }
    }

    @Override
    public void execute() throws Dlt645IOException, Dlt645SlaveException, Dlt645Exception {
        assertExecutable();

        if (!terminal.isActive()) {
            try {
                terminal.activate();
                transport = terminal.getTransport();
            } catch (Exception ex) {
                logger.debug("Terminal activation failed.", ex);
                throw new Dlt645IOException("Activation failed");
            }
        }

        int retryCount = 0;
        while (retryCount <= retries) {
            try {
                synchronized (this) {
                    transport.writeRequest(request);
                    response = transport.readResponse();
                    break;
                }
            } catch (Dlt645IOException ex) {
                retryCount++;
                if (retryCount > retries) {
                    logger.error("Cannot send UDP message", ex);
                } else {
                    Dlt645Util.sleep(getRandomSleepTime(retryCount));
                }
            }
        }

        if (response instanceof ExceptionResponse) {
            throw new Dlt645SlaveException(((ExceptionResponse)response).getExceptionCode());
        }

        if (isCheckingValidity()) {
            checkValidity();
        }

        incrementTransactionID();
    }

    private void assertExecutable() throws Dlt645Exception {
        if (request == null || terminal == null) {
            throw new Dlt645Exception("Assertion failed, transaction not executable");
        }
    }

    private void incrementTransactionID() {
        if (isCheckingValidity()) {
            if (transactionID >= Dlt645.MAX_TRANSACTION_ID) {
                transactionID = Dlt645.DEFAULT_TRANSACTION_ID;
            } else {
                transactionID++;
            }
        }
        request.setTransactionID(getTransactionID());
    }
}
