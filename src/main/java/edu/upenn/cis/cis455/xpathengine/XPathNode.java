package edu.upenn.cis.cis455.xpathengine;

import java.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class XPathNode{
    final static Logger logger = LogManager.getLogger(XPathNode.class);
    
    private String node=null;
    
    private String txt=null;
    
    private LinkedList<String> texts=new LinkedList <String>();
    private LinkedList<String> contains=new LinkedList <String>();
    
    private boolean part;
    
    public String get_node(){
        return this.node;
    }
    
    public String get_txt(){
        return this.txt;
    }
    public void set_txt(String txt){
        this.txt=txt;
    }
    
    public LinkedList<String> get_texts(){
        return this.texts;
    }
    
    public LinkedList<String> get_contains(){
        return this.contains;
    }
    public boolean get_part(){
        return this.part;
    }
    
    public XPathNode(String node, boolean part){
        this.node=node;
        this.part=part;
    }
    
    public void addcontains(String contain){
        this.contains.add(contain);
        
    }
    
    public void addtext(String text){
        this.texts.add(text);
    }
    
    public boolean match(XPathNode Node){
        if (this.part || !Node.get_part()){
            logger.error("Not a document or a expression");
            
            return false;
        }
//        if (this.node.equals("title")){
//            logger.debug(this.node);
//            logger.debug(Node.get_node());
//            logger.debug(this.txt);
//            logger.debug(Node.get_contains());
//        }
        if (!this.node.equals(Node.get_node())){
            
           // logger.debug("the node is different");
           // logger.debug(this.node);
           // logger.debug(Node.get_node());
            return false;
        }
        //logger.debug("match");
        //logger.debug(this.node);
        //logger.debug(Node.get_node());
        
        if (Node.get_texts().isEmpty() && Node.get_contains().isEmpty()){
            return true;
        }
        if (this.txt!=null){
            for (String text: Node.get_texts()){
                if (this.txt.toLowerCase().equals(text.toLowerCase())){
                    logger.debug("match text(): "+text);
                    return true;
                }
            }
            
            for (String contain: Node.get_contains()){
                if (this.txt.toLowerCase().contains(contain.toLowerCase())){
                    logger.debug("match contain()"+contain);
                    return true;
                }
                
            }
            
            
        }
        return false;
        
    }
    
    public String totxt(){
        String output=this.node;
        if (!this.part){
            return output+"[" + txt + "]";
        }
        for (String text : this.texts) {
            output =output+ "[text() = \"" + text + "\"]";
        }
        for (String contain : this.contains) {
            output =output+ "[contains(text(), \"" + contain + "\")]";
        }
        return output;
    }
    
    
}