package io.zfunny.j2dlt.dlt645.net;

import com.fazecast.jSerialComm.SerialPort;
import io.zfunny.j2dlt.dlt645.io.AbstractDlt645Transport;

import java.io.IOException;
import java.util.Set;

public abstract class AbstractSerialConnection {

    /**
     * Parity values
     */
    public static final int NO_PARITY = SerialPort.NO_PARITY;
    public static final int ODD_PARITY = SerialPort.ODD_PARITY;
    public static final int EVEN_PARITY = SerialPort.EVEN_PARITY;
    public static final int MARK_PARITY = SerialPort.MARK_PARITY;
    public static final int SPACE_PARITY = SerialPort.SPACE_PARITY;

    /**
     * Stop bits values
     */
    public static final int ONE_STOP_BIT = SerialPort.ONE_STOP_BIT;
    public static final int ONE_POINT_FIVE_STOP_BITS = SerialPort.ONE_POINT_FIVE_STOP_BITS;
    public static final int TWO_STOP_BITS = SerialPort.TWO_STOP_BITS;

    /**
     * Flow control values
     */
    public static final int FLOW_CONTROL_DISABLED = SerialPort.FLOW_CONTROL_DISABLED;
    public static final int FLOW_CONTROL_RTS_ENABLED = SerialPort.FLOW_CONTROL_RTS_ENABLED;
    public static final int FLOW_CONTROL_CTS_ENABLED = SerialPort.FLOW_CONTROL_CTS_ENABLED;
    public static final int FLOW_CONTROL_DSR_ENABLED = SerialPort.FLOW_CONTROL_DSR_ENABLED;
    public static final int FLOW_CONTROL_DTR_ENABLED = SerialPort.FLOW_CONTROL_DTR_ENABLED;
    public static final int FLOW_CONTROL_XONXOFF_IN_ENABLED = SerialPort.FLOW_CONTROL_XONXOFF_IN_ENABLED;
    public static final int FLOW_CONTROL_XONXOFF_OUT_ENABLED = SerialPort.FLOW_CONTROL_XONXOFF_OUT_ENABLED;

    /**
     * Open delay (msec)
     */
    public static final int OPEN_DELAY = 0;

    /**
     * Timeout
     */
    public static final int TIMEOUT_NONBLOCKING = SerialPort.TIMEOUT_NONBLOCKING;
    public static final int TIMEOUT_READ_SEMI_BLOCKING = SerialPort.TIMEOUT_READ_SEMI_BLOCKING;
    public static final int TIMEOUT_READ_BLOCKING = SerialPort.TIMEOUT_READ_BLOCKING;
    public static final int TIMEOUT_WRITE_BLOCKING = SerialPort.TIMEOUT_WRITE_BLOCKING;
    public static final int TIMEOUT_SCANNER = SerialPort.TIMEOUT_SCANNER;

    public abstract void open() throws IOException;

    public abstract AbstractDlt645Transport getDlt645Transport();

    public abstract int readBytes(byte[] buffer, long bytesToRead);

    public abstract int writeBytes(byte[] buffer, long bytesToWrite);

    /**
     * Bytes available to read
     *
     * @return number of bytes currently available to read
     */
    public abstract int bytesAvailable();

    /**
     * Close the port and clean up associated elements.
     */
    public abstract void close();

    /**
     * Returns current baud rate
     *
     * @return Baud rate
     */
    public abstract int getBaudRate();

    /**
     * Returns current data bits value
     *
     * @return Number of data bits
     */
    public abstract int getNumDataBits();

    /**
     * Returns current stop bits
     *
     * @return Number of stop bits
     */
    public abstract int getNumStopBits();

    /**
     * Returns current parity
     *
     * @return Parity type
     */
    public abstract int getParity();

    /**
     * Returns a name of the port
     *
     * @return a <tt>String</tt> instance.
     */
    public abstract String getPortName();

    /**
     * Returns a descriptive name of the  port
     *
     * @return a <tt>String</tt> instance.
     */
    public abstract String getDescriptivePortName();

    /**
     * Set port timeouts
     *
     * @param newTimeoutMode  Timeout mode
     * @param newReadTimeout  Read timeout
     * @param newWriteTimeout Write timeout
     */
    public abstract void setComPortTimeouts(int newTimeoutMode, int newReadTimeout, int newWriteTimeout);

    /**
     * Reports the open status of the port.
     *
     * @return true if port is open, false if port is closed.
     */
    public abstract boolean isOpen();

    /**
     * Returns the timeout for this <tt>UDPMasterConnection</tt>.
     *
     * @return the timeout as <tt>int</tt>.
     */
    public abstract int getTimeout();

    /**
     * Sets the timeout for this <tt>UDPMasterConnection</tt>.
     *
     * @param timeout the timeout as <tt>int</tt>.
     */
    public abstract void setTimeout(int timeout);

    /**
     * Returns a set of all the available comm port names
     *
     * @return Set of comm port names
     */
    public abstract Set<String> getCommPorts();







}
