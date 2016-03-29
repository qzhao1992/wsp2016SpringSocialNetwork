package wspFinalProject;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.sql.DataSource;
import wspFinalProject.Message.MessageType;

/**
 *
 * @author justin.hampton
 */
@Named(value = "MessageManager")
@SessionScoped
public class MessageManager implements Serializable{

    @Resource(name = "jdbc/db1")
    private DataSource ds;
    
    static List<Message> messageList = new ArrayList<>();
    private Message newMessage = new Message();
    
    private boolean reversed = false;
    
    @PostConstruct
    public void init(){
        try{loadMessages();}catch(Exception e){}   
    }//end post construct
    
    ////////////////////////////////////////////////////////////////////////////
    //  GET MESSAGE LIST                                                         //
    ////////////////////////////////////////////////////////////////////////////
    public List<Message> getMessageList() throws SQLException{
        return messageList;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    //  LOAD MESSAGES FROM DATABASE INTO MEMORY                                  //
    ////////////////////////////////////////////////////////////////////////////    
    private void loadMessages() throws SQLException{
        messageList.clear();
        if (ds == null) {
            throw new SQLException("Cannot get DataSource");
        }
        Connection conn = ds.getConnection();
        if (conn == null) {
            throw new SQLException("Cannot get connection");
        }

        try {
            conn.setAutoCommit(false);
            boolean committed = false;
            
            try {
                PreparedStatement selectStatement = conn.prepareStatement("SELECT * from message");
                ResultSet rs = selectStatement.executeQuery();
                
                while(rs.next()){
                    
                    int id = rs.getInt("id");
                    int typeInt = rs.getInt("type");
                    MessageType type = null;
                    switch(typeInt){
                        case 1:
                            type = MessageType.TEXT;
                            break;
                        case 2:
                            type = MessageType.FILE;
                            break;
                        case 3:
                            type = MessageType.IMAGE;
                            break;
                        default:
                            break;
                    }
                    String text = rs.getString("text");
                    
                    messageList.add(new Message(id, type, text));
                }
                
                conn.commit();
                committed = true;
            } finally {
                if (!committed) {
                    conn.rollback();
                }
            }
        } finally {
            conn.close();
        }
        
        if(!reversed){
            Collections.reverse(messageList);
            reversed = true;
        } 
    }//end loadMessages
    
    public String postMessage() throws SQLException{
        
        if (ds == null) {
            throw new SQLException("Cannot get DataSource");
        }
        Connection conn = ds.getConnection();
        if (conn == null) {
            throw new SQLException("Cannot get connection");
        }

        try {
            conn.setAutoCommit(false);
            boolean committed = false;
            
            try {
                
                PreparedStatement statement = conn.prepareStatement("INSERT INTO message (id, type, text) values (?,?,?)");
                statement.setInt(1, 0);
                statement.setInt(2, 1);
                statement.setString(3, newMessage.getText());
                
                statement.executeUpdate();
                
                conn.commit();
                committed = true;
            } finally {
                if (!committed) {
                    conn.rollback();
                }
            }
        } finally {
            conn.close();
        }
        return null;
    }//end postMessage
    
    
    ////////////////////////////////////////////////////////////////////////////
    //  SAVE CHANGES                                                          //
    ////////////////////////////////////////////////////////////////////////////
    public String save() throws SQLException{
        
//        for(Book b : bookList){
//            b.setEditable(false);
//        }
        
        clearMessages();
        
        if (ds == null) {
            throw new SQLException("Cannot get DataSource");
        }
        Connection conn = ds.getConnection();
        if (conn == null) {
            throw new SQLException("Cannot get connection");
        }

        try {
            conn.setAutoCommit(false);
            boolean committed = false;
            
            try {
                PreparedStatement statement = conn.prepareStatement(
                        "INSERT INTO message VALUES (?,?,?)");
                
                for(Message message : messageList){                    
                    statement.setInt(1, message.getId());
                    statement.setInt(2, message.getTypeInt());
                    statement.setString(3, message.getText());         
                    statement.addBatch();
                }
                
                statement.executeBatch();
                
                conn.commit();
                committed = true;
            } finally {
                if (!committed) {
                    conn.rollback();
                }
            }
        } finally {
            conn.close();
        }
        loadMessages();
        return null;
    }//end save
    
    // For adding new message
    public String save(Message m) throws SQLException{
        Collections.reverse(messageList);
        reversed = false;
        messageList.add(m);
        cancel();
        save();
        return null;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    //  DELETE ALL MESSAGES FROM THE DATABASE                                    //
    ////////////////////////////////////////////////////////////////////////////    
    public void clearMessages() throws SQLException{
        
        if (ds == null) {
            throw new SQLException("Cannot get DataSource");
        }
        Connection conn = ds.getConnection();
        if (conn == null) {
            throw new SQLException("Cannot get connection");
        }

        try {
            conn.setAutoCommit(false);
            boolean committed = false;
            
            try {
                PreparedStatement statement = conn.prepareStatement(
                        "DELETE FROM message");
                
                statement.executeUpdate();
                
                conn.commit();
                committed = true;
            } finally {
                if (!committed) {
                    conn.rollback();
                }
            }
        } finally {
            conn.close();
        }        
    }//end clearMessages
    
    ////////////////////////////////////////////////////////////////////////////
    //  CANCEL ADDING A NEW BOOK                                              //
    ////////////////////////////////////////////////////////////////////////////    
    public String cancel() throws SQLException{
//        newMessage.clear();
//        addBook.add(new Book());
//        addingBook = false;
        newMessage = new Message();
        return null;
    }//end cancel
    
    public String home(){
        return "index";
    }

    public Message getNewMessage() {
        return newMessage;
    }
        
    public String getMessageClass(Message m){
        String result = "messageEven";
//        if(messageList.indexOf(m) % 2 != 0) result = "messageOdd";
        if( messageList.size() % 2 == 0) {
            if(messageList.indexOf(m) % 2 != 0) result = "messageOdd";
        }
        else{
            result = "messageOdd";
            if(messageList.indexOf(m) % 2 != 0) result = "messageEven";
        }
        
        return result;
    }
    
    public String getTimeConnectorClass(Message m){
        String result = "timeConnectorLeft";
        if( messageList.indexOf(m) % 2 == 0 ){
            result = "timeConnectorRight";
        }
        return result;
    }
    
    public String uploadImage(Message m){
        return null;
    }
    
    public String uploadFile(Message m){
        return null;
    }
}//end BookManagementBean

