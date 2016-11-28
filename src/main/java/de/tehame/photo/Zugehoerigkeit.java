package de.tehame.photo;

public enum Zugehoerigkeit {
	PRIVAT(0),
	FAMILIE(1),
	FREUNDE(2),
	OEFFENTLICH(3);
	
	private int status;
	
	Zugehoerigkeit(int status) {
		this.status = status;
	}
}
