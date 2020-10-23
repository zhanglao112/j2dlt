package io.zfunny.j2dlt.dlt645;

public class Dlt645SlaveException extends Dlt645Exception {

    private static final long serialVersionUID = 1L;

    private final int type;

    public Dlt645SlaveException(int type) {
        super();
        this.type = type;
    }

    public static String getMessage(int type) {
        switch (type) {
            case 1:
                return "Illegal Function";
            case 2:
                return "Illegal Data Address";
            case 3:
                return "Illegal Data Value";
            case 4:
                return "Slave Device Failure";
            case 5:
                return "Acknowledge";
            case 6:
                return "Slave Device Busy";
            case 8:
                return "Memory Parity Error";
            case 10:
                return "Gateway Path Unavailable";
            case 11:
                return "Gateway Target Device Failed to Respond";
            default:
                return "Error Code = " + type;
        }
    }

    public int getType() {
        return type;
    }

    public boolean isType(int type) {
        return (type == this.type);
    }

    @Override
    public String getMessage() {
        return getMessage(type);
    }
}
