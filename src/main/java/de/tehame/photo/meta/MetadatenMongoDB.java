package de.tehame.photo.meta;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import org.jboss.logging.Logger;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import de.tehame.TehameProperties;
import de.tehame.photo.Zugehoerigkeit;
import de.tehame.user.User;

/**
 * Diese Klasse ermöglicht Zugriffe auf MongoDB, um Details und Metadaten zu
 * Photos zu speichern. 
 */
public class MetadatenMongoDB implements Serializable {
	private static final Logger LOGGER = Logger.getLogger(MetadatenMongoDB.class);

	private MongoClient client = null;
	
	/**
	 * Baut eine Verbindung zur MongoDB auf und liefert die Collection des Strings name.
	 * @param name
	 * @return
	 */
	private DBCollection loadConnectMongoCollection(String name) {
		MongoClientURI uri  = new MongoClientURI(TehameProperties.MONGO_DB_URL);  
	    if (client == null)
	    	client = new MongoClient(uri);
	    DB db = client.getDB(uri.getDatabase());
	    DBCollection collection = db.getCollection(name);	
	    return collection;
	}
	
	private void closeMongoConnection() {
		this.client.close();
	}
	
	/**
	 * Speichert Details und Metadaten zu einem Photo in der MongoDB.
	 * 
	 * @param user Tehame User.
	 * @param s3key S3 Object Key.
	 * @param s3bucket S3 Bucket Name.
	 * @param metadaten Photo Metadaten.
	 * @throws IOException I/O Fehler.
	 */
	public void savePhotoDetailsToMongo(
			final User user, 
			final String s3key, 
			final String s3bucket, 
			final PhotoMetadaten metadaten) 
			throws IOException {		
		
		DBCollection photos = loadConnectMongoCollection("picture");
	   
	    BasicDBObject photo = new BasicDBObject();
	    photo.put("s3key", s3key);
	    photo.put("s3bucket", s3bucket);
	    photo.put("longitude", metadaten.getLongitude());
	    photo.put("latitude", metadaten.getLatitude());
	    photo.put("aufnahmeZeitpunkt", String.valueOf(new Timestamp(new Date().getTime())));	
	    photo.put("useruuid", user.getUuid());	
	    photo.put("zugehoerigkeit", metadaten.getZugehoerigkeit());	
	    photo.put("breite", metadaten.getBreite());	
	    photo.put("hoehe", metadaten.getHoehe());	
	    photos.insert(photo);	    
	    
	    //closeMongoConnection();
	    LOGGER.trace("Details zu Photo mit S3 Key '" + s3key + "' in MondoDB gespeichert.");
	}
	
	/**
	 * Liefert alle eigenen Bilder des Users.
	 * @param user
	 * @return
	 */
	public ArrayList<PhotoMetadaten> getPhotosByUser(User user) {
		ArrayList<PhotoMetadaten> result = new ArrayList<PhotoMetadaten>();
		
		DBCollection photos = loadConnectMongoCollection("picture");
		
		BasicDBObject whereQuery = new BasicDBObject();
		whereQuery.put("useruuid", user.getUuid());
		DBCursor cursor = photos.find(whereQuery);
		while(cursor.hasNext()) {
			DBObject tobj = cursor.next();
			PhotoMetadaten photoMetadaten = new PhotoMetadaten(
					(String) tobj.get("aufnahmeZeitpunkt"), 
					(double) tobj.get("longitude"), 
					(double) tobj.get("latitude"), 
					(int) tobj.get("breite"), 
					(int) tobj.get("hoehe"),
					(String) tobj.get("s3bucket"),
					(String) tobj.get("s3key"),
					(int) tobj.get("zugehoerigkeit"));	
			result.add(photoMetadaten);
		}
		//closeMongoConnection();	  
		
		return result;
	}
	
	/**
	 * Liefert alle Bilder des Users mit der entsprechenden Zugehörigkeit.
	 * @param user
	 * @param zugehoerigkeit
	 * @return
	 */
	public ArrayList<PhotoMetadaten> getPhotosByUserAndZugehoerigkeit(User user, int zugehoerigkeit) {
		ArrayList<PhotoMetadaten> result = new ArrayList<PhotoMetadaten>();
		
		DBCollection photos = loadConnectMongoCollection("picture");
		
		BasicDBObject whereQuery = new BasicDBObject();
		whereQuery.put("useruuid", user.getUuid());
		whereQuery.put("zugehoerigkeit", zugehoerigkeit);
		DBCursor cursor = photos.find(whereQuery);
		while(cursor.hasNext()) {
			DBObject tobj = cursor.next();
			PhotoMetadaten photoMetadaten = new PhotoMetadaten(
					(String) tobj.get("aufnahmeZeitpunkt"), 
					(double) tobj.get("longitude"), 
					(double) tobj.get("latitude"), 
					(int) tobj.get("breite"), 
					(int) tobj.get("hoehe"),
					(String) tobj.get("s3bucket"),
					(String) tobj.get("s3key"),
					(int) tobj.get("zugehoerigkeit"));	
			result.add(photoMetadaten);
		}

		//closeMongoConnection();	  
		
		return result;
	}
	
}
