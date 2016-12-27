package de.tehame.photo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.jboss.logging.Logger;

import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

import de.tehame.TehameProperties;

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
		s3.setRegion(RegionUtils.getRegion(TehameProperties.REGION.getName()));
		String bucketName = TehameProperties.PHOTO_BUCKET;
		String key = UUID.randomUUID().toString();
		ObjectMetadata metadata = new ObjectMetadata();
		
		byte[] resultByte = DigestUtils.md5(fileData);
		String streamMD5 = new String(Base64.encodeBase64(resultByte));
		metadata.setContentMD5(streamMD5);
		
		metadata.setContentLength((long) fileData.length);
		
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
	 * @throws AmazonS3Exception Wenn der Key nicht existiert.
	 */
	public byte[] ladePhoto(String bucket, String key) throws IOException, AmazonS3Exception {
		AmazonS3Client s3 = new AmazonS3Client();
		s3.setRegion(RegionUtils.getRegion(TehameProperties.REGION.getName()));
		GetObjectRequest getRequest = new GetObjectRequest(bucket, key);
		S3Object s3object = s3.getObject(getRequest);
		InputStream is = s3object.getObjectContent();
		byte[] photoData = IOUtils.toByteArray(is);
		LOGGER.trace("S3 Object mit Key '" + key + "' aus Bucket '" + bucket + "' geladen.");
		return photoData;
	}
}
