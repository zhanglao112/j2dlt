package io.zfunny.j2dlt.dlt645.msg;

import io.zfunny.j2dlt.dlt645.Dlt645;
import io.zfunny.j2dlt.dlt645.net.AbstractDlt645Listener;

public abstract class Dlt645Request extends Dlt645MessageImpl {

    public static Dlt645Request createDlt645Request(int functionCode, int type) {
        Dlt645Request request;

        switch (functionCode) {
            case Dlt645.READ_DATA:
                request = new ReadRequest();
                ((ReadRequest)request).setDataIdentity(Dlt645.dataIdentity[1]); // test
                break;
            default:
                request = null;
                break;
        }

        return request;
    }

    public abstract Dlt645Response getResponse();

    public abstract Dlt645Response createResponse(AbstractDlt645Listener listener);

    public Dlt645Response createExceptionResponse(int code) {
        return updateResponseWithHeader(new ExceptionResponse(getFunctionCode(), code));
    }

    Dlt645Response updateResponseWithHeader(Dlt645Response response) {
        return updateResponseWithHeader(response, false);
    }

    Dlt645Response updateResponseWithHeader(Dlt645Response response, boolean ignoreFunctionCode) {
        response.setHeadless(isHeadless());
        if (!isHeadless()) {
            response.setTransactionID(getTransactionID());
        } else {
            response.setHeadless();
        }
        response.setUnitID(getUnitID());
        if (!ignoreFunctionCode) {
            response.setFunctionCode(getFunctionCode());
        }
        return response;
    }
}
