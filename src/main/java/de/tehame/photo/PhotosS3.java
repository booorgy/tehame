package de.tehame.photo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.jboss.logging.Logger;

import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

/**
 * Diese Klasse ermöglicht Zugriffe auf S3.
 */
public class PhotosS3 {

	private static final Logger LOGGER = Logger.getLogger(PhotosS3.class);

	public static final String REGION = "eu-central-1";
	public static final String BUCKET_PHOTOS = "tehame20161";
	public static final String BUCKET_THUMBNAILS = "tehame-thumbnails";

	/**
	 * Speichert das Photo im S3 Bucket.
	 * 
	 * @param fileData Byte Array.
	 * @return S3 Object Key.
	 */
	public String speicherePhoto(final byte[] fileData) {
		AmazonS3Client s3 = new AmazonS3Client();
		s3.setRegion(RegionUtils.getRegion(REGION));
		String bucketName = BUCKET_PHOTOS;
		String key = UUID.randomUUID().toString();
		ObjectMetadata metadata = new ObjectMetadata(); // TODO ?
		ByteArrayInputStream inputStream = new ByteArrayInputStream(fileData);
		PutObjectRequest putRequest = new PutObjectRequest(bucketName, key, inputStream, metadata);
		s3.putObject(putRequest);
		LOGGER.trace("S3 Object mit Key '" + key + "' ins Bucket '" + bucketName + "' geschrieben.");
		return key;
	}

	/**
	 * Lade ein Photo aus S3.
	 * 
	 * @param bucket S3 Bucket.
	 * @param key S3 Key.
	 * @return Photo Daten aus S3.
	 * @throws IOException I/O.
	 */
	public byte[] ladePhoto(String bucket, String key) throws IOException {
		AmazonS3Client s3 = new AmazonS3Client();
		s3.setRegion(RegionUtils.getRegion(REGION));
		GetObjectRequest getRequest = new GetObjectRequest(bucket, key);
		S3Object s3object = s3.getObject(getRequest);
		InputStream is = s3object.getObjectContent();
		byte[] photoData = IOUtils.toByteArray(is);
		LOGGER.trace("S3 Object mit Key '" + key + "' aus Bucket '" + bucket + "' geladen.");
		return photoData;
	}
}
