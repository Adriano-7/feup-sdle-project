package org.project.client;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;

public class Client2 {
    public static void main(String[] args)
    {
        try (ZContext context = new ZContext()) {
            //  Socket to talk to server
            System.out.println("Connecting to server");

            ZMQ.Socket socket = context.createSocket(SocketType.REQ);
            socket.connect("tcp://localhost:5555");


            String request = "read 34dbcbe3-2d4c-42ea-9e0a-fdc2c3c6a9bd";
            socket.send(request.getBytes(ZMQ.CHARSET), 0);

            byte[] reply = socket.recv(0);
            System.out.println(
                    "Received " + new String(reply, ZMQ.CHARSET)
            );
        }
    }
}
