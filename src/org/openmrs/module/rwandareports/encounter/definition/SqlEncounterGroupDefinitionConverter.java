package org.openmrs.module.rwandareports.encounter.definition;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.SerializedObject;
import org.openmrs.module.reporting.definition.DefinitionContext;
import org.openmrs.module.reporting.definition.converter.DefinitionConverter;
import org.openmrs.module.reporting.definition.service.SerializedDefinitionService;



@Handler
public class SqlEncounterGroupDefinitionConverter implements DefinitionConverter{
		
	protected static Log log = LogFactory.getLog(SqlEncounterGroupDefinitionConverter.class);

	/**
	 * @see DefinitionConverter#getInvalidDefinitions()
	 */
	public List<SerializedObject> getInvalidDefinitions() {
    	SerializedDefinitionService service = Context.getService(SerializedDefinitionService.class);
    	return service.getInvalidDefinitions(SqlEncounterGroupDefinition.class, true);
	}
	
	/**
	 * @see DefinitionConverter#convert()
	 * @should convert legacy definitions to latest format
	 */
	public boolean convertDefinition(SerializedObject so) {
		
		String xml = so.getSerializedData();
		log.debug("Starting xml: " + xml);
		
		try {
			int qStart = xml.indexOf("<queryString>") + 13;
			int qEnd = xml.indexOf("</queryString>");
			String queryString = xml.substring(qStart, qEnd);
			log.debug("Retrieved query string: " + queryString);
			
			StringBuilder newXml = new StringBuilder();
			newXml.append(xml.substring(0, xml.indexOf("<queryDefinition ")));
			newXml.append("<query>"+queryString+"</query>");
			newXml.append(xml.substring(xml.indexOf("</queryDefinition>") + 18, xml.length()));
			log.debug("Ending xml: " + newXml);
			
			so.setSerializedData(newXml.toString());
			Context.getService(SerializedDefinitionService.class).saveSerializedDefinition(so);
			
			// Confirm this works
			SqlEncounterGroupDefinition scd = DefinitionContext.getDefinitionByUuid(SqlEncounterGroupDefinition.class, so.getUuid());
			log.info("Successfully converted SqlEncounterGroupDefinition named '" + scd.getName() + "' to new format");
			return true;
		}
		catch (Exception e) {
			log.warn("Unable to successfully migrate definition with uuid: " + so.getUuid(), e);
			return false;
		}
	}
	
}
