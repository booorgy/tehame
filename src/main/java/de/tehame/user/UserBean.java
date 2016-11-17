package de.tehame.user;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
@LocalBean
public class UserBean {

	@PersistenceContext(unitName = "tehamePU")
	private EntityManager em;
	
	public void persist(User user) {
		this.em.persist(user);
	}
}
