package org.openmrs.module.rwandareports.encounter.query.db.hibernate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.openmrs.OpenmrsObject;
import org.openmrs.module.reporting.IllegalDatabaseAccessException;
import org.openmrs.module.reporting.ReportingException;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterException;
import org.openmrs.module.reporting.report.util.SqlUtils;
import org.openmrs.module.rwandareports.encounter.EncounterGroup;
import org.openmrs.module.rwandareports.encounter.query.db.EncounterGroupQueryServiceDAO;

public class HibernateEncounterGroupQueryServiceDAO implements EncounterGroupQueryServiceDAO {
	
	
	protected static final Log log = LogFactory.getLog(HibernateEncounterGroupQueryServiceDAO.class);

	/**
	 * Hibernate session factory
	 */
	private SessionFactory sessionFactory;

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	public EncounterGroup executeSqlQuery(String sqlQuery, Map<String, Object> paramMap) { 
		try { 			
			validateSqlQuery(sqlQuery, paramMap);
			
			Query query = prepareQuery(sqlQuery, paramMap);	
			return executeQuery(query);
		} 
		catch (HibernateException e) { 
			throw new ParameterException("Error while executing SQL query [" + sqlQuery + "] with the parameters [" + paramMap + "]: " + e.getMessage() + ".  See tomcat log file for more details.", e);
		}
	}	
	
	
	/**
	 * This need to be a separate method so we can call it from both the 
	 * executeSqlQuery() and validateSqlQuery() methods 
	 */
	private EncounterGroup executeQuery(Query query) { 
		try { 			
				
			return new EncounterGroup(query.list());
		} 
		catch (HibernateException e) { 
			throw new ParameterException("Error while executing SQL query [" + query.getQueryString() + "]: " + e.getMessage() + ".  See tomcat log file for more details.", e);
		}
	}	
	
	
	
	/**
	 * Binds the given paramMap to the query by replacing all named 
	 * parameters (e.g. :paramName) with their corresponding values 
	 * in the parameter map.
	 * 
	 * TODO Should add support for other classes.  
	 * TODO Should refactor to make more generalizable (create a new param map with correct param values)
	 * 
	 * @param query
	 * @param paramMap
	 */
	@SuppressWarnings("unchecked")
	private void bindQueryParameters(Query query, Map<String, Object> paramMap) { 

		// Iterate over parameters and bind them to the Query object
		for(String paramName : paramMap.keySet()) { 			
			
			Object paramValue = paramMap.get(paramName);				
			
			// Indicates whether we should bind this parameter in the query 
			boolean bindParameter = (query.getQueryString().indexOf(":" + paramName) > 0 );
					
			if (bindParameter) { 
				// Make sure parameter value is not null
				if (paramValue == null) { 
					// TODO Should try to convert 'columnName = null' to 'columnName IS NULL'  
					throw new ParameterException("Cannot bind an empty value to parameter " + paramName + ". " + 
							"Please provide a real value or use the 'IS NULL' constraint in your query (e.g. 'table.columnName IS NULL').");					
				}
				
				// EncounterGroup (needs to be first, otherwise it will resolve as OpenmrsObject)
				if (EncounterGroup.class.isAssignableFrom(paramValue.getClass())) { 
					query.setParameterList(paramName, ((EncounterGroup) paramValue).getMemberIds());				
				}
				// OpenmrsObject (e.g. Location)
				else if (OpenmrsObject.class.isAssignableFrom(paramValue.getClass())) { 					
					query.setInteger(paramName, ((OpenmrsObject) paramValue).getId());
				}	
				// List<OpenmrsObject> (e.g. List<Location>)
				else if (List.class.isAssignableFrom(paramValue.getClass())) { 
					// If first element in the list is an OpenmrsObject
					if (OpenmrsObject.class.isAssignableFrom(((List) paramValue).get(0).getClass())) { 
						query.setParameterList(paramName, 
								SqlUtils.openmrsObjectIdListHelper((List<OpenmrsObject>) paramValue));
					}
					// a List of Strings, Integers?
					else { 
						query.setParameterList(paramName, 
								SqlUtils.objectListHelper((List<Object>) paramValue));
					}
				}
				// java.util.Date and subclasses
				else if (paramValue instanceof Date) {
					query.setDate(paramName, (Date) paramValue);
				}
				// String, Integer, et al (this might break since this is a catch all for all other classes)
				else { 
					query.setString(paramName, new String(paramValue.toString()));	// need to create new string for some reason
				}
			}
		}		
	}
	 
	
	/**
	 * Prepare a Hibernate Query object using the given sql query string 
	 * and parameter mapping.
	 * 
	 * @param sqlQuery
	 * @param paramMap
	 * @return	a Hibernate Query object
	 */
	public Query prepareQuery(String sqlQuery, Map<String, Object> paramMap) { 
		Query query = null;
		try { 			
			query = sessionFactory.getCurrentSession().createSQLQuery(sqlQuery.toString());					
			//query.setCacheMode(CacheMode.IGNORE);	// TODO figure out what this does before using it
						
			// Bind the query parameters (query is mutable
			bindQueryParameters(query, paramMap);
			
		} 
		catch (Exception e) { 
			log.error("Error while preparing sql query " + query.getQueryString() + ": " + e.getMessage());
			throw new ReportingException("Error while preparing sql query " + query.getQueryString() + ": " + e.getMessage(), e);			
		}
		return query;
	}
	
	
	/**
	 * Validate the given sqlQuery based on the following validation rules.
	 * 
	 * @should validate that given paramMap matches parameter in given sqlQuery
	 * @should validate that given sqlQuery is not null or empty
	 * @should validate that given sqlQuery is valid sql
	 * @should validate that given sqlQuery has single column projection
	 * @should validate that given sqlQuery does not contain select star
	 * @should validate that given sqlQuery does not contain sql injection attack
	 * 
	 * @param sqlQuery
	 */
	private void validateSqlQuery(String sqlQuery, Map<String, Object> paramMap) throws ReportingException { 

		// TODO Should not allow user to provide empty sql query
		// FIXME This is going to be a really quick validation implementation  
		// TODO We need to implement a validation framework within the reporting module
		if (sqlQuery == null || sqlQuery.equals("")) 
			throw new ReportingException("SQL query string is required");
		if (!SqlUtils.isSelectQuery(sqlQuery)) {
			throw new IllegalDatabaseAccessException();
		}
    	// TODO Should have specified all parameters required to execute the query
    	List<Parameter> parameters = getNamedParameters(sqlQuery);    	
    	for (Parameter parameter : parameters) { 
    		Object parameterValue = paramMap.get(parameter.getName());
    		if (parameterValue == null) 
    			throw new ParameterException("Must specify a value for the parameter [" +  parameter.getName() + "]");    		
    	}		
		
		// TODO Should have a single column projection
		
		// TODO Should not allow use of 'select *'
		
		// TODO Should allow use of 'select distinct column'
		
		// TODO Should execute explain plan to make sure 
		// FIXME This might be a bad idea if the query does not perform well so 
		// make sure it's the last step in the validation process.
		try { 
			// Assume we are executing query on mysql, oracle, 
			// This isn't going to work like this ... 
			/* 
			Query query = 
				prepareQuery("explain plan for " + sqlQuery, paramMap);			
			executeQuery(query);
			*/
			
		} 
		catch (Exception e) { 
			log.error("Error while validating SQL query: " + e.getMessage(), e);
			throw new ReportingException("Error while validating SQL query: " + e.getMessage() + ".  See tomcat log file for more details.", e);
		}
	}	
	
	/**
	 * Simple regular expression parser.  
	 * 
	 * As a first pass, we must support named parameters:  
	 *  	column = :paramName2
	 *  
	 * Eventually we Should support named parameters with datatype: 
	 * 		column = :paramName::java.lang.Date
	 */
	public List<Parameter> getNamedParameters(String sqlQuery) {
		List<Parameter> parameters = new ArrayList<Parameter>();

		// TODO Need to move regex code into a utility method 
		Pattern pattern = Pattern.compile("\\:\\w+\\b");
		Matcher matcher = pattern.matcher(sqlQuery);

		while (matcher.find()) {			
			// Index is 1 because we need to strip off the colon (":")
			String parameterName = matcher.group().substring(1);			
			Parameter parameter = new Parameter();			
			parameter.setName(parameterName);
			parameter.setLabel(parameterName);
			parameter.setType(String.class);	// TODO Need to be able to support more data types!
			parameters.add(parameter);
		}		
		return parameters;
	}
	
	
	
}
