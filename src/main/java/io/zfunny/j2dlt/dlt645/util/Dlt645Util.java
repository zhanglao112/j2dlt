package io.zfunny.j2dlt.dlt645.util;

import io.zfunny.j2dlt.dlt645.Dlt645;
import io.zfunny.j2dlt.dlt645.io.BytesOutputStream;
import io.zfunny.j2dlt.dlt645.msg.Dlt645Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class Dlt645Util {
    private static final Logger logger = LoggerFactory.getLogger(Dlt645Util.class);

    private Dlt645Util() {}

    public static String toHex(Dlt645Message msg) {
        BytesOutputStream byteOutputStream = new BytesOutputStream(Dlt645.MAX_MESSAGE_LENGTH);
        String ret = "-1";
        try {
            msg.writeTo(byteOutputStream);
            ret = toHex(byteOutputStream.getBuffer(), 0, byteOutputStream.size());
        } catch (IOException ex) {
            logger.debug("Hex conversion error {}", ex);
        }
        return ret;
    }

    public static String toHex(byte[] data) {
        return toHex(data, 0, data.length);
    }

    public static String toHex(byte[] data, int off, int end) {
        //double size, two bytes (hex range) for one byte
        StringBuilder buf = new StringBuilder(data.length * 2);
        if (end > data.length) {
            end = data.length;
        }
        for (int i = off; i < end; i++) {
            //don't forget the second hex digit
            if (((int)data[i] & 0xff) < 0x10) {
                buf.append("0");
            }
            buf.append(Long.toString((int)data[i] & 0xff, 16).toUpperCase());
            if (i < end - 1) {
                buf.append(" ");
            }
        }
        return buf.toString();
    }

    /**
     * Returns a <tt>byte[]</tt> containing the given
     * byte as unsigned hexadecimal number digits.
     *
     * @param i the int to be converted into a hex string.
     *
     * @return the generated hexadecimal representation as <code>byte[]</code>.
     */
    public static byte[] toHex(int i) {
        StringBuilder buf = new StringBuilder(2);
        //don't forget the second hex digit
        if ((i & 0xff) < 0x10) {
            buf.append("0");
        }
        buf.append(Long.toString(i & 0xff, 16).toUpperCase());
        try {
            return buf.toString().getBytes("US-ASCII");
        }
        catch (Exception e) {
            logger.debug("Problem converting bytes to string - {}", e.getMessage());
        }
        return null;
    }


    public static boolean isBlank(String value) {
        return value == null || value.isEmpty();
    }

    public static boolean isBlank(List<Object> list) {
        return list == null || list.isEmpty();
    }

    public static boolean isBlank(Object[] list) {
        return list == null || list.length == 0;
    }

    public static void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException ex) {
            logger.warn("Backout sleep timer has been interrupted");
        }
    }

    public static int calculateCS(byte[] byteArray, int start, int end) {
        int ret = 0;
        for (int i = start; i <= end; i++) {
            ret += (byteArray[i] % 256);
        }
        return ret & 0xFF;
    }

    /**
     * Converts a byte[4] binary int value to a primitive int.<br>
     * The value returned is:
     *
     * <pre><code>
     * (((a &amp; 0xff) &lt;&lt; 24) | ((b &amp; 0xff) &lt;&lt; 16) |
     * &#32;((c &amp; 0xff) &lt;&lt; 8) | (d &amp; 0xff))
     * </code></pre>
     *
     * @param bytes registers as <tt>byte[4]</tt>.
     *
     * @return the integer contained in the given register bytes.
     */
    public static int registersToInt(byte[] bytes) {
        return (((bytes[0] & 0xff) << 24) |
                ((bytes[1] & 0xff) << 16) |
                ((bytes[2] & 0xff) << 8) |
                (bytes[3] & 0xff)
        );
    }

    /**
     * Converts an int value to a byte[4] array.
     *
     * @param v the value to be converted.
     *
     * @return a byte[4] containing the value.
     */
    public static byte[] intToRegisters(int v) {
        byte[] registers = new byte[4];
        registers[0] = (byte)(0xff & (v >> 24));
        registers[1] = (byte)(0xff & (v >> 16));
        registers[2] = (byte)(0xff & (v >> 8));
        registers[3] = (byte)(0xff & v);
        return registers;
    }

    /**
     * Converts the given register (16-bit value) into
     * a <tt>short</tt>.
     * The value returned is:
     *
     * <pre><code>
     * (short)((a &lt;&lt; 8) | (b &amp; 0xff))
     * </code></pre>
     *
     * This conversion has been taken from the documentation of
     * the <tt>DataInput</tt> interface.
     *
     * @param bytes bytes a register as <tt>byte[2]</tt>.
     *
     * @return the signed short as <tt>short</tt>.
     */
    public static short registerToShort(byte[] bytes) {
        return (short)((bytes[0] << 8) | (bytes[1] & 0xff));
    }

    /**
     * Converts the register (16-bit value) at the given index
     * into a <tt>short</tt>.
     * The value returned is:
     *
     * <pre><code>
     * (short)((a &lt;&lt; 8) | (b &amp; 0xff))
     * </code></pre>
     *
     * This conversion has been taken from the documentation of
     * the <tt>DataInput</tt> interface.
     *
     * @param bytes a <tt>byte[]</tt> containing a short value.
     * @param idx   an offset into the given byte[].
     *
     * @return the signed short as <tt>short</tt>.
     */
    public static short registerToShort(byte[] bytes, int idx) {
        return (short)((bytes[idx] << 8) | (bytes[idx + 1] & 0xff));
    }


    public static int unsignedByteToInt(byte b) {
        return (int)b & 0xFF;
    }

    public static long bytes2long(byte[] bs)  throws Exception {
        int bytes = bs.length;
        if(bytes > 1) {
            if((bytes % 2) != 0 || bytes > 8) {
                throw new Exception("not support");
            }}
        switch(bytes) {
            case 0:
                return 0;
            case 1:
                return (long)((bs[0] & 0xff));
            case 2:
                return (long)((bs[0] & 0xff) <<8 | (bs[1] & 0xff));
            case 4:
                return (long)((bs[0] & 0xffL) <<24 | (bs[1] & 0xffL) << 16 | (bs[2] & 0xffL) <<8 | (bs[3] & 0xffL));
            case 6:
                return (long)((bs[0] & 0xffL) <<40 | (bs[1] & 0xffL) << 32 | (bs[2] & 0xffL) <<24 | (bs[3] & 0xffL)<<16 |
                        (bs[4] & 0xffL) <<8 | (bs[5] & 0xffL));
            case 8:
                return (long)((bs[0] & 0xffL) <<56 | (bs[1] & 0xffL) << 48 | (bs[2] & 0xffL) <<40 | (bs[3] & 0xffL)<<32 |
                        (bs[4] & 0xffL) <<24 | (bs[5] & 0xffL) << 16 | (bs[6] & 0xffL) <<8 | (bs[7] & 0xffL));
            default:
                throw new Exception("not support");
        }
        //return 0;
    }

    public static byte[] unitIdString2Bytes(String addr) {
        byte[] result = new byte[6];

        for (int i = 1; i < 12; i += 2)
        {
            int a = addr.charAt(i);
            int b = addr.charAt(i-1) ;
            if(a>=65 && a<=70)  {
                a -= 15;
            }else if(a>=97 && a<=102)
            {
                a -= 87;
            }else
            {
                a -= 48;
            }

            if(b>=65 && b<=70)  {
                b -= 15;
            }else if(b>=97 && b<=102)
            {
                b -= 87;
            }else
            {
                b -= 48;
            }

            result[(i)/2] = (byte)(b*16+a);
        }
        return result;
    }

}
