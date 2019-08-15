package edu.upenn.cis.cis455.model;

import java.io.Serializable;

public class DocumentEntry implements Serializable{
    
    private Integer documentId;
    private String documentContent;
    
    public DocumentEntry(Integer Id, String Content){
        this.documentId=Id;
        this.documentContent=Content;
    }
    public Integer get_documentId(){
        return this.documentId;
    }
    
    public String get_documentContent(){
        return this.documentContent;
    }
    public void Content(String Content){
        this.documentContent=Content;
    }
    public int get_hashCode(){
        return this.documentId.hashCode();
    }
    public boolean match(Object another){
        if (!(another instanceof DocumentEntry)){
            return false;
    }
    DocumentEntry compare=(DocumentEntry) another;
    if (compare.get_documentId().equals(this.get_documentId())){
        return true;
    }
    return false;
    }
}