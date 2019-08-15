package edu.upenn.cis.cis455.crawler.handlers;

import spark.Request;
import spark.Route;
import spark.Response;
import spark.HaltException;
import spark.Session;
import edu.upenn.cis.cis455.storage.StorageInterface;
import edu.upenn.cis.cis455.storage.DatabaseEnv;

public class LogoutHandler implements Route{
    
    public String handle(Request req, Response resp){
        Session session = req.session(false);
        session.invalidate();
        resp.redirect("/login.html");
        return "";
    }
}