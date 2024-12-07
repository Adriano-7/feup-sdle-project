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

            //String request = "read/34dbcbe3-2d4c-42ea-9e0a-fdc2c3c6a9bd";
            String request = "write/{\"listID\":\"34dbcbe3-2d4c-42ea-9e0a-fdc2c3c6a9bd\",\"name\":\"lidl\",\"items\":{\"peras\":{\"item\":{\"name\":\"peras\",\"counter\":{\"payload\":{\"fdf83fcb-2ae1-4f10-95b6-48dd934b87dc\":6,\"06041095-9887-44b5-8f06-42d8aca521e3\":5,\"53d6443d-2099-41cd-af74-3fa3386e9a63\":12,\"76c8dd43-9494-4cd0-ada8-8ea1b72122c2\":2,\"01910289-df6d-409f-a206-4e518218e737\":4},\"maxValue\":35}},\"rmv-time\":0.0,\"add-time\":1.733388799551E9},\"couves\":{\"item\":{\"name\":\"couves\",\"counter\":{\"payload\":{\"06041095-9887-44b5-8f06-42d8aca521e3\":3},\"maxValue\":3}},\"rmv-time\":0.0,\"add-time\":1.733264497752E9}}}";
            socket.send(request.getBytes(ZMQ.CHARSET), 0);

            byte[] reply = socket.recv(0);
            System.out.println(
                    "Received " + new String(reply, ZMQ.CHARSET)
            );
        }
    }

}
