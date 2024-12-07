package org.project.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.project.data_structures.LWWSet;
import org.project.data_structures.LWWSetSerializer;
import org.project.data_structures.ShoppingListDeserializer;
import org.project.model.ShoppingList;
import org.project.server.database.ServerDB;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;

import java.util.Map;

public class Server {
    public static void main(String[] args) {
        Map<String, ShoppingList> shoppingLists = ServerDB.loadShoppingLists();
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LWWSet.class, new LWWSetSerializer())
                .registerTypeAdapter(ShoppingList.class, new ShoppingListDeserializer())
                .create();

        System.out.println("Starting server...");
        //System.out.println(shoppingLists);

        /*
        Comandos suportados:

        read <id> -> Envia a lista de compras com o id especificado
        */
        try (ZContext context = new ZContext()) {
            // Socket to talk to clients
            ZMQ.Socket socket = context.createSocket(SocketType.REP);
            socket.bind("tcp://*:5555");

            while (!Thread.currentThread().isInterrupted()) {
                byte[] reply = socket.recv(0);

                System.out.println(
                        "Received " + ": [" + new String(reply, ZMQ.CHARSET) + "]"
                );

                if(reply.length > 0 && reply[0] == 'r' && reply[1] == 'e' && reply[2] == 'a' && reply[3] == 'd' && reply[4] == ' '){
                    System.out.println("Reading shopping list...");

                    String id = new String(reply).substring(5);

                    ShoppingList shoppingList = shoppingLists.get(id);
                    if(shoppingList == null){
                        System.out.println("Shopping list not found.");
                        socket.send("Shopping list not found.".getBytes(ZMQ.CHARSET), 0);
                    }
                    else{
                        String response = gson.toJson(shoppingList);

                        socket.send(response.getBytes(ZMQ.CHARSET), 0);
                    }
                }

            }
        }
    }
}
