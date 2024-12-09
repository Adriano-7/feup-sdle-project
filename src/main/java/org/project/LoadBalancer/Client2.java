package org.project.LoadBalancer;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
public class Client2 {
    private final String clientAddress;
    private final String frontendAddr = "tcp://*:5559";

    public Client2(String clientAddress) {
        this.clientAddress = clientAddress;
    }

    public void start() {
        try (ZContext context = new ZContext()) {
            Socket clientSocket = context.createSocket(ZMQ.REQ);
            clientSocket.setIdentity(clientAddress.getBytes());

            clientSocket.connect(frontendAddr);
            System.out.println("Client" + clientAddress + " connected to " + frontendAddr);

            while (!Thread.currentThread().isInterrupted()) {
                clientSocket.send("CLIENT " + clientAddress + " request");
                String reply = clientSocket.recvStr();
                System.out.println("Client " + clientAddress + " received reply: " + reply);
            }
        }
    }

    public static void main(String[] args) {
        Client2 client = new Client2("1");
        client.start();
    }
}
