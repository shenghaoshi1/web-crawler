package edu.upenn.cis.cis455.xpathengine;

import java.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import edu.upenn.cis.cis455.model.OccurrenceEvent;

public class XPathEngineImpl implements XPathEngine{
    final static Logger logger = LogManager.getLogger(XPathEngineImpl.class);
    private ArrayList<ArrayList<XPathNode>> expressionNode;
    private String[] expressions=null;
    private HashMap <String,boolean[]> match=new  HashMap <String,boolean[]>();
    private HashMap <String, ArrayList<XPathNode>> doc_xpathnode=new HashMap <String, ArrayList<XPathNode>>();
    public void setXPaths(String[] expressions){
        this.expressions=expressions;
        expressionNode=new ArrayList<>();
        
        for (int i=0; i<expressions.length;i++){
            expressionNode.add(new ArrayList<XPathNode> ());
            
            String[] xpathnodes=expressions[i].split("/");
            
            for (String xpathnode: xpathnodes){
                if (xpathnode.equals("")){
                    continue;
                }
                String[] parts=xpathnode.split("\\[");
                XPathNode temp_node=new XPathNode(parts[0],true);
                for (String part:parts){
                   
                    if (part.contains("contains")){
                        temp_node.addcontains(part.split("\"")[1]);
                    }
                     else if (part.contains("text")){
                        temp_node.addtext(part.split("\"")[1]);
                    }
                    
                }
                expressionNode.get(i).add(temp_node);
            }
            
            
        }
        //logger.debug("size of expressionNode"+expressionNode.size());
        
    }
    
    public boolean isValid(int i){
        if (this.expressions==null || expressions[i]==null){
            return false;
        }
        String expression=expressions[i];
        if (!expression.startsWith("/")){
            return false;
        }
        String [] parts=expression.split("/");
        String text = " *text\\(\\) *= *\".*\" *";
        String contains = " *contains\\( *text\\(\\) *, *\".*\" *\\) *";
        String test = "((" + text + ")|(" + contains + "))";
        String name = "[a-z]+";
        String match = name + "(\\[" + test + "\\])*";
        for (String part:parts){
            if (!part.matches(match)){
                return false;
                
            }
        }
        return true;
        
        
    }
    public boolean isValid(String expression){
        if (!expression.startsWith("/")){
            return false;
        }
         String text = " *text\\(\\) *= *\".*\" *";
        String contains= " *contains\\( *text\\(\\) *, *\".*\" *\\) *";
        String test = "((" + text + ")|(" + contains + "))";
        String nodename = "[a-z]+";
        String step = nodename + "(\\[" + test + "\\])*";
        String XPath = "(/" + step + ")+";

        return expression.matches(XPath);
        
    }
    public void update(String docId){
        boolean[] matchstate=this.match.get(docId);
        ArrayList<XPathNode> xpathnodes=doc_xpathnode.get(docId);
        //logger.debug("document node size"+xpathnodes.size());
        for (int i=0;i<expressionNode.size();i++){
            ArrayList<XPathNode> expressionxpathnodes=expressionNode.get(i);
            
            if (matchstate[i] || xpathnodes.size()!=expressionxpathnodes.size()){
                //logger.debug("already True");
                continue;
                
            }
            boolean temp=true;
            for (int j=0;j<xpathnodes.size();j++){
//                if (xpathnodes.get(j).get_node().equals("title")){
//                    logger.debug("title information"+xpathnodes.size()+"j:"+j);
//                }
                if(!xpathnodes.get(j).match(expressionxpathnodes.get(j)))
                {
                    temp=false;
                    break;
                }
            }
            if (xpathnodes.get(xpathnodes.size()-1).get_node().equals("title")){
                //logger.debug("temp:"+temp);
            }
            if (!matchstate[i]){
            matchstate[i]=temp;
        }

            
        }
        this.match.put(docId,matchstate);
        
        
    }
    
    public boolean[] evaluateEvent(OccurrenceEvent event){
        String docId=event.get_docId();
        String type=event.get_type();
        String value=event.get_value();
        //logger.debug("type:"+type+" value:" +value);
        if (type.equals("open")){
            if (!match.containsKey(docId) || !doc_xpathnode.containsKey(docId)){
                boolean[] states= new boolean[this.expressions.length];
                //This will auto-initialize to false since boolean's default value is false.
                match.put(docId, states);
                ArrayList<XPathNode> xpathnodes=new ArrayList<XPathNode>();
                xpathnodes.add(new XPathNode(value,false));
                doc_xpathnode.put(docId,xpathnodes);
                }
            else{
                ArrayList<XPathNode> xpathnodes=doc_xpathnode.get(docId);
                xpathnodes.add(new XPathNode(value,false));
                doc_xpathnode.put(docId,xpathnodes);
                
            }
            this.update(docId);
            
        }
        else if (type.equals("close")){
            ArrayList<XPathNode> xpathnodes=doc_xpathnode.get(docId);
            if (xpathnodes!=null)
            {int index=xpathnodes.size()-1;
            if (xpathnodes.get(index).get_node().equals(value)){
                xpathnodes.remove(index);
                doc_xpathnode.put(docId,xpathnodes);
            }
            if (xpathnodes.isEmpty()){
                
            }
            
        }

        }
        else if (type.equals("text")){
            ArrayList<XPathNode> xpathnodes=doc_xpathnode.get(docId);
            if (xpathnodes!=null){
                XPathNode temp=xpathnodes.get(xpathnodes.size()-1);
                temp.set_txt(value);
                xpathnodes.set(xpathnodes.size()-1,temp);
                doc_xpathnode.put(docId,xpathnodes);
            }
            this.update(docId);
            
        }
        
        return match.get(docId);
        
    }
    
    
    
}