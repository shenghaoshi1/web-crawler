package edu.upenn.cis.cis455.crawler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static spark.Spark.*;
import edu.upenn.cis.cis455.crawler.handlers.LoginFilter;
import edu.upenn.cis.cis455.storage.StorageFactory;
import edu.upenn.cis.cis455.storage.StorageInterface;
import edu.upenn.cis.cis455.crawler.handlers.LoginHandler;
import edu.upenn.cis.cis455.crawler.handlers.RigistrationHandler;
import edu.upenn.cis.cis455.crawler.handlers.LookupHandler;
import edu.upenn.cis.cis455.crawler.handlers.ShowHandler;
import edu.upenn.cis.cis455.crawler.handlers.LogoutHandler;
import edu.upenn.cis.cis455.crawler.handlers.HomePage;
import edu.upenn.cis.cis455.crawler.handlers.CreateHandler;
import edu.upenn.cis.cis455.storage.DatabaseEnv;
public class WebInterface {
    public static void main(String args[]) {
        if (args.length < 1 || args.length > 2) {
            System.out.println("Syntax: WebInterface {path} {root}");
            System.exit(1);
        }
        
        if (!Files.exists(Paths.get(args[0]))) {
            try {
                Files.createDirectory(Paths.get(args[0]));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        port(8080);
         DatabaseEnv database = StorageFactory.getDatabaseInstance(args[0]);
        
        LoginFilter testIfLoggedIn = new LoginFilter(database);
        
        if (args.length == 2) {
            staticFiles.externalLocation(args[1]);
            staticFileLocation(args[1]);
        }

            
        before("/*", "POST", testIfLoggedIn);
        // TODO:  add /register, /logout, /index.html, /, /lookup
         get("/index.html", new HomePage(database));
         get("/", new HomePage(database));
        post("/register", new RigistrationHandler(database));
        post("/login", new LoginHandler(database));
        get("/logout", new LogoutHandler());
        get("/lookup", new LookupHandler(database));
        get("/show",new ShowHandler(database));
        get("/create/:name",new CreateHandler(database));
        
        awaitInitialization();
    }
}
