package network.elrond.p2p;

public class DHTResponseObject<T> {
    T object;
    ResponseDHT response;

    public DHTResponseObject(T object, ResponseDHT response) {
        this.object = object;
        this.response = response;
    }

    public T getObject() {
        return object;
    }

    public void setObject(T object){
        this.object = object;
    }

    public ResponseDHT getResponse() {
        return response;
    }

    public void setResponse(ResponseDHT response){
        this.response = response;
    }

}
