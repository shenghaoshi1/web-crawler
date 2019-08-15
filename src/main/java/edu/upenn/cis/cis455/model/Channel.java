package edu.upenn.cis.cis455.model;

import java.io.Serializable;
import java.util.*;
public class Channel implements Serializable{
    private String name;
    private String XPath;
    private HashSet<String> urls=new HashSet<String>();
    private String user=null;
    public void set_user(String user){
        this.user=user;
        
    }
    public String get_user(){
        return this.user;
    }
    
    public Channel(String name, String XPath){
        this.name=name;
        this.XPath=XPath;
    
    }
    
    public String get_name(){
        return this.name;
    }
    
    public String get_XPath(){
        return this.XPath;
    }
    
    public HashSet<String> get_urls(){
        return this.urls;
    }
    
    public void add_url(String url){
        this.urls.add(url);
    }
    
    
    
}