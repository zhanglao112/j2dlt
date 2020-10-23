package io.zfunny.j2dlt.dlt645.net;

import io.zfunny.j2dlt.dlt645.Dlt645IOException;
import io.zfunny.j2dlt.dlt645.io.Dlt645UDPTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Dlt645UDPListener extends AbstractDlt645Listener {
    private static final Logger logger = LoggerFactory.getLogger(Dlt645UDPListener.class);
    private UDPSlaveTerminal terminal;

    public Dlt645UDPListener(InetAddress ifc) {
        address = ifc;
        listening = true;
    }

    public Dlt645UDPListener() {
        try {
            address = InetAddress.getByAddress(new byte[]{0, 0, 0, 0});
        } catch (UnknownHostException e) {

        }
    }

    @Override
    public void setTimeout(int timeout) {
        super.setTimeout(timeout);
        if (terminal != null && listening) {
            terminal.setTimeout(timeout);
        }
    }

    @Override
    public void run() {
        if (threadName == null || threadName.isEmpty()) {
            threadName = String.format("Dlt645 UDP Listener [port:%d]", port);
        }
        Thread.currentThread().setName(threadName);

        Dlt645UDPTransport transport;
        try {
            if (address == null) {
                terminal = new UDPSlaveTerminal(InetAddress.getByAddress(new byte[]{0, 0, 0, 0}));
            } else {
                terminal = new UDPSlaveTerminal(address);
            }
            terminal.setTimeout(timeout);
            terminal.setPort(port);
            terminal.activate();
            transport = new Dlt645UDPTransport(terminal);
        } catch (Exception e) {
            error = String.format("Cannot start UDP listener on port %d - %s", port, e.getMessage());
            listening = false;
            return;
        }

        listening = true;
        try {
            while (listening) {
                handleRequest(transport, this);
            }
        } catch (Dlt645IOException ex1) {
            if (!ex1.isEOF()) {
                logger.error("Exception occurred before EOF while handling request", ex1);
            }
        } finally {
            try {
                terminal.deactivate();
                transport.close();
            } catch (Exception ex) {
                // ignore
            }
        }
    }

    @Override
    public void stop() {
        terminal.deactivate();
        listening = false;
    }
}
