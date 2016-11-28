package de.tehame.user;

import java.io.Serializable;
import javax.persistence.*;

/**
 * Eine Relation zwischen zwei Usern.
 */
@Entity(name = "relation")
public class Relation implements Serializable {
	private static final long serialVersionUID = 1L;

	@EmbeddedId
	private RelationPK id;

	/**
	 * Bidirektionale Beziehung (N:1) zu User. (Lazy Loading ist wichtig) 
	 * Diese Entity ist nicht für das Ändern der UUIDs zuständig.
	 */
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="uuidusera", insertable=false, updatable=false)
	private User user1;

	/**
	 * Bidirektionale Beziehung (N:1) zu User. (Lazy Loading ist wichtig) 
	 * Diese Entity ist nicht für das Ändern der UUIDs zuständig.
	 */
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="uuiduserb", insertable=false, updatable=false)
	private User user2;

	public Relation() { }

	public RelationPK getId() {
		return this.id;
	}
	public void setId(RelationPK id) {
		this.id = id;
	}
	public User getUser1() {
		return this.user1;
	}
	public void setUser1(User user1) {
		this.user1 = user1;
	}
	public User getUser2() {
		return this.user2;
	}
	public void setUser2(User user2) {
		this.user2 = user2;
	}
}
