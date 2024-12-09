package org.project.LoadBalancer.server;

import org.zeromq.ZLoop;
import org.zeromq.ZMQ.PollItem;
import org.zeromq.ZMsg;

public class FrontendHandler implements ZLoop.IZLoopHandler {
    @Override
    public int handle(ZLoop loop, PollItem item, Object arg_) {
        LBBroker arg = (LBBroker) arg_;
        ZMsg msg = ZMsg.recvMsg(arg.frontend);
        if (msg != null) {
            msg.wrap(arg.workers.poll());
            msg.send(arg.backend);

            if (arg.workers.size() == 0) {
                loop.removePoller(new PollItem(arg.frontend, 0));
            }
        }
        return 0;
    }
}
