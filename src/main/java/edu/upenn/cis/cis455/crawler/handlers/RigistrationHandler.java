package edu.upenn.cis.cis455.crawler.handlers;

import spark.Request;
import spark.Route;
import spark.Response;
import spark.HaltException;
import spark.Session;
import edu.upenn.cis.cis455.storage.StorageInterface;
import static spark.Spark.halt;
import edu.upenn.cis.cis455.storage.DatabaseEnv;
public class RigistrationHandler implements Route{
    private DatabaseEnv db;
    public RigistrationHandler(DatabaseEnv db){
        this.db=db;
        
    }
    @Override
    public String handle(Request req, Response resp){
        String user = req.queryParams("username");
        String pass = req.queryParams("password");
        String firstname=req.queryParams("firstname");
        String lastname=req.queryParams("lastname");
        if (user==null || pass==null || firstname==null || lastname==null){
            halt(400, "Invalid form");
            
        }
        else if (this.db.checkUser(user)){
            return "User already exists";
            
        }
        else{
            this.db.addUser(user,pass,firstname,lastname);
            resp.redirect("/login-form");
        }
        
        return "";
    }
    
}