package org.project.LoadBalancer;

import java.util.LinkedList;
import java.util.Queue;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;
public class LoadBalancer {
    private static final String FRONTEND_ADDR = "tcp://*:5559";
    private static final String BACKEND_ADDR = "tcp://*:5560";

    private final Queue<String> workers = new LinkedList<>();

    public void start() {
        try (ZContext context = new ZContext()) {
            Socket frontend = context.createSocket(ZMQ.ROUTER);
            frontend.bind(FRONTEND_ADDR);
            System.out.println("Frontend bound to " + FRONTEND_ADDR);

            Socket backend = context.createSocket(ZMQ.ROUTER);
            backend.bind(BACKEND_ADDR);
            System.out.println("Backend bound to " + BACKEND_ADDR);

            while (!Thread.currentThread().isInterrupted()) {
                Poller poller = context.createPoller(2);
                poller.register(backend, Poller.POLLIN);

                if(!workers.isEmpty()) {
                    poller.register(frontend, Poller.POLLIN);
                }

                if(poller.poll() < 0) {
                    break;
                }

                if (poller.pollin(0)) {
                    String workerAddress = backend.recvStr();
                    workers.add(workerAddress);

                    String delimiter = backend.recvStr();
                    assert (delimiter.isEmpty());

                    String clientAddress = backend.recvStr();
                    if (!clientAddress.equals("READY")) {
                        delimiter = backend.recvStr();
                        assert (delimiter.isEmpty());

                        String message = backend.recvStr();
                        System.out.println("LoadBalancer received reply: " + message + " from " + workerAddress);
                        frontend.sendMore(clientAddress);
                        frontend.sendMore("");
                        frontend.send(message);
                    }
                }

                if (poller.pollin(1)) {
                    String clientAddress = frontend.recvStr();
                    String delimiter = frontend.recvStr();
                    assert (delimiter.isEmpty());

                    String message = frontend.recvStr();
                    System.out.println("LoadBalancer received message: " + message + " from " + clientAddress);

                    if (workers.isEmpty()) {
                        frontend.sendMore("");
                        frontend.send("No workers available");
                    } else {
                        String workerAddress = workers.poll();  //TODO: Use HashRing to select worker
                        backend.sendMore(workerAddress);
                        backend.sendMore("");
                        backend.sendMore(clientAddress);
                        backend.sendMore("");
                        backend.send(message);
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        LoadBalancer loadBalancer = new LoadBalancer();
        loadBalancer.start();
    }
}
