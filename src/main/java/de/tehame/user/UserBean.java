package de.tehame.user;

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
	 * Liefert das UserObject des angemeldeten Users.
	 * @return
	 */
	public User getLoggedInUser() {
		String login = context.getCallerPrincipal().getName();
        TypedQuery<User> createQuery = em.createQuery("from user where email = '" + login + "'", User.class);
        User user = createQuery.getSingleResult();                       
        return user;
	}
}
