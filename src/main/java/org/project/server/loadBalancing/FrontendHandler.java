package org.project.server.loadBalancing;

import org.zeromq.ZFrame;
import org.zeromq.ZLoop;
import org.zeromq.ZMQ.PollItem;
import org.zeromq.ZMsg;

public class FrontendHandler implements ZLoop.IZLoopHandler {
    @Override
    public int handle(ZLoop loop, PollItem item, Object arg_) {
        LBBroker arg = (LBBroker) arg_;
        ZMsg msg = ZMsg.recvMsg(arg.frontend);

        if (msg != null) {
            if (msg.toString().contains("ping")) {
                ZFrame worker = arg.workers.getWorker("");
                if (worker != null) {
                    msg.wrap(worker);
                    msg.send(arg.backend);
                }
            }
            else{
                String listId = getListId(msg);
                ZFrame worker = arg.workers.getWorker(listId);
                if (worker != null) {
                    msg.wrap(worker);
                    msg.send(arg.backend);
                }
            }

            if (arg.workers == null) {
                loop.removePoller(new PollItem(arg.frontend, 0));
            }
        }
        return 0;
    }

    String getListId(ZMsg msg) {
        String msgString = msg.toString().replace("[", "").replace("]", "").trim();
        String[] elements = msgString.split(",\\s*");
        String thirdElement = elements[2];
        String[] parts = thirdElement.split("/");
        return parts[1];
    }
}
