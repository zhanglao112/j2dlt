package io.zfunny.j2dlt.dlt645.msg;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public interface Dlt645Message {

    boolean isHeadless();

    void setHeadless();

    int getTransactionID();

    int getDataLength();

    String getUnitIDString();

    byte[] getUnitID();

    int getFunctionCode();

    byte[] getMessage();

    String getHexMessage();

    int getOutputLength();

    void writeTo(DataOutput dout) throws IOException;

    void readFrom(DataInput din) throws IOException;

}
