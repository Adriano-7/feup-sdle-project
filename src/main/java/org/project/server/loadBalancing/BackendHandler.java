package org.project.server.loadBalancing;

import org.zeromq.ZFrame;
import org.zeromq.ZLoop;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.PollItem;
import org.zeromq.ZMsg;
import java.util.Arrays;

public class BackendHandler implements ZLoop.IZLoopHandler {
    private static final byte[] WORKER_READY = { '\001' };

    @Override
    public int handle(ZLoop loop, PollItem item, Object arg_) {
        LBBroker arg = (LBBroker) arg_;
        ZMsg msg = ZMsg.recvMsg(arg.backend);
        if (msg != null) {
            ZFrame address = msg.unwrap();
            arg.workers.addWorker(address);

            if (arg.workers.getWorker("") != null) { // Ensure at least one worker
                PollItem newItem = new PollItem(arg.frontend, ZMQ.Poller.POLLIN);
                loop.addPoller(newItem, new FrontendHandler(), arg);
            }

            ZFrame frame = msg.getFirst();
            if (Arrays.equals(frame.getData(), WORKER_READY)) {
                msg.destroy();
            } else {
                msg.send(arg.frontend);
            }
        }
        return 0;
    }
}
