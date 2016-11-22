package de.tehame.photo;

import java.io.ByteArrayInputStream;
import java.util.UUID;

import org.jboss.logging.Logger;

import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

/**
 * Diese Klasse ermöglicht Zugriffe auf S3. 
 */
public class PhotosS3 {
	
	private static final Logger LOGGER = Logger.getLogger(PhotosS3.class);

	/**
	 * Speichert das Photo im S3 Bucket.
	 * 
	 * @param fileData Byte Array.
	 * @return S3 Object Key.
	 */
	public String speicherePhoto(final byte[] fileData) {
		AmazonS3Client s3 = new AmazonS3Client();
		s3.setRegion(RegionUtils.getRegion("eu-central-1"));
        String bucketName = "tehame";
        String key = "tehame-" + UUID.randomUUID();	            
        ObjectMetadata metadata = new ObjectMetadata(); // TODO
        ByteArrayInputStream inputStream = new ByteArrayInputStream(fileData);
        PutObjectRequest putRequest = new PutObjectRequest(bucketName, key, inputStream, metadata);
        s3.putObject(putRequest);
        LOGGER.trace("S3 Object mit Key '" + key + "' ins Bucket '" + bucketName + "' geschrieben.");
        return key;
	}
}
