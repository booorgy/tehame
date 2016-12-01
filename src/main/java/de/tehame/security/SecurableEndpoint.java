package de.tehame.security;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import de.tehame.user.User;
import de.tehame.user.UserBean;

public abstract class SecurableEndpoint {
	protected void auth(User user, String passwort, final UserBean userBean) {
		if (!userBean.authenticated(user, passwort)) {
			throw new WebApplicationException(Status.UNAUTHORIZED);
		}
	}
}
