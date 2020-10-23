package io.zfunny.j2dlt.dlt645.slave;

import io.zfunny.j2dlt.dlt645.util.Dlt645Util;

public enum Dlt645SlaveType {
    TCP, UDP, SERIAL;

    public boolean is(Dlt645SlaveType... types) {
        if (!Dlt645Util.isBlank(types)) {
            for (Dlt645SlaveType type: types) {
                if (equals(type)) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getKey(int port) {
        return toString() + port;
    }
}
