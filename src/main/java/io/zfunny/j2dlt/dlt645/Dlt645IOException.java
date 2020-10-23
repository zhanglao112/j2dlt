package io.zfunny.j2dlt.dlt645;

public class Dlt645IOException extends Dlt645Exception {

    private static final long serialVersionUID = 1L;
    private boolean eof = false;

    public Dlt645IOException() {
    }

    public Dlt645IOException(String message) {
        super(message);
    }

    public Dlt645IOException(String message, Object... values) {
        super(message, values);
    }

    public Dlt645IOException(boolean b) {
        eof = b;
    }

    public Dlt645IOException(String message, boolean b) {
        super(message);
        eof = b;
    }

    public Dlt645IOException(String message, Throwable cause) {
        super(message, cause);
    }

    public boolean isEOF() {
        return eof;
    }

    public void setEOF(boolean b) {
        eof = b;
    }
}
