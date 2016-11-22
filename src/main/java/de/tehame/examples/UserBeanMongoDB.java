package de.tehame.examples;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import de.tehame.TehameProperties;

public class UserBeanMongoDB {

	public boolean authUser(String email, String passwort) {
	   	MongoClientURI uri  = new MongoClientURI(TehameProperties.MONGO_DB_URL); 
	    MongoClient client = new MongoClient(uri);
	    DB db = client.getDB(uri.getDatabase());
	    DBCollection users = db.getCollection("user");
	    BasicDBObject query = new BasicDBObject();
	    query.put("email", email);
	    query.put("passwort", passwort);
	    DBCursor cursor = users.find(query);
		
	    if(cursor.size() == 1) {
	    	return true;
	    } else {
		    return false;	    	
	    }
	}
}
