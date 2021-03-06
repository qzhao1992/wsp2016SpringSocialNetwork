package wspFinalProject;

import java.sql.Timestamp;

/**
 *
 * @author justin.hampton
 */
public class Message {

    public enum MessageType{
        TEXT(1),
        FILE(2),
        IMAGE(3);
        private final int value;
        private MessageType(int value){
            this.value = value;
        }
        public int getValue(){
            return value;
        }
    }
    
    private MessageType type;
    private String text;
    private int id;
    private Timestamp date;
    
    public Message(){
        this(0, MessageType.TEXT, "", new Timestamp(System.currentTimeMillis()));
    }//end constructor

    public Message(int id, MessageType type, String text, Timestamp date){
        this.id = id;
        this.type = type;
        this.text = text;
        this.date = date;
    }
    
    public String getType() {
        String result = "";
        switch(type.value){
            case 1:
                result = "TEXT";
                break;
            case 2:
                result = "FILE";
                break;
            case 3:
                result = "IMAGE";
                break;
            default:
                break;
        }
        return result;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public int getTypeInt(){
        return type.value;
    }
    
    public String getText() {
        return text;
    }

    public void setText(String content) {
        this.text = content;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    public Timestamp getDate(){
        return date;
    }
    
    public void setDate(Timestamp date){
        this.date = date;
    }
    
    public boolean isFile(){
        return type == MessageType.FILE;
    }
    
    public boolean isImage(){
        return type == MessageType.IMAGE;
    }
    
}//end Message
