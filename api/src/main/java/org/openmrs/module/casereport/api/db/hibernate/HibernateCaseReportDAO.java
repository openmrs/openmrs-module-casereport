/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.casereport.api.db.hibernate;

import static org.openmrs.module.casereport.CaseReport.Status;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Patient;
import org.openmrs.module.casereport.CaseReport;
import org.openmrs.module.casereport.api.db.CaseReportDAO;
import java.lang.reflect.Method;

/**
 * Default implementation of {@link CaseReportDAO}.
 */
public class HibernateCaseReportDAO implements CaseReportDAO {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	private SessionFactory sessionFactory;
	
	/**
	 * @param sessionFactory the sessionFactory to set
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * @return the sessionFactory
	 */
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}
	

	private org.hibernate.Session getCurrentSession() {
		try {
			return sessionFactory.getCurrentSession();
		}
		catch (NoSuchMethodError ex) {
			try {
				Method method = sessionFactory.getClass().getMethod("getCurrentSession", null);
				return (org.hibernate.Session)method.invoke(sessionFactory, null);
			}
			catch (Exception e) {
				log.error("Failed to get the hibernate session", e);
			}
			return null;
		}
	}
	
	/**
	 * @see CaseReportDAO#getCaseReport(Integer)
	 */
	@Override
	public CaseReport getCaseReport(Integer caseReportId) {
		return (CaseReport) getCurrentSession().get(CaseReport.class, caseReportId);
	}
	
	/**
	 * @see CaseReportDAO#getCaseReportByUuid(String)
	 */
	@Override
	public CaseReport getCaseReportByUuid(String uuid) {
		return (CaseReport) getCurrentSession().createCriteria(CaseReport.class).add(Restrictions.eq("uuid", uuid))
		        .uniqueResult();
	}
	
	/**
	 * @see CaseReportDAO#getCaseReports(Patient, boolean, String, Boolean, Status...)
	 */
	@Override
	public List<CaseReport> getCaseReports(Patient patient, boolean includeVoided, String orderBy, Boolean asc,
	                                       Status... statuses) {
		
		Criteria criteria = getCurrentSession().createCriteria(CaseReport.class);
		if (patient != null) {
			criteria.add(Restrictions.eq("patient", patient));
		}
		
		if (ArrayUtils.isNotEmpty(statuses)) {
			criteria.add(Restrictions.in("status", statuses));
		}
		
		if (!includeVoided) {
			criteria.add(Restrictions.eq("voided", false));
		}
		
		if (StringUtils.isNotBlank(orderBy) && asc != null) {
			Order order = asc ? Order.asc(orderBy) : Order.desc(orderBy);
			criteria.addOrder(order);
		}
		
		return criteria.list();
	}
	
	/**
	 * @see CaseReportDAO#saveCaseReport(CaseReport)
	 */
	@Override
	public CaseReport saveCaseReport(CaseReport caseReport) {
		getCurrentSession().saveOrUpdate(caseReport);
		return caseReport;
	}
}
