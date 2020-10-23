package io.zfunny.j2dlt.dlt645.msg;

import io.zfunny.j2dlt.dlt645.Dlt645;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

public class ReadResponse extends Dlt645Response {

    private int byteCount;
    private byte[] data;

    public ReadResponse() {
        super();
        setFunctionCode(Dlt645.READ_DATA);
    }

    public ReadResponse(byte[] data) {
        super();

        setFunctionCode(Dlt645.READ_DATA);
        setDataLength(data == null ? 0 : (data.length + 1));

        this.data = data == null ? null : Arrays.copyOf(data, data.length);
        byteCount = data == null ? 0 : data.length;
    }

    public int getByteCount() {
        return byteCount;
    }

    public synchronized byte[] getData() {
        byte[] dest = new byte[data.length];
        System.arraycopy(data, 0, dest, 0, dest.length);
        return dest;
    }

    public synchronized void setData(byte[] data) {
        byteCount = data == null ? 0 : data.length;
        this.data = data == null ? null : Arrays.copyOf(data, data.length);
        setDataLength(byteCount + 1);
    }

    @Override
    public void writeData(DataOutput dout) throws IOException {
        dout.writeByte(byteCount);
        dout.write(data);
    }

    @Override
    public void readData(DataInput din) throws IOException {
        byteCount = din.readUnsignedByte() - 4;
        byte[] dataIdentity = new byte[4];
        din.readFully(dataIdentity, 0, 4);

        data = new byte[byteCount];
        din.readFully(data, 0, data.length);

        setDataLength(byteCount + 1);
    }

    @Override
    public byte[] getMessage() {
        byte[] result = new byte[byteCount + 1];
        result[0] = (byte)byteCount;

        System.arraycopy(data, 0, result, 1, byteCount);
        return result;
    }
}
