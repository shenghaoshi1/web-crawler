package edu.upenn.cis.cis455.model;
import java.util.*;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Tuple;

/**
 * TODO: this class encapsulates the data from a keyword "occurrence"
 */
public class OccurrenceEvent {
    //three  value of type:{"open","text","close"}
    private String docId;
    private String type;
    private String value;
    
    public OccurrenceEvent(String docId, String type, String value){
        this.docId=docId;
        this.type=type;
        this.value=value;
    }
    
    public String get_docId(){
        return this.docId;
    }
    public String get_type(){
        return this.type;
    }
    
    public String get_value(){
        return this.value;
    }
    
    
}
