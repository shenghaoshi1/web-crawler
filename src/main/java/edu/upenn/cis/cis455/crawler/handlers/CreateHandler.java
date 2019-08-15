package edu.upenn.cis.cis455.crawler.handlers;
import edu.upenn.cis.cis455.model.DocumentEntry;
import spark.Request;
import spark.Route;
import spark.Response;
import spark.HaltException;
import spark.Session;
import edu.upenn.cis.cis455.storage.StorageInterface;
import static spark.Spark.halt;
import edu.upenn.cis.cis455.storage.DatabaseEnv;
import edu.upenn.cis.cis455.model.Channel;
import edu.upenn.cis.cis455.xpathengine.XPathEngineFactory;
import edu.upenn.cis.cis455.xpathengine.XPathEngineImpl;

public class CreateHandler implements Route{
    private DatabaseEnv db;
    public CreateHandler(DatabaseEnv db){
        this.db=db;
        
    }
    public String handle(Request req, Response resp){
        String name=req.params(":name");
        String xpath=req.queryParams("xpath");
        
        if (name==null || xpath==null){
            return "the create form is wrong";
        }
         XPathEngineImpl engine=XPathEngineFactory.getXPathEngine();
        if (!engine.isValid(xpath)){
            return "xpath is not valid";
            
        }
        Channel channel=new Channel(name,xpath);
        channel.set_user(req.attribute("user"));
        this.db.add_channel(name,channel);
        resp.redirect("/index.html");
        return null;
        
    }
    
    
    
}