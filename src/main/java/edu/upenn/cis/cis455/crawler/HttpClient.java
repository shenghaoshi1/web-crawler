package edu.upenn.cis.cis455.crawler;
import edu.upenn.cis.cis455.crawler.info.URLInfo;
import javax.net.ssl.HttpsURLConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;

public class HttpClient{
    private Socket socket;
    private URLInfo info;
    public HttpClient(URLInfo info){
        this.info=info;
        //try {
           // socket = new Socket(info.getHostName(), info.getPortNo());
           // System.out.println("create sockect ！");
            
        //} 
        //catch (Exception e){
           // System.out.println("create sockect failed"+info.getHostName()+info.getPortNo());
        //}
        
    }
    
     public InputStream sendRequest(String requestType, String lastChecked) {
        try {
            OutputStream out = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(out, false);
            String request = requestType + " " + info.getFilePath() + " HTTP/1.1\r\n" +
                             "Host: " + info.getHostName() + "\r\n";
            if (lastChecked!=null){
                request=request+"If-Modified-Since: " + lastChecked + "\r\n";
            }
            request=request+"User-Agent: cis455crawler\r\n\r\n";
                             
            writer.print(request);
            writer.flush();
            return socket.getInputStream();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("send request failed");
            return null;
        }
    }
    public headinfo getHeadInfo(String lastModified) {
        BufferedReader rd = new BufferedReader(new InputStreamReader(sendRequest("HEAD", lastModified)));
        String contenttype="";
        int contentlength=-1;
        try{
            String line;
        while((line=rd.readLine())!=null){
            if (line.contains("304")){
                return new headinfo(contenttype,contentlength,false);
            }
            if (line.startsWith("Content-Type")){
                contenttype=line.replace("Content-Type: ","");
            }
            if (line.startsWith("Content-Length")){
                contentlength=Integer.valueOf(line.replace("Content-Length: ",""));
            }
            if (!contenttype.equals("") && contentlength != -1 || line.contains("keep-alive")) {
                    break;
                }
                
            
        }
        rd.close();
        
    }
    catch (Exception e){
        System.out.println("get headinfo failed");
    }

        return new headinfo(contenttype,contentlength,true);
     
    }
    public  headinfo getSecuredHeadInfo(URLInfo info, String lastModified) {
        URL url;
        try {
            url = new URL(info.tourl());
            if (info.isSecure()){
            HttpsURLConnection connection = (HttpsURLConnection)url.openConnection();
            connection.setRequestProperty("User-Agent", "cis455crawler");
            if (lastModified != null) {
                connection.setRequestProperty("If-Modified-Since", lastModified);
            }
            connection.setRequestMethod("HEAD");
            InputStream stream = connection.getInputStream();
            return new headinfo(connection.getContentType(), connection.getContentLength(), connection.getResponseCode() != 304);}
            else{
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestProperty("User-Agent", "cis455crawler");
            if (lastModified != null) {
                connection.setRequestProperty("If-Modified-Since", lastModified);
            }
            connection.setRequestMethod("HEAD");
            InputStream stream = connection.getInputStream();
            return new headinfo(connection.getContentType(), connection.getContentLength(), connection.getResponseCode() != 304);}
                
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        
    
}

     public static InputStream downloadPage(URLInfo info, URL url) throws IOException {
        if (info.isSecure()) {
            HttpsURLConnection connect = (HttpsURLConnection)url.openConnection();
            connect.setRequestProperty("User-Agent", "cis455crawler");
            connect.setInstanceFollowRedirects(false);
            return connect.getInputStream();
        } else {
            HttpClient connect = new HttpClient(info);
            return connect.sendRequest("GET", null);
        }
    
}

   public class headinfo{
    private String contentType;
    private int contentLength;
    private boolean modified;
    
    public headinfo(String contentType, int contentLength, boolean modified) {
        this.contentType = contentType;
        this.contentLength = contentLength;
        this.modified = modified;
    }
    
    public int getContentLength() {
        return contentLength;
    }
    
    public String getContentType() {
        return contentType;
    }
    
    public boolean getModified() {
        return modified;
    }
    
   
    public String toheader() {
        return "Type : " + contentType + ", Length : " + contentLength + ", Modified : " + modified;
    }
    
       
   }
}