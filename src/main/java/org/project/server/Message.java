package org.project.server;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

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

    public void send(SocketChannel channel) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ObjectOutputStream objStream = new ObjectOutputStream(byteStream);
        objStream.writeObject(this);
        objStream.flush();

        byte[] data = byteStream.toByteArray();
        ByteBuffer buffer = ByteBuffer.allocate(4 + data.length);
        buffer.putInt(data.length);
        buffer.put(data);
        buffer.flip();

        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
    }

    public static Message read(SocketChannel channel) throws IOException, ClassNotFoundException {
        ByteBuffer sizeBuffer = ByteBuffer.allocate(4);
        if (channel.read(sizeBuffer) < 4) return null;

        sizeBuffer.flip();
        int size = sizeBuffer.getInt();

        ByteBuffer dataBuffer = ByteBuffer.allocate(size);
        channel.read(dataBuffer);
        dataBuffer.flip();

        byte[] data = new byte[size];
        dataBuffer.get(data);

        ObjectInputStream objStream = new ObjectInputStream(new ByteArrayInputStream(data));
        return (Message) objStream.readObject();
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
