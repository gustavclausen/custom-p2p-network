package main.java;

import main.java.messages.JoinMessage;

import java.math.BigInteger;
import java.net.Socket;

class PlacementHandler {
    static void placeNewPeer(Peer currentPeer, JoinMessage message) {
        BigInteger hashIdOfInstantiator = message.getHashIdOfInstantiator();

        if (currentPeer.getHashId().compareTo(hashIdOfInstantiator) == 0)
            throw new RuntimeException("This shouldn't happen! :O");

        // Must be predecessor
        if (currentPeer.getHashId().compareTo(hashIdOfInstantiator) > 0) {
            Socket predecessor = currentPeer.getPredecessor();
            if (predecessor != null) {
                BigInteger predecessorHashId = currentPeer.requestInfoFromPeer(predecessor).getHashId();

                if (predecessorHashId.compareTo(hashIdOfInstantiator) > 0) {
                    currentPeer.sendMessageToPeer(predecessor, message);
                } else if (predecessorHashId.compareTo(hashIdOfInstantiator) < 0) {
                    // TODO: Place them in-between
                }
            } else {
                placeAsPredecessor(currentPeer, message);
            }
        }
        // Must be successor
        else if (currentPeer.getHashId().compareTo(hashIdOfInstantiator) < 0) {
            Socket successor = currentPeer.getPredecessor();
            if (successor != null) {
                BigInteger successorHashId = currentPeer.requestInfoFromPeer(successor).getHashId();

                if (successorHashId.compareTo(hashIdOfInstantiator) > 0) {
                    currentPeer.sendMessageToPeer(successor, message);
                } else if (successorHashId.compareTo(hashIdOfInstantiator) < 0) {
                    // TODO: Place them in-between
                }
            } else {
                placeAsSuccessor(currentPeer, message);
            }
        }
    }

    private static void placeAsPredecessor(Peer currentPeer, JoinMessage message) {
        System.out.println("PREDECESSOR");
    }

    private static void placeAsSuccessor(Peer currentPeer, JoinMessage message) {
        System.out.println("SUCCESSOR");
    }
}
