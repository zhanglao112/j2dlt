package io.zfunny.j2dlt.dlt645;

public interface Dlt645 {


    int READ_DATA = 17;

    int READ_SUB_DATA = 18;

    int WRITE_DATA = 20;

    int CONTROL = 28;

    byte[] A_VALTAGE_DATA_IDENTITY = {(byte)0x00,(byte)0x01,(byte)0x01,(byte)0x02};


    byte ADD_PARAM = 0x33;
    byte[] wakeBytes = {(byte) 0xfe, (byte) 0xfe, (byte) 0xfe, (byte) 0xfe};
    byte startByte = 0x68;
    byte endByte = 0x16;

    byte[][] dataIdentity = {
            new byte[]{(byte)0x00, (byte)0xff, (byte)0x01, (byte)0x02},
            new byte[]{(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00},
            new byte[]{(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00},
            new byte[]{(byte)0x00,(byte)0x01,(byte)0x01,(byte)0x02},
            new byte[]{(byte)0x00,(byte)0x02,(byte)0x01,(byte)0x02},
            new byte[]{(byte)0x00,(byte)0x03,(byte)0x01,(byte)0x02},
            new byte[]{(byte)0x00,(byte)0x01,(byte)0x02,(byte)0x02},
            new byte[]{(byte)0x00,(byte)0x02,(byte)0x02,(byte)0x02},
            new byte[]{(byte)0x00,(byte)0x03,(byte)0x02,(byte)0x02}
    };


    /**
     * Defines the maximum number of bits in multiple read/write
     * of input discretes or coils (<b>2000</b>).
     */
    int MAX_BITS = 2000;

    /**
     * Defines the Modbus slave exception offset that is added to the
     * function code, to flag an exception.
     */
    int EXCEPTION_OFFSET = 128;            //the last valid function code is 127

    /**
     * Defines the Modbus slave exception type <tt>illegal function</tt>.
     * This exception code is returned if the slave:
     * <ul>
     * <li>does not implement the function code <b>or</b></li>
     * <li>is not in a state that allows it to process the function</li>
     * </ul>
     */
    int ILLEGAL_FUNCTION_EXCEPTION = 1;

    /**
     * Defines the Modbus slave exception type <tt>illegal data address</tt>.
     * This exception code is returned if the reference:
     * <ul>
     * <li>does not exist on the slave <b>or</b></li>
     * <li>the combination of reference and length exceeds the bounds
     * of the existing registers.
     * </li>
     * </ul>
     */
    int ILLEGAL_ADDRESS_EXCEPTION = 2;

    /**
     * Defines the Modbus slave exception type <tt>illegal data value</tt>.
     * This exception code indicates a fault in the structure of the data values
     * of a complex request, such as an incorrect implied length.<br>
     * <b>This code does not indicate a problem with application specific validity
     * of the value.</b>
     */
    int ILLEGAL_VALUE_EXCEPTION = 3;

    /**
     * Defines the Modbus slave exception type <tt>slave device failure</tt>.
     * This exception code indicates a fault in the slave device itself.
     */
    int SLAVE_DEVICE_FAILURE = 4;

    /**
     * Defines the Modbus slave exception type <tt>slave busy</tt>.  This
     * exception indicates the the slave is unable to perform the operation
     * because it is performing an operation which cannot be interrupted.
     */
    int SLAVE_BUSY_EXCEPTION = 6;

    /**
     * Defines the Modbus slave exception type <tt>negative acknowledgment</tt>.
     * This exception code indicates the slave cannot perform the requested
     * action.
     */
    int NEGATIVE_ACKNOWLEDGEMENT = 7;

    /**
     * Defines the Modbus slave exception type <tt>Gateway target failed to
     * respond</tt>.  This exception code indicates that a Modbus gateway
     * failed to receive a response from the specified target.
     */
    int GATEWAY_TARGET_NO_RESPONSE = 11;

    /**
     * Defines the default port number of Modbus
     * (=<tt>502</tt>).
     */
    int DEFAULT_PORT = 502;

    /**
     * Defines the maximum message length in bytes
     * (=<tt>256</tt>).
     */
    int MAX_MESSAGE_LENGTH = 256;

    /**
     * Defines the default transaction identifier (=<tt>0</tt>).
     */
    int DEFAULT_TRANSACTION_ID = 0;

    /**
     * Defines the default protocol identifier (=<tt>0</tt>).
     */
    int DEFAULT_PROTOCOL_ID = 0;

    /**
     * Defines the default unit identifier (=<tt>0</tt>).
     */
    int DEFAULT_UNIT_ID = 0;

    /**
     * Defines the default setting for validity checking
     * in transactions (=<tt>true</tt>).
     */
    boolean DEFAULT_VALIDITYCHECK = true;

    /**
     * Defines the default setting for I/O operation timeouts
     * in milliseconds (=<tt>3000</tt>).
     */
    int DEFAULT_TIMEOUT = 3000;

    /**
     * Defines the sleep period between transaction retries
     * in milliseconds (=<tt>200</tt>).
     */
    int RETRY_SLEEP_TIME = 500;

    /**
     * Defines the default reconnecting setting for
     * transactions (=<tt>false</tt>).
     */
    boolean DEFAULT_RECONNECTING = false;

    /**
     * Defines the default amount of retires for opening
     * a connection (=<tt>3</tt>).
     */
    int DEFAULT_RETRIES = 5;

    /**
     * Defines the default number of msec to delay before transmission<br>
     * Inter-message delays are managed by the SerialTransaction object automatically based on the
     * baud rate. Setting this value to anything other than zero will bypass that process and force
     * a specific inter-message delay
     * (=<tt>0</tt>).
     */
    int DEFAULT_TRANSMIT_DELAY = 0;

    /**
     * Defines the default number of msec to delay before transmission if not overridden by DEFAULT_TRANSMIT_DELAY
     * (=<tt>2</tt>).
     */
    int MINIMUM_TRANSMIT_DELAY = 2;

    /**
     * The number of characters delay that must be maintained between adjacent requests on
     * the same serial port (within the same transaction)
     */
    double INTER_MESSAGE_GAP = 4;

    /**
     * The number of characters delay that is the allowed maximum between characters on
     * the same serial port (within the same transaction)
     */
    double INTER_CHARACTER_GAP = 1.5;

    /**
     * Defines the maximum value of the transaction identifier.
     *
     * <p><b>Note:</b> The standard requires that the server copy whatever
     * value the client provides. However, the transaction ID is being
     * limited to signed 16-bit integers to prevent problems with servers
     * that might incorrectly assume the value is a signed value.
     */
    int MAX_TRANSACTION_ID = Short.MAX_VALUE;

    /**
     * Defines the serial encoding "ASCII".
     */
    String SERIAL_ENCODING_ASCII = "ascii";

    /**
     * Defines the serial encoding "RTU".
     */
    String SERIAL_ENCODING_RTU = "rtu";

    /**
     * Defines the default serial encoding (ASCII).
     */
    String DEFAULT_SERIAL_ENCODING = SERIAL_ENCODING_ASCII;
}
