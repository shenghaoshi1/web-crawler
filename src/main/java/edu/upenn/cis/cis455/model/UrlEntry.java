package edu.upenn.cis.cis455.model;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class UrlEntry implements Serializable{
    private String url;
    private Integer docId;
    private String lastchecktime;
    
    public UrlEntry(Integer docId, String url){
        this.docId=docId;
        this.url=url;
        ZonedDateTime dateTime = ZonedDateTime.now();
        this.lastchecktime = dateTime.format(DateTimeFormatter.RFC_1123_DATE_TIME);
    }
    
    public Integer get_docId(){
        return this.docId;
    }
    public String get_url(){
        return this.url;
    }
    
    public String get_lastchecktime(){
        return this.lastchecktime;
    }
    
    public void update_lastchecktime(){
        ZonedDateTime dateTime = ZonedDateTime.now();
        this.lastchecktime = dateTime.format(DateTimeFormatter.RFC_1123_DATE_TIME);
        
    }
    public int get_hashCode(){
        return this.docId.hashCode();
    }
    public boolean match(Object another){
        if (!(another instanceof UrlEntry)){
            return false;
    }
    UrlEntry compare=(UrlEntry) another;
    if (compare.get_docId().equals(this.get_docId())){
        return true;
    }
    return false;
    }
    
}