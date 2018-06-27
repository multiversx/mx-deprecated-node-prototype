package network.elrond.consensus;

import java.io.Serializable;

public class ConsensusMessage<T> implements Serializable {
    private ConsensusMessageType messageType;
    private ConsensusCommandType commandType;
    private T message;

    public void setMessageType(ConsensusMessageType messageType) {
        this.messageType = messageType;
    }

    public void setCommandType(ConsensusCommandType commandType) {
        this.commandType = commandType;
    }

    public void setMessage(T message) {
        this.message = message;
    }

    public ConsensusMessageType getMessageType() {
        return messageType;
    }

    public ConsensusCommandType getCommandType() {
        return commandType;
    }

}
