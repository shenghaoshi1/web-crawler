package edu.upenn.cis.cis455.model;

import java.io.Serializable;
public class WordEntry implements Serializable{
    
    private Integer wordId;
    private String wordString;
    public WordEntry(Integer wordId, String wordString){
        this.wordId=wordId;
        this.wordString=wordString;
        
    }
    public Integer get_wordId(){
        return this.wordId;
    }
    
    public String get_wordString(){
        return this.wordString;
    }
    public int get_hashCode(){
        return this.wordId.hashCode();
    }
    public boolean match(Object another){
        if (!(another instanceof WordEntry)){
            return false;
    }
    WordEntry compare=(WordEntry) another;
    if (compare.get_wordId().equals(this.get_wordId())){
        return true;
    }
    return false;

    }
    
}