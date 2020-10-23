package io.zfunny.j2dlt.dlt645.io;

import com.fazecast.jSerialComm.SerialPort;
import io.zfunny.j2dlt.dlt645.Dlt645;
import io.zfunny.j2dlt.dlt645.Dlt645IOException;
import io.zfunny.j2dlt.dlt645.msg.Dlt645Message;
import io.zfunny.j2dlt.dlt645.msg.Dlt645Request;
import io.zfunny.j2dlt.dlt645.msg.Dlt645Response;
import io.zfunny.j2dlt.dlt645.net.AbstractDlt645Listener;
import io.zfunny.j2dlt.dlt645.net.AbstractSerialConnection;
import io.zfunny.j2dlt.dlt645.util.Dlt645Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class Dlt645SerialTransport extends AbstractDlt645Transport {

    private static final Logger logger = LoggerFactory.getLogger(Dlt645SerialTransport.class);

    /**
     * Defines a virtual number for the FRAME START token (COLON).
     */
    static final int FRAME_START = 1000;
    /**
     * Defines a virtual number for the FRAME_END token (CR LF).
     */
    static final int FRAME_END = 2000;


    private static final int NS_IN_A_MS = 1000000;
    private static final String CANNOT_READ_FROM_SERIAL_PORT = "Cannot read from serial port";
    private static final String COMM_PORT_IS_NOT_VALID_OR_NOT_OPEN = "Comm port is not valid or not open";
    private AbstractSerialConnection commPort;
    boolean echo = false;
    private final Set<AbstractSerialTransportListener> listeners = Collections.synchronizedSet(new HashSet<AbstractSerialTransportListener>());

    @Override
    public Dlt645Transaction createTransaction() {
        Dlt645SerialTransaction transaction = new Dlt645SerialTransaction();
        transaction.setTransport(this);
        return transaction;
    }

    @Override
    public void writeResponse(Dlt645Response msg) throws Dlt645IOException {
        if (msg.getAuxiliaryType().equals(Dlt645Response.AuxiliaryMessageTypes.UNIT_ID_MISSMATCH)) {
            logger.debug("Ignoring response not meant for us");
        } else {
            // We need to pause before sending the response
            waitBetweenFrames();

            // Send the response
            writeMessage(msg);
        }
    }

    @Override
    public void writeRequest(Dlt645Request msg) throws Dlt645IOException {
        writeMessage(msg);
    }

    private void writeMessage(Dlt645Message msg) throws Dlt645IOException {
        open();
        notifyListenersBeforeWrite(msg);
        try {
            writeMessageOut(msg);
            long startTime = System.nanoTime();

            // Wait here for the message to have been sent

            double bytesPerSec = ((double)commPort.getBaudRate()) / (((commPort.getNumDataBits() == 0) ? 8 : commPort.getNumDataBits()) + ((commPort.getNumStopBits() == 0) ? 1 : commPort.getNumStopBits()) + ((commPort.getParity() == SerialPort.NO_PARITY) ? 0 : 1));
            double delay = 1000000000.0 * msg.getOutputLength() / bytesPerSec;
            double delayMilliSeconds = Math.floor(delay / 1000000);
            double delayNanoSeconds = delay % 1000000;
            try {

                // For delays less than a millisecond, we need to chew CPU cycles unfortunately
                // There are some fiddle factors here to allow for some oddities in the hardware

                if (delayMilliSeconds == 0.0) {
                    int priority = Thread.currentThread().getPriority();
                    Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
                    long end = startTime + ((int) (delayNanoSeconds * 1.3));
                    while (System.nanoTime() < end) {
                        // noop
                    }
                    Thread.currentThread().setPriority(priority);
                }
                else {
                    Thread.sleep((int) (delayMilliSeconds * 1.7), (int) (delayNanoSeconds * 1.5));
                }
            }
            catch (Exception e) {
                logger.debug("nothing to do");
            }
        }
        finally {
            notifyListenersAfterWrite(msg);
        }
    }

    @Override
    public Dlt645Request readRequest(AbstractDlt645Listener listener) throws Dlt645IOException {
        open();
        notifyListenersBeforeRequest();
        Dlt645Request req = readRequestIn(listener);
        notifyListenersAfterRequest(req);
        return req;
    }

    @Override
    public Dlt645Response readResponse() throws Dlt645IOException {
        notifyListenersBeforeResponse();
        Dlt645Response res = readResponseIn();
        notifyListenersAfterResponse(res);
        return res;
    }

    private void open() throws Dlt645IOException {
        if (commPort != null && !commPort.isOpen()) {
            setTimeout(timeout);
            try {
                commPort.open();
            } catch (IOException e) {
                throw new Dlt645IOException(String.format("Cannot open port %s - %s", commPort.getDescriptivePortName(), e.getMessage()));
            }
        }
    }

    @Override
    public void setTimeout(int time) {
        super.setTimeout(time);
        if (commPort != null) {
            commPort.setComPortTimeouts(AbstractSerialConnection.TIMEOUT_READ_BLOCKING, timeout, timeout);
        }
    }

    protected abstract void writeMessageOut(Dlt645Message msg) throws Dlt645IOException;

    protected abstract Dlt645Request readRequestIn(AbstractDlt645Listener listener) throws Dlt645IOException;

    protected abstract Dlt645Response readResponseIn() throws Dlt645IOException;

    public void addListener(AbstractSerialTransportListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void removeListener(AbstractSerialTransportListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    public void clearListeners() {
        listeners.clear();
    }

    private void notifyListenersBeforeRequest() {
        synchronized (listeners) {
            for (AbstractSerialTransportListener listener : listeners) {
                listener.beforeRequestRead(commPort);
            }
        }
    }

    private void notifyListenersAfterRequest(Dlt645Request req) {
        synchronized (listeners) {
            for (AbstractSerialTransportListener listener : listeners) {
                listener.afterRequestRead(commPort, req);
            }
        }
    }

    private void notifyListenersBeforeResponse() {
        synchronized (listeners) {
            for (AbstractSerialTransportListener listener : listeners) {
                listener.beforeResponseRead(commPort);
            }
        }
    }

    private void notifyListenersAfterResponse(Dlt645Response res) {
        synchronized (listeners) {
            for (AbstractSerialTransportListener listener : listeners) {
                listener.afterResponseRead(commPort, res);
            }
        }
    }

    private void notifyListenersBeforeWrite(Dlt645Message msg) {
        synchronized (listeners) {
            for (AbstractSerialTransportListener listener : listeners) {
                listener.beforeMessageWrite(commPort, msg);
            }
        }
    }

    private void notifyListenersAfterWrite(Dlt645Message msg) {
        synchronized (listeners) {
            for (AbstractSerialTransportListener listener : listeners) {
                listener.afterMessageWrite(commPort, msg);
            }
        }
    }

    public void setCommPort(AbstractSerialConnection cp) throws IOException {
        commPort = cp;
        setTimeout(timeout);
    }

    public AbstractSerialConnection getCommPort() {
        return commPort;
    }

    public boolean isEcho() {
        return echo;
    }

    public void setEcho(boolean b) {
        this.echo = b;
    }

    protected void readEcho(int len) throws IOException {
        byte[] echoBuf = new byte[len];
        int echoLen = commPort.readBytes(echoBuf, len);
        if (logger.isDebugEnabled()) {
            logger.debug("Echo: {}", Dlt645Util.toHex(echoBuf, 0, echoLen));
        }
        if (echoLen != len) {
            logger.debug("Error: Transmit echo not received");
            throw new IOException("Echo not received");
        }
    }

    protected int availableBytes() {
        return commPort.bytesAvailable();
    }

    protected int readByte() throws IOException {
        if (commPort != null && commPort.isOpen()) {
            byte[] buffer = new byte[1];
            int cnt = commPort.readBytes(buffer, 1);
            if (cnt != 1) {
                throw new IOException(CANNOT_READ_FROM_SERIAL_PORT);
            }
            else {
                return buffer[0] & 0xff;
            }
        }
        else {
            throw new IOException(COMM_PORT_IS_NOT_VALID_OR_NOT_OPEN);
        }
    }

    void readBytes(byte[] buffer, long bytesToRead) throws IOException {
        if (commPort != null && commPort.isOpen()) {
            int cnt = commPort.readBytes(buffer, bytesToRead);
            if (cnt != bytesToRead) {
                throw new IOException("Cannot read from serial port - truncated");
            }
        }
        else {
            throw new IOException(COMM_PORT_IS_NOT_VALID_OR_NOT_OPEN);
        }
    }

    final int writeBytes(byte[] buffer, long bytesToWrite) throws IOException {
        if (commPort != null && commPort.isOpen()) {
            return commPort.writeBytes(buffer, bytesToWrite);
        }
        else {
            throw new IOException(COMM_PORT_IS_NOT_VALID_OR_NOT_OPEN);
        }
    }

    int readAsciiByte() throws IOException {
        if (commPort != null && commPort.isOpen()) {
            byte[] buffer = new byte[1];
            int cnt = commPort.readBytes(buffer, 1);
            if (cnt != 1) {
                throw new IOException(CANNOT_READ_FROM_SERIAL_PORT);
            }
            else if (buffer[0] == ':') {
                return FRAME_START;
            }
            else if (buffer[0] == '\r' || buffer[0] == '\n') {
                return FRAME_END;
            }
            else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Read From buffer: {} ({})", buffer[0], String.format("%02X", buffer[0]));
                }
                byte firstValue = buffer[0];
                cnt = commPort.readBytes(buffer, 1);
                if (cnt != 1) {
                    throw new IOException(CANNOT_READ_FROM_SERIAL_PORT);
                }
                else {
                    int combinedValue = (Character.digit(firstValue, 16) << 4) + Character.digit(buffer[0], 16);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Returning combined value of: {}", String.format("%02X", combinedValue));
                    }
                    return combinedValue;
                }
            }
        }
        else {
            throw new IOException(COMM_PORT_IS_NOT_VALID_OR_NOT_OPEN);
        }
    }

    final int writeAsciiByte(int value) throws IOException {
        if (commPort != null && commPort.isOpen()) {
            byte[] buffer;

            if (value == FRAME_START) {
                buffer = new byte[]{58};
                logger.debug("Wrote FRAME_START");
            }
            else if (value == FRAME_END) {
                buffer = new byte[]{13, 10};
                logger.debug("Wrote FRAME_END");
            }
            else {
                buffer = Dlt645Util.toHex(value);
                if (logger.isDebugEnabled()) {
                    logger.debug("Wrote byte {}={}", value, Dlt645Util.toHex(value));
                }
            }
            if (buffer != null) {
                return commPort.writeBytes(buffer, buffer.length);
            }
            else {
                throw new IOException("Message to send is empty");
            }
        }
        else {
            throw new IOException(COMM_PORT_IS_NOT_VALID_OR_NOT_OPEN);
        }
    }

    int writeAsciiBytes(byte[] buffer, long bytesToWrite) throws IOException {
        if (commPort != null && commPort.isOpen()) {
            int cnt = 0;
            for (int i = 0; i < bytesToWrite; i++) {
                if (writeAsciiByte(buffer[i]) != 2) {
                    return cnt;
                }
                cnt++;
            }
            return cnt;
        }
        else {
            throw new IOException(COMM_PORT_IS_NOT_VALID_OR_NOT_OPEN);
        }
    }

    void clearInput() throws IOException {
        if (commPort.bytesAvailable() > 0) {
            int len = commPort.bytesAvailable();
            byte[] buf = new byte[len];
            readBytes(buf, len);
            if (logger.isDebugEnabled()) {
                logger.debug("Clear input: {}", Dlt645Util.toHex(buf, 0, len));
            }
        }
    }

    @Override
    public void close() throws IOException {
        commPort.close();
    }

    /**
     * Injects a delay dependent on the baud rate
     */
    private void waitBetweenFrames() {
        waitBetweenFrames(0, 0);
    }

    /**
     * Injects a delay dependent on the last time we received a response or
     * if a fixed delay has been specified
     *
     * @param transDelayMS             Fixed transaction delay (milliseconds)
     * @param lastTransactionTimestamp Timestamp of last transaction
     */
    void waitBetweenFrames(int transDelayMS, long lastTransactionTimestamp) {

        // If a fixed delay has been set
        if (transDelayMS > 0) {
            Dlt645Util.sleep(transDelayMS);
        }
        else {
            // Make use we have a gap of 3.5 characters between adjacent requests
            // We have to do the calculations here because it is possible that the caller may have changed
            // the connection characteristics if they provided the connection instance
            int delay = getInterFrameDelay() / 1000;

            // How long since the last message we received
            long gapSinceLastMessage = (System.nanoTime() - lastTransactionTimestamp) / NS_IN_A_MS;
            if (delay > gapSinceLastMessage) {
                long sleepTime = delay - gapSinceLastMessage;

                Dlt645Util.sleep(sleepTime);

                if (logger.isDebugEnabled()) {
                    logger.debug("Waited between frames for {} ms", sleepTime);
                }
            }
        }
    }

    /**
     * In microseconds
     *
     * @return Delay between frames
     */
    int getInterFrameDelay() {
        if (commPort.getBaudRate() > 19200) {
            return 1750;
        }
        else {
            return Math.max(getCharInterval(Dlt645.INTER_MESSAGE_GAP), Dlt645.MINIMUM_TRANSMIT_DELAY);
        }
    }

    /**
     * The maximum delay between characters in microseconds
     *
     * @return microseconds
     */
    long getMaxCharDelay() {
        if (commPort.getBaudRate() > 19200) {
            return 1750;
        }
        else {
            return getCharIntervalMicro(Dlt645.INTER_CHARACTER_GAP);
        }
    }

    /**
     * Calculates an interval based on a set number of characters.
     * Used for message timings.
     *
     * @param chars Number of characters
     * @return char interval in milliseconds
     */
    int getCharInterval(double chars) {
        return (int) (getCharIntervalMicro(chars) / 1000);
    }

    /**
     * Calculates an interval based on a set number of characters.
     * Used for message timings.
     *
     * @param chars Number of caracters
     * @return microseconds
     */
    long getCharIntervalMicro(double chars) {
        // Make use we have a gap of 3.5 characters between adjacent requests
        // We have to do the calculations here because it is possible that the caller may have changed
        // the connection characteristics if they provided the connection instance
        return (long) chars * NS_IN_A_MS * (1 + commPort.getNumDataBits() + commPort.getNumStopBits() + (commPort.getParity() == AbstractSerialConnection.NO_PARITY ? 0 : 1)) / commPort.getBaudRate();
    }

    /**
     * Spins until the timeout or the condition is met.
     * This method will repeatedly poll the available bytes, so it should not have any side effects.
     *
     * @param waitTimeMicroSec The time to wait for the condition to be true in microseconds
     * @return true if the condition ended the spin, false if the tim
     */
    boolean spinUntilBytesAvailable(long waitTimeMicroSec) {
        long start = System.nanoTime();
        while (availableBytes() < 1) {
            long delta = System.nanoTime() - start;
            if (delta > waitTimeMicroSec * 1000) {
                return false;
            }
        }
        return true;
    }

}
