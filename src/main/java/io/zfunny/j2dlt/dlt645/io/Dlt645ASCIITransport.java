package io.zfunny.j2dlt.dlt645.io;

import io.zfunny.j2dlt.dlt645.Dlt645;
import io.zfunny.j2dlt.dlt645.Dlt645IOException;
import io.zfunny.j2dlt.dlt645.msg.Dlt645Message;
import io.zfunny.j2dlt.dlt645.msg.Dlt645Request;
import io.zfunny.j2dlt.dlt645.msg.Dlt645Response;
import io.zfunny.j2dlt.dlt645.net.AbstractDlt645Listener;
import io.zfunny.j2dlt.dlt645.util.Dlt645Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Class that implements the Modbus/ASCII transport
 * flavor.
 *
 * @author Dieter Wimberger
 * @author John Charlton
 * @author Steve O'Hara (4NG)
 * @version 2.0 (March 2016)
 */
public class Dlt645ASCIITransport extends Dlt645SerialTransport {

    private static final Logger logger = LoggerFactory.getLogger(Dlt645ASCIITransport.class);
    private static final String I_O_EXCEPTION_SERIAL_PORT_TIMEOUT = "I/O exception - Serial port timeout";
    private final byte[] inBuffer = new byte[Dlt645.MAX_MESSAGE_LENGTH];
    private final BytesInputStream byteInputStream = new BytesInputStream(inBuffer);         //to read message from
    private final BytesOutputStream byteInputOutputStream = new BytesOutputStream(inBuffer);     //to buffer message to
    private final BytesOutputStream byteOutputStream = new BytesOutputStream(Dlt645.MAX_MESSAGE_LENGTH);      //write frames

    /**
     * Constructs a new <tt>MobusASCIITransport</tt> instance.
     */
    public Dlt645ASCIITransport() {
        // No op
    }

    @Override
    protected void writeMessageOut(Dlt645Message msg) throws Dlt645IOException {
        try {
            synchronized (byteOutputStream) {
                //write message to byte out
                msg.setHeadless();
                msg.writeTo(byteOutputStream);
                byte[] buf = byteOutputStream.getBuffer();
                int len = byteOutputStream.size();

                //write message
                writeAsciiByte(FRAME_START);               //FRAMESTART
                writeAsciiBytes(buf, len);                 //PDU
                if (logger.isDebugEnabled()) {
                    logger.debug("Writing: {}", Dlt645Util.toHex(buf, 0, len));
                }
                writeAsciiByte(calculateLRC(buf, 0, len)); //LRC
                writeAsciiByte(FRAME_END);                 //FRAMEEND
                byteOutputStream.reset();
                // clears out the echoed message
                // for RS485
                if (echo) {
                    // read back the echoed message
                    readEcho(len + 3);
                }
            }
        }
        catch (IOException ex) {
            throw new Dlt645IOException("I/O failed to write");
        }
    }

    @Override
    public Dlt645Request readRequestIn(AbstractDlt645Listener listener) throws Dlt645IOException {
        boolean done = false;
        Dlt645Request request = null;
        int in;

        try {
            do {
                //1. Skip to FRAME_START
                while ((readAsciiByte()) != FRAME_START) {
                    // Nothing to do
                }

                //2. Read to FRAME_END
                synchronized (inBuffer) {
                    byteInputOutputStream.reset();
                    while ((in = readAsciiByte()) != FRAME_END) {
                        if (in == -1) {
                            throw new IOException(I_O_EXCEPTION_SERIAL_PORT_TIMEOUT);
                        }
                        byteInputOutputStream.writeByte(in);
                    }
                    //check LRC
                    if (inBuffer[byteInputOutputStream.size() - 1] != calculateLRC(inBuffer, 0, byteInputOutputStream.size(), 1)) {
                        continue;
                    }
                    byteInputStream.reset(inBuffer, byteInputOutputStream.size());

                    // Read the unit ID which we're not interested in
                    byteInputStream.readUnsignedByte();

                    int functionCode = byteInputStream.readUnsignedByte();
                    //create request
                    request = Dlt645Request.createDlt645Request(functionCode, 0); // FIXME
                    request.setHeadless();
                    //read message
                    byteInputStream.reset(inBuffer, byteInputOutputStream.size());
                    request.readFrom(byteInputStream);
                }
                done = true;
            } while (!done);
            return request;
        }
        catch (Exception ex) {
            logger.debug(ex.getMessage());
            throw new Dlt645IOException("I/O exception - failed to read");
        }

    }

    @Override
    protected Dlt645Response readResponseIn() throws Dlt645IOException {
        boolean done = false;
        Dlt645Response response = null;
        int in;

        try {
            do {
                //1. Skip to FRAME_START
                while ((in = readAsciiByte()) != FRAME_START) {
                    if (in == -1) {
                        throw new IOException(I_O_EXCEPTION_SERIAL_PORT_TIMEOUT);
                    }
                }
                //2. Read to FRAME_END
                synchronized (inBuffer) {
                    byteInputOutputStream.reset();
                    while ((in = readAsciiByte()) != FRAME_END) {
                        if (in == -1) {
                            throw new IOException(I_O_EXCEPTION_SERIAL_PORT_TIMEOUT);
                        }
                        byteInputOutputStream.writeByte(in);
                    }
                    int len = byteInputOutputStream.size();
                    if (logger.isDebugEnabled()) {
                        logger.debug("Received: {}", Dlt645Util.toHex(inBuffer, 0, len));
                    }
                    //check LRC
                    if (inBuffer[len - 1] != calculateLRC(inBuffer, 0, len, 1)) {
                        continue;
                    }

                    byteInputStream.reset(inBuffer, byteInputOutputStream.size());
                    byteInputStream.readUnsignedByte();
                    // JDC: To check slave unit identifier in a response we need to know
                    // the slave id in the request.  This is not tracked since slaves
                    // only respond when a master request is made and there is only one
                    // master.  We are the only master, so we can assume that this
                    // response message is from the slave responding to the last request.
                    in = byteInputStream.readUnsignedByte();
                    //create request
                    response = Dlt645Response.createDlt645Response(in);
                    response.setHeadless();
                    //read message
                    byteInputStream.reset(inBuffer, byteInputOutputStream.size());
                    response.readFrom(byteInputStream);
                }
                done = true;
            } while (!done);
            return response;
        }
        catch (Exception ex) {
            logger.debug(ex.getMessage());
            throw new Dlt645IOException("I/O exception - failed to read");
        }
    }

    /**
     * Calculates a LRC checksum
     *
     * @param data   Data to use
     * @param off    Offset into byte array
     * @param length Number of bytes to use
     * @return Checksum
     */
    private static int calculateLRC(byte[] data, int off, int length) {
        return calculateLRC(data, off, length, 0);
    }

    /**
     * Calculates a LRC checksum
     *
     * @param data     Data to use
     * @param off      Offset into byte array
     * @param length   Number of bytes to use
     * @param tailskip Bytes to skip at tail
     * @return Checksum
     */
    private static byte calculateLRC(byte[] data, int off, int length, int tailskip) {
        int lrc = 0;
        for (int i = off; i < length - tailskip; i++) {
            lrc += ((int) data[i]) & 0xFF;
        }
        return (byte) ((-lrc) & 0xff);
    }

}

