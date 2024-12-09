package org.project.LoadBalancer;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class Worker {
    private final String workerAddress;
    private final String backendAddress = "tcp://*:5560";

    public Worker(String workerAddress) {
        this.workerAddress = workerAddress;
    }

    public void start() {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket workerSocket = context.createSocket(ZMQ.REQ);
            workerSocket.setIdentity(workerAddress.getBytes());

            workerSocket.connect(backendAddress);
            System.out.println("Worker" + workerAddress + " connected to " + backendAddress);

            workerSocket.send("READY");

            while (!Thread.currentThread().isInterrupted()) {
                String clientAddress = workerSocket.recvStr();
                String delimiter = workerSocket.recvStr();
                assert (delimiter.isEmpty());

                String message = workerSocket.recvStr();
                System.out.println("Worker" + workerAddress + " received message: " + message);

                //TODO: Process the message

                workerSocket.sendMore(clientAddress);
                workerSocket.sendMore("");
                workerSocket.send("WORKER " + workerAddress + " processed message: " + message);

                System.out.println("Worker" + workerAddress + " sent reply to " + clientAddress);
            }
        }
    }

    public static void main(String[] args) {
        Worker worker = new Worker("1");
        worker.start();
    }
}
