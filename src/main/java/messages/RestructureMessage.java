package main.java.messages;

import main.java.PeerAddress;

public class RestructureMessage extends Message {
    private final Type type;

    public RestructureMessage(PeerAddress senderPeerAddress, Type type) {
        super(senderPeerAddress);
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        NEW_SUCCESSOR,
        NEW_NEXT_SUCCESSOR
    }
}
