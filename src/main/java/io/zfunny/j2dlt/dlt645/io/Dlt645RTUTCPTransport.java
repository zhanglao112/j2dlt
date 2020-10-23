package io.zfunny.j2dlt.dlt645.io;

import io.zfunny.j2dlt.dlt645.Dlt645IOException;
import io.zfunny.j2dlt.dlt645.msg.Dlt645Request;
import io.zfunny.j2dlt.dlt645.msg.Dlt645Response;

import java.net.Socket;

public class Dlt645RTUTCPTransport extends Dlt645TCPTransport {

    public Dlt645RTUTCPTransport() {
        setHeadless();
    }

    public Dlt645RTUTCPTransport(Socket socket) {
        super(socket);
        // RTU over TCP is headless by default
        setHeadless();
    }

    @Override
    public void writeResponse(Dlt645Response msg) throws Dlt645IOException {
        writeMessage(msg, true);
    }

    @Override
    public void writeRequest(Dlt645Request msg) throws Dlt645IOException {
        writeMessage(msg, true);
    }
}
