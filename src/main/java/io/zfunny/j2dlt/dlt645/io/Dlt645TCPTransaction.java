package io.zfunny.j2dlt.dlt645.io;

import io.zfunny.j2dlt.dlt645.Dlt645;
import io.zfunny.j2dlt.dlt645.Dlt645Exception;
import io.zfunny.j2dlt.dlt645.Dlt645IOException;
import io.zfunny.j2dlt.dlt645.Dlt645SlaveException;
import io.zfunny.j2dlt.dlt645.msg.Dlt645Request;
import io.zfunny.j2dlt.dlt645.msg.ExceptionResponse;
import io.zfunny.j2dlt.dlt645.net.TCPMasterConnection;
import io.zfunny.j2dlt.dlt645.util.Dlt645Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Dlt645TCPTransaction extends Dlt645Transaction {

    private static final Logger logger = LoggerFactory.getLogger(Dlt645TCPTransaction.class);

    private TCPMasterConnection connection;
    protected boolean reconnecting = Dlt645.DEFAULT_RECONNECTING;

    public Dlt645TCPTransaction() {
    }

    public Dlt645TCPTransaction(Dlt645Request request) {
        setRequest(request);
    }

    public Dlt645TCPTransaction(TCPMasterConnection con) {
        setConnection(con);
        transport = con.getDlt645Transport();
    }

    public void setConnection(TCPMasterConnection con) {
        connection = con;
        transport = con.getDlt645Transport();
    }

    public boolean isReconnecting() {
        return reconnecting;
    }

    public void setReconnecting(boolean b) {
        reconnecting = b;
    }

    @Override
    public synchronized void execute() throws Dlt645Exception {
        if (request == null || connection == null) {
            throw new Dlt645Exception("Invalid request or connection");
        }

        int retryCounter = 0;
        int retryLimit = (retries > 0 ? retries : Dlt645.DEFAULT_RETRIES);
        boolean keepTrying = true;

        while (keepTrying) {
            if (!connection.isConnected()) {
                try {
                    logger.debug("Connecting to: {}:{}", connection.getAddress(), connection.getPort());
                    connection.connect();
                    transport = connection.getDlt645Transport();
                } catch (Exception ex) {
                    throw new Dlt645IOException("Connection failed for %s:%d %s", connection.getAddress().toString(), connection.getPort(), ex.getMessage());
                }
            }

            transport.setTimeout(connection.getTimeout());

            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("Writing request: {} (try: {}) request transaction ID = {} to {}:{}", request.getHexMessage(), retryCounter, request.getTransactionID(), connection.getAddress(), connection.getPort());
                }
                transport.writeRequest(request);

                response = transport.readResponse();
                if (logger.isDebugEnabled()) {
                    logger.debug("Read response: {} (try: {}) response transaction ID = {} from {}:{}", response.getHexMessage(), retryCounter, response.getTransactionID(), connection.getAddress(), connection.getPort());
                }
                keepTrying = false;

                if (response instanceof ExceptionResponse) {
                    throw new Dlt645SlaveException(((ExceptionResponse)response).getExceptionCode());
                }

                if (responseIsInValid()) {
                    retryCounter++;
                    if (retryCounter >= retryLimit) {
                        throw new Dlt645IOException("Executing transaction failed (tried %d times)", retryLimit);
                    }
                    keepTrying = true;
                    long sleepTime = getRandomSleepTime(retryCounter);
                    if (response == null) {
                        logger.debug("Failed to get any response (try: {}) - retrying after {} milliseconds", retryCounter, sleepTime);
                    } else {
                        logger.debug("Failed to get a valid response, transaction IDs do not match (try: {}) - retrying after {} milliseconds", retryCounter, sleepTime);
                    }
                    Dlt645Util.sleep(sleepTime);
                }
            } catch (Dlt645IOException ex) {
                retryCounter++;
                if (retryCounter >= retryLimit) {
                    throw new Dlt645IOException("Executing transaction %s failed (tried %d times) %s", request.getHexMessage(), retryLimit, ex.getMessage());
                } else {
                    long sleepTime = getRandomSleepTime(retryCounter);
                    logger.debug("Failed transaction Request: {} (try: {}) - retrying after {} milliseconds", request.getHexMessage(), retryCounter, sleepTime);
                    Dlt645Util.sleep(sleepTime);
                }
                logger.debug("Failed request {} (try: {}) request transaction ID = {} - {} closing and re-opening connection {}:{}", request.getHexMessage(), retryCounter, request.getTransactionID(), ex.getMessage(), connection.getAddress().toString(), connection.getPort());
                connection.close();
            }

            if (keepTrying) {
                incrementTransactionID();
            }
        }

        if (isReconnecting()) {
            connection.close();
        }
        incrementTransactionID();
    }

    private boolean responseIsInValid() {
        if (response == null) {
            return true;
        }
        else if (!response.isHeadless() && validityCheck) {
            return request.getTransactionID() != response.getTransactionID();
        }
        else {
            return false;
        }
    }

    private synchronized void incrementTransactionID() {
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
