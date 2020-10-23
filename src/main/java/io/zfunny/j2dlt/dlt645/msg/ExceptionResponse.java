package io.zfunny.j2dlt.dlt645.msg;

import io.zfunny.j2dlt.dlt645.Dlt645;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ExceptionResponse extends Dlt645Response {

    private int exceptionCode = -1;

    public ExceptionResponse(int fc, int exc) {
        setDataLength(1);
        setFunctionCode(fc | Dlt645.EXCEPTION_OFFSET);

        exceptionCode = exc;
    }

    public ExceptionResponse(int fc) {
        setDataLength(1);
        setFunctionCode(fc | Dlt645.EXCEPTION_OFFSET);
    }

    public ExceptionResponse() {
        setDataLength(1);
    }

    public int getExceptionCode() {
        return exceptionCode;
    }

    @Override
    public void writeData(DataOutput dout) throws IOException {
        dout.writeByte(getExceptionCode());
    }

    @Override
    public void readData(DataInput din) throws IOException {
        exceptionCode = din.readUnsignedByte();
    }

    @Override
    public byte[] getMessage() {
        byte[] result = new byte[1];
        result[0] = (byte)getExceptionCode();
        return result;
    }
}
