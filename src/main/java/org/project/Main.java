package org.project;

import org.project.server.LoadBalancer;

public class Main {
    public static void main(String[] args) {
        LoadBalancer loadBalancer = new LoadBalancer();

        // Adicionando servidores
        loadBalancer.addServer("Server1");
        loadBalancer.addServer("Server2");
        loadBalancer.addServer("Server3");

        // Simulação de requisições
        String key1 = "lista1";
        String key2 = "lista2";

        System.out.println(loadBalancer.processRequest(key1, "CREATE:Supermercado"));
        System.out.println(loadBalancer.processRequest(key1, "ADD:Banana,5"));
        System.out.println(loadBalancer.processRequest(key2, "CREATE:Mercado Local"));
        System.out.println(loadBalancer.processRequest(key2, "ADD:Tomate,10"));
    }
}
