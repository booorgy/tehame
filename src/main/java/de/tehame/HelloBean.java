package de.tehame;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import java.io.Serializable;

@ManagedBean
@SessionScoped
public class HelloBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;

	public String getName() {
		
	       MongoClientURI uri  = new MongoClientURI("mongodb://gude:gude8888@ds143777.mlab.com:43777/tehame"); 
	        MongoClient client = new MongoClient(uri);
	        DB db = client.getDB(uri.getDatabase());
	        DBCollection songs = db.getCollection("testc");
	       
	        
	        BasicDBObject eighties = new BasicDBObject();
	        eighties.put("decade", "1980s");
	        eighties.put("artist", "Olivia Newton-John");
	        eighties.put("song", "Physical");
	        eighties.put("weeksAtOne", 10);	        
	        songs.insert(eighties);

		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}