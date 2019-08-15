package edu.upenn.cis.cis455.crawler;
import java.util.concurrent.TimeUnit;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.concurrent.ConcurrentHashMap;
import java.io.IOException;
import edu.upenn.cis.cis455.crawler.HttpClient;
import edu.upenn.cis.cis455.crawler.HttpClient.headinfo;

import edu.upenn.cis.cis455.crawler.info.URLInfo;
import edu.upenn.cis.cis455.storage.StorageInterface;
import edu.upenn.cis.cis455.storage.DatabaseEnv;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class CrawlerWorker extends Thread{
    final static Logger logger = LogManager.getLogger(CrawlerWorker.class);
    private ConcurrentHashMap<String, List<String>> urlMap;
     private BlockingQueue<String> hostnameQueue;
     
     private Crawler master;
     private DatabaseEnv database;
     
     public CrawlerWorker( Crawler master){
         setDaemon(true);
         this.master=master;
         this.database=this.master.getDb();
         this.urlMap=this.master.geturlMap();
         this.hostnameQueue=this.master.gethostnameQueue();
     }
    public void run(){
        while (!master.isDone()){
            try{
                String hostname=this.hostnameQueue.poll(10,TimeUnit.SECONDS);
                if (hostname!=null){
                    this.master.setWorking(true);
                    this.crawl(hostname);
                    this.master.setWorking(false);
                    ww
                }
                
        }

            catch (Exception e){
                logger.error("worker interuppted");
            }
        }
        this.master.notifyThreadExited();
    logger.debug("exit");
    logger.debug("working number"+this.master.workingnumber());
    }
     
     public void crawl(String hostname){
         URLInfo info=null;
         boolean parsedb=false;
         boolean parseurl=false;
         List<String> urls = this.urlMap.get(hostname);
         while (!master.isDone() && urls!=null && !urls.isEmpty()){
             info=new URLInfo(urls.remove(0));
             if (this.master.isOKtoCrawl(hostname,info.getPortNo(), info.isSecure())){
                 if (master.deferCrawl(hostname)){
                     urls.add(0,info.tourl());
                    //hostnameQueue.add(hostname);
                     
                 }
                 else if (this.master.isOKtoParse(hostname,info)){
                     logger.debug("allowed: "+info.tourl());
                     if (!master.havecrawled(info)){
                         logger.debug("have not crawled"+info.tourl());
                         if (master.getHeadInfo(info)==null){
                             headinfo headInfo;
                              String lastModified = database.getDocumentLastchecktime(info.tourl());
                               HttpClient connection = new HttpClient(info);
                                logger.debug("start to get headinfo");
                                headInfo = connection.getSecuredHeadInfo(info, lastModified);
                                
                                master.setHeadInfo(info, headInfo);
                         }
                         if (!this.master.canDownload(info)){
                             //logger.debug("parse from db: "+info.tourl());
                             parsedb=true;
                             
                         }
                         else if (master.checkHeadInfo(info)){
                             //logger.debug("parse from url: "+info.tourl());
                             parseurl=true;
                         }
                         
                         else{
                             logger.error("wrong headinfo:  " +info.tourl() );
                         }
                         
                     }
                     if (parsedb|| parseurl){
                         if (!urls.isEmpty()){
                             this.hostnameQueue.add(hostname);
                            
                         }
                          break;
            
                     
                     
                 }
                
                 
                 
                 
             }
              
         }
         
         }
         if (parsedb|| parseurl){
             master.incCount();
             master.addcrawled(info);
             logger.debug("boolean parseurl: "+parseurl);
             logger.debug("boolean parsedb: "+parsedb);
             if (parseurl){
                 logger.debug("start to parse from :"+info.tourl());
                 this.parseUrl(info);
             }
             if (parsedb){
                 this.parseDb(info);
             }
         }
         
     }
    
    public void addtoDb(String url, String content){
        if (this.database.getDocument(url)==null){
            this.database.addDocument(url,content);
        }
        else{
            this.database.modifyDocument(url,content);
        }
    }
    public synchronized void addtoQueue(String url){
        URLInfo info=new URLInfo(url);
        if (!this.urlMap.containsKey(info.getHostName()))
            {
        this.urlMap.put(info.getHostName(), new ArrayList<>());
        }
        this.urlMap.get(info.getHostName()).add(url);
        this.hostnameQueue.add(info.getHostName());
        

    }
    
    public void equeue(URLInfo info, String url){
        if (url.startsWith("http://") || url.startsWith("https://")){
            this.addtoQueue(url);
            return;
            
        }
        if (url.startsWith("/")){
            String absurl=(info.isSecure() ? "https://" : "http://") +
                info.getHostName() + (info.getPortNo() == 80 ? "" : ":" + info.getPortNo()) +
                url;
            this.addtoQueue(absurl);
            return;
        }
    }
    public void parseUrl(URLInfo info){
        try{
        logger.info(info.tourl()+" :downloading...");
        Document doc = Jsoup.connect(info.tourl()).userAgent("cis455crawler").get();
        //acquire htmlstring
        String htmlstring=doc.html();
        
        this.addtoDb(info.tourl(),htmlstring);
        
        //acquire href link
        Elements links = doc.select("a[href]");
        for (Element link : links){
            logger.debug("add link"+link.attr("abs:href"));
            this.equeue(info,link.attr("abs:href"));
        }
    

        logger.debug("dowload succeed"+info.tourl());
    }
    catch (IOException e){
        logger.error("download failed");
    }

        
    }
    public void parseDb(URLInfo info){
        logger.info(info.tourl() + ": Not modified");
        String htmlstring = database.getDocument(info.tourl());
        //logger.debug("htmlstring"+htmlstring);
        
           
        Document doc = Jsoup.parse(htmlstring,info.tourl());
        Elements links = doc.select("a[href]");
        for (Element link : links){
            //logger.debug("add link"+link.attr("abs:href"));
            this.equeue(info,link.attr("abs:href"));
        }
    

        
        
   }
   



}