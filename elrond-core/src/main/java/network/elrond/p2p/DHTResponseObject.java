package network.elrond.p2p;

public class DHTResponseObject<T> {
    T object;
    ResponseDHT response;

    DHTResponseObject(T object, ResponseDHT response) {
        this.object = object;
        this.response = response;
    }

    public T getObject() {
        return object;
    }

    public ResponseDHT getResponse() {
        return response;
    }

}
