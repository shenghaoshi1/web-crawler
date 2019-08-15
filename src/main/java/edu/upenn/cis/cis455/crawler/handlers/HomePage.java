package edu.upenn.cis.cis455.crawler.handlers;

import spark.Request;
import spark.Route;
import spark.Response;
import spark.HaltException;
import spark.Session;
import edu.upenn.cis.cis455.storage.StorageInterface;
import edu.upenn.cis.cis455.storage.DatabaseEnv;
import edu.upenn.cis.cis455.model.Channel;
import java.util.*;



public class HomePage implements Route{
    private DatabaseEnv db;
    public HomePage(DatabaseEnv db){
        this.db=db;
        
    }
    public String handle(Request req, Response resp){
         StringBuilder builder = new StringBuilder();
        
        builder.append("<html><head><title>Welcome to CIS 455/555 HW2</title></head>");
        builder.append("<body><h1>Welcome to CIS 455/555 HW2</h1>");
        builder.append("Welcome, " +req.session().attribute("firstname")+" "+req.session().attribute("lastname"));
        
        builder.append("<ul>");
        builder.append("<li><a href='/login-form.html'>Log in as a different user</a>");
        builder.append("<li><a href='/register.html'>Register a user</a>");
        builder.append("<li><a href='/logout'>Log out</a>");
        ArrayList<Channel> channels=this.db.get_channels();
        for (Channel channel:channels){
            builder.append("<li><a href='/show?channel="+channel.get_name()+"'>"+" channelname:"+channel.get_name() +"</a>");
        }
        builder.append("</ul>");
        
        builder.append("</body></html>");
        
        resp.type("text/html");
        return builder.toString();
        
    }
    
}