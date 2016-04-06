package wspFinalProject;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
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

    private boolean descendDate;
    
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
        
        loadConfig();
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
                String statement = descendDate ? "SELECT * FROM message ORDER BY date DESC" : "SELECT * FROM message ORDER BY date";
                PreparedStatement selectStatement = conn.prepareStatement(statement);
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
                    Timestamp date = rs.getTimestamp("date");
                    
                    messageList.add(new Message(id, type, text, date));
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
        
    }//end loadMessages
    
    ////////////////////////////////////////////////////////////////////////////
    //  LOAD CONFIG SETTINGS                                                  //
    ////////////////////////////////////////////////////////////////////////////    
    private void loadConfig() throws SQLException{
                
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
                PreparedStatement selectStatement = conn.prepareStatement("SELECT * FROM config_settings WHERE user_id = ?");
                selectStatement.setInt(1, 1);// TODO: change to use current user's id
                ResultSet rs = selectStatement.executeQuery();
                
                String order_by = "";
                while(rs.next()){                    
                    int id = rs.getInt("id");
                    order_by = rs.getString("order_by");
                }
                
                descendDate = order_by.contains("date_desc");
                
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
    }//end loadConfig
    
    ////////////////////////////////////////////////////////////////////////////
    //  SAVE CHANGES // For adding new message                                //
    ////////////////////////////////////////////////////////////////////////////    
    public String save() throws SQLException{
        messageList.add(0, newMessage);
        newMessage = new Message();        
        
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
                        "INSERT INTO message (id, type, text, date) VALUES (?,?,?,?)");
                
                Message message = messageList.get(0);
                
                statement.setInt(1, message.getId());
                statement.setInt(2, message.getTypeInt());
                statement.setString(3, message.getText());
                Timestamp date = new Timestamp(System.currentTimeMillis());
                statement.setTimestamp(4, date);
                
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
    }
    
    ////////////////////////////////////////////////////////////////////////////
    //  DELETE ALL MESSAGES FROM THE DATABASE                                 //
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
    //  CANCEL                                                                //
    ////////////////////////////////////////////////////////////////////////////    
    public String cancel() throws SQLException{        
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
    
    
    
    
    
    
    ////////////////////////////////////////////////////////////////////////////
    //  UPLOAD FILE                                                           //
    ////////////////////////////////////////////////////////////////////////////
    private Part part;
    
    public void uploadFile() throws IOException, SQLException {
        
        if(part.getContentType().contains("image")){
            newMessage.setType(MessageType.IMAGE);
        }
        else{
            newMessage.setType(MessageType.FILE);
        }
        
        newMessage.setText(part.getSubmittedFileName());
        
        save();
        
        FacesContext facesContext = FacesContext.getCurrentInstance();
        
        Connection conn = ds.getConnection();

        InputStream inputStream;
        inputStream = null;
        try {
            inputStream = part.getInputStream();
            PreparedStatement insertQuery = conn.prepareStatement(
                    "INSERT INTO FILESTORAGE (FILE_NAME, FILE_TYPE, FILE_SIZE, FILE_CONTENTS, FILE_ID) "
                            + "VALUES (?,?,?,?,LAST_INSERT_ID())");
            insertQuery.setString(1, part.getSubmittedFileName());
            insertQuery.setString(2, part.getContentType());
            insertQuery.setLong(3, part.getSize());
            insertQuery.setBinaryStream(4, inputStream);

            int result = insertQuery.executeUpdate();
            if (result == 1) {
                facesContext.addMessage("uploadForm:upload",
                        new FacesMessage(FacesMessage.SEVERITY_INFO, 
                                part.getSubmittedFileName()
                                + ": uploaded successfuly !!", null));
            } else {
                // if not 1, it must be an error.
                facesContext.addMessage("uploadForm:upload",
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                                result + " file uploaded", null));
            }
        } catch (IOException e) {
            facesContext.addMessage("uploadForm:upload",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            "File upload failed !!", null));
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
    }

    public void validateFile(FacesContext ctx, UIComponent comp, Object value) {
        if (value == null) {
            throw new ValidatorException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            "Select a file to upload", null));
        }
        Part file = (Part) value;
        System.out.println("getContentType(): " + file.getContentType());
        
        long size = file.getSize();
        if (size <= 0) {
            throw new ValidatorException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            "the file is empty", null));
        }
        if (size > 1024 * 1024 * 100) { // 100 MB limit
            throw new ValidatorException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            size + "bytes: file too big (limit 100MB)", null));
        }
    }

    public Part getPart() {
        return part;
    }

    public void setPart(Part part) {
        this.part = part;
    }

    
    
    
    
    
    
    
    
    
    
    ////////////////////////////////////////////////////////////////////////////
    //  DOWNLOAD FILE                                                         //
    ////////////////////////////////////////////////////////////////////////////
    public String downloadFile(int fileID) throws SQLException, IOException {
        
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext context = facesContext.getExternalContext();
        HttpServletResponse response = (HttpServletResponse) context.getResponse();
        ServletOutputStream outStream = response.getOutputStream();

        Connection conn = ds.getConnection();
        PreparedStatement selectQuery = conn.prepareStatement(
                "SELECT * FROM FILESTORAGE WHERE FILE_ID=?");
        selectQuery.setInt(1, fileID);

        ResultSet result = selectQuery.executeQuery();
        if (!result.next()) {
            facesContext.addMessage("downloadForm:download",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "file download failed for id = " + fileID, null));
        }

        String fileType = result.getString("FILE_TYPE");
        String fileName = result.getString("FILE_NAME");
        long fileSize = result.getLong("FILE_SIZE");
        Blob fileBlob = result.getBlob("FILE_CONTENTS");

        response.setContentType(fileType);
        response.setHeader("Content-Disposition", "attachment; filename=\""
                + fileName + "\"");

        final int BYTES = 1024;
        int length = 0;
        InputStream in = fileBlob.getBinaryStream();
        byte[] bbuf = new byte[BYTES];

        while ((in != null) && ((length = in.read(bbuf)) != -1)) {
            outStream.write(bbuf, 0, length);
        }

        outStream.close();
        conn.close();

        return null;
    }//end downloadFile

    public boolean isDescendDate() {
//        System.out.println("Getting order by... [decendDate: " + descendDate + "]");
        return descendDate;
    }

    public void setDescendDate(boolean descendDate) {
//        System.out.println("Setting order by... [descendDate: " + descendDate + "]");
        this.descendDate = descendDate;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    //  UPDATE FILTER                                                         //
    ////////////////////////////////////////////////////////////////////////////    
    public void updateFilter() throws SQLException{
                
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
//                System.out.println("Updating filter. descendDate: " + descendDate);
                String filterString = descendDate ? "date_desc" : "date_asc";
                PreparedStatement selectStatement = conn.prepareStatement("UPDATE config_settings SET order_by = ? WHERE user_id = ?");
                selectStatement.setString(1, filterString);
                selectStatement.setInt(2, 1);// TODO: change to use current user's id
                selectStatement.executeUpdate();
                
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
    }//end updateFilter
    
}//end BookManagementBean

