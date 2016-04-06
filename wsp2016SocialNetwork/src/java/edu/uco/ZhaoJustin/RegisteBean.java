/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uco.ZhaoJustin;

import java.io.Serializable;
import static java.lang.Character.isUpperCase;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.sql.DataSource;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 *
 * @author Qing
 */
@Named(value = "registeBean")
@RequestScoped
public class RegisteBean {
    //New one
    private PersonInfo rgPersonInfo;
    private List<PersonInfo> rgListPersonInfo;
    
    @Resource(name="jdbc/db1")
    private DataSource ds;
    
    @PostConstruct
    public void init(){
        rgPersonInfo = new PersonInfo();
        rgListPersonInfo = new ArrayList();
    }
    
    public void setRGPersonInfoID(int pID){
        rgPersonInfo.setPersonID(pID);
    }
    
    public int getRGPersonInfoID(){
        return rgPersonInfo.getPersonID();
    }
    
    public void setRGPersonInfoUserName(String username){
        rgPersonInfo.setUserName(username);
    }
    
    public String getRGPersonInfoUserName(){
        return rgPersonInfo.getUserName();
    }
    
    public void setRGPersonInfoPassword(String password){
        rgPersonInfo.setPassword(password);
    }
    
    public String getRGPersonInfoPassword(){
        return rgPersonInfo.getPassword();
    }
    
    public void validateRGUserName(FacesContext context, 
            UIComponent componentToValidate, Object value)
            throws ValidatorException{
        
        boolean isCaptal = false;
        String id = (String) value;
        
        if(id.length() < 5){
            FacesMessage message = 
                    new FacesMessage("Username must be more than 5 letter");
            throw new ValidatorException(message);
        }
    }
    
    public void validateRGPassword(FacesContext context, 
            UIComponent componentToValidate, Object value)
            throws ValidatorException{
        
        boolean isCaptal = false;
        String id = (String) value;
        for(int i = 0; i < id.length(); i ++){
            if(isUpperCase(id.charAt(i))){
                isCaptal = true;
            }
        }
        if(id.length() < 5 || isCaptal == false){
            FacesMessage message = 
                    new FacesMessage("Password must more than 5 letter and at leat one captal letter");
            throw new ValidatorException(message);
        }
    }
    
    public void insertToDatabase() throws SQLException {
        if (ds == null) {
            throw new SQLException("Cannot get DataSource");
        }

        Connection conn = ds.getConnection();
        if (conn == null) {
            throw new SQLException("Cannot get connection");
        }
        
        try{
            PreparedStatement ps = conn.prepareStatement("Insert into PERSONWSPFINAL(USERNAME, PASSWORD)"
                        + "VALUES(?,?)");
            ps.setString(1, rgPersonInfo.getUserName());
            ps.setString(2, rgPersonInfo.getPassword());
            ps.executeUpdate();
        }
        finally {
            conn.close();
        }
    }
    
    public String pressRegisteButton() throws SQLException{
        insertToDatabase();
        return "index.xhtml";
    }
    
   
}
