package edu.upenn.cis.cis455;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.junit.After;

import static org.junit.Assert.*;
import edu.upenn.cis.cis455.model.Channel;

public class TestChannel {
    @Before
  public void setUp() {
  }

    
    @Test
    public void test(){
        
        Channel channel=new Channel("shaozilan","/foo/bar/xyz");
        assertEquals("namewrong","shaozilan",channel.get_name());
        assertEquals("xpathwrong","/foo/bar/xyz",channel.get_XPath());
    }
    @After
public void tearDown() {}
}