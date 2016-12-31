package de.tehame.photo.meta;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import de.tehame.event.Event;
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
	    photo.put("useruuid", 		metadaten.getUserUuid());	
	    photo.put("zugehoerigkeit", metadaten.getZugehoerigkeit());	
	    photo.put("breite", 		metadaten.getBreite());	
	    photo.put("hoehe", 			metadaten.getHoehe());
	    photo.put("eventuuid", 		metadaten.getEventUuid());
	    photo.put("labels",			metadaten.getLabels());
	    
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
		/* Beispiel JSON:
			{
			    "_id" : ObjectId("58679fac49293e15f8812613"),
			    "s3key" : "cbeaa59f-da9b-414f-b2e6-a17a617c1e89",
			    "s3bucket" : "tehame-ireland",
			    "loc" : {
			        "type" : "Point",
			        "coordinates" : [ 
			            -1.0, 
			            -1.0
			        ]
			    },
			    "aufnahmeZeitpunkt" : NumberLong(1483186092),
			    "useruuid" : "e26fc393-9219-44b5-b681-f08f054a79ea",
			    "zugehoerigkeit" : 2,
			    "breite" : -1,
			    "hoehe" : -1,
			    "eventuuid" : "9f33df8d-0c7a-40d1-b913-b526fbfa59ac",
			    "labels" : [ 
			        "People", 
			        "Person", 
			        "Human", 
			        "Chair", 
			        "Furniture", 
			        "Dinner", 
			        "Food", 
			        "Meal", 
			        "Supper"
			    ]
			}
		*/
		
		DBObject metadataDoc = cursor.next();
		
		DBObject geoJsonDoc = (DBObject) metadataDoc.get("loc");
		BasicDBList labels = (BasicDBList) metadataDoc.get("labels");
		
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
				(int) metadataDoc.get("zugehoerigkeit"),
				labels.toArray(new String[labels.size()]));
		
		photoMetadaten.setEventUuid((String) metadataDoc.get("eventuuid"));
		
		return photoMetadaten;
	}
	
	/**
	 * @return Alle Bilder der User mit der entsprechenden Zugehörigkeit und den Events.
	 */
	public ArrayList<PhotoMetadaten> getPhotosByUserAndZugehoerigkeit(List<String> userUuids, int zugehoerigkeit, String[] eventUuids) {
		ArrayList<PhotoMetadaten> result = new ArrayList<PhotoMetadaten>();
		
		DBCollection photos = loadConnectMongoCollection("picture");
		
		BasicDBObject whereQuery = new BasicDBObject();
		
		whereQuery.put("zugehoerigkeit", zugehoerigkeit);
		whereQuery.put("useruuid", new BasicDBObject("$in", userUuids));
		whereQuery.put("eventuuid", new BasicDBObject("$in", eventUuids));
		
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

	public ArrayList<PhotoMetadaten> getPhotosByUserAndZugehoerigkeit(User user, int zugehoerigkeit, List<String> events) {
		ArrayList<PhotoMetadaten> result = new ArrayList<PhotoMetadaten>();
		
		DBCollection photos = loadConnectMongoCollection("picture");
		
		BasicDBObject whereQuery = new BasicDBObject();
		
		whereQuery.put("eventuuid", events);
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
