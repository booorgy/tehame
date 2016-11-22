package de.tehame.photo.meta;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;

import org.jboss.logging.Logger;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import de.tehame.TehameProperties;
import de.tehame.user.User;

/**
 * Diese Klasse ermöglicht Zugriffe auf MongoDB, um Details und Metadaten zu
 * Photos zu speichern. 
 */
public class MetadatenMongoDB {
	private static final Logger LOGGER = Logger.getLogger(MetadatenMongoDB.class);

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
		
		MongoClientURI uri  = new MongoClientURI(TehameProperties.MONGO_DB_URL);  
	    MongoClient client = new MongoClient(uri);
	    DB db = client.getDB(uri.getDatabase());
	    DBCollection photos = db.getCollection("picture");
	   
	    BasicDBObject photo = new BasicDBObject();
	    photo.put("s3key", s3key);
	    photo.put("s3bucket", s3bucket);
	    photo.put("long", metadaten.getLongitude());
	    photo.put("lat", metadaten.getLatitude());
	    // TODO alle metadaten speichern
	    photo.put("uploadtimestamp", new Timestamp(new Date().getTime()));	
	    photo.put("useruuid", user.getUuid());	
	    photos.insert(photo);
	    
	    client.close();
	    
	    LOGGER.trace("Details zu Photo mit S3 Key '" + s3key + "' in MondoDB gespeichert.");
	}
}
