package org.project.LoadBalancer.server;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZMsg;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZThread;

public class WorkerTask implements ZThread.IDetachedRunnable {
    private static final byte[] WORKER_READY = { '\001' };

    @Override
    public void run(Object[] args) {
        // Prepare context and socket
        try (ZContext context = new ZContext()) {
            Socket worker = context.createSocket(SocketType.REQ);
            worker.setIdentity(("W" + Math.random()).getBytes());
            worker.connect("ipc://backend.ipc");

            // Notify backend of readiness
            ZFrame frame = new ZFrame(WORKER_READY);
            frame.send(worker, 0);

            while (true) {
                ZMsg msg = ZMsg.recvMsg(worker);
                if (msg == null)
                    break;

                msg.getLast().reset("OK");
                msg.send(worker);
            }
        }
    }
}
