package de.tehame.photo;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import de.tehame.photo.meta.MetadatenMongoDB;
import de.tehame.photo.meta.PhotoMetadaten;
import de.tehame.user.UserBean;

import java.io.Serializable;
import java.util.ArrayList;

@Named
@SessionScoped
public class PhotoMB implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int tabIndex = 0;
	
	@Inject
	UserBean userBean;
	
	@Inject
	private MetadatenMongoDB metadatenDB;
	
	/**
	 * Liefere Die Bilder der entsprenden Kategorie (tabIndex) als HTML
	 * @return
	 */
	public String zeigeBilderFuerKategorie() {
		return "<img src=\"http://localhost:8080/tehame/rest/v1/photos/www/tehame20161/MyObjectKey-03bad40f-c5ab-4b6b-9783-7b3bf040406a\" width=\"700\"/>";
	}
	
	public String test() {
		ArrayList<PhotoMetadaten> check = metadatenDB.getPhotosByUser(userBean.getLoggedInUser());
		if (check.size() > 0) {
			return check.get(0).toString();
		}
		return userBean.getLoggedInUser().getEmail();
	}

	public int getTabIndex() {
		return tabIndex;
	}

	public void setTabIndex(int tabIndex) {
		this.tabIndex = tabIndex;
	}

	
}
