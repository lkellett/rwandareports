package org.openmrs.module.rwandareports.serializer;

import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.serializer.ReportingShortConverter;
import org.openmrs.module.rwandareports.objectgroup.definition.ObjectGroupDefinition;
import org.openmrs.module.rwandareports.objectgroup.service.ObjectGroupDefinitionService;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.ConverterLookup;
import com.thoughtworks.xstream.mapper.Mapper;

public class ObjectGroupDefinitionConverter extends ReportingShortConverter implements Converter {
	
	public ObjectGroupDefinitionConverter(Mapper mapper, ConverterLookup converterLookup) {
	    super(mapper, converterLookup);
    }


	
	public boolean canConvert(Class c) {
		return ObjectGroupDefinition.class.isAssignableFrom(c);
	}



	@Override
	public Object getByUUID(String arg0) {
		return Context.getService(ObjectGroupDefinitionService.class).getDefinitionByUuid(arg0);
	}
	
}
