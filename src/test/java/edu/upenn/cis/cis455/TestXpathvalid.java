package edu.upenn.cis.cis455;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.junit.After;

import static org.junit.Assert.*;
import edu.upenn.cis.cis455.xpathengine.XPathEngineFactory;
import edu.upenn.cis.cis455.xpathengine.XPathEngineImpl;
public class TestXpathvalid{
    
    @Before
    public void setUp() {
         
         
        
    }
    
    @Test
    public void test(){
        XPathEngineImpl engine=XPathEngineFactory.getXPathEngine();
       String String1="/foo/bar/xyz";
        String String2="/xyz/abc[contains(text(),\"someSubstring\")]";
        String String3="/a/b/c[text()=\"theEntireText\"]";
        String String4="/d/e/f/foo[text()=\"something\"]/bar";
        String String5="/a/b/c[text() =   \"whiteSpacesShouldNotMatter\"]";
        assertTrue(engine.isValid(String1));
        assertTrue(engine.isValid(String2));
        assertTrue(engine.isValid(String3));
        assertTrue(engine.isValid(String4));
        assertTrue(engine.isValid(String5));
        
    }
     @After
     public void tearDown() {}
    
    
    
}