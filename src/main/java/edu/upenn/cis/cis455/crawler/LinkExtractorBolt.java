package edu.upenn.cis.cis455.crawler;

import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.routers.IStreamRouter;
import edu.upenn.cis.stormlite.bolt.IRichBolt;
import edu.upenn.cis.stormlite.bolt.OutputCollector;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Tuple;
import edu.upenn.cis.stormlite.tuple.Values;
import edu.upenn.cis.cis455.crawler.Crawler;
import edu.upenn.cis.cis455.storage.DatabaseEnv;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import edu.upenn.cis.cis455.crawler.HttpClient;
import edu.upenn.cis.cis455.crawler.HttpClient.headinfo;
import edu.upenn.cis.cis455.crawler.info.URLInfo;
import java.util.concurrent.ConcurrentHashMap;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class LinkExtractorBolt implements IRichBolt{
    Fields schema = new Fields("url");
	String exeID = UUID.randomUUID().toString();
	private OutputCollector collector;
	private Crawler master;
    private DatabaseEnv database;
    private  BlockingQueue<String> hostnameQueue;
    final static Logger logger = LogManager.getLogger(LinkExtractorBolt.class);
    private ConcurrentHashMap<String, List<String>> urlMap;
    
    
    public LinkExtractorBolt(){
         this.database=Crawler.getDb();
	    this.hostnameQueue=Crawler.gethostnameQueue();
	    this.urlMap=Crawler.geturlMap();
    }
    @Override
	public void execute(Tuple input){
	    String htmlstring = input.getStringByField("document");
	    String url=input.getStringByField("url");
	    Crawler.counter=Crawler.counter-1;
	    URLInfo info=new URLInfo(url);
	    Document doc = Jsoup.parse(htmlstring,info.tourl());
        Elements links = doc.select("a[href]");
        for (Element link : links){
            logger.debug("add link"+link.attr("abs:href"));
            this.equeue(info,link.attr("abs:href"));
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

    
}