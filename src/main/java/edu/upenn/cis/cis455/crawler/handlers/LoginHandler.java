package edu.upenn.cis.cis455.crawler.handlers;

import spark.Request;
import spark.Route;
import spark.Response;
import spark.HaltException;
import spark.Session;
import edu.upenn.cis.cis455.storage.StorageInterface;
import edu.upenn.cis.cis455.storage.DatabaseEnv;

public class LoginHandler implements Route {
    DatabaseEnv db;
    
    public LoginHandler(DatabaseEnv db) {
        this.db = db;
    }

    @Override
    public String handle(Request req, Response resp) throws HaltException {
        String user = req.queryParams("username");
        String pass = req.queryParams("password");
        
        System.err.println("Login request for " + user + " and " + pass);
        if (db.getSessionForUser(user, pass)) {
            System.err.println("Logged in!");
            Session session = req.session();
            
            session.attribute("user", user);
            session.attribute("password", pass);
            System.out.println(db.getUser(user).getFirstName());
            session.attribute("firstname",db.getUser(user).getFirstName());
            session.attribute("lastname",db.getUser(user).getLastName());
            session.maxInactiveInterval(300);
            resp.redirect("/index.html");
        } else {
            System.err.println("Invalid credentials");
            resp.redirect("/login-form.html");
        }

            
        return "";
    }
}
