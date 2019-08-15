package edu.upenn.cis.cis455;

import java.io.File;
import java.util.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.After;

import static org.junit.Assert.*;
import edu.upenn.cis.cis455.model.DomToOccurrence;
import edu.upenn.cis.cis455.model.OccurrenceEvent;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class TestDomToOccurence{
    
    @Test
    public void test(){
        DomToOccurrence converter = new DomToOccurrence();
        String str1="<a>t<b>a<c>abc</c><d>com</d></b></a>";
        try{
        Document doc=DomToOccurrence.getDomNode(str1);
        List<OccurrenceEvent> events = converter.getOccurrenceEvents("00000",doc);
        assertEquals("wroang sice",events.size(),12);
    }
    catch(Exception e){
        
    }

        
        
    }
    
}