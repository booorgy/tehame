package de.tehame.photo;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import de.tehame.TehameProperties;
import de.tehame.event.Event;
import de.tehame.event.EventBean;
import de.tehame.photo.meta.MetadatenMongoDB;
import de.tehame.photo.meta.PhotoMetadaten;
import de.tehame.user.UserBean;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named
@SessionScoped
public class PhotoMB2 implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Inject
	UserBean userBean;
	
	@Inject
	EventBean eventBean;
	
	@Inject
	private MetadatenMongoDB metadatenDB;
	
	private ArrayList<String> photoURIs = null;
	
	public void selektiereBilder(int selectedZugehoerigkeit) {
		this.photoURIs = this.getBilderFuerZugehoerigkeit(selectedZugehoerigkeit);
	}

	/**
	 * Liefere Die Bilder der entsprenden Kategorie (tabIndex) als HTML-Image-Src
	 * @return
	 */
	public ArrayList<String> getBilderFuerZugehoerigkeit(int zugehoerigkeit) {
		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		String url = request.getRequestURL().toString();
		// Beispiel URL: http://localhost:8080/tehame/secured/photos.xhtml
		// Beispiel URI:                      /tehame/secured/photos.xhtml
		// Base URL:     http://localhost:8080
		String baseURL = url.substring(0, url.length() - request.getRequestURI().length()) + request.getContextPath() + "/";
		
		ArrayList<String> res = new ArrayList<String>();
		
		ArrayList<PhotoMetadaten> metadatens = new ArrayList<PhotoMetadaten>();
		
		List<String> users = userBean.sucheRelationenMitZugehoerigkeit(userBean.getLoggedInUser(), zugehoerigkeit);
		List<Event> events = eventBean.sucheEvents(users);
		
		metadatens = metadatenDB.getPhotosByUserAndZugehoerigkeit(
				users, 
				zugehoerigkeit, 
				events.stream().map(e -> e.getUuid()).toArray(String[]::new));
		
		for (PhotoMetadaten metadaten : metadatens) {
			res.add(baseURL + "rest/v1/photos/www/" + TehameProperties.THUMBNAIL_BUCKET + "/" + metadaten.getS3key());
		}
		
		return res;		
	}
	
	public ArrayList<String> getPhotoURIs() {
		return photoURIs;
	}
}
