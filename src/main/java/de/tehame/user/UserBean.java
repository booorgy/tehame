package de.tehame.user;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.LocalBean;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.jboss.crypto.CryptoUtil;

import de.tehame.event.Event;

@Stateless
@LocalBean
public class UserBean {

	@PersistenceContext(unitName = "tehamePU")
	private EntityManager em;
	
	@Resource 
	private SessionContext context;
	
	/**
	 * @param user Ein neuer User.
	 * @return Ob der User registriert wurde.
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public boolean registrieren(User user) {
		
		TypedQuery<User> q = this.em.createQuery(
				"SELECT u FROM user AS u WHERE u.email = :email", 
				User.class)
				.setParameter("email", user.getEmail());
		
		try {
			q.getSingleResult();
		} catch (NoResultException e) {
			// Es wurde noch kein User gefunden.
			this.em.persist(user);
			return true;	
		}
		
		// Es wurde bereits ein User mit der EMail gefunden.
		return false;
	}
		
	/**
	 * Sucht einen User anhand seiner EMail Adresse.
	 * @param email EMail.
	 * @return User.
	 */
	public User sucheUser(String email) {
		TypedQuery<User> q = this.em.createQuery(
				"SELECT u FROM user AS u WHERE u.email = :email", 
				User.class)
				.setParameter("email", email);
		
		try {
			return q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}
	
	/**
	 * @return Die User Entity des angemeldeten Users.
	 */
	public User getLoggedInUser() {
		String login = this.context.getCallerPrincipal().getName();
        TypedQuery<User> createQuery = this.em.createQuery(
        		"from user where email = '" + login + "'", 
        		User.class);
        User user = createQuery.getSingleResult();                       
        return user;
	}
	
	/**
	 * @param user User Entity.
	 * @param passwort Passwort.
	 * @return Ob die Credentials gültig sind.
	 */
	public boolean authenticated(User user, String passwort) {
		
		if (user == null) {
			return false;
		}
		
		boolean authenticated;
		
		// TODO später passwort bereits gehasht übermitteln
		final String passwortHash = CryptoUtil.createPasswordHash(
				"SHA-256", "base64", null, null, passwort);
		
		authenticated = user.getPasswort().equals(passwortHash);
		
		return authenticated;
	}
	
	/**
	 * @param user User.
	 * @return Alle Relation eines Users.
	 */
	public List<String> sucheRelationen(User user) {
		User u = this.em.find(User.class, user.getUuid());
		List<Relation> relations = u.getRelations1();
		List<String> uuids = new ArrayList<String>(relations.size());
		
		for (Relation r : relations) {
			uuids.add(r.getId().getUuiduserb());
		}
		
		return uuids;
	}
	
	/**
	 * Sucht für einen User liste mit uuids der Beziehungen.
	 * @param user Der User
	 * @param zugehoerigkeit die enstprechende Zugehörigkeit
	 * @return liste mit uuids
	 */
	public List<String> sucheRelationenMitZugehoerigkeit(User user, int zugehoerigkeit) {
		User u = this.em.find(User.class, user.getUuid());
		List<Relation> relations = u.getRelations1();
		
		List<String> uuids = new LinkedList<String>();
		uuids.add(user.getUuid());
		
		// FIXME Ugly N+1 Problem
		for (Relation r : relations) {
			if (r.getId().getType() == zugehoerigkeit)
				uuids.add(r.getUser2().getUuid());
		}
		return uuids;
	}
}
