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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import edu.upenn.cis.cis455.model.DomToOccurrence;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import edu.upenn.cis.cis455.model.OccurrenceEvent;

public class DomParserBolt implements IRichBolt{
     Fields schema = new Fields("url", "occurentEvents");
	String exeID = UUID.randomUUID().toString();
	private OutputCollector collector;
	private Crawler master;
    private DatabaseEnv database;
    private String url;
    private String docId;
    private String content;
    final static Logger logger = LogManager.getLogger(DomParserBolt.class);

    public DomParserBolt(){
        this.database=Crawler.getDb();
    }
    @Override
	public void execute(Tuple input){
	this.url=input.getStringByField("url");
	this.content=input.getStringByField("document");
	
	//consider url as docId
	DomToOccurrence converter = new DomToOccurrence();
	try{
	Document doc = DomToOccurrence.getDomNode(this.content);
	List<OccurrenceEvent> events = converter.getOccurrenceEvents(url,doc);
	collector.emit(new Values<Object> (url, events));
	// for debug
//	for (int i=0;i<events.size();i++){
//	         logger.debug(i+": "+events.get(i).get_value()+" "+events.get(i).get_type());
//	         //logger.debug(i+": "+events.get(i).get_value().length()+" "+events.get(i).get_type());
//	     }
//	
   }
    catch (Exception e){
        logger.error("dom to occurrence failed");
        
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