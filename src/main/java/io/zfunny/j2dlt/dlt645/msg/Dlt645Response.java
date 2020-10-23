package io.zfunny.j2dlt.dlt645.msg;

import io.zfunny.j2dlt.dlt645.Dlt645;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public abstract class Dlt645Response extends Dlt645MessageImpl {
    private static final Logger logger = LoggerFactory.getLogger(Dlt645Response.class);

    public enum AuxiliaryMessageTypes {
        NONE, UNIT_ID_MISSMATCH
    }
    private AuxiliaryMessageTypes auxiliaryType = AuxiliaryMessageTypes.NONE;

    public static Dlt645Response createDlt645Response(int functionCode) {
        Dlt645Response response;

        switch (functionCode) {
            case Dlt645.READ_DATA:
                response = new ReadResponse();
                break;
            default:
                // todo check functionCode
                response = new ExceptionResponse();
                break;
        }
        return response;
    }

    protected void setMessage(byte[] msg) {
        try {
            readData(new DataInputStream(new ByteArrayInputStream(msg)));
        } catch (IOException ex) {
            logger.error("Problem setting response message - {}", ex.getMessage());
        }
    }

    public AuxiliaryMessageTypes getAuxiliaryType() {
        return auxiliaryType;
    }

    public void setAuxiliaryType(AuxiliaryMessageTypes auxiliaryType) {
        this.auxiliaryType = auxiliaryType;
    }


}
