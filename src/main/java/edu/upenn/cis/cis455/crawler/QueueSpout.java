package edu.upenn.cis.cis455.crawler;

import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.routers.IStreamRouter;
import edu.upenn.cis.stormlite.spout.IRichSpout;
import edu.upenn.cis.stormlite.spout.SpoutOutputCollector;
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

public class QueueSpout implements IRichSpout{
    private  BlockingQueue<String> hostnameQueue;
    String exeID = UUID.randomUUID().toString();
	Fields schema = new Fields("hostname");
	private SpoutOutputCollector collector;
	private Crawler master;
     private DatabaseEnv database;
     final static Logger logger = LogManager.getLogger(QueueSpout.class);
	public QueueSpout(){
	    this.database=Crawler.getDb();
	    this.hostnameQueue=Crawler.gethostnameQueue();
	    
	}
	
	public QueueSpout(Crawler master){
	    this.master=master;
	    this.database=master.getDb();
	    this.hostnameQueue=this.master.gethostnameQueue();
	    
	    
	}
	@Override
	public String getExecutorId() {
		return exeID;
	}
	
	@Override
	public void open(Map<String, String> config, TopologyContext topo,
			SpoutOutputCollector collector) {
		this.collector = collector;
	}

	@Override
	public void close() {
		
	}
	@Override
	public void setRouter(IStreamRouter router) {
		this.collector.setRouter(router);
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer){
	    declarer.declare(schema);
	    
	}
	@Override
	public void nextTuple(){
	    if(!this.hostnameQueue.isEmpty()){
	        try{
	        String hostname=this.hostnameQueue.poll(10,TimeUnit.SECONDS);
	        if (hostname!=null){
	        collector.emit(new Values<Object>(hostname));
	        Crawler.counter=Crawler.counter+1;
        }

        }
        catch (Exception e){
            logger.error("cannot poll out a site");
            
        }

	    }
	    Thread.yield();
	    
	}
	}

    
    
    

