package de.tehame.user;

import javax.ejb.LocalBean;
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
}
