package de.tehame.rs.v1;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jboss.logging.Logger;

@Path("v1/photos")
public class Photos {
	
	private static final Logger LOGGER = Logger.getLogger(Photos.class);

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
	
	// Beispiel: curl --data-binary ./img.jpg localhost:8080/tehame/v1/photos -v -H "Content-Type: image/jpeg" 
	@POST
	@Path("/")
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes("image/jpeg")
	public String addPhoto(InputStream is) {

		byte[] bytes = new byte[1024];
		int readBytes = 0;

		try {
			while ((readBytes = is.read(bytes)) != -1) {
				LOGGER.trace(readBytes);
			}
		} catch (IOException e) {
			LOGGER.error(e);
		}

		return "eine id";
	}
}
