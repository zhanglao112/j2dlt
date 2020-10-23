package io.zfunny.j2dlt.dlt645.io;

import io.zfunny.j2dlt.dlt645.msg.Dlt645Message;
import io.zfunny.j2dlt.dlt645.msg.Dlt645Request;
import io.zfunny.j2dlt.dlt645.msg.Dlt645Response;
import io.zfunny.j2dlt.dlt645.net.AbstractSerialConnection;

public abstract class AbstractSerialTransportListener {

    public void beforeMessageWrite(AbstractSerialConnection port, Dlt645Message msg) {
    }

    public void afterMessageWrite(AbstractSerialConnection port, Dlt645Message msg){
    }

    public void beforeRequestRead(AbstractSerialConnection port) {
    }

    public void afterRequestRead(AbstractSerialConnection port, Dlt645Request req) {
    }

    public void beforeResponseRead(AbstractSerialConnection port){
    }

    public void afterResponseRead(AbstractSerialConnection port, Dlt645Response res) {
    }
}
