package edu.upenn.cis.cis455.model;

import java.io.Serializable;

public class User implements Serializable {
    Integer userId;
    String userName;
    String password;
    String firstname;
    String lastname;
    
    public User(Integer userId, String userName, String password, String firstname, String lastname) {
        this.userId = userId;
        this.userName = userName;
        this.password = password;
        this.firstname=firstname;
        this.lastname=lastname;
    }
    
    public Integer getUserId() {
        return userId;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public String getPassword() {
        return password;
    }
    public String getFirstName(){
        return this.firstname;
    }
    public String getLastName(){
        return this.lastname;
    }
}
