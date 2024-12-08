package org.project.server;

import java.io.*;

public class Message implements Serializable {
    private final String operation; // Ex: "CREATE", "ADD"
    private final String key;       // Ex: ID of the list
    private final String value;     // Ex: item or quantity

    public Message(String operation, String key, String value) {
        this.operation = operation;
        this.key = key;
        this.value = value;
    }

    // Getters
    public String getOperation() {
        return operation;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    // Serializa a mensagem para envio via ZeroMQ
    public String serialize() {
        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            ObjectOutputStream objStream = new ObjectOutputStream(byteStream);
            objStream.writeObject(this);
            objStream.flush();
            return byteStream.toString("ISO-8859-1");
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize message: " + e.getMessage(), e);
        }
    }

    // Desserializa uma string recebida via ZeroMQ para uma mensagem
    public static Message deserialize(String data) {
        try {
            ByteArrayInputStream byteStream = new ByteArrayInputStream(data.getBytes("ISO-8859-1"));
            ObjectInputStream objStream = new ObjectInputStream(byteStream);
            return (Message) objStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to deserialize message: " + e.getMessage(), e);
        }
    }

    @Override
    public String toString() {
        return "Message{" +
                "operation='" + operation + '\'' +
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}