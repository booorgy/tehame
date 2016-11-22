package de.tehame.photo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.imaging.ImageReadException;
import org.jboss.crypto.CryptoUtil;
import org.jboss.logging.Logger;

import de.tehame.photo.meta.MetadataBuilder;
import de.tehame.photo.meta.MetadatenMongoDB;
import de.tehame.photo.meta.PhotoMetadaten;
import de.tehame.user.User;
import de.tehame.user.UserBean;

@Path("v1/photos")
public class PhotosV1RS {
	
	private static final Logger LOGGER = Logger.getLogger(PhotosV1RS.class);
	
	@Inject
	private UserBean userBean;
	
	@Inject
	private MetadatenMongoDB metadatenDB;
	
	@Inject
	private PhotosS3 photosS3;

	@GET
	@Path("status")
	public String status() {
		return "OK";
	}
	
	@GET
	@Path("{id}")
	public String photo(@PathParam("id") String id) {
		return id;
	}
	
	// Beispiel: curl http://localhost:8080/tehame/rest/v1/photos -v -H "Content-Type: image/jpeg" -H "email: admin@tehame.de" -H "passwort: a" --data-binary @"../../photos/trump.jpg"
	// Das @ Zeichen definiert einen Pfad
	@POST
	@Path("/")
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes("image/jpeg")
	public String addPhoto(
			final InputStream is, 
			@HeaderParam("email") String email, 
			@HeaderParam("passwort") String passwort) {
		
		LOGGER.trace("Versuche Photo hochzuladen. HeaderParams: email: " 
				+ email + ", passwort: " + passwort);
		
		final User user = this.userBean.sucheUser(email);
		
		// TODO später passwort bereits gehasht übermitteln
		final String passwortHash = CryptoUtil.createPasswordHash(
				"SHA-256", "base64", null, null, passwort);
		
		if (user != null && user.getPasswort().equals(passwortHash)) {		
			// TODO prüfen, dass upload ein bild ist und nicht ausführbar etc. (MetaDaten Angriffe)
			
			final byte[] fileData = this.leseStream(is);
			
			String s3key = null;
			
			if (fileData != null && fileData.length != 0) {
				
				PhotoMetadaten metadaten = null;
				
				try {
					metadaten = MetadataBuilder.getMetaData(fileData);
				} catch (ImageReadException e) {
					LOGGER.error("Das Bild konnte nicht geparsed werden."); 
					throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
				} catch (IOException e) {
					LOGGER.error(e);
					throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
				}
				
				try {
					s3key = this.photosS3.speicherePhoto(fileData);
				} catch (Exception e) {
					LOGGER.error("Konnte Object nicht in S3 speichern.", e);
				}
				
				try {
					this.metadatenDB.savePhotoDetailsToMongo(user, s3key, "tehame", metadaten);
				} catch (Exception e) {
					LOGGER.error("Konnte Photo Details nicht in MongoDB speichern.", e);
				}
				
				// TODO wenn ein fehler auftritt schritte davor rückgängig machen
				
			} else {
				throw new WebApplicationException("File size is zero", Response.Status.BAD_REQUEST);
			}
			
			return s3key;
		} else {
			throw new WebApplicationException( Response.Status.UNAUTHORIZED);
		}
	}

	/**
	 * Erstellt im Arbeitsspeicher ein Byte-Array aus dem InputStream.
	 * 
	 * @param is InputStream des Uploads.
	 * @return ByteArray aus dem InputStream.
	 */
	private byte[] leseStream(final InputStream is) {
		byte[] filedata = null;
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int anzBytes = 0;
		int anzBytesTotal = 0;
		boolean ioError = false;

		try {
			while ((anzBytes = is.read(buffer)) != -1) {
				anzBytesTotal += anzBytes;
				// TODO Maximale Größe limitieren
				os.write(buffer, 0, anzBytes);
			}

			LOGGER.trace(anzBytesTotal + " Bytes aus InputStream gelesen");
			
			os.flush();
			filedata = os.toByteArray();
			
		} catch (IOException e) {
			LOGGER.error(e);
			ioError = true;
		} finally {
			try {
				os.close();
			} catch (IOException e) {
				LOGGER.error(e);
				ioError = true;
			}
			try {
				is.close();
			} catch (IOException e) {
				LOGGER.error(e);
				ioError = true;
			}
		}
		
		if (ioError) {
			throw new WebApplicationException("Internal I/O Error while processing upload", 
					Response.Status.INTERNAL_SERVER_ERROR);
		}
		
		return filedata;
	}
}
