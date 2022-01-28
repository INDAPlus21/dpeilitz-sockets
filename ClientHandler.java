
//basic chat functions taken from this excellent video
//https://www.youtube.com/watch?v=gLfuZrrfKes&ab_channel=WittCode
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUsername;
    private String clientColour;
    // Define color constants
    public static final String TEXT_RESET = "\u001B[0m";
    public static final String TEXT_BLACK = "\u001B[30m";
    public static final String TEXT_RED = "\u001B[31m";
    public static final String TEXT_GREEN = "\u001B[32m";
    public static final String TEXT_YELLOW = "\u001B[33m";
    public static final String TEXT_BLUE = "\u001B[34m";
    public static final String TEXT_PURPLE = "\u001B[35m";
    public static final String TEXT_CYAN = "\u001B[36m";
    public static final String TEXT_WHITE = "\u001B[37m";

    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientUsername = bufferedReader.readLine();
            this.clientColour = TEXT_RESET;
            clientHandlers.add(this);
            broadcastMessage("SERVER: " + clientUsername + " has entered the chat!", TEXT_RESET);
            System.out.println("new client " + clientUsername + " has connected!");
            welcomeMessage(this, "Welcome " + clientUsername + " to the chat!");

        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    @Override
    public void run() {
        String messageFromClient;

        while (socket.isConnected()) {
            try {
                messageFromClient = bufferedReader.readLine();

                // direct message
                if (messageFromClient.startsWith("/w ")) {
                    directMessage(clientUsername, messageFromClient);
                }
                // change username
                else if (messageFromClient.startsWith("/changeusername")) {
                    broadcastMessage(clientUsername + " has changed nickname to " + messageFromClient.split(" ")[1],
                            clientColour);
                    clientUsername = messageFromClient.split(" ")[1];
                }
                // change color
                else if (messageFromClient.startsWith("/color")) {
                    clientColour = colour(messageFromClient.split(" ")[1]);
                }
                // broadcast the message to everyone if the message is not a command
                else {
                    broadcastMessage(clientUsername + ": " + messageFromClient, clientColour); // gör snyggare med
                                                                                               // format
                }

            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    // Broadcast message to every client except the user who sent it
    public void broadcastMessage(String messageToSend, String colour) {
        for (ClientHandler clientHandler : clientHandlers) {
            try {

                if (!clientHandler.clientUsername.equals(clientUsername)) {
                    clientHandler.bufferedWriter.write(colour + messageToSend + TEXT_RESET);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    // Direct messages are made with the format /w [NAME] [MESSAGE].
    public void directMessage(String sender, String messageToSend) {
        // loop through clients and find the one that the message is meant for
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                if (messageToSend.split(" ")[1].equals(clientHandler.clientUsername)) {
                    clientHandler.bufferedWriter.write(sender + " whispers: " + messageToSend.split(" ", 3)[2]);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }

            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }

        }
    }

    // Welcome message sent out to client upon entering chat
    public void welcomeMessage(ClientHandler clientHandler, String Message) {
        try {
            clientHandler.bufferedWriter.write(Message);
            clientHandler.bufferedWriter.newLine();
            clientHandler.bufferedWriter.flush();

        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    // translates common speech to colour code
    public String colour(String colour) {
        switch (colour) {
            case "reset":
                return TEXT_RESET;
            case "black":
                return TEXT_BLACK;
            case "red":
                return TEXT_RED;
            case "green":
                return TEXT_GREEN;
            case "yellow":
                return TEXT_YELLOW;
            case "blue":
                return TEXT_BLUE;
            case "purple":
                return TEXT_PURPLE;
            case "cyan":
                return TEXT_CYAN;
            case "white":
                return TEXT_WHITE;
            default:
                return TEXT_RESET;
        }
    }

    public void removeClientHandler() {
        clientHandlers.remove(this);
        System.out.println(clientUsername + " has disconnected");
        broadcastMessage("SERVER: " + clientUsername + " has left the chat :C", TEXT_RESET);
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClientHandler();
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}