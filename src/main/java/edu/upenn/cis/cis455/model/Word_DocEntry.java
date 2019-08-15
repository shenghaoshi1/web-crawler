package edu.upenn.cis.cis455.model;

import java.io.Serializable;
public class Word_DocEntry implements Serializable{
    private Integer wordId;
    private Integer docId;
    public Word_DocEntry(Integer wordId, Integer docId){
        this.wordId=wordId;
        this.docId=docId;
    }
    
    
    public Integer get_wordId(){
        return this.wordId;
    }
    
    public Integer get_docId(){
        return this.docId;
    }
    
    
    public int get_wordId_hasCode(){
        return this.wordId.hashCode();
        
    }
    
    
    public int get_docId_hashCode(){
        return this.docId.hashCode();
    }
    
    
    public boolean match(Object another){
        if (!(another instanceof Word_DocEntry)){
            return false;
        }
        Word_DocEntry compare=(Word_DocEntry) another;
        if (compare.get_wordId().equals(this.get_wordId()) && compare.get_docId().equals(this.get_docId())){
            return true;
        }
        return false;
    }    
    
    
    
}