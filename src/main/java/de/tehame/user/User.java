package de.tehame.user;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;

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
	
	public User() { super(); }
	
	public User(String email, String passwort, UUID uuid) {
		super();
		this.email = email;
		this.passwort = passwort;
		this.setUuid(uuid.toString());
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
