package io.zfunny.j2dlt.dlt645.util;

import io.zfunny.j2dlt.dlt645.Dlt645;
import io.zfunny.j2dlt.dlt645.net.AbstractSerialConnection;

import java.util.Properties;

public class SerialParameters {

    //instance attributes
    private String portName;
    private int baudRate;
    private int flowControlIn;
    private int flowControlOut;
    private int databits;
    private int stopbits;
    private int parity;
    private String encoding;
    private boolean echo;
    private int openDelay;

    public SerialParameters() {
        portName = "";
        baudRate = 9600;
        flowControlIn = AbstractSerialConnection.FLOW_CONTROL_DISABLED;
        flowControlOut = AbstractSerialConnection.FLOW_CONTROL_DISABLED;
        databits = 8;
        stopbits = AbstractSerialConnection.ONE_STOP_BIT;
        parity = AbstractSerialConnection.NO_PARITY;
        encoding = Dlt645.DEFAULT_SERIAL_ENCODING;
        echo = false;
        openDelay = AbstractSerialConnection.OPEN_DELAY;
    }

    /**
     * Constructs a new <tt>SerialParameters</tt> instance with
     * given parameters.
     *
     * @param portName       The name of the port.
     * @param baudRate       The baud rate.
     * @param flowControlIn  Type of flow control for receiving.
     * @param flowControlOut Type of flow control for sending.
     * @param databits       The number of data bits.
     * @param stopbits       The number of stop bits.
     * @param parity         The type of parity.
     * @param echo           Flag for setting the RS485 echo mode.
     */
    public SerialParameters(String portName, int baudRate,
                            int flowControlIn,
                            int flowControlOut,
                            int databits,
                            int stopbits,
                            int parity,
                            boolean echo) {
        this.portName = portName;
        this.baudRate = baudRate;
        this.flowControlIn = flowControlIn;
        this.flowControlOut = flowControlOut;
        this.databits = databits;
        this.stopbits = stopbits;
        this.parity = parity;
        this.echo = echo;
    }

    /**
     * Constructs a new <tt>SerialParameters</tt> instance with
     * parameters obtained from a <tt>Properties</tt> instance.
     *
     * @param props  a <tt>Properties</tt> instance.
     * @param prefix a prefix for the properties keys if embedded into
     *               other properties.
     */
    public SerialParameters(Properties props, String prefix) {
        if (prefix == null) {
            prefix = "";
        }
        setPortName(props.getProperty(prefix + "portName", ""));
        setBaudRate(props.getProperty(prefix + "baudRate", "" + 9600));
        setFlowControlIn(props.getProperty(prefix + "flowControlIn", "" + AbstractSerialConnection.FLOW_CONTROL_DISABLED));
        setFlowControlOut(props.getProperty(prefix + "flowControlOut", "" + AbstractSerialConnection.FLOW_CONTROL_DISABLED));
        setParity(props.getProperty(prefix + "parity", "" + AbstractSerialConnection.NO_PARITY));
        setDatabits(props.getProperty(prefix + "databits", "8"));
        setStopbits(props.getProperty(prefix + "stopbits", "" + AbstractSerialConnection.ONE_STOP_BIT));
        setEncoding(props.getProperty(prefix + "encoding", Dlt645.DEFAULT_SERIAL_ENCODING));
        setEcho("true".equals(props.getProperty(prefix + "echo")));
        setOpenDelay(props.getProperty(prefix + "openDelay", "" + AbstractSerialConnection.OPEN_DELAY));
    }

    /**
     * Returns the port name.
     *
     * @return the port name.
     */
    public String getPortName() {
        return portName;
    }

    /**
     * Sets the port name.
     *
     * @param name the new port name.
     */
    public void setPortName(String name) {
        portName = name;
    }

    /**
     * Sets the baud rate.
     *
     * @param rate the new baud rate.
     */
    public void setBaudRate(int rate) {
        baudRate = rate;
    }

    /**
     * Return the baud rate as <tt>int</tt>.
     *
     * @return the baud rate as <tt>int</tt>.
     */
    public int getBaudRate() {
        return baudRate;
    }

    /**
     * Sets the baud rate.
     *
     * @param rate the new baud rate.
     */
    public void setBaudRate(String rate) {
        baudRate = Integer.parseInt(rate);
    }

    /**
     * Returns the baud rate as a <tt>String</tt>.
     *
     * @return the baud rate as <tt>String</tt>.
     */
    public String getBaudRateString() {
        return Integer.toString(baudRate);
    }

    /**
     * Sets the type of flow control for the input
     * as given by the passed in <tt>int</tt>.
     *
     * @param flowcontrol the new flow control type.
     */
    public void setFlowControlIn(int flowcontrol) {
        flowControlIn = flowcontrol;
    }

    /**
     * Returns the input flow control type as <tt>int</tt>.
     *
     * @return the input flow control type as <tt>int</tt>.
     */
    public int getFlowControlIn() {
        return flowControlIn;
    }

    /**
     * Sets the type of flow control for the input
     * as given by the passed in <tt>String</tt>.
     *
     * @param flowcontrol the flow control for reading type.
     */
    public void setFlowControlIn(String flowcontrol) {
        flowControlIn = stringToFlow(flowcontrol);
    }

    /**
     * Returns the input flow control type as <tt>String</tt>.
     *
     * @return the input flow control type as <tt>String</tt>.
     */
    public String getFlowControlInString() {
        return flowToString(flowControlIn);
    }

    /**
     * Sets the output flow control type as given
     * by the passed in <tt>int</tt>.
     *
     * @param flowControlOut new output flow control type as <tt>int</tt>.
     */
    public void setFlowControlOut(int flowControlOut) {
        this.flowControlOut = flowControlOut;
    }

    /**
     * Returns the output flow control type as <tt>int</tt>.
     *
     * @return the output flow control type as <tt>int</tt>.
     */
    public int getFlowControlOut() {
        return flowControlOut;
    }

    /**
     * Sets the output flow control type as given
     * by the passed in <tt>String</tt>.
     *
     * @param flowControlOut the new output flow control type as <tt>String</tt>.
     */
    public void setFlowControlOut(String flowControlOut) {
        this.flowControlOut = stringToFlow(flowControlOut);
    }

    /**
     * Returns the output flow control type as <tt>String</tt>.
     *
     * @return the output flow control type as <tt>String</tt>.
     */
    public String getFlowControlOutString() {
        return flowToString(flowControlOut);
    }

    /**
     * Sets the number of data bits.
     *
     * @param databits the new number of data bits.
     */
    public void setDatabits(int databits) {
        this.databits = databits;
    }

    /**
     * Returns the number of data bits as <tt>int</tt>.
     *
     * @return the number of data bits as <tt>int</tt>.
     */
    public int getDatabits() {
        return databits;
    }

    public void setDatabits(String databits) {
        if (!Dlt645Util.isBlank(databits) && databits.matches("[0-9]+")) {
            this.databits = Integer.parseInt(databits);
        } else {
            this.databits = 8;
        }
    }

    /**
     * Returns the number of data bits as <tt>String</tt>.
     *
     * @return the number of data bits as <tt>String</tt>.
     */
    public String getDatabitsString() {
        return databits + "";
    }

    /**
     * Sets the number of stop bits.
     *
     * @param stopbits the new number of stop bits setting.
     */
    public void setStopbits(int stopbits) {
        this.stopbits = stopbits;
    }

    /**
     * Returns the number of stop bits as <tt>int</tt>.
     *
     * @return the number of stop bits as <tt>int</tt>.
     */
    public int getStopbits() {
        return stopbits;
    }

    public void setStopbits(String stopbits) {
        if (Dlt645Util.isBlank(stopbits) || stopbits.equals("1")) {
            this.stopbits = AbstractSerialConnection.ONE_STOP_BIT;
        } else if (stopbits.equals("1.5")) {
            this.stopbits = AbstractSerialConnection.ONE_POINT_FIVE_STOP_BITS;
        } else if (stopbits.equals("2")) {
            this.stopbits = AbstractSerialConnection.TWO_STOP_BITS;
        }
    }

    public String getStopbitsString() {
        switch (stopbits) {
            case AbstractSerialConnection.ONE_STOP_BIT:
                return "1";
            case AbstractSerialConnection.ONE_POINT_FIVE_STOP_BITS:
                return "1.5";
            case AbstractSerialConnection.TWO_STOP_BITS:
                return "2";
            default:
                return "1";
        }
    }

    /**
     * Sets the parity schema.
     *
     * @param parity the new parity schema as <tt>int</tt>.
     */
    public void setParity(int parity) {
        this.parity = parity;
    }

    /**
     * Returns the parity schema as <tt>int</tt>.
     *
     * @return the parity schema as <tt>int</tt>.
     */
    public int getParity() {
        return parity;
    }

    public void setParity(String parity) {
        if (Dlt645Util.isBlank(parity) || parity.equalsIgnoreCase("none")) {
            this.parity = AbstractSerialConnection.NO_PARITY;
        }
        else if (parity.equalsIgnoreCase("even")) {
            this.parity = AbstractSerialConnection.EVEN_PARITY;
        }
        else if (parity.equalsIgnoreCase("odd")) {
            this.parity = AbstractSerialConnection.ODD_PARITY;
        }
        else if (parity.equalsIgnoreCase("mark")) {
            this.parity = AbstractSerialConnection.MARK_PARITY;
        }
        else if (parity.equalsIgnoreCase("space")) {
            this.parity = AbstractSerialConnection.SPACE_PARITY;
        }
        else {
            this.parity = AbstractSerialConnection.NO_PARITY;
        }
    }

    /**
     * Returns the parity schema as <tt>String</tt>.
     *
     * @return the parity schema as <tt>String</tt>.
     */
    public String getParityString() {
        switch (parity) {
            case AbstractSerialConnection.NO_PARITY:
                return "none";
            case AbstractSerialConnection.EVEN_PARITY:
                return "even";
            case AbstractSerialConnection.ODD_PARITY:
                return "odd";
            case AbstractSerialConnection.MARK_PARITY:
                return "mark";
            case AbstractSerialConnection.SPACE_PARITY:
                return "space";
            default:
                return "none";
        }
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String enc) {
        if (!Dlt645Util.isBlank(enc) &&
                (enc.equalsIgnoreCase(Dlt645.SERIAL_ENCODING_ASCII) || enc.equalsIgnoreCase(Dlt645.SERIAL_ENCODING_RTU))) {
            encoding = enc;
        }
        else {
            encoding = Dlt645.DEFAULT_SERIAL_ENCODING;
        }
    }

    public boolean isEcho() {
        return echo;
    }

    public void setEcho(boolean newEcho) {
        echo = newEcho;
    }

    private int stringToFlow(String flowcontrol) {
        if (Dlt645Util.isBlank(flowcontrol) || flowcontrol.equalsIgnoreCase("none")) {
            return AbstractSerialConnection.FLOW_CONTROL_DISABLED;
        }
        else if (flowcontrol.equalsIgnoreCase("xon/xoff out")) {
            return AbstractSerialConnection.FLOW_CONTROL_XONXOFF_OUT_ENABLED;
        }
        else if (flowcontrol.equalsIgnoreCase("xon/xoff in")) {
            return AbstractSerialConnection.FLOW_CONTROL_XONXOFF_IN_ENABLED;
        }
        else if (flowcontrol.equalsIgnoreCase("rts/cts")) {
            return AbstractSerialConnection.FLOW_CONTROL_CTS_ENABLED | AbstractSerialConnection.FLOW_CONTROL_RTS_ENABLED;
        }
        else if (flowcontrol.equalsIgnoreCase("dsr/dtr")) {
            return AbstractSerialConnection.FLOW_CONTROL_DSR_ENABLED | AbstractSerialConnection.FLOW_CONTROL_DTR_ENABLED;
        }
        return AbstractSerialConnection.FLOW_CONTROL_DISABLED;
    }

    private String flowToString(int flowcontrol) {
        switch (flowcontrol) {
            case AbstractSerialConnection.FLOW_CONTROL_DISABLED:
                return "none";
            case AbstractSerialConnection.FLOW_CONTROL_XONXOFF_OUT_ENABLED:
                return "xon/xoff out";
            case AbstractSerialConnection.FLOW_CONTROL_XONXOFF_IN_ENABLED:
                return "xon/xoff in";
            case AbstractSerialConnection.FLOW_CONTROL_CTS_ENABLED:
                return "rts/cts";
            case AbstractSerialConnection.FLOW_CONTROL_DTR_ENABLED:
                return "dsr/dtr";
            default:
                return "none";
        }
    }

    public int getOpenDelay() {
        return openDelay;
    }

    public void setOpenDelay(int openDelay) {
        this.openDelay = openDelay;
    }

    public void setOpenDelay(String openDelay) {
        this.openDelay = Integer.parseInt(openDelay);
    }

    @Override
    public String toString() {
        return "SerialParameters{" +
                "portName='" + portName + '\'' +
                ", baudRate=" + baudRate +
                ", flowControlIn=" + flowControlIn +
                ", flowControlOut=" + flowControlOut +
                ", databits=" + databits +
                ", stopbits=" + stopbits +
                ", parity=" + parity +
                ", encoding='" + encoding + '\'' +
                ", echo=" + echo +
                ", openDelay=" + openDelay +
                '}';
    }
}
