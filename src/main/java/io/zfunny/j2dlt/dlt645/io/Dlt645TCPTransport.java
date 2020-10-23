package io.zfunny.j2dlt.dlt645.io;


import io.zfunny.j2dlt.dlt645.Dlt645;
import io.zfunny.j2dlt.dlt645.Dlt645IOException;
import io.zfunny.j2dlt.dlt645.msg.Dlt645Message;
import io.zfunny.j2dlt.dlt645.msg.Dlt645Request;
import io.zfunny.j2dlt.dlt645.msg.Dlt645Response;
import io.zfunny.j2dlt.dlt645.net.AbstractDlt645Listener;
import io.zfunny.j2dlt.dlt645.net.TCPMasterConnection;
import io.zfunny.j2dlt.dlt645.util.Dlt645Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class Dlt645TCPTransport extends AbstractDlt645Transport {

    private static final Logger logger = LoggerFactory.getLogger(Dlt645TCPTransport.class);

    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private final BytesInputStream byteInputStream = new BytesInputStream(Dlt645.MAX_MESSAGE_LENGTH + 6); // TODO
    private final BytesOutputStream byteOutputStream = new BytesOutputStream(Dlt645.MAX_MESSAGE_LENGTH + 6);
    protected Socket socket = null;
    protected TCPMasterConnection master = null;
    private boolean headless = false;
    private long lastActivityTimestamp;

    public Dlt645TCPTransport() {
        lastActivityTimestamp = System.nanoTime();
    }

    public Dlt645TCPTransport(Socket socket) {
        lastActivityTimestamp = System.nanoTime();

        try {
            setSocket(socket);
        } catch (IOException ex) {
            logger.debug("Dlt645TCPTransport::Socket invalid");

            throw new IllegalStateException("Socket invalid", ex);
        }

    }

    public void setSocket(Socket socket) throws IOException {
        if (this.socket != null) {
            this.socket.close();
            this.socket = null;
        }
        this.socket = socket;
        setTimeout(timeout);
        prepareStreams(socket);
    }

    public void setHeadless() {
        headless = true;
    }

    public void setHeadless(boolean headless) {
        this.headless = headless;
    }

    public void setMaster(TCPMasterConnection master) {
        this.master = master;
    }

    @Override
    public void setTimeout(int time) {
        super.setTimeout(time);
        if (socket != null) {
            try {
                socket.setSoTimeout(time);
            } catch (SocketException e) {
                logger.warn("Socket exception occurred while setting timeout to {}", time, e);
            }
        }
    }

    public long getLastActivityTimestamp() {
        return lastActivityTimestamp;
    }

    @Override
    public void close() throws IOException {
        dataInputStream.close();
        dataOutputStream.close();
        socket.close();
    }

    @Override
    public Dlt645Transaction createTransaction() {
        if (master == null) {
            master = new TCPMasterConnection(socket.getInetAddress());
            master.setPort(socket.getPort());
            master.setDlt645Transport(this);
        }
        return new Dlt645TCPTransaction(master);
    }

    @Override
    public void writeResponse(Dlt645Response msg) throws Dlt645IOException {
        writeMessage(msg, false);
    }

    @Override
    public void writeRequest(Dlt645Request msg) throws Dlt645IOException {
        writeMessage(msg, false);
    }

    @Override
    public Dlt645Request readRequest(AbstractDlt645Listener listener) throws Dlt645IOException {
        lastActivityTimestamp = System.nanoTime();

        Dlt645Request req;
        try {
            byteInputStream.reset();

            synchronized (byteInputStream) {
                byte[] buffer = byteInputStream.getBuffer();

                if (!headless) { // todo
                    req = null;
//                    dataInputStream.readFully(buffer, 0, 6);
//
//                    // The transaction ID must be treated as an unsigned short in
//                    // order for validation to work correctly.
//
//                    int transaction = Dlt645Util.registerToShort(buffer, 0) & 0x0000FFFF;
//                    int protocol = Dlt645Util.registerToShort(buffer, 2);
//                    int count = Dlt645Util.registerToShort(buffer, 4);
//
//                    dataInputStream.readFully(buffer, 6, count);
//
//                    if (logger.isDebugEnabled()) {
//                        logger.debug("Read: {}", Dlt645Util.toHex(buffer, 0, count + 6));
//                    }
//
//                    byteInputStream.reset(buffer, (6 + count));
//                    byteInputStream.skip(6);
//
//                    int unit = byteInputStream.readByte();
//                    int functionCode = byteInputStream.readUnsignedByte();
//
//                    byteInputStream.reset();
//                    req = Dlt645Request.createDlt645Request(functionCode);
//                    req.setUnitID(unit);
//                    req.setHeadless(false);
//
//                    req.setTransactionID(transaction);
//                    req.setProtocolID(protocol);
//                    req.setDataLength(count);
//
//                    req.readFrom(byteInputStream);
                }
                else {

                    // This is a headless request.

                    // wake bytes
                    byte[] wakeAndStartBytes = new byte[4 + 1];
                    dataInputStream.readFully(wakeAndStartBytes, 0, 5);

                    byte[] unitID = new byte[6];
                    dataInputStream.readFully(unitID, 0, 6);
                    // int unit = dataInputStream.readByte();

                    dataInputStream.readByte(); // ignore start byte
                    int function = dataInputStream.readByte();

                    //req = Dlt645Request.createDlt645Request(function);
                    req = Dlt645Request.createDlt645Request(function, 0); // todo get type by dataIdentity
                    req.setUnitID(unitID);
                    req.setHeadless(true);
                    req.readData(dataInputStream);

                    // Discard the CRC. This is a TCP/IP connection, which has
                    // proper error correction and recovery.

                    dataInputStream.readShort();
                    if (logger.isDebugEnabled()) {
                        logger.debug("Read: {}", req.getHexMessage());
                    }
                }
            }
            return req;
        }
        catch (EOFException eoex) {
            throw new Dlt645IOException("End of File", true);
        }
        catch (SocketTimeoutException x) {
            throw new Dlt645IOException("Timeout reading request", x);
        }
        catch (SocketException sockex) {
            throw new Dlt645IOException("Socket Exception", sockex);
        }
        catch (IOException ex) {
            throw new Dlt645IOException("I/O exception - failed to read", ex);
        }
    }

    @Override
    public Dlt645Response readResponse() throws Dlt645IOException {
        lastActivityTimestamp = System.nanoTime();

        try {
            Dlt645Response response;

            synchronized (byteInputStream) {
                // use same buffer
                byte[] buffer = byteInputStream.getBuffer();
                logger.debug("Reading response...");
                if (!headless) {
                    // All Dlt645 TCP transactions start with 6 bytes. Get them.
                    dataInputStream.readFully(buffer, 0, 6);

                    /*
                     * The transaction ID is the first word (offset 0) in the
                     * data that was just read. It will be echoed back to the
                     * requester.
                     *
                     * The protocol ID is the second word (offset 2) in the
                     * data. It should always be 0, but I don't check.
                     *
                     * The length of the payload is the third word (offset 4) in
                     * the data that was just read. That's what I need in order
                     * to read the rest of the response.
                     */
                    int transaction = Dlt645Util.registerToShort(buffer, 0) & 0x0000FFFF;
                    int protocol = Dlt645Util.registerToShort(buffer, 2);
                    int count = Dlt645Util.registerToShort(buffer, 4);

                    dataInputStream.readFully(buffer, 6, count);
                    byteInputStream.reset(buffer, (6 + count));
                    byteInputStream.reset();
                    byteInputStream.skip(7);
                    int function = byteInputStream.readUnsignedByte();
                    response = Dlt645Response.createDlt645Response(function);

                    // Rewind the input buffer, then read the data into the
                    // response.
                    byteInputStream.reset();
                    response.readFrom(byteInputStream);

                    response.setTransactionID(transaction);
                }
                else {
                    // This is a headless response. It has the same format as a
                    // RTU over Serial response.
                    byte[] wakeandstartBytes = new byte[4 + 1];
                    dataInputStream.readFully(wakeandstartBytes, 0, 5);
                    byte[] unitID = new byte[6];
                    dataInputStream.readFully(unitID, 0, 6);
//                    int unit = dataInputStream.readByte();

                    dataInputStream.readByte(); // ignore start byte
                    int function = dataInputStream.readByte();

                    response = Dlt645Response.createDlt645Response(function);
                    response.setUnitID(unitID);
                    response.setHeadless();
                    response.readData(dataInputStream);

                    // Now discard the CRC. Which hopefully wasn't needed
                    // because this is a TCP transport.
                    dataInputStream.readShort();
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully read: {}", response.getHexMessage());
            }
            return response;
        }
        catch (EOFException ex1) {
            throw new Dlt645IOException("Premature end of stream (Message truncated) - %s", ex1.getMessage());
        }
        catch (SocketTimeoutException ex2) {
            throw new Dlt645IOException("Socket timeout reading response - %s", ex2.getMessage());
        }
        catch (Exception ex3) {
            throw new Dlt645IOException("General exception - failed to read - %s", ex3.getMessage());
        }
    }

    private void prepareStreams(Socket socket) throws IOException {
        try {
            if (dataInputStream != null) {
                dataInputStream.close();
            }
            if (dataOutputStream != null) {
                dataOutputStream.close();
            }
        } catch (IOException x) {

        }

        dataInputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        dataOutputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    }

    /**
     * Writes a <tt>Dlt645Message</tt> to the
     * output stream of this <tt>Dlt645Transport</tt>.
     * <p>
     *
     * @param msg           a <tt>Dlt645Message</tt>.
     * @param useRtuOverTcp True if the RTU protocol should be used over TCP
     *
     * @throws Dlt645IOException data cannot be
     *                           written properly to the raw output stream of
     *                           this <tt>Dlt645Transport</tt>.
     */
    void writeMessage(Dlt645Message msg, boolean useRtuOverTcp) throws Dlt645IOException {
        lastActivityTimestamp = System.nanoTime();

        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Sending: {}", msg.getHexMessage());
            }
            byte[] message = msg.getMessage();

            byteOutputStream.reset();
            if (!headless) {
                byteOutputStream.writeShort(msg.getTransactionID());
                //byteOutputStream.writeShort(msg.getProtocolID());
                byteOutputStream.writeShort((message != null ? message.length : 0) + 2);
            }
            //byteOutputStream.writeByte(msg.getUnitID());  // todo
            byteOutputStream.writeByte(msg.getFunctionCode());
            if (message != null && message.length > 0) {
                byteOutputStream.write(message);
            }

            // Add CRC for RTU over TCP
            if (useRtuOverTcp) {
                int len = byteOutputStream.size();
                // TODO
//                int[] crc = Dlt645Util.calculateCRC(byteOutputStream.getBuffer(), 0, len);
//                byteOutputStream.writeByte(crc[0]);
//                byteOutputStream.writeByte(crc[1]);
            }

            dataOutputStream.write(byteOutputStream.toByteArray());
            dataOutputStream.flush();
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully sent: {}", Dlt645Util.toHex(byteOutputStream.toByteArray()));
            }
            // write more sophisticated exception handling
        }
        catch (SocketException ex1) {
            if (master != null && !master.isConnected()) {
                try {
                    master.connect(useRtuOverTcp);
                }
                catch (Exception e) {
                    // Do nothing.
                }
            }
            throw new Dlt645IOException("I/O socket exception - failed to write - %s", ex1.getMessage());
        }
        catch (Exception ex2) {
            throw new Dlt645IOException("General exception - failed to write - %s", ex2.getMessage());
        }
    }
}
