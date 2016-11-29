package de.tehame.photo;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import de.tehame.TehameProperties;
import de.tehame.photo.meta.MetadatenMongoDB;
import de.tehame.photo.meta.PhotoMetadaten;
import de.tehame.user.UserBean;
import java.io.Serializable;
import java.util.ArrayList;

@Named
@SessionScoped
public class PhotoMB implements Serializable {

	private static final long serialVersionUID = 1L;
		
	@Inject
	UserBean userBean;
	
	@Inject
	private MetadatenMongoDB metadatenDB;
	
	private int bildHoehe = 50;
	
	/**
	 * Liefere Die Bilder der entsprenden Kategorie (tabIndex) als HTML-Image-Src
	 * @return
	 */
	public ArrayList<String> getBilderFuerZugehoerigkeit(int zugehoerigkeit) {
		ArrayList<String> res = new ArrayList<String>();
		
		ArrayList<PhotoMetadaten> metadatens = new ArrayList<PhotoMetadaten>();
		metadatens = metadatenDB.getPhotosByUserAndZugehoerigkeit(userBean.getLoggedInUser(), zugehoerigkeit);
		
		for(PhotoMetadaten metadaten : metadatens) {
			res.add(new String(TehameProperties.IMAGE_CALLBACK_URL_JSF 
					+ metadaten.getS3bucket() + "/"
					+ metadaten.getS3key() + "/"));
		}
		
		return res;		
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

	
}
