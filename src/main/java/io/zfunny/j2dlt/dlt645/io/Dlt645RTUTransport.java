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
import java.util.Arrays;

/**
 * Class that implements the Dlt645RTU transport flavor.
 *
 * @author John Charlton
 * @author Dieter Wimberger
 * @author Julie Haugh
 * @author Steve O'Hara (4NG)
 * @version 2.0 (March 2016)
 */
public class Dlt645RTUTransport extends Dlt645SerialTransport {

    private static final Logger logger = LoggerFactory.getLogger(Dlt645RTUTransport.class);

    private final byte[] inBuffer = new byte[Dlt645.MAX_MESSAGE_LENGTH];
    private final BytesInputStream byteInputStream = new BytesInputStream(inBuffer); // to read message from
    private final BytesOutputStream byteInputOutputStream = new BytesOutputStream(inBuffer); // to buffer message to
    private final BytesOutputStream byteOutputStream = new BytesOutputStream(Dlt645.MAX_MESSAGE_LENGTH); // write frames
    private byte[] lastRequest = null;

    /**
     * Read the data for a request of a given fixed size
     *
     * @param byteCount Byte count excluding the 2 byte CRC
     * @param out       Output buffer to populate
     * @throws IOException If data cannot be read from the port
     */
    private void readRequestData(int byteCount, BytesOutputStream out) throws IOException {
        byteCount += 2;
        byte[] inpBuf = new byte[byteCount];
        readBytes(inpBuf, byteCount);
        out.write(inpBuf, 0, byteCount);
    }

    /**
     * getRequest - Read a request, after the unit and function code
     *
     * @param function - Dlt645 function code
     * @param out      - Byte stream buffer to hold actual message
     */
    private void getRequest(int function, BytesOutputStream out) throws IOException {
        int byteCount;
        byte[] inpBuf = new byte[256];
        try {
            if ((function & 0x80) == 0) {
                switch (function) {
                    case Dlt645.READ_DATA:
                        //FIXME
                        break;
                    default:
                        throw new IOException(String.format("getResponse unrecognised function code [%s]", function));
                }
            }
        }
        catch (IOException e) {
            throw new IOException("getResponse serial port exception");
        }
    }

    /**
     * getResponse - Read a <tt>Dlt645Response</tt> from a slave.
     *
     * @param function The function code of the request
     * @param out      The output buffer to put the result
     * @throws IOException If data cannot be read from the port
     */
    private void getResponse(int function, BytesOutputStream out) throws IOException {
        byte[] inpBuf = new byte[256];
        try {
            if ((function & 0x80) == 0) {
                switch (function) {
                    case Dlt645.READ_DATA:
                        // Read the data payload byte count. There will be two
                        // additional CRC bytes afterwards.
                        int cnt = readByte();
                        out.write(cnt);
                        readRequestData(cnt, out);
                        break;
                    default:
                        throw new IOException(String.format("getResponse unrecognised function code [%s]", function));

                }
            }
            else {
                // read the exception code, plus two CRC bytes.
                readRequestData(1, out);

            }
        }
        catch (IOException e) {
            throw new IOException(String.format("getResponse serial port exception - %s", e.getMessage()));
        }
    }

    /**
     * Writes the Dlt645 message to the comms port
     *
     * @param msg a <code>Dlt645Message</code> value
     * @throws Dlt645IOException If an error occurred bundling the message
     */
    @Override
    protected void writeMessageOut(Dlt645Message msg) throws Dlt645IOException {
        try {
            int len;
            synchronized (byteOutputStream) {
                // first clear any input from the receive buffer to prepare
                // for the reply since RTU doesn't have message delimiters
                clearInput();
                // write message to byte out
                byteOutputStream.reset();
                msg.setHeadless();
                msg.writeTo(byteOutputStream);
                len = byteOutputStream.size();
                int cs = Dlt645Util.calculateCS(byteOutputStream.getBuffer(), 4, len - 1);
                byteOutputStream.writeByte(cs);
                byteOutputStream.writeByte(0x16);

                System.out.println(Dlt645Util.toHex(byteOutputStream.getBuffer(), 0, byteOutputStream.size()));
                // write message
                writeBytes(byteOutputStream.getBuffer(), byteOutputStream.size());
                if (logger.isDebugEnabled()) {
                    logger.debug("Sent: {}", Dlt645Util.toHex(byteOutputStream.getBuffer(), 0, byteOutputStream.size()));
                }
                // clears out the echoed message
                // for RS485
                if (echo) {
                    readEcho(len + 2);
                }
                lastRequest = new byte[len];
                System.arraycopy(byteOutputStream.getBuffer(), 0, lastRequest, 0, len);
            }
        }
        catch (IOException ex) {
            throw new Dlt645IOException("I/O failed to write");
        }
    }

    @Override
    protected Dlt645Request readRequestIn(AbstractDlt645Listener listener) throws Dlt645IOException {
        Dlt645Request request = null;

        try {
            while (request == null) {
                synchronized (byteInputStream) {
                    int uid = readByte();

                    byteInputOutputStream.reset();
                    byteInputOutputStream.writeByte(uid);

                    if (listener.getProcessImage(uid) != null) {
                        // Read a proper request

                        int fc = readByte();
                        byteInputOutputStream.writeByte(fc);

                        // create request to acquire length of message
                        request = Dlt645Request.createDlt645Request(fc, 0);
                        request.setHeadless();

                        /*
                         * With Dlt645 RTU, there is no end frame. Either we
                         * assume the message is complete as is or we must do
                         * function specific processing to know the correct
                         * length. To avoid moving frame timing to the serial
                         * input functions, we set the timeout and to message
                         * specific parsing to read a response.
                         */
                        getRequest(fc, byteInputOutputStream);
                        int dlength = byteInputOutputStream.size() - 2; // less the crc
                        if (logger.isDebugEnabled()) {
                            logger.debug("Request: {}", Dlt645Util.toHex(byteInputOutputStream.getBuffer(), 0, dlength + 2));
                        }

                        byteInputStream.reset(inBuffer, dlength);

                        // check CRC
                        //int[] crc = Dlt645Util.calculateCRC(inBuffer, 0, dlength); // does not include CRC
                        int[] crc = {0,0}; // FIXME
                        if (Dlt645Util.unsignedByteToInt(inBuffer[dlength]) != crc[0] || Dlt645Util.unsignedByteToInt(inBuffer[dlength + 1]) != crc[1]) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("CRC should be {}, {}", Integer.toHexString(crc[0]), Integer.toHexString(crc[1]));
                            }

                            // Drain the input in case the frame was misread and more
                            // was to follow.
                            clearInput();
                            throw new IOException("CRC Error in received frame: " + dlength + " bytes: " + Dlt645Util.toHex(byteInputStream.getBuffer(), 0, dlength));
                        }

                        // read request
                        byteInputStream.reset(inBuffer, dlength);
                        request.readFrom(byteInputStream);

                        return request;

                    }
                    else {
                        // This message is not for us, read and wait for the 3.5t delay

                        // Wait for max 1.5t for data to be available
                        while (true) {
                            boolean bytesAvailable = availableBytes() > 0;
                            if (!bytesAvailable) {
                                // Sleep the 1.5t to see if there will be more data
                                if (logger.isDebugEnabled()) {
                                    logger.debug("Waiting for {} microsec", getMaxCharDelay());
                                }
                                bytesAvailable = spinUntilBytesAvailable(getMaxCharDelay());
                            }

                            if (bytesAvailable) {
                                // Read the available data
                                while (availableBytes() > 0) {
                                    byteInputOutputStream.writeByte(readByte());
                                }
                            }
                            else {
                                // Transition to wait for the 3.5t interval
                                break;
                            }
                        }

                        // Wait for 2t to complete the 3.5t wait
                        // Is there is data available the interval was not respected, we should discard the message
                        if (logger.isDebugEnabled()) {
                            logger.debug("Waiting for {} microsec", getCharIntervalMicro(2));
                        }
                        if (spinUntilBytesAvailable(getCharIntervalMicro(2))) {
                            // Discard the message
                            if (logger.isDebugEnabled()) {
                                logger.debug("Discarding message (More than 1.5t between characters!) - {}", Dlt645Util.toHex(byteInputOutputStream.getBuffer(), 0, byteInputOutputStream.size()));
                            }
                        }
                        else {
                            // This message is complete
                            if (logger.isDebugEnabled()) {
                                logger.debug("Read message not meant for us: {}", Dlt645Util.toHex(byteInputOutputStream.getBuffer(), 0, byteInputOutputStream.size()));
                            }
                        }
                    }
                }
            }

            // We will never get here
            return null;
        }
        catch (IOException ex) {
            // An exception mostly means there is no request. The master should
            // retry the request.

            if (logger.isDebugEnabled()) {
                logger.debug("Failed to read response! {}", ex.getMessage());
            }

            return null;
        }
    }

    /**
     * readResponse - Read the bytes for the response from the slave.
     *
     * @return a <tt>Dlt645Respose</tt>
     *
     * @throws Dlt645IOException If the response cannot be read from the socket/port
     */
    @Override
    protected Dlt645Response readResponseIn() throws Dlt645IOException {
        boolean done;
        Dlt645Response response;
        int dlength;

        try {
            do {
                // 1. read to function code, create request and read function
                // specific bytes
                synchronized (byteInputStream) {
                    byte[] wakebytes = new byte[4];
                    readBytes(wakebytes, 4);

                    byte[] firstBytes = new byte[1 + 6 + 1];
                    readBytes(firstBytes, 8);

                    int fc = readByte();
                    byteInputOutputStream.reset();
                    byteInputOutputStream.write(firstBytes);
                    byteInputOutputStream.writeByte(fc);

                    // create response to acquire length of message
                    response = Dlt645Response.createDlt645Response(fc);
                    response.setHeadless();

                        /*
                         * With Dlt645 RTU, there is no end frame. Either we
                         * assume the message is complete as is or we must do
                         * function specific processing to know the correct
                         * length. To avoid moving frame timing to the serial
                         * input functions, we set the timeout and to message
                         * specific parsing to read a response.
                         */
                    getResponse(fc, byteInputOutputStream);
                    dlength = byteInputOutputStream.size() - 2; // less the crc
                    if (logger.isDebugEnabled()) {
                        logger.debug("Response: {}", Dlt645Util.toHex(byteInputOutputStream.getBuffer(), 0, dlength + 2));
                    }
                    byteInputStream.reset(inBuffer, dlength);

                    // check CS
                    int cs = Dlt645Util.calculateCS(inBuffer, 0, dlength -1);
                    if (cs != Dlt645Util.unsignedByteToInt(inBuffer[dlength])) {
                        logger.debug("CS should be {}", cs);
                        throw new IOException("CS Error in received frame: " + dlength + "bytes: " + Dlt645Util.toHex(byteInputStream.getBuffer(), 0, dlength));
                    }

                    // read response
                    byteInputStream.reset(inBuffer, dlength);
                    response.readFrom(byteInputStream);
                    done = true;
                }
            } while (!done);
            return response;
        }
        catch (IOException ex) {
            // FIXME: This printout is wrong when reading response from other slave
            throw new Dlt645IOException("I/O exception - failed to read response for request [%s] - %s", Dlt645Util.toHex(lastRequest), ex.getMessage());
        }
    }
}

