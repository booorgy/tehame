package de.tehame.photo;

import java.util.List;

import org.jboss.resteasy.logging.Logger;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Region;
import com.amazonaws.services.rekognition.AmazonRekognitionClient;
import com.amazonaws.services.rekognition.model.AmazonRekognitionException;
import com.amazonaws.services.rekognition.model.DetectLabelsRequest;
import com.amazonaws.services.rekognition.model.DetectLabelsResult;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.Label;
import com.amazonaws.services.rekognition.model.S3Object;
import com.fasterxml.jackson.core.JsonProcessingException;

import de.tehame.TehameProperties;

/**
 * Aufrufe der Amazon Rekognition API. 
 */
public class PhotoRekognition {

	private static final Logger LOGGER = Logger.getLogger(PhotoRekognition.class);
	
	public List<Label> labelsVonPhoto(String s3key) throws AmazonRekognitionException, JsonProcessingException {
		
		AWSCredentials credentials = new DefaultAWSCredentialsProviderChain().getCredentials();
		
		LOGGER.trace("DetectLabels Operation auf S3 Objekt " + s3key);
        
		DetectLabelsRequest request = new DetectLabelsRequest()
				.withImage(new Image()
						.withS3Object(new S3Object()
							.withName(s3key)
							.withBucket(TehameProperties.PHOTO_BUCKET)))
				.withMaxLabels(TehameProperties.REKOGNITION_MAX_LABELS)
				.withMinConfidence(TehameProperties.REKOGNITION_MIN_CONFIDENCE);

		AmazonRekognitionClient rekognitionClient = new AmazonRekognitionClient(credentials);
		Region REGION = Region.getRegion(TehameProperties.REKOGNITION_REGION);
		
		// Diese Methode wird im Tutorial verwendet. Wenn man sie verwendet, ist der HTTP Header nicht vollständig.
		// In der JavaDoc steht 
		// "An internal method used to explicitly override the internal signer region computed by the default implementation. 
		// This method is not expected to be normally called except for AWS internal development purposes."
		
		// rekognitionClient.setSignerRegionOverride(REGION.toString());
		rekognitionClient.setRegion(REGION);

		DetectLabelsResult result = rekognitionClient.detectLabels(request);
		
		for (Label label : result.getLabels()) {
			LOGGER.trace(label.getName());
		}
		
		return result.getLabels();
	}
}
