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
import edu.upenn.cis.cis455.model.OccurrenceEvent;
import edu.upenn.cis.cis455.model.Channel;
import edu.upenn.cis.cis455.xpathengine.XPathEngineFactory;
import edu.upenn.cis.cis455.xpathengine.XPathEngineImpl;

public class MatchBolt implements IRichBolt{
    
    
    Fields schema = new Fields("channel");
	String exeID = UUID.randomUUID().toString();
	private OutputCollector collector;
	private DatabaseEnv database;
	private String[] expressions;
	private ArrayList<Channel> channels;
	List<OccurrenceEvent> events;
	String url;
	
    private boolean[] states;
    final static Logger logger = LogManager.getLogger(MatchBolt.class);
    public MatchBolt(){
        this.database=Crawler.getDb();
        this.channels=this.database.get_channels();
        this.expressions=new String [this.channels.size()];
        for (int i=0;i<this.channels.size();i++){
            this.expressions[i]=this.channels.get(i).get_XPath();
            
        }
        
    }
	 @Override
	public void execute(Tuple input){
	    url= input.getStringByField("url");
	   
	    events=(List<OccurrenceEvent>) input.getObjectByField("occurentEvents");
	     //logger.debug("the nodes for "+url);
//            for (int i=0;i<events.size();i++){
//	         logger.debug(i+": "+events.get(i).get_value()+" "+events.get(i).get_type());
//	         //logger.debug(i+": "+events.get(i).get_value().length()+" "+events.get(i).get_type());
//	     }
	
    
	    XPathEngineImpl engine=XPathEngineFactory.getXPathEngine();
	    engine.setXPaths(this.expressions);
	    for (int i=0;i<events.size();i++){
	        states=engine.evaluateEvent(events.get(i));
	    }
	    String name;
	    for (int i=0; i<states.length;i++){
	        if (states[i]){
	            name=channels.get(i).get_name();
	            this.database.add_urltochannel(name,url);
	            
	            
	            
	        }
	    }
	    
	    Crawler.counter_2=Crawler.counter_2-1;
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

