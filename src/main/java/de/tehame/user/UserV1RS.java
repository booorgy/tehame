package de.tehame.user;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.ejb.EJB;
import javax.ejb.Stateless;
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
import org.jboss.logging.Logger;

import de.tehame.examples.UserBeanMongoDB;
import de.tehame.photo.meta.MetadataBuilder;
import de.tehame.photo.meta.PhotoMetadaten;

@Path("v1/user")
@Stateless
public class UserV1RS {
	
	private static final Logger LOGGER = Logger.getLogger(UserV1RS.class);
	
	@EJB
	private UserBean userBean;
	
	@POST
	public String neuerUser(@HeaderParam("email") String email, @HeaderParam("passwort") String passwort) {
		String id = null;
		
		User user = new User(email, passwort);
		
		this.userBean.persist(user);
		
		return id;
	}
}
