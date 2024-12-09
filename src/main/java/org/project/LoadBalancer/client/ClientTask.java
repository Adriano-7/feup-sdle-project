package org.project.LoadBalancer.client;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZThread;

public class ClientTask implements ZThread.IDetachedRunnable {

    @Override
    public void run(Object[] args) {
        // Prepare context and socket
        try (ZContext context = new ZContext()) {
            Socket client = context.createSocket(SocketType.REQ);
            client.setIdentity(("C" + Math.random()).getBytes());
            client.connect("ipc://frontend.ipc");

            // Send request, get reply
            client.send("HELLO");
            String reply = client.recvStr();
            System.out.println("Client: " + reply);
        }
    }
}
