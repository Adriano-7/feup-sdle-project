package org.project.server;

import org.project.server.loadBalancing.BackendHandler;
import org.project.server.loadBalancing.HashRing;
import org.project.server.loadBalancing.LBBroker;
import org.project.server.loadBalancing.WorkerTask;
import org.zeromq.*;
import org.zeromq.ZMQ.PollItem;

public class ServerMain {
    private static final int NBR_WORKERS = 7;

    public static void main(String[] args) {
        try (ZContext context = new ZContext()) {
            LoadBalancer loadBalancer = new LoadBalancer(); // Inicializa o LoadBalancer
            loadBalancer.addNode("server1", "tcp://127.0.0.1:5556");
            loadBalancer.addNode("server2", "tcp://127.0.0.1:5557");

            LBBroker arg = new LBBroker();
            arg.frontend = context.createSocket(SocketType.ROUTER);
            arg.backend = context.createSocket(SocketType.ROUTER);
            arg.frontend.bind("ipc://frontend.ipc");
            arg.backend.bind("ipc://backend.ipc");

            for (int workerNbr = 0; workerNbr < 3; workerNbr++) {
                ZThread.start(new WorkerTask(workerNbr));
            }

            arg.workers = new HashRing();

            ZLoop reactor = new ZLoop(context);
            PollItem backendItem = new PollItem(arg.backend, ZMQ.Poller.POLLIN);
            reactor.addPoller(backendItem, new BackendHandler(loadBalancer), arg);

            reactor.start();
        }
    }

}
