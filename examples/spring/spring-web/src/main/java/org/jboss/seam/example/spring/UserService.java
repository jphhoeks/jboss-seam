package org.jboss.seam.example.spring;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jboss.seam.annotations.Logger;
import org.jboss.seam.log.Log;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Mike Youngstrom
 * @author Marek Novotny
 *
 */
public class UserService {
	
	@Logger
	private static Log logger;
	
	
	@PersistenceContext
	private EntityManager em;

	public UserService() {
		super(); 
	}
	
	@Transactional
    public boolean changePassword(String username, String oldPassword, String newPassword) {
		logger.debug("change password " + oldPassword + " to " + newPassword);
        if (newPassword == null || newPassword.length()==0) {
            throw new IllegalArgumentException("newPassword cannot be null.");
        }

        User user = findUser(username);
        logger.debug("USER" + user);
        if (user.getPassword().equals(oldPassword)) {
            user.setPassword(newPassword);
            return true;
        } else {
            return false;
        }
    }

	@Transactional
    public User findUser(String username) {
        if (username == null || "".equals(username)) {
            throw new IllegalArgumentException("Username cannot be null");
        }
        return em.find(User.class, username);
    }

	@Transactional
    public User findUser(String username, String password) {
        try {
            List<User> result = em.createQuery("select u from User u where u.username=:username and u.password=:password", User.class).setParameter("username", username).setParameter("password", password).getResultList();
            if (!result.isEmpty()) {
               return result.get(0);   
            }
            else {
               return null;
            }
        } catch (DataAccessException e) {
            return null;
        }
    }

	@Transactional
    public void createUser(User user) throws ValidationException {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        User existingUser = findUser(user.getUsername());
        if (existingUser != null) {
            throw new ValidationException("Username "+user.getUsername()+" already exists");
        }
        em.persist(user);
        em.flush();
    }
}
