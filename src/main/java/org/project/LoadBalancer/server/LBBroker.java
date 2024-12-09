package org.project.LoadBalancer.server;

import org.zeromq.ZMQ.Socket;
import org.zeromq.ZFrame;

import java.util.Queue;

public class LBBroker {
    public Socket frontend; // Listen to clients
    public Socket backend;  // Listen to workers
    public Queue<ZFrame> workers;  // List of ready workers
}
