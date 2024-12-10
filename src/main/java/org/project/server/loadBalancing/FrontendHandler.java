package org.project.server.loadBalancing;

import org.project.server.LoadBalancer;
import org.zeromq.ZFrame;
import org.zeromq.ZLoop;
import org.zeromq.ZMQ.PollItem;
import org.zeromq.ZMsg;

public class FrontendHandler implements ZLoop.IZLoopHandler {
    private final LoadBalancer loadBalancer;

    public FrontendHandler(LoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer; // Inicializa o LoadBalancer
    }

    @Override
    public int handle(ZLoop loop, PollItem item, Object arg_) {
        LBBroker arg = (LBBroker) arg_;
        ZMsg msg = ZMsg.recvMsg(arg.frontend);

        if (msg != null) {
            String listId = getListId(msg); // Obtém o ID da lista
            String serverId = loadBalancer.getServerForKey(listId); // Obtém o worker responsável pela lista

            if (serverId == null) {
                System.err.println("No server found for list ID: " + listId);
                msg.destroy();
                return 0;
            }

            // Obtém o endereço do worker correto
            String workerAddress = loadBalancer.getServerForKey(serverId);

            if (workerAddress == null) {
                System.err.println("No worker address found for server ID: " + serverId);
                msg.destroy();
                return 0;
            }

            // Envolve a mensagem com o endereço do worker apropriado
            msg.wrap(new ZFrame(workerAddress));
            msg.send(arg.backend);

            if (arg.workers.size() == 0) {
                loop.removePoller(new PollItem(arg.frontend, 0));
            }
        }
        return 0;
    }

    private String getListId(ZMsg msg) {
        String msgString = msg.toString().replace("[", "").replace("]", "").trim();
        String[] elements = msgString.split(",\\s*");
        String thirdElement = elements[2];
        String[] parts = thirdElement.split("/");
        return parts[1];
    }
}
