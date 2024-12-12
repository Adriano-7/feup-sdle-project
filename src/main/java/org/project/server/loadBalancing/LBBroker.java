package org.project.server.loadBalancing;

import org.zeromq.ZFrame;
import org.zeromq.ZMQ.Socket;

import java.util.Queue;

public class LBBroker {
    public Socket frontend; // Listen to clients
    public Socket backend;  // Listen to workers
    public HashRing workers;  // Hash ring of ready workers
}
