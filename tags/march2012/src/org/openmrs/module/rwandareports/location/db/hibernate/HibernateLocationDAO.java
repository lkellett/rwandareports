package org.openmrs.module.rwandareports.location.db.hibernate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.openmrs.module.rwandareports.location.db.LocationDAO;

public class HibernateLocationDAO implements LocationDAO {

	protected static final Log log = LogFactory.getLog(HibernateLocationDAO.class);

	/**
	 * Hibernate session factory
	 */
	private SessionFactory sessionFactory;

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	
}
