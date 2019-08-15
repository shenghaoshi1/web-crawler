package edu.upenn.cis.cis455.crawler;

import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.routers.IStreamRouter;
import edu.upenn.cis.stormlite.bolt.IRichBolt;
import edu.upenn.cis.stormlite.bolt.OutputCollector;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Tuple;
import edu.upenn.cis.stormlite.tuple.Values;
import  edu.upenn.cis.cis455.crawler.Crawler;
import edu.upenn.cis.cis455.storage.DatabaseEnv;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import edu.upenn.cis.cis455.crawler.HttpClient;
import edu.upenn.cis.cis455.crawler.Crawler;
import edu.upenn.cis.cis455.crawler.HttpClient.headinfo;
import edu.upenn.cis.cis455.crawler.info.URLInfo;
import java.util.concurrent.ConcurrentHashMap;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import edu.upenn.cis.cis455.crawler.info.RobotsTxtInfo;
public class DocFetcherBolt implements IRichBolt{
    
    Fields schema = new Fields("url", "document");
	String exeID = UUID.randomUUID().toString();
	private OutputCollector collector;
	private Crawler master;
    private DatabaseEnv database;
    private  BlockingQueue<String> hostnameQueue;
    final static Logger logger = LogManager.getLogger(DocFetcherBolt.class);
    private ConcurrentHashMap<String, List<String>> urlMap;
    public DocFetcherBolt(){
        this.database=Crawler.getDb();
	    this.hostnameQueue=Crawler.gethostnameQueue();
	    this.urlMap=Crawler.geturlMap();
        
    }
    public  boolean isDone() {
        if (Crawler.crawldone>=Crawler.count){
            return true;
        }
        return false;
   } 

   
    @Override
	public void execute(Tuple input){
	    //logger.debug("start execute");
	    
	    String hostname = input.getStringByField("hostname");
	    
	    logger.debug("gethostname: "+hostname);
	    URLInfo info=null;
         boolean parsedb=false;
         boolean parseurl=false;
         List<String> urls = this.urlMap.get(hostname);
         //logger.debug(urls.get(0));
         logger.debug("isdone"+this.isDone()); 
         while (!this.isDone() && urls!=null && !urls.isEmpty()){
             info=new URLInfo(urls.remove(0));
             logger.debug("info"+info.tourl());
             if (this.isOKtoCrawl(hostname,info.getPortNo(), info.isSecure())){
                 logger.debug("ok to crawl");
                 if (this.deferCrawl(hostname)){
                     urls.add(0,info.tourl());
                    //hostnameQueue.add(hostname);
                    logger.debug("defer");
                     
                 }
                 else if (this.isOKtoParse(hostname,info)){
                     logger.debug("allowed: "+info.tourl());
                     if (!Crawler.havecrawled(info)){
                         logger.debug("have not crawled"+info.tourl());
                         if (Crawler.getHeadInfo(info)==null){
                             headinfo headInfo;
                              String lastModified = database.getDocumentLastchecktime(info.tourl());
                               HttpClient connection = new HttpClient(info);
                                logger.debug("start to get headinfo");
                                headInfo = connection.getSecuredHeadInfo(info, lastModified);
                                
                                Crawler.setHeadInfo(info, headInfo);
                         }
                         if (!Crawler.canDownload(info)){
                             //logger.debug("parse from db: "+info.tourl());
                             parsedb=true;
                             
                         }
                         else if (Crawler.checkHeadInfo(info)){
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
     String content=null;
     if (parsedb|| parseurl){
             this.incCount();
             Crawler.addcrawled(info);
             logger.debug("boolean parseurl: "+parseurl);
             logger.debug("boolean parsedb: "+parsedb);
             if (parseurl){
                 logger.debug("start to parse from :"+info.tourl());
                 content=this.parseUrl(info);
             }
             if (parsedb){
                 
                  content=this.parseDb(info);
             }
//        logger.debug("info"+info.tourl());
//         logger.debug("content "+content);
         collector.emit(new Values<Object> (info.tourl(), content));
         
         Crawler.counter_2=Crawler.counter_2+1;
        // logger.debug("have emitted: " + info.tourl()); 
         }
        

	}
	 public void incCount() {
        Crawler.crawldone=Crawler.crawldone+1;
        
    }
    
    
    
     public void addtoDb(String url, String content){
        if (this.database.getDocument(url)==null){
            this.database.addDocument(url,content);
        }
        else{
            this.database.modifyDocument(url,content);
        }
    }
    public String parseUrl(URLInfo info){
        try{
        logger.info(info.tourl()+" :downloading...");
        Document doc = Jsoup.connect(info.tourl()).userAgent("cis455crawler").get();
        //acquire htmlstring
        String htmlstring=doc.html();
        
        this.addtoDb(info.tourl(),htmlstring);
        
        //acquire href link
       
        logger.debug("dowload raw content succeed"+info.tourl());
        return htmlstring;
        
    }
    catch (Exception e){
        logger.error("download failed");
    }
    return null;
        
    }
    
    public String parseDb(URLInfo info){
        logger.info(info.tourl() + ": Not modified");
        String htmlstring = database.getDocument(info.tourl());
        //logger.debug("htmlstring"+htmlstring);
        
           
        //Document doc = Jsoup.parse(htmlstring,info.tourl());
        

        return htmlstring;
        
   }
    
    
    @Override
	public String getExecutorId() {
		return exeID;
	}
	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(schema);
	}
	@Override
	public void cleanup() {
		
	}
	@Override
	public void prepare(Map<String, String> stormConf, TopologyContext context,
			OutputCollector collector) {
		this.collector = collector;
	}
	@Override
	public void setRouter(IStreamRouter router) {
		this.collector.setRouter(router);
		
	}
	@Override
	public Fields getSchema() {
		return schema;
	}
public   boolean isOKtoCrawl(String site, int port, boolean isSecure) {
        if (!Crawler.robots.containsKey(site)){
        Crawler.downloadRobot(site,port,isSecure);
    }

        if (!Crawler.robots.containsKey(site)){
            return true;
        }
        return Crawler.OK(Crawler.robots.get(site),"/");
        
        
         }
         
    public boolean deferCrawl(String site) {
        if (!Crawler.accessTimes.containsKey(site)){
            return false;
        }
        RobotsTxtInfo robot =Crawler.robots.get(site);
        if (robot==null){
            return false;
            
        }
        else{
             Integer Delay = robot.getCrawlDelay("cis455crawler");
             if (Delay==null){
                 return false;
             }
             else{
                 return System.currentTimeMillis() - Crawler.accessTimes.get(site) < 1000 * Delay;
             }
        }
        
         }
         
    public  boolean isOKtoParse(String site,URLInfo url) {
        if (!Crawler.robots.containsKey(site)){
            return true;
        }
        
        return Crawler.OK(Crawler.robots.get(site),url.getFilePath()) && Crawler.OK(Crawler.robots.get(site),url.getFilePath()+"/");
        
         }
    
    
    
    
}