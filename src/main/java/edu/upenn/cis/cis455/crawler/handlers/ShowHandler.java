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
public class ShowHandler implements Route{
    private DatabaseEnv db;
    public ShowHandler(DatabaseEnv db){
        this.db=db;
        
    }
     public String handle(Request req, Response resp){
         String name=req.queryParams("channel");
         Channel channel=this.db.get_channel(name);
         StringBuilder builder = new StringBuilder();
         builder.append("<html><head><title>Welcome to CIS 455/555 HW2</title></head>");
         builder.append("<body><div class=\"channelheader\">"+" channelname: "+name+ " created by: "+channel.get_user()+"\n" );
        builder.append("</div>");
         for(String url:channel.get_urls()){
             String doc=this.db.getDocument(url);
             
             builder.append("<ul>");
             builder.append("Crawled on:  "+this.db.getDocumentLastchecktime(url)+"\r\n"+"Location: "+"<li><a href='"+url+"'>"+url+"</a>"+"\n");
             builder.append("</ul>");
             builder.append("<div class=\"document\">"+doc+"</div>");
             
             
         }
         builder.append("</div>");
         builder.append("</body></html>"); 
         resp.type("text/html");
         return builder.toString();
         
     }
}