package network.elrond.core;

public class ResponseObject {
    private final boolean success;
    private final String message;
    private final Object payload;

    public ResponseObject(){
        success = false;
        message = "";
        payload = null;
    }

    public ResponseObject(boolean success, String message, Object payload){
        this.success = success;
        this.message = message;
        this.payload = payload;
    }

    public boolean isSuccess(){
        return(success);
    }

    public String getMessage(){
        return(message);
    }

    public Object getPayload(){
        return(payload);
    }

    @Override
    public String toString(){
        return(String.format("ResponseObject{success=%b, message='%s', object=%s}", success, message, payload));
    }
}
