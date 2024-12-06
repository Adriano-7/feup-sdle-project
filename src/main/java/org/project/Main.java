package org.project;

import org.project.server.LoadBalancer;
import org.project.server.Server;

public class Main {
    public static void main(String[] args) {

        LoadBalancer loadBalancer = new LoadBalancer(3);

        Server server1 = new Server("Server1");
        Server server2 = new Server("Server2");
        Server server3 = new Server("Server3");

        loadBalancer.addServer(server1);
        loadBalancer.addServer(server2);
        loadBalancer.addServer(server3);

        String key1 = "list1";
        String key2 = "list2";

        System.out.println(loadBalancer.handleRequest(key1, "CREATE:Supermercado"));
        System.out.println(loadBalancer.handleRequest(key1, "ADD:Banana,5"));
        System.out.println(loadBalancer.handleRequest(key2, "CREATE:Mercado Local"));
        System.out.println(loadBalancer.handleRequest(key2, "ADD:Tomate,10"));

        //to debug
        System.out.println("Data no server 1: " + server1.getData());
        System.out.println("Data no server 2: " + server2.getData());
        System.out.println("Data no server 3: " + server3.getData());
    }
}
