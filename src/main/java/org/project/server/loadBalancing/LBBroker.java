package org.project.server.loadBalancing;

import org.zeromq.ZMQ.Socket;
public class LBBroker {
    public Socket frontend; // Listen to clients
    public Socket backend;  // Listen to workers
    public HashRing workers;  // Hash ring of ready workers
}
