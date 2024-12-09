package org.project.server.loadBalancing;

import org.zeromq.ZLoop;
import org.zeromq.ZMQ.PollItem;
import org.zeromq.ZMsg;

//Frontend: clients -> worker
public class FrontendHandler implements ZLoop.IZLoopHandler {
    @Override
    public int handle(ZLoop loop, PollItem item, Object arg_) {
        LBBroker arg = (LBBroker) arg_;
        ZMsg msg = ZMsg.recvMsg(arg.frontend);

        String listId = getListId(msg); //This will be useful to get the node from the hashRing that has the list

        if (msg != null) {
            msg.wrap(arg.workers.poll()); //TODO: @Inês em vez do poll, vamos ter que usar o hashRing para saber qual é o worker que tem a lista
            msg.send(arg.backend);

            if (arg.workers.size() == 0) {
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
