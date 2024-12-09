package org.project.server;

import org.project.server.loadBalancing.BackendHandler;
import org.project.server.loadBalancing.LBBroker;
import org.project.server.loadBalancing.WorkerTask;
import org.zeromq.*;
import org.zeromq.ZMQ.PollItem;

import java.util.LinkedList;

public class ServerMain {
    private static final int NBR_WORKERS = 3;

    public static void main(String[] args) {
        try (ZContext context = new ZContext()) {
            LBBroker arg = new LBBroker();
            arg.frontend = context.createSocket(SocketType.ROUTER);
            arg.backend = context.createSocket(SocketType.ROUTER);
            arg.frontend.bind("ipc://frontend.ipc");
            arg.backend.bind("ipc://backend.ipc");

            for (int workerNbr = 0; workerNbr < NBR_WORKERS; workerNbr++)
                ZThread.start(new WorkerTask(workerNbr));

            arg.workers = new LinkedList<>();

            ZLoop reactor = new ZLoop(context);
            PollItem item = new PollItem(arg.backend, ZMQ.Poller.POLLIN);
            reactor.addPoller(item, new BackendHandler(), arg);
            reactor.start();
        }
    }
}
