package de.tehame.photo.meta;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.UUID;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.io.FileUtils;
import org.jboss.logging.Logger;

import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import de.tehame.Properties;

public class MetadatenBean {
	private static final Logger LOGGER = Logger.getLogger(MetadatenBean.class);

	/**
	 * 
	 * @param fileData
	 * @throws ImageReadException
	 * @throws IOException
	 */
	public void saveImageToS3andMongo(byte[] fileData, String email) throws ImageReadException, IOException{		
		//--- Write File to S3
		AmazonS3Client s3 = new AmazonS3Client();
		s3.setRegion(RegionUtils.getRegion("eu-central-1"));
		 
        String bucketName = "tehame20161";
        String key = "MyObjectKey-" + UUID.randomUUID();	            
        
        // ERST einmal erstellen lassen !!! ka warum es von hand net geht!
        // s3.createBucket(bucketName); 
       
        File file = new File(key);
        FileUtils.writeByteArrayToFile(file, fileData);
        s3.putObject(new PutObjectRequest(bucketName, key, file));
        file.delete();
        LOGGER.trace("File wrote to S3 " + bucketName);
        
        //--- Write File to MongoDB
		final PhotoMetadaten photoMetadaten = MetadataBuilder.getMetaData(fileData);
		
		MongoClientURI uri  = new MongoClientURI(Properties.MONGO_URL);  
	    MongoClient client = new MongoClient(uri);
	    DB db = client.getDB(uri.getDatabase());
	    DBCollection pictures = db.getCollection("picture");
	   
	    
	    BasicDBObject picture = new BasicDBObject();
	    picture.put("pfad", key);
	    picture.put("long", photoMetadaten.getLongitude());
	    picture.put("lat", photoMetadaten.getLatitude());
	    java.util.Date date= new java.util.Date();
	    picture.put("timestamp", new Timestamp(date.getTime()));	
	    picture.put("user", email);	
	    pictures.insert(picture);
	    LOGGER.trace("Metadata wrote to Mongo ");
	}
	
}
