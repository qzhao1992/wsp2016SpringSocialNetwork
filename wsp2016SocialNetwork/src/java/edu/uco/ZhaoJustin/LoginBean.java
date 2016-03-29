
package edu.uco.ZhaoJustin;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.inject.Named;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.SessionScoped;
import javax.sql.DataSource;


@Named(value = "loginBean")
@SessionScoped
public class LoginBean implements Serializable {
    
    private PersonInfo personInfo;
    private List<PersonInfo> listPersonInfo;
    
    @Resource(name="jdbc/db1")
    private DataSource ds;
    
    @PostConstruct
    public void init(){
        personInfo = new PersonInfo();
        listPersonInfo = new ArrayList();
    }
    
    public void setPersonInfoID(int pID){
        personInfo.setPersonID(pID);
    }
    
    public int getPersonInfoID(){
        return personInfo.getPersonID();
    }
    
    public void setPersonInfoUserName(String username){
        personInfo.setUserName(username);
    }
    
    public String getPersonInfoUserName(){
        return personInfo.getUserName();
    }
    
    public void setPersonInfoPassword(String password){
        personInfo.setPassword(password);
    }
    
    public String getPersonInfoPassword(){
        return personInfo.getPassword();
    }

    public PersonInfo getPersonInfo() {
        return personInfo;
    }

    public void setPersonInfo(PersonInfo personInfo) {
        this.personInfo = personInfo;
    }

    public List<PersonInfo> getListPersonInfo() {
        return listPersonInfo;
    }

    public void setListPersonInfo(List<PersonInfo> listPersonInfo) {
        this.listPersonInfo = listPersonInfo;
    }
    
    
    
    public void addToListPInfo() throws SQLException{
        if (ds == null) {
            throw new SQLException("ds is null; Can't get data source");
        }

        Connection conn = ds.getConnection();

        if (conn == null) {
            throw new SQLException("conn is null; Can't get db connection");
        }
        
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "select PID, USERNAME, PASSWORD from PERSONWSPFINAL"
            );
             // retrieve customer data from database
            ResultSet result = ps.executeQuery();
            while (result.next()) {
                PersonInfo p = new PersonInfo();
                p.setPersonID(result.getInt("PID"));
                System.out.println("Pid: " + result.getInt("PID"));
                p.setPersonName(result.getString("USERNAME"));
                p.setPassword(result.getString("PASSWORD"));
                
                checkDuplicateAndAdd(p);
                
            }
        }
        finally {
            conn.close();
        }
    }
    
    public void checkDuplicateAndAdd(PersonInfo p1){
        boolean found= false;
        for (PersonInfo pList : listPersonInfo) {
            if(pList.getPersonID() == p1.getPersonID()){
            
                found = true;
                
            }
        }
            if (!found) {
            
                listPersonInfo.add(p1);
            }
    }
    
    public String pressLoginButton() throws SQLException{
        //addToListPInfo();
        //CheckPasswordAndUserName();
        if(personInfo.getPassword().equalsIgnoreCase("password")){
            return "Home.xhtml";
        }
        else return "index.xhtml";
        
    }
    
    public boolean CheckPasswordAndUserName(){
        boolean check = false;
        for (PersonInfo pList : listPersonInfo) {
            if(pList.getUserName().equalsIgnoreCase(personInfo.getUserName()) &&
                    pList.getPassword().equals(personInfo.getPassword())){
            
                check = true;
                break;
            }
            
        }
        
        return check;
    }

//    public void insertToPersonalInfoDB() throws SQLException{
//    if(ds == null){
//        throw new SQLException("Can't get data source");
//    }
//    
//    Connection conn = ds.getConnection();
//        if (conn == null) {
//            throw new SQLException("Cannot get connection");
//        }
//        
//        try{
//            PreparedStatement ps = conn.prepareStatement("Insert into PERSONWSPFINAL(PID, USERNAME, PASSWORD)"
//                        + "VALUES(?,?,?,?)");
//            
//            ps.setString(1, personInfo.getTitle());
//            ps.setString(2, book.getAuthor());
//            ps.setDouble(3, book.getPrice());
//            ps.setInt(4, book.getPublication());
//            
//            ps.executeUpdate();
//        }
//        finally {
//            conn.close();
//        }
        
//}
    
}
