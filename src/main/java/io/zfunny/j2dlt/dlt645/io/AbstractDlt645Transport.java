package io.zfunny.j2dlt.dlt645.io;

import io.zfunny.j2dlt.dlt645.Dlt645;
import io.zfunny.j2dlt.dlt645.Dlt645IOException;
import io.zfunny.j2dlt.dlt645.msg.Dlt645Request;
import io.zfunny.j2dlt.dlt645.msg.Dlt645Response;
import io.zfunny.j2dlt.dlt645.net.AbstractDlt645Listener;

import java.io.IOException;

public abstract class AbstractDlt645Transport {

    protected int timeout = Dlt645.DEFAULT_TIMEOUT;

    public void setTimeout(int time) {
        timeout = time;
    }

    public abstract void close() throws IOException;

    public abstract Dlt645Transaction createTransaction();

    public abstract void writeRequest(Dlt645Request msg) throws Dlt645IOException;

    public abstract void writeResponse(Dlt645Response msg) throws Dlt645IOException;

    public abstract Dlt645Request readRequest(AbstractDlt645Listener listener) throws Dlt645IOException;

    public abstract Dlt645Response readResponse() throws Dlt645IOException;
}
