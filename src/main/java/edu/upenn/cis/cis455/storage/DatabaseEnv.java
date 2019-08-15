package edu.upenn.cis.cis455.storage;
import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.collections.StoredSortedMap;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import edu.upenn.cis.cis455.model.User;
import edu.upenn.cis.cis455.model.UrlEntry;
import edu.upenn.cis.cis455.model.DocumentEntry;
import edu.upenn.cis.cis455.model.WordEntry;
import edu.upenn.cis.cis455.model.Channel;
import java.math.BigInteger; 
import java.security.MessageDigest; 
import java.security.NoSuchAlgorithmException; 
public class DatabaseEnv implements StorageInterface {
    private String homeDirectory;
    private Environment env;
    private static final String CLASS_CATALOG = "java_class_catalog";
    private StoredClassCatalog javaCatalog;
    
    private Map<String,User> userMap;
    private Map<Integer,DocumentEntry> documentMap;
    private Map<Integer,WordEntry> wordMap;
    private Map<String,Integer> inverted_wordMap;
    private Map<Integer,UrlEntry> urlMap;
    private Map<String, Integer> inverted_urlMap;
    private Map<String,Channel> channelMap; //name:channel
   
    
    private static final String USER_STORE="user_store";
    private static final String DOCUMENT_STORE="document_store";
    private static final String WORD_STORE="word_store";
    private static final String INVERTEDWORD_STORE="invertedword_store";
    private static final String URL_STORE="url_store";
    private static final String INVERTEDURL_STORE="invertedurl_store";
    private static final String CHANNEL_STORE="channel_store";
   
    
    
    
    private Database userDb;
    private Database documentDb;
    private Database wordDb;
    private Database inverted_wordDb;
    private Database urlDb;
    private Database inverted_urlDb;
    private Database channel_Db;
   
    
    
    
    
    public DatabaseEnv( String homeDirectory) throws DatabaseException, FileNotFoundException{
        this.homeDirectory=homeDirectory;
        File directory = new File(this.homeDirectory);
        if (!directory.exists()) {
            directory.mkdir();
        }
        System.out.println("Opening environment in: " + this.homeDirectory);
        EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setTransactional(true);
        envConfig.setAllowCreate(true);

        this.env = new Environment(new File(this.homeDirectory), envConfig);
        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setTransactional(true);
        dbConfig.setAllowCreate(true);
        Database catalogDb = env.openDatabase(null, CLASS_CATALOG, 
                                              dbConfig);

        javaCatalog = new StoredClassCatalog(catalogDb);
        
        this.userDb =env.openDatabase(null, USER_STORE, dbConfig);
        this.documentDb=env.openDatabase(null,DOCUMENT_STORE,dbConfig);
        this.wordDb=env.openDatabase(null,WORD_STORE,dbConfig);
        this.inverted_wordDb=env.openDatabase(null,INVERTEDWORD_STORE,dbConfig);
        this.urlDb=env.openDatabase(null,URL_STORE,dbConfig);
        this.inverted_urlDb=env.openDatabase(null,INVERTEDURL_STORE,dbConfig);
        this.channel_Db=env.openDatabase(null,CHANNEL_STORE,dbConfig);
        
        
        EntryBinding<String> stringBinding = new StringBinding();
	    EntryBinding<Integer> intBinding = new IntegerBinding();
	    EntryBinding<User> userBinding = new SerialBinding<User>(javaCatalog, User.class);
	    EntryBinding <DocumentEntry> documentBinding=new SerialBinding<DocumentEntry>(javaCatalog, DocumentEntry.class);
	    EntryBinding <WordEntry> wordBinding=new SerialBinding<WordEntry>(javaCatalog, WordEntry.class);
	    EntryBinding <UrlEntry> urlBinding=new SerialBinding<UrlEntry>(javaCatalog, UrlEntry.class);
	    EntryBinding <Channel>  channelBinding=new SerialBinding<Channel>(javaCatalog,Channel.class);
	    
	    
	    userMap = new StoredSortedMap<String,User>(userDb, stringBinding, userBinding, true);
	    documentMap= new StoredSortedMap<Integer,DocumentEntry>(documentDb,intBinding,documentBinding,true);
        wordMap=new StoredSortedMap<Integer,WordEntry>(wordDb,intBinding,wordBinding,true);
        inverted_wordMap=new StoredSortedMap<String,Integer> (inverted_wordDb,stringBinding,intBinding,true);
        urlMap=new StoredSortedMap<Integer,UrlEntry> (urlDb,intBinding,urlBinding,true);
        inverted_urlMap=new StoredSortedMap<String,Integer>(inverted_urlDb,stringBinding,intBinding,true);
        channelMap=new StoredSortedMap<String,Channel>(channel_Db,stringBinding,channelBinding,true);
        
    }
    public final Database getChannelDatabase(){
        return this.channel_Db;
    }
    
    public final Database getUserDatabase(){
        return this.userDb;
    }
    public final Database getDocumentDatabase(){
        return this.documentDb;
    }
    
    public final Database getWordDatabase(){
        return this.wordDb;
    }
    
    public final Database geInvertedWordDatabase(){
        return this.inverted_wordDb;
    }
    
    public final Database getUrlDatabase(){
        return this.urlDb;
    }
    
    public final Database getInvertedUrlDatabase(){
        return this.inverted_urlDb;
    }
    public Channel get_channel(String name){
        return this.channelMap.get(name);
    }
    
    public void add_channel(String name, String Xpath){
        Channel channel=new Channel(name,Xpath);
        this.channelMap.put(name,channel);
    }
    
    public void add_channel(String name, Channel channel){
        this.channelMap.put(name,channel);
        
    }
    public synchronized void add_urltochannel(String name, String url){
        if (this.channelMap.containsKey(name)){
            Channel channel=this.channelMap.get(name);
            channel.add_url(url);
            this.channelMap.put(name,channel);
        }
        
    }
    public ArrayList<Channel> get_channels(){
        ArrayList<Channel> res= new ArrayList<Channel>();
        for (Channel channel: this.channelMap.values()){
            //System.out.println(channel.get_name());
            res.add(channel);
            
        }
        return res;
    }
    
    @Override
    public void close()throws DatabaseException
    {   this.userDb.close();
        this.documentDb.close();
        this.wordDb.close();
        this.inverted_wordDb.close();
        this.urlDb.close();
        this.inverted_urlDb.close();
        this.channel_Db.close();
        
        
        this.javaCatalog.close();
        this.env.close();
    } 
    
    public final StoredClassCatalog getClassCatalog() {
        return javaCatalog;
    } 
    /**
     * How many documents so far?
     */
	public int getCorpusSize(){
	    return this.documentMap.size();
	    
	}
	
	/**
	 * Add a new document, getting its ID
	 */
	public int addDocument(String url, String documentContents){
	    int documentId;
	    synchronized (this){
	        documentId=this.getCorpusSize()+1;
	        this.documentMap.put(documentId,new DocumentEntry(documentId,documentContents));
	        this.urlMap.put(documentId,new UrlEntry(documentId,url));
	        this.inverted_urlMap.put(url,documentId);
	    }
	    return documentId;
	}
	
	/**
	 * How many keywords so far?
	 */
	public int getLexiconSize(){
	    return this.wordMap.size();
	    
	}
	
	/**
	 * Gets the ID of a word (adding a new ID if this is a new word)
	 */
	public int addOrGetKeywordId(String keyword){
	    if (inverted_wordMap.containsKey(keyword)){
	        return inverted_wordMap.get(keyword);
	        
	    }
    int wordId=wordMap.size()+1;
    wordMap.put(wordId,new WordEntry(wordId,keyword));
    inverted_wordMap.put(keyword,wordId);
    return wordId;
	    
	}
	
	/**
	 * Adds a user and returns an ID
	 */
	public int addUser(String username, String password, String firstname, String lastname){
	    int userId=userMap.size()+1;
	    userMap.put(username, new User(userId,username,getSHA(password),firstname,lastname));
	    return userId;
	    
	}
	
	public boolean checkUser(String username){
	    return userMap.containsKey(username);
	}
	
	/**
	 * Tries to log in the user, or else throws a HaltException
	 */
	public boolean getSessionForUser(String username, String password){
	    if (userMap.containsKey(username) && userMap.get(username).getPassword().equals(getSHA(password))){
	        return true;
	    }
	    return false;
	    
	}
	
	/**
	 * Retrieves a document's contents by URL
	 */
	public String getDocument(String url){
	    if (!inverted_urlMap.containsKey(url)){
	        return null;
	    }
	    int docId=inverted_urlMap.get(url);
	    return documentMap.get(docId).get_documentContent();
	    
	}
	public DocumentEntry getdoc(String url){
	    if (!inverted_urlMap.containsKey(url)){
	        return null;
	    }
	    int docId=inverted_urlMap.get(url);
	    return documentMap.get(docId);
	    
	}
	public User getUser(String username){
	    if (!userMap.containsKey(username)){
	        return null;
	    }
	    return userMap.get(username);
	}
	public String getDocId(String url){
	    if (!inverted_urlMap.containsKey(url)){
	        return null;
	    }
	    int docId=inverted_urlMap.get(url);
	    return String.valueOf(docId); 

	    
	}
	
	public void modifyDocument(String url, String content){
	    if (!inverted_urlMap.containsKey(url)){
	        this.addDocument(url,content);
	    }
	    else{
	        int docId=inverted_urlMap.get(url);
	        DocumentEntry doc=documentMap.get(docId);
	        doc.Content(content);
	        documentMap.put(docId,doc);
	        urlMap.get(docId).update_lastchecktime();
	    }
	}
	
	public String getDocumentLastchecktime(String url){
	    if (!inverted_urlMap.containsKey(url)){
	        return null;
	    }
	    int docId=inverted_urlMap.get(url);
	    return urlMap.get(docId).get_lastchecktime();
	    
	}
       public static String getSHA(String input) 
    { 
  
        try { 
  
            // Static getInstance method is called with hashing SHA 
            MessageDigest md = MessageDigest.getInstance("SHA-256"); 
  
            // digest() method called 
            // to calculate message digest of an input 
            // and return array of byte 
            byte[] messageDigest = md.digest(input.getBytes()); 
  
            // Convert byte array into signum representation 
            BigInteger no = new BigInteger(1, messageDigest); 
  
            // Convert message digest into hex value 
            String hashtext = no.toString(16); 
  
            while (hashtext.length() < 32) { 
                hashtext = "0" + hashtext; 
            } 
  
            return hashtext; 
        } 
  
        // For specifying wrong message digest algorithms 
        catch (NoSuchAlgorithmException e) { 
            System.out.println("Exception thrown"
                               + " for incorrect algorithm: " + e); 
  
            return null; 
        } 
    }
    
    
    
    
}