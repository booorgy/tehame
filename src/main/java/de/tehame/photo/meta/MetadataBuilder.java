package de.tehame.photo.meta;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.common.ImageMetadata.ImageMetadataItem;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.GpsTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfo;
import org.jboss.logging.Logger;

import com.amazonaws.services.rekognition.model.Label;

import de.tehame.TehameProperties;
import de.tehame.user.User;

public class MetadataBuilder {

	private static final Logger LOGGER = Logger.getLogger(MetadataBuilder.class);
	
	/**
	 * Beispiel DateTimeOriginal: '2016:06:13 17:06:23'.
	 */
	private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");

	public static PhotoMetadaten createMetaData(final byte[] fileData, int zugehoerigkeit, User user, String s3key, List<Label> labels) 
			throws ImageReadException, IOException {
		
		// -1 um nicht gesetzte ungültige Werte zu erkennen
		long dateTimeOriginal = -1; 
		double longitude = -1; 
		double latitude = -1;
		int breite = -1;
		int hoehe = -1;
		
		// Greife auf die Metadaten zu, die im Exif-Format gespeichert werden.
		// Siehe Wikipedia: https://de.wikipedia.org/wiki/Exchangeable_Image_File_Format 
		final ImageMetadata metadata = Imaging.getMetadata(fileData);

		// Dump to String
		LOGGER.trace(metadata);

		if (metadata instanceof JpegImageMetadata) {
			final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;

			// Jpeg EXIF metadata is stored in a TIFF-based directory structure
			// and is identified with TIFF tags.
			// Here we look for the "x resolution" tag, but
			// we could just as easily search for any other tag.
			//
			// see the TiffConstants file for a list of TIFF tags.

			// print out various interesting EXIF tags.
			logTagValue(jpegMetadata, TiffTagConstants.TIFF_TAG_XRESOLUTION);
			logTagValue(jpegMetadata, TiffTagConstants.TIFF_TAG_DATE_TIME);
			logTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
			logTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_DATE_TIME_DIGITIZED);
			logTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_ISO);
			logTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_SHUTTER_SPEED_VALUE);
			logTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_APERTURE_VALUE);
			logTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_BRIGHTNESS_VALUE);
			logTagValue(jpegMetadata, GpsTagConstants.GPS_TAG_GPS_LATITUDE_REF);
			logTagValue(jpegMetadata, GpsTagConstants.GPS_TAG_GPS_LATITUDE);
			logTagValue(jpegMetadata, GpsTagConstants.GPS_TAG_GPS_LONGITUDE_REF);
			logTagValue(jpegMetadata, GpsTagConstants.GPS_TAG_GPS_LONGITUDE);
			
			// Die Breite des Bildes.
			final TiffField tiffFieldWidth = jpegMetadata.findEXIFValueWithExactMatch(
					ExifTagConstants.EXIF_TAG_EXIF_IMAGE_WIDTH);
			
			if (tiffFieldWidth != null) {
				try {
					breite = Integer.parseInt(tiffFieldWidth.getValueDescription());
				} catch (NumberFormatException e) {
					LOGGER.error(e);
				}
			}
			
			// Die Höhe des Bildes.
			final TiffField tiffFieldLength = jpegMetadata.findEXIFValueWithExactMatch(
					ExifTagConstants.EXIF_TAG_EXIF_IMAGE_LENGTH);
			
			if (tiffFieldLength != null) {
				try {
					hoehe = Integer.parseInt(tiffFieldLength.getValueDescription());
				} catch (NumberFormatException e) {
					LOGGER.error(e);
				}
			}
			
			// Das Aufnahmedatum.
			final TiffField tiffFieldDateTimeOriginal = jpegMetadata.findEXIFValueWithExactMatch(
					ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
			
			if (tiffFieldDateTimeOriginal != null) {
				dateTimeOriginal = toUnixTimestamp(tiffFieldDateTimeOriginal.getValueDescription());
			}

			// simple interface to GPS data
			final TiffImageMetadata exifMetadata = jpegMetadata.getExif();
			if (null != exifMetadata) {
				final TiffImageMetadata.GPSInfo gpsInfo = exifMetadata.getGPS();
				if (null != gpsInfo) {
					final String gpsDescription = gpsInfo.toString();
					longitude = gpsInfo.getLongitudeAsDegreesEast();
					latitude = gpsInfo.getLatitudeAsDegreesNorth();

					LOGGER.trace("GPS Description: " + gpsDescription);
					LOGGER.trace("GPS Longitude (Degrees East): " + longitude);
					LOGGER.trace("GPS Latitude (Degrees North): " + latitude);
				}
			}

			final List<ImageMetadataItem> items = jpegMetadata.getItems();
			for (int i = 0; i < items.size(); i++) {
				final ImageMetadataItem item = items.get(i);
				LOGGER.trace("    " + "item: " + item);
			}
		}
		
		// Amazon Rekognition Labels als String Array umwandeln
		String[] labelsStr = null;
		
		if (labels != null) {
			labelsStr = new String[labels.size()];
			
			for (int i = 0; i < labels.size(); i++) {
				labelsStr[i] = labels.get(i).getName();
			}
			
		} else {
			labelsStr = new String[0];
		}
		
		final PhotoMetadaten metaDaten = new PhotoMetadaten(user.getUuid(), dateTimeOriginal, longitude, latitude, 
				breite, hoehe, TehameProperties.PHOTO_BUCKET, s3key, zugehoerigkeit, labelsStr);
		LOGGER.trace(metaDaten);
		
		return metaDaten;
	}

	private static void logTagValue(final JpegImageMetadata jpegMetadata, final TagInfo tagInfo) {
		
		final TiffField field = jpegMetadata.findEXIFValueWithExactMatch(tagInfo);
		
		if (field == null) {
			LOGGER.trace(tagInfo.name + ": " + "Not Found.");
		} else {
			LOGGER.trace(tagInfo.name + ": " + field.getValueDescription());
		}
	}
    
    public static long toUnixTimestamp(String exifDatum) {
    	LocalDateTime date = null;
    	
    	try {
    		// Entferne vorher die Hochkommata aus dem Text '2016:06:13 17:06:23'
    		date = LocalDateTime.parse(exifDatum.replace("'", ""), DATE_TIME_FORMATTER);
    	} catch (DateTimeParseException e) {
    		LOGGER.error(e);
    		// TODO Was machen mit Photos ohne Datum oder Geodaten?
    		return -1;
    	}
    	
    	// TODO Über Timezones gedanken machen, der Client müsste seine Timezone mitschicken,
    	// denn die steht nicht im Exif.
    	long timestamp = date.toEpochSecond(ZoneOffset.UTC);
    	return timestamp;
    }
}
