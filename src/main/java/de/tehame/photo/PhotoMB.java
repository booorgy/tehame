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
public class PhotoMB implements Serializable {

	private static final long serialVersionUID = 1L;
		
	@Inject
	UserBean userBean;
	
	@Inject
	EventBean eventBean;
	
	@Inject
	private MetadatenMongoDB metadatenDB;
	
	private int bildHoehe = 50;
	
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
		metadatens = metadatenDB.getPhotosByUserAndZugehoerigkeit(userBean.getLoggedInUser(), zugehoerigkeit);
		
		for (PhotoMetadaten metadaten : metadatens) {
			res.add(baseURL + "rest/v1/photos/www/" + TehameProperties.THUMBNAIL_BUCKET + "/" + metadaten.getS3key());
		}
		
		return res;		
	}
		
/*	public ArrayList<Event> getEventsFuerZugehoerigkeit(int zugehoerigkeit) {
		ArrayList<Event> res = new ArrayList<Event>();
		res = (ArrayList<Event>) eventBean.sucheEvents(userBean.sucheRelationenMitZugehoerigkeit(userBean.getLoggedInUser(), zugehoerigkeit));
		return res;
	}*/
	
	/**
	 * Liefere Die Bilder der entsprenden Kategorie (tabIndex) als HTML-Image-Src
	 * @return
	 */
	public ArrayList<Event> getEventsFuerZugehoerigkeit(int zugehoerigkeit) {
		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		String url = request.getRequestURL().toString();
		// Beispiel URL: http://localhost:8080/tehame/secured/photos.xhtml
		// Beispiel URI:                      /tehame/secured/photos.xhtml
		// Base URL:     http://localhost:8080
		String baseURL = url.substring(0, url.length() - request.getRequestURI().length()) + request.getContextPath() + "/";
		
		
		
		ArrayList<PhotoMetadaten> metadatens = new ArrayList<PhotoMetadaten>();
		
		List<String> users = userBean.sucheRelationenMitZugehoerigkeit(userBean.getLoggedInUser(), zugehoerigkeit);
		ArrayList<Event> events = (ArrayList<Event>) eventBean.sucheEvents(users);
		
		for (Event event : events) {
			ArrayList<Photo> photos = new ArrayList<Photo>();
			metadatens = metadatenDB.getPhotosByUserAndZugehoerigkeit(
					users, 
					zugehoerigkeit, 
					events.stream().map(e -> event.getUuid()).toArray(String[]::new));
		
			for (PhotoMetadaten metadaten : metadatens) {
				photos.add(new Photo(
						baseURL + "rest/v1/photos/www/" + TehameProperties.THUMBNAIL_BUCKET + "/" + metadaten.getS3key(), 
						baseURL + "rest/v1/photos/www/" + TehameProperties.PHOTO_BUCKET + "/" + metadaten.getS3key(),
						metadaten, 
						event));
			}
				event.setPhotos(photos);					
		}
		
		// Nur die Events zurückgeben, welche auch Bilder haben bzw. die Zugehörigkeit passt.
		ArrayList<Event> eventsMitBildern = new ArrayList<Event>();
		for (Event event : events) {
			if (event.getPhotos().size() > 0)
				eventsMitBildern.add(event);
		}
		
		return eventsMitBildern;		
	}
	
	public String test() {
		ArrayList<PhotoMetadaten> check = metadatenDB.getPhotosByUser(userBean.getLoggedInUser());
		if (check.size() > 0) {
			return check.get(0).toString();
		}
		return userBean.getLoggedInUser().getEmail();
	}

	public int getBildHoehe() {
		return bildHoehe;
	}

	public void setBildHoehe(int bildHoehe) {
		this.bildHoehe = bildHoehe;
	}

	public ArrayList<String> getPhotoURIs() {
		return photoURIs;
	}
}
