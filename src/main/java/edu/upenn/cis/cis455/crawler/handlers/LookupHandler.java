package edu.upenn.cis.cis455.crawler.handlers;

import spark.Request;
import spark.Route;
import spark.Response;
import spark.HaltException;
import spark.Session;
import edu.upenn.cis.cis455.storage.StorageInterface;
import edu.upenn.cis.cis455.storage.DatabaseEnv;

public class LookupHandler implements Route{
    
    DatabaseEnv db;
    public LookupHandler (DatabaseEnv db){
        this.db=db;
    }
    @Override
    public String handle(Request req, Response resp){
        if(req.queryParams("url").endsWith("xml")){
            resp.type("xml");
        }
        else{resp.type("text/html");}
        return db.getDocument(req.queryParams("url"));
    }
}