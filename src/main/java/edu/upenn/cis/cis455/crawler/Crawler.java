package edu.upenn.cis.cis455.crawler;
import java.util.HashSet;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.net.ssl.HttpsURLConnection;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.IOException;
import edu.upenn.cis.cis455.crawler.info.RobotsTxtInfo;
import edu.upenn.cis.cis455.crawler.info.URLInfo;
import edu.upenn.cis.cis455.storage.StorageFactory;
import edu.upenn.cis.cis455.storage.StorageInterface;
import edu.upenn.cis.cis455.storage.DatabaseEnv;
import edu.upenn.cis.cis455.crawler.CrawlerWorker;
import edu.upenn.cis.cis455.crawler.HttpClient;
import edu.upenn.cis.cis455.crawler.HttpClient.headinfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Level;
import edu.upenn.cis.cis455.crawler.info.URLInfo;
import edu.upenn.cis.cis455.crawler.info.RobotsTxtInfo;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.upenn.cis.stormlite.Config;
import edu.upenn.cis.stormlite.LocalCluster;
import edu.upenn.cis.stormlite.Topology;
import edu.upenn.cis.stormlite.TopologyBuilder;
import edu.upenn.cis.stormlite.tuple.Fields;


public class Crawler implements CrawlMaster {
    static final int NUM_WORKERS = 10;
    final static Logger logger = LogManager.getLogger(Crawler.class);
    private String startUrl;
    private  static int size;
    public  static int count;
    private static DatabaseEnv db;
    public  static HashMap<String,RobotsTxtInfo> robots=new HashMap<String,RobotsTxtInfo>();
    private static ArrayList <CrawlerWorker> workers = new ArrayList<CrawlerWorker>();
    private  static BlockingQueue<String> hostnameQueue = new LinkedBlockingQueue<String>();
    public static int counter;
    public static int counter_2;
    private static ConcurrentHashMap<String,List<String>> urlMap = new ConcurrentHashMap<String,List<String>>();
    private  static HashSet<String> crawledurl = new HashSet<String>();
    public  static Map<String, Long> accessTimes = new HashMap<>();
    public static int crawldone=0;
    public  static int workingcount=0;
    private int shutdown=0;
    private  static HashMap<String, headinfo> headMap = new HashMap<>();
/**
     * Main thread
     */
    public void start(){
        for (int i=0;i<NUM_WORKERS;i++){
            CrawlerWorker worker=new CrawlerWorker(this);
            workers.add(worker);
            worker.start();
            
        }
    }
    
    
    public Crawler(String startUrl, DatabaseEnv setdb, int setsize, int count) {
         robots=new HashMap<String,RobotsTxtInfo>();
         hostnameQueue = new LinkedBlockingQueue<String>();
          urlMap = new ConcurrentHashMap<String,List<String>>();
          crawledurl = new HashSet<String>();
           headMap = new HashMap<>();
          accessTimes = new HashMap<>();
          counter=0;
          counter_2=0;
        this.startUrl=startUrl;
         db=setdb;
        size=setsize;
        this.count=count;
        //put the first url into queue
        crawldone=0;
        workingcount=0;
        URLInfo info=new URLInfo(this.startUrl);
        logger.debug("url:"+this.startUrl);
        logger.debug("infourl:"+info.tourl());
        logger.debug("firsthostname:"+info.getHostName());
        hostnameQueue.add(info.getHostName());
        urlMap.put(info.getHostName(),new ArrayList<String>());
        urlMap.get(info.getHostName()).add(this.startUrl);
    }
    
    public  static DatabaseEnv getDb(){
        return db;
    }
    
    public  static ConcurrentHashMap<String,List<String>> geturlMap(){
        return urlMap;
    }
    
    public static  BlockingQueue<String> gethostnameQueue(){
        return  hostnameQueue;
    }
    
    
    
    ///// TODO: you'll need to flesh all of this out.  You'll need to build a thread
    // pool of CrawlerWorkers etc. and to implement the functions below which are
    // stubs to compile
    
    public static  void downloadRobot(String site, int port, boolean isSecure){
        logger.debug("downloading...Robot.txt"+site);
        try{
        RobotsTxtInfo robot = new RobotsTxtInfo();
        URLInfo initialinfo=new URLInfo(site);
       
        String roboturl = (isSecure ? "https://" : "http://") + site + ((port != 80) ? ":" + port : "") + "/robots.txt";
         URL url=new URL(roboturl);
         URLInfo robotinfo=new URLInfo(roboturl);
         InputStream stream = HttpClient.downloadPage(robotinfo, url);
         String line;
         BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
         String user_agent=null;
          while ((line = reader.readLine()) != null){
              if (line.startsWith("Location:")){
                  
                  roboturl=line.replace("Location:","");
                  url=new URL(roboturl);
                  robotinfo=new URLInfo(roboturl);
                  stream = HttpClient.downloadPage(robotinfo, url);
                  reader = new BufferedReader(new InputStreamReader(stream));
                  continue;
                  
                  
                  
              }
              if (line.startsWith("User-agent:")){
                  user_agent=line.replace("User-agent: ","");
                  robot.addUserAgent(user_agent);
                  continue;
              }
              
              if (line.startsWith("Disallow:")){
                  robot.addDisallowedLink(user_agent,line.replace("Disallow: ",""));
                  continue;
                  
              }
              
              if (line.startsWith("Allow:")){
                  robot.addAllowedLink(user_agent,line.replace("Allow: ",""));
                  continue;
              }
              
              if(line.startsWith("Crawl-delay:")){
                  robot.addCrawlDelay(user_agent, Integer.valueOf(line.replace("Crawl-delay: ","")));
                  accessTimes.put(site,System.currentTimeMillis());
                  continue;
              }
              
              if (line.startsWith("Sitemap:")){
                  robot.addSitemapLink(line.replace("Sitemap: ", ""));
                  continue;
                  
              }
              
              
          }
          robots.put(site,robot);
          logger.debug("download robot.txt succeed! "+site);
     }
     catch (Exception e){
         logger.error("download failed"+site);
     }

    }
    public static boolean OK(RobotsTxtInfo robot, String path){
        if ((robot.getDisallowedLinks("*") != null && robot.getDisallowedLinks("*").contains(path))||(robot.getDisallowedLinks("cis455crawler") != null && robot.getDisallowedLinks("cis455crawler").contains(path)) ){
            return false;
        }
        if (robot.getAllowedLinks("cis455crawler") != null && robot.getAllowedLinks("cis455crawler").contains(path)){
            return true;
        }
        return true;
    }
    
    /**
     * Main thread
     */
    
    /**
     * Returns true if it's permissible to access the site right now
     * eg due to robots, etc.
     */
    public   boolean isOKtoCrawl(String site, int port, boolean isSecure) {
        if (!robots.containsKey(site)){
        downloadRobot(site,port,isSecure);
    }

        if (!robots.containsKey(site)){
            return true;
        }
        return OK(robots.get(site),"/");
        
        
         }

    /**
     * Returns true if the crawl delay says we should wait
     */
    public boolean deferCrawl(String site) {
        if (!accessTimes.containsKey(site)){
            return false;
        }
        RobotsTxtInfo robot = robots.get(site);
        if (robot==null){
            return false;
            
        }
        else{
             Integer Delay = robot.getCrawlDelay("cis455crawler");
             if (Delay==null){
                 return false;
             }
             else{
                 return System.currentTimeMillis() - accessTimes.get(site) < 1000 * Delay;
             }
        }
        
         }
    
    /**
     * Returns true if it's permissible to fetch the content,
     * eg that it satisfies the path restrictions from robots.txt
     */
    public  boolean isOKtoParse(String site,URLInfo url) {
        if (!robots.containsKey(site)){
            return true;
        }
        
        return OK(robots.get(site),url.getFilePath()) && this.OK(robots.get(site),url.getFilePath()+"/");
        
         }
    
    /**
     * Returns true if the document content looks worthy of indexing,
     * eg that it doesn't have a known signature
     */
    public boolean isIndexable(String content) { return true; }
    
    /**
     * We've indexed another document
     */
    public void incCount() {
        crawldone=crawldone+1;
        
    }
    
    /**
     * Workers can poll this to see if they should exit, ie the
     * crawl is done
     */
    public  boolean isDone() {
        if (crawldone>=count && counter_2==0){
            return true;
       }
//       if (crawldone>=count){
//           return true;e3
//       }
        
        return false; }
    public int workingnumber(){
        logger.debug("queue size"+hostnameQueue.size());
        return this.workingcount;
    }
    
    /**
     * Workers should notify when they are processing an URL
     */
    public synchronized void setWorking(boolean working) {
        if (working){
            this.workingcount=this.workingcount+1;
        }
        else{
            this.workingcount=this.workingcount-1;
        }
    }
    public static boolean havecrawled(URLInfo url){
        return crawledurl.contains(url.tourl());
    }
    
    public static void addcrawled(URLInfo url){
        crawledurl.add(url.tourl());
    }
    
    public static headinfo getHeadInfo(URLInfo url) {
        return headMap.get(url.tourl());
    }
    
    public static  void setHeadInfo(URLInfo url, headinfo headInfo) {
        headMap.put(url.tourl(), headInfo);
    }
    
    public static  boolean canDownload(URLInfo url) {
        headinfo headInfo = headMap.get(url.tourl());
        return headInfo.getModified();
    }

    
    
    public static boolean checkHeadInfo(URLInfo url) {
        headinfo headInfo = headMap.get(url.tourl());
        
        int contentLength = headInfo.getContentLength();
        String contentType = headInfo.getContentType();
        return contentLength != -1 && contentLength <= size * 1000000 && 
               (contentType.contains("text/html") || contentType.contains("text/xml") || contentType.contains("application/xml") || contentType.contains("+xml"));
    }

    
    /**
     * Workers should call this when they exit, so the master
     * knows when it can shut down
     */
    public synchronized void notifyThreadExited() {
        this.shutdown=this.shutdown+1;
    }
    public void waitforThread(){
        while (!isDone()){
           try{ Thread.sleep(10);}
           catch (InterruptedException e){
               logger.error("thread abnormal");
               
           }
          
        }
         logger.debug("all threads end");
    }
    public void close(){
        db.close();
    }
    public void stormCrawl() throws InterruptedException{
        String Queue_Spout="QUEUE_SPOUT";
        String DocFetcher_Bolt="DOCFETCHER_BOLT";
        String LinkExtractor_Bolt="LINKEXTRCTOR_BOLT";
        String DomParser_Bolt="DomParser_BOLT";
        String Match_Bolt="MATCH_BOLT";
        Config config = new Config();
        
        QueueSpout spout=new QueueSpout();
        DocFetcherBolt docfetcher=new  DocFetcherBolt ();
        LinkExtractorBolt linkextractor=new LinkExtractorBolt();
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout(Queue_Spout, new QueueSpout(), 2);
        
         builder.setBolt(DocFetcher_Bolt, new  DocFetcherBolt(), 2).shuffleGrouping(Queue_Spout);
         
         builder.setBolt( LinkExtractor_Bolt, new LinkExtractorBolt(), 4).shuffleGrouping(DocFetcher_Bolt);
         builder.setBolt(DomParser_Bolt,new DomParserBolt(),4).shuffleGrouping(DocFetcher_Bolt);
         builder.setBolt(Match_Bolt,new MatchBolt(),4).shuffleGrouping(DomParser_Bolt);
         
        LocalCluster cluster = new LocalCluster();
        Topology topo = builder.createTopology();
        ObjectMapper mapper = new ObjectMapper();
		try {
			String str = mapper.writeValueAsString(topo);

			System.out.println("The StormLite topology is:\n" + str);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		cluster.submitTopology("crawler", config, builder.createTopology());
		waitforThread();
		
		cluster.killTopology("crawler");
		cluster.shutdown();
    }


    
    /**
     * Main program:  init database, start crawler, wait
     * for it to notify that it is done, then close.
     */
    public static void main(String args[]) {
        org.apache.logging.log4j.core.config.Configurator.setLevel("edu.upenn.cis.cis455", Level.DEBUG);
        if (args.length < 3 || args.length > 5) {
            System.out.println("Usage: Crawler {start URL} {database environment path} {max doc size in MB} {number of files to index}");
            System.exit(1);
        }
        
        System.out.println("Crawler starting");
        String startUrl = args[0];
        System.out.println("startUrl:"+startUrl);
        String envPath = args[1];
        Integer size = Integer.valueOf(args[2]);
        Integer count = args.length == 4 ? Integer.valueOf(args[3]) : 100;
        
        DatabaseEnv db = StorageFactory.getDatabaseInstance(envPath);
        //below is a test for channel;
        //db.add_channel("CNN","/rss/channel/title[contains(text(),\"CNN\")]");                                                                                   
        
        Crawler crawler = new Crawler(startUrl, db, size, count);
        
        System.out.println("Starting crawl of " + count + " documents, starting at " + startUrl);
        try{
        crawler.stormCrawl();}
        catch(Exception e){
            logger.error("storm error");
        }
        
        
        while (!crawler.isDone())
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
        logger.debug("CNN channel: "+Crawler.db.get_channel("CNN").get_urls().size());
        logger.debug("sports channel: "+Crawler.db.get_channel("sports").get_urls().size());
        //logger.debug("CNN channel: "+Crawler.db.get_channel("sports").get_XPath());
        //crawler.waitforThread();
        crawler.close();
        System.out.println("Done crawling!");
        System.exit(0);
    }

}
