import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UDPClient {
    private static final int SERVER_PORT = 12345;
    private static final int BUFFER_SIZE = 1024;

    private DatagramSocket socket;
    private InetAddress serverAddress;
    private String username;

    private JTextArea chatTextArea;
    private JTextField inputTextField;
    private JButton sendButton;
    private JButton quitButton;
    private JFrame frame;
    private JLabel nameLabel;
    SimpleDateFormat dateFormat;

    public UDPClient(String serverIP) {
        dateFormat = new SimpleDateFormat("HH:mm:ss");
        try {
            socket = new DatagramSocket();
            serverAddress = InetAddress.getByName(serverIP);
            initGUI();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initGUI() {
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 600);
        frame.setLayout(new BorderLayout());

        JPanel navbarPanel = new JPanel(new BorderLayout());
        navbarPanel.setPreferredSize(new Dimension(frame.getWidth(), 60));
        // navbarPanel.setBackground(new Color(63, 81, 181)); 
        navbarPanel.setBackground(new Color(35, 57, 91)); 

        nameLabel = new JLabel();
        nameLabel.setFont(new Font("Arial", Font.BOLD, 18));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        navbarPanel.add(nameLabel, BorderLayout.WEST);

        frame.add(navbarPanel, BorderLayout.NORTH);

        chatTextArea = new JTextArea();
        chatTextArea.setEditable(false);
        chatTextArea.setFont(new Font("Arial", Font.PLAIN, 16));
        chatTextArea.setLineWrap(true);
        chatTextArea.setWrapStyleWord(true);
        // chatTextArea.setBackground(new Color(225, 255, 225)); 
        chatTextArea.setBackground(new Color(185, 227, 198)); 
        chatTextArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(chatTextArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        frame.add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        inputTextField = new JTextField();
        inputTextField.setFont(new Font("Arial", Font.PLAIN, 16));

        sendButton = new JButton("Send");
        sendButton.setFont(new Font("Arial", Font.BOLD, 14));
        // sendButton.setBackground(new Color(0, 153, 0)); 
        sendButton.setBackground(new Color(89, 201, 165)); 
        sendButton.setForeground(Color.WHITE);

        quitButton = new JButton("Quit");
        quitButton.setFont(new Font("Arial", Font.BOLD, 14));
        // quitButton.setBackground(new Color(220, 20, 60));
        quitButton.setBackground(new Color(216, 30, 91));
        quitButton.setForeground(Color.WHITE);

        inputTextField.setPreferredSize(new Dimension(200, 40));
        sendButton.setPreferredSize(new Dimension(100, 40));
        quitButton.setPreferredSize(new Dimension(100, 40));

        inputPanel.add(inputTextField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        inputPanel.add(quitButton, BorderLayout.WEST);
        frame.add(inputPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        inputTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        quitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                quit();
            }
        });

        frame.setVisible(true);
        inputTextField.requestFocus();
    }

    public void sendMessage() {
        String message = inputTextField.getText();
        if (!message.isEmpty()) {
            // displaySentMessage(message);
            sendToServer(username + ": " + message);
            inputTextField.setText("");
        }
    }

    private void sendToServer(String message) {
        byte[] sendData = message.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, SERVER_PORT);
        try {
            socket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startClient() {
        username = JOptionPane.showInputDialog("Enter your username:");
        if (username == null || username.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Username cannot be empty.");
            System.exit(0);
        }
        nameLabel.setText(username);
        frame.setTitle("Chat Application");

        String joinMessage = "/join " + username;
        sendToServer(joinMessage);

        Thread receiverThread = new Thread(() -> {
            try {
                while (true) {
                    byte[] receiveData = new byte[BUFFER_SIZE];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    socket.receive(receivePacket);
                    String receivedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    displayReceivedMessage(receivedMessage);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        receiverThread.start();
    }

    private void displayReceivedMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestampedMessage="";
            if(message.length()-1!=']'){
                timestampedMessage = message + "        [" + dateFormat.format(new Date()) + "]";
            }
            chatTextArea.append(timestampedMessage + "\n");
        });
    }
    public void quit() {
        String quitMessage = "/quit " + username;
        sendToServer(quitMessage);
        socket.close();
        System.exit(0);
    }

    public static void main(String[] args) {
        String serverIP = JOptionPane.showInputDialog("Enter the server's IP address:");
        if (serverIP != null && !serverIP.trim().isEmpty()) {
            UDPClient client = new UDPClient(serverIP);
            client.startClient();
        } else {
            JOptionPane.showMessageDialog(null, "Invalid server IP address.");
        }
    }
}