package org.openmrs.module.rwandareports.serializer;

import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.serializer.ReportingShortConverter;
import org.openmrs.module.rwandareports.encounter.definition.EncounterGroupDefinition;
import org.openmrs.module.rwandareports.encounter.service.EncounterGroupDefinitionService;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.ConverterLookup;
import com.thoughtworks.xstream.mapper.Mapper;

public class EncounterGroupDefinitionConverter extends ReportingShortConverter implements Converter {
	
	public EncounterGroupDefinitionConverter(Mapper mapper, ConverterLookup converterLookup) {
	    super(mapper, converterLookup);
    }


	
	public boolean canConvert(Class c) {
		return EncounterGroupDefinition.class.isAssignableFrom(c);
	}



	@Override
	public Object getByUUID(String arg0) {
		return Context.getService(EncounterGroupDefinitionService.class).getDefinitionByUuid(arg0);
	}
	
}
