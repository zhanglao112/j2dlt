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

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Class implementing a byte array output stream with
 * a DataInput interface.
 *
 * @author Dieter Wimberger
 * @author Steve O'Hara (4NG)
 * @version 2.0 (March 2016)
 */
public class BytesOutputStream extends FastByteArrayOutputStream implements DataOutput {

    private final DataOutputStream dataOutputStream;

    /**
     * Constructs a new <tt>BytesOutputStream</tt> instance with
     * a new output buffer of the given size.
     *
     * @param size the size of the output buffer as <tt>int</tt>.
     */
    public BytesOutputStream(int size) {
        super(size);
        dataOutputStream = new DataOutputStream(this);
    }

    /**
     * Constructs a new <tt>BytesOutputStream</tt> instance with
     * a given output buffer.
     *
     * @param buffer the output buffer as <tt>byte[]</tt>.
     */
    public BytesOutputStream(byte[] buffer) {
        buf = buffer;
        count = 0;
        dataOutputStream = new DataOutputStream(this);
    }

    /**
     * Returns the reference to the output buffer.
     *
     * @return the reference to the <tt>byte[]</tt> output buffer.
     */
    public synchronized byte[] getBuffer() {
        byte[] dest = new byte[buf.length];
        System.arraycopy(buf, 0, dest, 0, dest.length);
        return dest;
    }

    @Override
    public void reset() {
        count = 0;
    }

    @Override
    public void writeBoolean(boolean v) throws IOException {
        dataOutputStream.writeBoolean(v);
    }

    @Override
    public void writeByte(int v) throws IOException {
        dataOutputStream.writeByte(v);
    }

    @Override
    public void writeShort(int v) throws IOException {
        dataOutputStream.writeShort(v);
    }

    @Override
    public void writeChar(int v) throws IOException {
        dataOutputStream.writeChar(v);
    }

    @Override
    public void writeInt(int v) throws IOException {
        dataOutputStream.writeInt(v);
    }

    @Override
    public void writeLong(long v) throws IOException {
        dataOutputStream.writeLong(v);
    }

    @Override
    public void writeFloat(float v) throws IOException {
        dataOutputStream.writeFloat(v);
    }

    @Override
    public void writeDouble(double v) throws IOException {
        dataOutputStream.writeDouble(v);
    }

    @Override
    public void writeBytes(String s) throws IOException {
        int len = s.length();
        for (int i = 0; i < len; i++) {
            this.write((byte)s.charAt(i));
        }
    }

    @Override
    public void writeChars(String s) throws IOException {
        dataOutputStream.writeChars(s);
    }

    @Override
    public void writeUTF(String str) throws IOException {
        dataOutputStream.writeUTF(str);
    }

}