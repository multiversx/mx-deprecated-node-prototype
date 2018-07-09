package network.elrond.core;

public class ResponseObject {
    private boolean success;
    private String message;
    private Object payload;

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

    public void setSuccess(boolean success){
        this.success = success;
    }

    public String getMessage(){
        return(message);
    }

    public void setMessage(String message){
        this.message = message;
    }

    public Object getPayload(){
        return(payload);
    }

    public void setPayload(Object payload){
        this.payload = payload;
    }

    @Override
    public String toString(){
        return(String.format("ResponseObject{success=%b, message='%s', object=%s}", success, message, payload));
    }
}
