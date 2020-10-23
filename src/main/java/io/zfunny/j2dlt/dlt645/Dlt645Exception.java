package io.zfunny.j2dlt.dlt645;

public class Dlt645Exception extends Exception {

    private static final long serialVersionUID = 1L;

    public Dlt645Exception() {
        super();
    }

    public Dlt645Exception(String message) {
        super(message);
    }

    public Dlt645Exception(String message, Object... values) {
        super(String.format(message, values));
    }

    public Dlt645Exception(String message, Throwable cause) {
        super(message, cause);
    }
}
