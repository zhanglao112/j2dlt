/*
 * Copyright 2002-2016 jamod & j2mod development teams
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.zfunny.j2dlt.dlt645.io;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * Class implementing a byte array input stream with
 * a DataInput interface.
 *
 * @author Dieter Wimberger
 * @author Steve O'Hara (4NG)
 * @version 2.0 (March 2016)
 */
public class BytesInputStream
        extends FastByteArrayInputStream implements DataInput {

    DataInputStream dataInputStream;

    /**
     * Constructs a new <tt>BytesInputStream</tt> instance,
     * with an empty buffer of a given size.
     *
     * @param size the size of the input buffer.
     */
    public BytesInputStream(int size) {
        super(new byte[size]);
        dataInputStream = new DataInputStream(this);
    }

    /**
     * Constructs a new <tt>BytesInputStream</tt> instance,
     * that will read from the given data.
     *
     * @param data a byte array containing data to be read.
     */
    public BytesInputStream(byte[] data) {
        super(data);
        dataInputStream = new DataInputStream(this);
    }

    /**
     * Resets this <tt>BytesInputStream</tt> using the given
     * byte[] as new input buffer.
     *
     * @param data a byte array with data to be read.
     */
    public void reset(byte[] data) {
        pos = 0;
        mark = 0;
        buf = data;
        count = data.length;
    }

    /**
     * Resets this <tt>BytesInputStream</tt> using the given
     * byte[] as new input buffer and a given length.
     *
     * @param data   a byte array with data to be read.
     * @param length the length of the buffer to be considered.
     */
    public void reset(byte[] data, int length) {
        pos = 0;
        mark = 0;
        count = length;
        buf = data;
    }

    /**
     * Resets this <tt>BytesInputStream</tt>  assigning the input buffer
     * a new length.
     *
     * @param length the length of the buffer to be considered.
     */
    public void reset(int length) {
        pos = 0;
        count = length;
    }

    /**
     * Skips the given number of bytes or all bytes till the end
     * of the assigned input buffer length.
     *
     * @param n the number of bytes to be skipped as <tt>int</tt>.
     *
     * @return the number of bytes skipped.
     */
    public int skip(int n) {
        mark(pos);
        pos += n;
        return n;
    }

    /**
     * Returns the reference to the input buffer.
     *
     * @return the reference to the <tt>byte[]</tt> input buffer.
     */
    public synchronized byte[] getBuffer() {
        byte[] dest = new byte[buf.length];
        System.arraycopy(buf, 0, dest, 0, dest.length);
        return dest;
    }

    @Override
    public int getBufferLength() {
        return buf.length;
    }

    @Override
    public void readFully(byte[] b) throws IOException {
        dataInputStream.readFully(b);
    }

    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
        dataInputStream.readFully(b, off, len);
    }

    @Override
    public int skipBytes(int n) throws IOException {
        return dataInputStream.skipBytes(n);
    }

    @Override
    public boolean readBoolean() throws IOException {
        return dataInputStream.readBoolean();
    }

    @Override
    public byte readByte() throws IOException {
        return dataInputStream.readByte();
    }

    @Override
    public int readUnsignedByte() throws IOException {
        return dataInputStream.readUnsignedByte();
    }

    @Override
    public short readShort() throws IOException {
        return dataInputStream.readShort();
    }

    @Override
    public int readUnsignedShort() throws IOException {
        return dataInputStream.readUnsignedShort();
    }

    @Override
    public char readChar() throws IOException {
        return dataInputStream.readChar();
    }

    @Override
    public int readInt() throws IOException {
        return dataInputStream.readInt();
    }

    @Override
    public long readLong() throws IOException {
        return dataInputStream.readLong();
    }

    @Override
    public float readFloat() throws IOException {
        return dataInputStream.readFloat();
    }

    @Override
    public double readDouble() throws IOException {
        return dataInputStream.readDouble();
    }

    @Override
    public String readLine() throws IOException {
        throw new IOException("Not supported");
    }

    @Override
    public String readUTF() throws IOException {
        return dataInputStream.readUTF();
    }

}
