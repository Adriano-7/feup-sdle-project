package org.project.server.loadBalancing;

import org.zeromq.ZLoop;
import org.zeromq.ZMQ.PollItem;
import org.zeromq.ZMsg;

public class FrontendHandler implements ZLoop.IZLoopHandler {
    @Override
    public int handle(ZLoop loop, PollItem item, Object arg_) {
        LBBroker arg = (LBBroker) arg_;
        ZMsg msg = ZMsg.recvMsg(arg.frontend);
        if (msg != null) {
            msg.wrap(arg.workers.poll()); //TODO: @Inês em vez do poll, vamos ter que usar o hashRing para saber qual é o worker que vai receber a mensagem
            msg.send(arg.backend);

            if (arg.workers.size() == 0) {
                loop.removePoller(new PollItem(arg.frontend, 0));
            }
        }
        return 0;
    }
}
