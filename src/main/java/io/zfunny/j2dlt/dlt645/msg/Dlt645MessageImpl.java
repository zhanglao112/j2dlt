package io.zfunny.j2dlt.dlt645.msg;

import io.zfunny.j2dlt.dlt645.Dlt645;
import io.zfunny.j2dlt.dlt645.util.Dlt645Util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

public abstract class Dlt645MessageImpl implements Dlt645Message {

    private int transactionID = Dlt645.DEFAULT_TRANSACTION_ID;
    private int dataLength;
    private String unitIDString;
    private byte[] unitID;
    private int functionCode;
    private boolean headless = false;

    @Override
    public boolean isHeadless() {
        return headless;
    }

    @Override
    public void setHeadless() {
        headless = true;
    }

    @Override
    public int getTransactionID() {
        return transactionID & 0x0000FFFF;
    }

    public void setTransactionID(int tid) {
        transactionID = tid & 0x0000FFFF;
    }

    @Override
    public int getDataLength() {
        return dataLength;
    }

    public void setDataLength(int length) {
        if (length >= 200) {
            throw new IllegalArgumentException("Invalid length: " + length);
        }

        dataLength = length;
    }

    @Override
    public String getUnitIDString() {
        return unitIDString;
    }

    public void setUnitIDString(String unitIDString) {
        this.unitIDString = unitIDString;
    }

    @Override
    public byte[] getUnitID() {
        return unitID;
    }

    public void setUnitID(byte[] unitID) {
        this.unitID = Arrays.copyOf(unitID, unitID.length);
    }

    @Override
    public int getFunctionCode() {
        return functionCode;
    }

    protected void setFunctionCode(int code) {
        functionCode = code;
    }

    @Override
    public String getHexMessage() {
        return Dlt645Util.toHex(this);
    }

    public void setHeadless(boolean b) {
        headless = b;
    }

    @Override
    public int getOutputLength() {
        int l = 1 + getDataLength(); // todo

        return l;
    }

    @Override
    public void writeTo(DataOutput dout) throws IOException {
        dout.write(Dlt645.wakeBytes);
        dout.writeByte(Dlt645.startByte);
        dout.write(getUnitID());
        dout.writeByte(Dlt645.startByte);
        dout.writeByte(getFunctionCode());
        dout.writeByte(getDataLength());
        writeData(dout);
    }

    @Override
    public void readFrom(DataInput din) throws IOException {
        byte[] wakeBytes = new byte[4];
        din.readFully(wakeBytes, 0, 4);
        din.readUnsignedByte();

        byte[] addressBytes = new byte[6];
        din.readFully(addressBytes, 0, 6);
        setUnitID(addressBytes);

        din.readUnsignedByte();
        setFunctionCode(din.readUnsignedByte());
        readData(din);
    }

    public abstract void writeData(DataOutput dout) throws IOException;

    public abstract void readData(DataInput din) throws IOException;

}
