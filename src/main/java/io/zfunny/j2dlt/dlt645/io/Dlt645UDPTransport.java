package io.zfunny.j2dlt.dlt645.io;

import io.zfunny.j2dlt.dlt645.Dlt645;
import io.zfunny.j2dlt.dlt645.Dlt645IOException;
import io.zfunny.j2dlt.dlt645.msg.Dlt645Message;
import io.zfunny.j2dlt.dlt645.msg.Dlt645Request;
import io.zfunny.j2dlt.dlt645.msg.Dlt645Response;
import io.zfunny.j2dlt.dlt645.net.AbstractDlt645Listener;
import io.zfunny.j2dlt.dlt645.net.AbstractUDPTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.Arrays;

public class Dlt645UDPTransport extends AbstractDlt645Transport {

    private static final Logger logger = LoggerFactory.getLogger(Dlt645UDPTransport.class);

    private AbstractUDPTerminal terminal;
    private final BytesOutputStream byteOutputStream = new BytesOutputStream(Dlt645.MAX_MESSAGE_LENGTH);
    private final BytesInputStream byteInputStream = new BytesInputStream(Dlt645.MAX_MESSAGE_LENGTH);


    public Dlt645UDPTransport(AbstractUDPTerminal terminal) {
        this.terminal = terminal;
    }

    @Override
    public void setTimeout(int time) {
        super.setTimeout(time);
        if (terminal != null) {
            terminal.setTimeout(timeout);
        }
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public Dlt645Transaction createTransaction() {
        Dlt645UDPTransaction trans = new Dlt645UDPTransaction();
        trans.setTerminal(terminal);
        return trans;
    }

    @Override
    public void writeResponse(Dlt645Response msg) throws Dlt645IOException {
        writeMessage(msg);
    }

    @Override
    public void writeRequest(Dlt645Request msg) throws Dlt645IOException {
        writeMessage(msg);
    }

    @Override
    public Dlt645Request readRequest(AbstractDlt645Listener listener) throws Dlt645IOException {
        try {
            Dlt645Request req;
            synchronized (byteInputStream) {
                byteInputStream.reset(terminal.receiveMessage());
                byteInputStream.skip(7);
                int functionCode = byteInputStream.readUnsignedByte();
                byteInputStream.reset();
                req = Dlt645Request.createDlt645Request(functionCode, 0);
                req.readFrom(byteInputStream);
            }
            return req;
        }
        catch (Exception ex) {
            throw new Dlt645IOException("I/O exception - failed to read", ex);
        }
    }

    @Override
    public Dlt645Response readResponse() throws Dlt645IOException {

        try {
            Dlt645Response res;
            synchronized (byteInputStream) {
                byteInputStream.reset(terminal.receiveMessage());
                byteInputStream.skip(7);
                int functionCode = byteInputStream.readUnsignedByte();
                byteInputStream.reset();
                res = Dlt645Response.createDlt645Response(functionCode);
                res.readFrom(byteInputStream);
            }
            return res;
        }
        catch (InterruptedIOException ioex) {
            throw new Dlt645IOException("Socket was interrupted", ioex);
        }
        catch (Exception ex) {
            logger.debug("I/O exception while reading Dlt645 response.", ex);
            throw new Dlt645IOException("I/O exception - failed to read - %s", ex.getMessage());
        }
    }

    /**
     * Writes the request/response message to the port
     * @param msg Message to write
     * @throws Dlt645IOException If the port cannot be written to
     */
    private void writeMessage(Dlt645Message msg) throws Dlt645IOException {
        try {
            synchronized (byteOutputStream) {
                int len = msg.getOutputLength();
                byteOutputStream.reset();
                msg.writeTo(byteOutputStream);
                byte[] data = byteOutputStream.getBuffer();
                data = Arrays.copyOf(data, len);
                terminal.sendMessage(data);
            }
        }
        catch (Exception ex) {
            throw new Dlt645IOException("I/O exception - failed to write", ex);
        }
    }

}
