package edu.upenn.cis.cis455.storage;

public class StorageFactory {
    public static DatabaseEnv getDatabaseInstance(String directory) {
	// TODO: factory object, instantiate your storage server
	     try{
        return new DatabaseEnv(directory);
    }
    catch (Exception e){
        System.out.println("fail to create stoarge server");
        return null;
        
    }

    }
}
