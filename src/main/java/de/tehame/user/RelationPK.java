package de.tehame.user;

import java.io.Serializable;
import javax.persistence.*;

@Embeddable
public class RelationPK implements Serializable {
	private static final long serialVersionUID = 1L;

	@Column(insertable=false, updatable=false)
	private String uuidusera;

	@Column(insertable=false, updatable=false)
	private String uuiduserb;

	/**
	 * Der Beziehungstyp (Familie, Freund, Partner).
	 */
	private int type;

	public RelationPK() {
	}
	public String getUuidusera() {
		return this.uuidusera;
	}
	public void setUuidusera(String uuidusera) {
		this.uuidusera = uuidusera;
	}
	public String getUuiduserb() {
		return this.uuiduserb;
	}
	public void setUuiduserb(String uuiduserb) {
		this.uuiduserb = uuiduserb;
	}
	public int getType() {
		return this.type;
	}
	public void setType(int type) {
		this.type = type;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof RelationPK)) {
			return false;
		}
		RelationPK castOther = (RelationPK)other;
		return 
			this.uuidusera.equals(castOther.uuidusera)
			&& this.uuiduserb.equals(castOther.uuiduserb)
			&& (this.type == castOther.type);
	}

	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.uuidusera.hashCode();
		hash = hash * prime + this.uuiduserb.hashCode();
		hash = hash * prime + this.type;
		
		return hash;
	}
}