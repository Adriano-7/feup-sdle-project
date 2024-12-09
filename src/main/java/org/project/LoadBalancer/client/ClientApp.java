package org.project.LoadBalancer.client;

import org.zeromq.ZThread;

public class ClientApp {
    public static void main(String[] args) {
        ZThread.start(new ClientTask());

        System.out.println("Client started");
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}