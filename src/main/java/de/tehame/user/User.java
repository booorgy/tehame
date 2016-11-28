package de.tehame.user;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.jboss.crypto.CryptoUtil;

/**
 * JPA Entity für MySQL DB.
 * Der Tabellen-Name muss angegeben werden, 
 * weil unter Linux die Groß-/Kleinschreibung relevant ist. 
 * 
 * Es ist egal, ob Relations1 oder Relations2 verwendet wird. 
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
	 * Bidirektionale Beziehung (1:N) zu Relation. (Lazy Loading ist wichtig)
	 */
	@OneToMany(mappedBy="user1")
	private List<Relation> relations1;

	/**
	 * Bidirektionale Beziehung (1:N) zu Relation. (Lazy Loading ist wichtig)
	 */
	@OneToMany(mappedBy="user2")
	private List<Relation> relations2;
	
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
	public List<Relation> getRelations1() {
		return this.relations1;
	}
	public void setRelations1(List<Relation> relations1) {
		this.relations1 = relations1;
	}
	public Relation addRelations1(Relation relations1) {
		getRelations1().add(relations1);
		relations1.setUser1(this);

		return relations1;
	}
	public Relation removeRelations1(Relation relations1) {
		getRelations1().remove(relations1);
		relations1.setUser1(null);

		return relations1;
	}
	public List<Relation> getRelations2() {
		return this.relations2;
	}
	public void setRelations2(List<Relation> relations2) {
		this.relations2 = relations2;
	}
	public Relation addRelations2(Relation relations2) {
		getRelations2().add(relations2);
		relations2.setUser2(this);

		return relations2;
	}
	public Relation removeRelations2(Relation relations2) {
		getRelations2().remove(relations2);
		relations2.setUser2(null);

		return relations2;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result + ((passwort == null) ? 0 : passwort.hashCode());
		result = prime * result + ((relations1 == null) ? 0 : relations1.hashCode());
		result = prime * result + ((relations2 == null) ? 0 : relations2.hashCode());
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equals(other.email))
			return false;
		if (passwort == null) {
			if (other.passwort != null)
				return false;
		} else if (!passwort.equals(other.passwort))
			return false;
		if (relations1 == null) {
			if (other.relations1 != null)
				return false;
		} else if (!relations1.equals(other.relations1))
			return false;
		if (relations2 == null) {
			if (other.relations2 != null)
				return false;
		} else if (!relations2.equals(other.relations2))
			return false;
		if (uuid == null) {
			if (other.uuid != null)
				return false;
		} else if (!uuid.equals(other.uuid))
			return false;
		return true;
	}
}
