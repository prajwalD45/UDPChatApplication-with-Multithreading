import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UDPServer {
    private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    public static final int SERVER_PORT = 12345;
    public static final int BUFFER_SIZE = 4096;

    private Map<String, Integer> clientPorts = new HashMap<>();

    public void startServer() {
        try (DatagramSocket socket = new DatagramSocket(SERVER_PORT)) {
            System.out.println("Server is running on port " + SERVER_PORT);

            while (true) {
                byte[] receiveData = new byte[BUFFER_SIZE];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(receivePacket);

                // Handle each incoming client connection in a new thread
                new Thread(() -> {
                    String message = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    InetAddress clientAddress = receivePacket.getAddress();
                    int clientPort = receivePacket.getPort();

                    if (message.startsWith("/join")) {
                        String[] parts = message.split(" ");
                        if (parts.length >= 2) {
                            String username = parts[1];
                            clientPorts.put(username, clientPort);
                            String joiningMsg = username + " joined chat";
                            broadcast(socket, joiningMsg, clientAddress, clientPort, username); // Pass the socket
                        }
                    } else if (message.startsWith("/quit")) {
                        String[] parts = message.split(" ");
                        if (parts.length >= 2) {
                            String username = parts[1];
                            clientPorts.remove(username);
                            broadcast(socket, message, clientAddress, clientPort, username); // Pass the socket
                        }
                    } else {
                        for (Map.Entry<String, Integer> entry : clientPorts.entrySet()) {
                            try {
                                sendToClient(socket, message, clientAddress, entry.getValue());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendToClient(DatagramSocket socket, String message, InetAddress clientAddress, int clientPort)
            throws IOException {
        byte[] sendData = message.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
        socket.send(sendPacket);
    }

    private void broadcast(DatagramSocket socket, String message, InetAddress clientAddress, int clientPort,
            String senderUsername) {
        try {
            String timestampedMessage = "";
            if (message.length() - 1 != ']') {
                timestampedMessage = message + "        [" + dateFormat.format(new Date()) + "]";
            }
            byte[] sendData = timestampedMessage.getBytes();
            for (Map.Entry<String, Integer> entry : clientPorts.entrySet()) {
                if (!entry.getKey().equals(senderUsername)) { // Skip broadcasting to the sender
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress,
                            entry.getValue());
                    socket.send(sendPacket);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        UDPServer server = new UDPServer();
        server.startServer();
    }
}
