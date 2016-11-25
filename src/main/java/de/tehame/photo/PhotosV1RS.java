package de.tehame.photo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Principal;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.StreamingOutput;

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
	
	@Context 
	private SecurityContext securityContext;
	
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
	
	/**
	 * Liefert ein Photo aus. 
	 * In diesem Fall muss über die Header authentifiziert werden.
	 * 
	 * curl http://localhost:8080/tehame/rest/v1/photos/tehame/b533e8f7-5fdd-484e-8a06-f24d3cf643cd -v -H "email: admin@tehame.de" -H "passwort: a"
	 * 
	 * @param bucketName S3 Bucket Name.
	 * @param objectKey S3 Object Key.
	 * @return Photo.
	 */
	@GET
	@Path("{bucket}/{objectkey}")
	@Produces("image/jpg")
	public Response photo(
			@PathParam("bucket") 	 final String bucketName, 
			@PathParam("objectkey")  final String objectKey,
			@HeaderParam("email") 	 final String email, 
			@HeaderParam("passwort") final String passwort) {
		
		User user = this.userBean.sucheUser(email);
		this.auth(user, passwort);
		
		// TODO auth
//		LOGGER.trace("Request von User '" + this.getUserName() + "' zu Photo '" 
//				+ bucketName + "/" + objectKey + "'");
		
		return Response.ok(new StreamingOutput() {
			@Override
			public void write(OutputStream os) throws IOException, WebApplicationException {
				
				byte[] photo = null;
				try {
					photo = PhotosV1RS.this.photosS3.ladePhoto(
							PhotosS3.BUCKET_THUMBNAILS, objectKey);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				os.write(photo);
				os.flush();
			}
		}).build();
	}

	private void auth(User user, String passwort) {
		if (!this.userBean.authenticated(user, passwort)) {
			throw new WebApplicationException(Status.UNAUTHORIZED);
		}
	}

	/**
	 * @return Der Name des angemeldeten Benutzers.
	 */
	private String getUserName() {
		String userName = null;
		Principal principal = this.securityContext.getUserPrincipal();
		
		if (principal != null) {
			userName = principal.getName();
		} else {
			throw new WebApplicationException(Status.UNAUTHORIZED);
		}
		
		return userName;
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
		
		User user = this.userBean.sucheUser(email);
		this.auth(user, passwort);
		
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
