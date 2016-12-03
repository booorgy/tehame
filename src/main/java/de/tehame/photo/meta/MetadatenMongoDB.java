package de.tehame.photo.meta;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import org.jboss.logging.Logger;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import de.tehame.TehameProperties;
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
	 * @param metadaten Photo Metadaten.
	 * @throws IOException I/O Fehler.
	 */
	public void savePhotoDetailsToMongo(final PhotoMetadaten metadaten) 
			throws IOException {		
		
		// Erzeuge Photo Metadaten Objekt
	    BasicDBObject photo = new BasicDBObject();
	    photo.put("s3key", metadaten.getS3key());
	    photo.put("s3bucket", metadaten.getS3bucket());
	    
	    // Erzeuge GeoJSON Objekt
	    BasicDBObject location = new BasicDBObject();
	    location.put("type", "Point");
	    double[] geoPoint = new double[2];
	    geoPoint[0] = metadaten.getLongitude();
	    geoPoint[1] = metadaten.getLatitude();
	    location.put("coordinates", geoPoint);
	    
	    photo.put("loc", location);
	    if (metadaten.getAufnahmeZeitpunkt() == -1) {
	    	Date currentDate = new Date();
	    	photo.put("aufnahmeZeitpunkt", (currentDate.getTime() / 1000));	
	    } else {
	    	photo.put("aufnahmeZeitpunkt", metadaten.getAufnahmeZeitpunkt());		
	    }
	    photo.put("useruuid", metadaten.getUserUuid());	
	    photo.put("zugehoerigkeit", metadaten.getZugehoerigkeit());	
	    photo.put("breite", metadaten.getBreite());	
	    photo.put("hoehe", metadaten.getHoehe());
	    photo.put("eventuuid", metadaten.getEventUuid());
	    
	    LOGGER.trace("Speichere Metadaten in MongoDB: " + photo.toJson());
	    
		DBCollection photos = loadConnectMongoCollection("picture");
	    photos.insert(photo);
	    
	    //closeMongoConnection();
	    LOGGER.trace("Details zu Photo mit S3 Key '" + metadaten.getS3key() + "' in MondoDB gespeichert.");
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
			PhotoMetadaten photoMetadaten = ladeMetadaten(cursor);	
			result.add(photoMetadaten);
		}
		//closeMongoConnection();
		
		LOGGER.trace("Photo Metadaten von User " + user.getUuid() + ": " + result.toString());
		
		return result;
	}

	private PhotoMetadaten ladeMetadaten(DBCursor cursor) {
		DBObject metadataDoc = cursor.next();
		DBObject geoJsonDoc = (DBObject) metadataDoc.get("loc");
		BasicDBList coordinates = (BasicDBList) geoJsonDoc.get("coordinates");
		
		PhotoMetadaten photoMetadaten = new PhotoMetadaten(
				(String) metadataDoc.get("useruuid"),
				(long) metadataDoc.get("aufnahmeZeitpunkt"), 
				(double) coordinates.get(0),
				(double) coordinates.get(1),
				(int) metadataDoc.get("breite"), 
				(int) metadataDoc.get("hoehe"),
				(String) metadataDoc.get("s3bucket"),
				(String) metadataDoc.get("s3key"),
				(int) metadataDoc.get("zugehoerigkeit"));
		
		photoMetadaten.setEventUuid((String) metadataDoc.get("eventuuid"));
		
		return photoMetadaten;
	}
	
	/**
	 * @return Alle Bilder des Users mit der entsprechenden Zugehörigkeit und Event UUID.
	 */
	public ArrayList<PhotoMetadaten> getPhotosByUserAndZugehoerigkeit(User user, int zugehoerigkeit, String eventUuid) {
		ArrayList<PhotoMetadaten> result = new ArrayList<PhotoMetadaten>();
		
		DBCollection photos = loadConnectMongoCollection("picture");
		
		BasicDBObject whereQuery = new BasicDBObject();
		
		whereQuery.put("useruuid", user.getUuid());
		whereQuery.put("zugehoerigkeit", zugehoerigkeit);
		whereQuery.put("eventuuid", eventUuid);
		
		DBCursor cursor = photos.find(whereQuery);
		while(cursor.hasNext()) {
			PhotoMetadaten photoMetadaten = ladeMetadaten(cursor);
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
			PhotoMetadaten photoMetadaten = ladeMetadaten(cursor);
			result.add(photoMetadaten);
		}

		//closeMongoConnection();	  
		
		return result;
	}
	
}
