package main.java;

import main.java.messages.Message;
import main.java.messages.PeerDTO;
import main.java.utilities.Logging;
import main.java.utilities.SHA1Hasher;

import java.io.*;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Peer implements Serializable {
    private InetSocketAddress ownAddress;
    private BigInteger hashId;

    private Socket successor;
    private Socket nextSuccessor; // Successor's successor

    Peer(InetSocketAddress ownAddress) {
        this.ownAddress = ownAddress;

        this.hashId = SHA1Hasher.hashAddress(ownAddress.getHostName(), ownAddress.getPort());

        System.out.println(String.format("Peer started on %s:%d. ID: %s",
                                         ownAddress.getHostName(),
                                         ownAddress.getPort(),
                                         this.hashId));

        new Listener(this).start();
    }

    private class Listener extends Thread {
        private final Peer peer;

        private Listener(Peer peer) {
            this.peer = peer;
        }

        @Override
        public void run() {
            try (ServerSocket socket = new ServerSocket(peer.getOwnAddress().getPort())) {
                System.out.println("Now listening...");

                while(true) {
                    Socket clientSocket = socket.accept();
                    new ClientHandler(clientSocket, this.peer).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static class ClientHandler extends Thread {
        private Socket clientSocket;
        private Peer peer;

        private ClientHandler(Socket clientSocket, Peer peer) {
            this.clientSocket = clientSocket;
            this.peer = peer;
        }

        @Override
        public void run() {
            try (
                ObjectInputStream inputStream = new ObjectInputStream(this.clientSocket.getInputStream());
                ObjectOutputStream outputStream = new ObjectOutputStream(this.clientSocket.getOutputStream())
            ) {
                Object input = inputStream.readObject();

                if (input instanceof Message) {
                    Message message = (Message) input;
                    switch (message.getType()) {
                        case JOIN:
                            PlacementHandler.placeNewPeer(this.peer, message);
                            break;
                    }
                } else if (input instanceof String) {
                    switch ((String) input) {
                        case "GETPEERINFO":
                            outputStream.writeObject(new PeerDTO(this.peer));
                            break;
                        default:
                            break;
                    }
                }
            } catch (EOFException e) {
                // Do nothing... Object is deserialized
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    void connectToPeer(InetSocketAddress addressOfExistingPeer) {
        try (
            Socket socketToPeer = new Socket(addressOfExistingPeer.getHostName(),
                                             addressOfExistingPeer.getPort());
            ObjectOutputStream outputStream = new ObjectOutputStream(socketToPeer.getOutputStream())
        ) {
            if (socketToPeer.isConnected())
                Logging.debugLog("Connected to peer.", false);

            outputStream.writeObject(new Message(Message.Type.JOIN, this.ownAddress));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public InetSocketAddress getOwnAddress() {
        return ownAddress;
    }

    public BigInteger getHashId() {
        return this.hashId;
    }

    public Socket getSuccessor() {
        return successor;
    }

    public void setSuccessor(Socket successor) {
        this.successor = successor;

        if (successor != null)
            Logging.debugLog(String.format("Updated successor to %s:%d",
                                           successor.getInetAddress().getHostAddress(),
                                           successor.getPort()),
                             false);
    }

    public Socket getNextSuccessor() {
        return nextSuccessor;
    }

    public void setNextSuccessor(Socket nextSuccessor) {
        this.nextSuccessor = nextSuccessor;

        if (nextSuccessor != null)
            Logging.debugLog(String.format("Updated next successor to %s:%d",
                                           nextSuccessor.getInetAddress().getHostAddress(),
                                           nextSuccessor.getPort()),
                             false);
    }
}
