/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wspFinalProject;

import com.sun.rowset.CachedRowSetImpl;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Resource;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.inject.Named;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import javax.servlet.http.Part;
import javax.sql.DataSource;
import javax.sql.rowset.CachedRowSet;

@Named
@RequestScoped
public class FileBean implements Serializable {
    
    private Part part;

    @Resource(name = "jdbc/db1")
    private DataSource ds;

    public ResultSet getList() throws SQLException {
        Connection conn = ds.getConnection();
        try {
            Statement stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery(
                    "SELECT FILE_ID, FILE_NAME, FILE_TYPE, FILE_SIZE FROM FILESTORAGE"
            );
            CachedRowSet crs = new CachedRowSetImpl();
            crs.populate(result);
            return crs;
        } finally {
            conn.close();
        }
    }

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
    }

    public void uploadFile() throws IOException, SQLException {

        FacesContext facesContext = FacesContext.getCurrentInstance();
        
        Connection conn = ds.getConnection();

        InputStream inputStream;
        inputStream = null;
        try {
            inputStream = part.getInputStream();
            PreparedStatement insertQuery = conn.prepareStatement(
                    "INSERT INTO FILESTORAGE (FILE_NAME, FILE_TYPE, FILE_SIZE, FILE_CONTENTS) "
                            + "VALUES (?,?,?,?)");
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
        long size = file.getSize();
        if (size <= 0) {
            throw new ValidatorException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            "the file is empty", null));
        }
        if (size > 1024 * 1024 * 10) { // 10 MB limit
            throw new ValidatorException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            size + "bytes: file too big (limit 10MB)", null));
        }
    }

    public Part getPart() {
        return part;
    }

    public void setPart(Part part) {
        this.part = part;
    }

}
