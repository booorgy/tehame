package de.tehame.user;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.jboss.crypto.CryptoUtil;

/**
 * JPA Entity für MySQL DB.
 * Der Tabellen-Name muss angegeben werden, 
 * weil unter Linux die Groß-/Kleinschreibung relevant ist. 
 */
@Entity(name = "user")
public class User implements Serializable {
	private static final long serialVersionUID = 7914795743794418063L;

	/**
	 * Die Verwendung einer UUID macht es einfacher,
	 * weil die von der DB generierte ID nicht ermittelt werden muss.
	 */
	@Id
	private String uuid;
	private String email;
	private String passwort;
	
	/**
	 * Default Constructor für JPA.
	 */
	public User() { super(); }
	
	/**
	 * Instanziert eine neue User Entity.
	 * 
	 * @param email EMail.
	 * @param passwort Unverschlüsseltes Passwort.
	 */
	public User(String email, String passwort) {
		super();
		this.email = email;
		this.passwort = CryptoUtil.createPasswordHash("SHA-256", "base64", null, null, passwort);
		this.setUuid(UUID.randomUUID().toString());
	}
	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPasswort() {
		return passwort;
	}
	public void setPasswort(String passwort) {
		this.passwort = passwort;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}	
}
