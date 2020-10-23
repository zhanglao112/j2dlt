package io.zfunny.j2dlt.dlt645.msg;

import io.zfunny.j2dlt.dlt645.Dlt645;
import io.zfunny.j2dlt.dlt645.net.AbstractDlt645Listener;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

public class ReadRequest extends Dlt645Request {

    private byte[] dataIdentity;

    public ReadRequest() {
        super();

        setFunctionCode(Dlt645.READ_DATA);
        setDataLength(4);
    }

    public ReadRequest(byte[] dataIdentity) {
        super();

        setFunctionCode(Dlt645.READ_DATA);
        setDataLength(4);

        this.dataIdentity = Arrays.copyOf(dataIdentity, dataIdentity.length);
    }

    @Override
    public Dlt645Response getResponse() {
        return updateResponseWithHeader(new ReadResponse());
    }

    @Override
    public Dlt645Response createResponse(AbstractDlt645Listener listener) {
        ReadResponse response;
        return null; // todo

    }

    public byte[] getDataIdentity() {
        byte[] dest = new byte[4];
        System.arraycopy(dataIdentity, 0, dest, 0, 4);
        return dest;
    }

    public void setDataIdentity(byte[] di) {
        dataIdentity = Arrays.copyOf(di, di.length);
    }

    @Override
    public void writeData(DataOutput dout) throws IOException {
        for (byte b: dataIdentity) {
            dout.writeByte(b + 0x33);
        }
        //dout.write(dataIdentity);
    }

    @Override
    public void readData(DataInput din) throws IOException {
        din.readFully(dataIdentity, 0, 4);
    }

    @Override
    public byte[] getMessage() {
        byte[] result = new byte[4];
        System.arraycopy(dataIdentity, 0, result, 0, 4);

        return result;
    }
}
