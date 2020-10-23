package io.zfunny.j2dlt.dlt645.io;

import io.zfunny.j2dlt.dlt645.Dlt645;
import io.zfunny.j2dlt.dlt645.Dlt645Exception;
import io.zfunny.j2dlt.dlt645.Dlt645IOException;
import io.zfunny.j2dlt.dlt645.msg.Dlt645Request;
import io.zfunny.j2dlt.dlt645.msg.Dlt645Response;

import java.util.Random;

public abstract class Dlt645Transaction {

    protected AbstractDlt645Transport transport;
    protected Dlt645Request request;
    protected Dlt645Response response;
    boolean validityCheck = Dlt645.DEFAULT_VALIDITYCHECK;
    int retries = Dlt645.DEFAULT_RETRIES;
    private final Random random = new Random(System.nanoTime());
    static int transactionID = Dlt645.DEFAULT_TRANSACTION_ID;

    public Dlt645Request getRequest() {
        return request;
    }

    public void setRequest(Dlt645Request req) {
        request = req;
        if (req != null) {
            request.setTransactionID(getTransactionID());
        }
    }

    public Dlt645Response getResponse() {
        return response;
    }

    int getRetries() {
        return retries;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public boolean isCheckingValidity() {
        return validityCheck;
    }

    public void setCheckingValidity(boolean b) {
        validityCheck = b;
    }

    public synchronized int getTransactionID() {
        if (transactionID < Dlt645.DEFAULT_TRANSACTION_ID && isCheckingValidity()) {
            transactionID = Dlt645.DEFAULT_TRANSACTION_ID;
        }
        if (transactionID >= Dlt645.MAX_TRANSACTION_ID) {
            transactionID = Dlt645.DEFAULT_TRANSACTION_ID;
        }
        return transactionID;
    }

    long getRandomSleepTime(int count) {
        return (Dlt645.RETRY_SLEEP_TIME / 2) + (long) (random.nextDouble() * Dlt645.RETRY_SLEEP_TIME * count);
    }

    void checkValidity() throws Dlt645Exception {
        if (request != null && response != null) {
            if (!request.getUnitIDString().equals(response.getUnitIDString())) {
                throw new Dlt645IOException("Unit ID mismatch - Request [%s] Response [%s]", request.getHexMessage(), response.getHexMessage());
            }
            if(request.getFunctionCode() != response.getFunctionCode()) {
                throw new Dlt645IOException("Function code mismatch - Request [%s] Response [%s]", request.getHexMessage(), response.getHexMessage());
            }
            // TODO check dataIdentity
        }
    }

    public abstract void execute() throws Dlt645Exception;
}
