package org.openmrs.module.rwandareports.dataset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.definition.BaseDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.indicator.Indicator;
import org.openmrs.module.reporting.indicator.dimension.Dimension;
import org.openmrs.module.rwandareports.report.definition.RollingDailyPeriodIndicatorReportDefinition;
import org.openmrs.module.rwandareports.report.definition.RollingDailyPeriodIndicatorReportDefinition.RollingBaseReportQueryType;
import org.openmrs.util.OpenmrsUtil;


public class RollingDailyIndicatorDataSetDefinition extends BaseDataSetDefinition {
	
	
	
//***** PROPERTIES *****
	
	Map<String, Mapped<? extends Dimension>> dimensions = new HashMap<String, Mapped<? extends Dimension>>();
	List<RwandaReportsIndicatorAndDimensionColumn> columns = new ArrayList<RwandaReportsIndicatorAndDimensionColumn>();
	private String baseRollingQueryExtension = "";
	private RollingDailyPeriodIndicatorReportDefinition.RollingBaseReportQueryType rollingBaseReportQueryType = RollingBaseReportQueryType.NONE;
	
	
	public RollingDailyPeriodIndicatorReportDefinition.RollingBaseReportQueryType getRollingBaseReportQueryType() {
		return rollingBaseReportQueryType;
	}


	public void setRollingBaseReportQueryType(
			RollingDailyPeriodIndicatorReportDefinition.RollingBaseReportQueryType rollingBaseReportQueryType) {
		this.rollingBaseReportQueryType = rollingBaseReportQueryType;
	}


	public String getBaseRollingQueryExtension() {
		return baseRollingQueryExtension;
	}


	public void setBaseRollingQueryExtension(String baseRollingQueryExtension) {
		this.baseRollingQueryExtension = baseRollingQueryExtension;
	}
	
	
	//***** CONSTRUCTORS *****
	


	public RollingDailyIndicatorDataSetDefinition() {
		super();
	}

	//***** PROPERTY ACCESS AND INSTANCE METHODS
	
    /**
	 * @return the dimensions
	 */
	public Map<String, Mapped<? extends Dimension>> getDimensions() {
		if (dimensions == null) {
			dimensions = new LinkedHashMap<String, Mapped<? extends Dimension>>();
		}
		return dimensions;
	}
	
    /**
     * @param dimensions the dimensions to set
     */
    public void setDimensions(Map<String, Mapped<? extends Dimension>> dimensions) {
    	this.dimensions = dimensions;
    }
    
	/**
	 * Adds a Mapped<Dimension> referenced by the given key
	 */
	public void addDimension(String dimensionKey, Mapped<? extends Dimension> dimension) {
		getDimensions().put(dimensionKey, dimension);
	}
	
	/**
	 * Adds a Dimension referenced by the given key, dimension, and parameter mappings
	 */
	public void addDimension(String dimensionKey, Dimension dimension, Map<String, Object> parameterMappings) {
		addDimension(dimensionKey, new Mapped<Dimension>(dimension, parameterMappings));
	}
	

	public Mapped<? extends Dimension> getDimension(String key) {
	    return getDimensions().get(key);
    }
	
	/**
	 * Removes a Dimension with the given key
	 */
	public void removeDimension(String dimensionKey) {
		List<RwandaReportsIndicatorAndDimensionColumn> listToRemove = new ArrayList<RwandaReportsIndicatorAndDimensionColumn>();
		for(RwandaReportsIndicatorAndDimensionColumn c : getColumns()) {
			Map<String, String> dimOpts = c.getDimensionOptions();
			if (dimOpts.keySet().contains(dimensionKey)) {
				listToRemove.add(c);
			}
		}
		getColumns().removeAll(listToRemove);
		getDimensions().remove(dimensionKey);
	}

    /**
	 * @return the columns
	 */
	public List<RwandaReportsIndicatorAndDimensionColumn> getColumns() {
		if (columns == null) {
			columns = new ArrayList<RwandaReportsIndicatorAndDimensionColumn>();
		}
		return columns;
	}

	/**
     * @param columns the columns to set
     */
    public void setColumns(List<RwandaReportsIndicatorAndDimensionColumn> columns) {
    	this.columns = columns;
    }
    
    /**
     * Adds a Column 
     */
	public void addColumn(RwandaReportsIndicatorAndDimensionColumn column) {
		getColumns().add(column);
	}
    
    /**
     * Adds a Column with the given properties
     */
	public void addColumn(String name, String label, Mapped<? extends Indicator> indicator, Map<String, String> dimensionOptions) {
		getColumns().add(new RwandaReportsIndicatorAndDimensionColumn(name, label, indicator, dimensionOptions));
	}
	
    /**
     * Removes a column with the given name
     */
	public void removeColumn(String columnName) {
		for (Iterator<RwandaReportsIndicatorAndDimensionColumn> i = getColumns().iterator(); i.hasNext(); ) {
			if (i.next().getName().equals(columnName)) {
				i.remove();
			}
		}
	}
	
	/**
	 * Adds a Column with the given properties
	 * @param dimensionOptions something like gender=male|age=adult, where gender and age are keys into 'dimensions'
	 */
	public void addColumn(String name, String label, Mapped<? extends Indicator> indicator, String dimensionOptions) {
		addColumn(name, label, indicator, OpenmrsUtil.parseParameterList(dimensionOptions));
	}
    
    //***** INNER CLASSES *****

    /**
     * Column Definition which encapsulates information about the indicator and dimensions chosen for each column
     */
	public class RwandaReportsIndicatorAndDimensionColumn extends DataSetColumn implements Cloneable {

        private static final long serialVersionUID = 1L;
        
        //***** PROPERTIES *****
        
		private Mapped<? extends Indicator> indicator;
		private Map<String, String> dimensionOptions;
		
		//***** CONSTRUCTORS *****
		
		public RwandaReportsIndicatorAndDimensionColumn() {}
		
		public RwandaReportsIndicatorAndDimensionColumn(String name, String label, Mapped<? extends Indicator> indicator, Map<String, String> dimensionOptions) {
			super(name, label, Object.class);
			this.indicator = indicator;
			this.dimensionOptions = dimensionOptions;
		}
		
        /**
		 * @see java.lang.Object#clone()
		 */
		@Override
		public Object clone() throws CloneNotSupportedException {
			RwandaReportsIndicatorAndDimensionColumn c = new RwandaReportsIndicatorAndDimensionColumn();
			c.setName(this.getName());
			c.setLabel(this.getLabel());
			c.setDataType(this.getDataType());
			c.setIndicator(this.getIndicator());
			c.setDimensionOptions(this.getDimensionOptions());
			return c;
		}
		
		//***** PROPERTY ACCESS *****

		/**
         * @return the indicator
         */
        public Mapped<? extends Indicator> getIndicator() {
        	return indicator;
        }  
		
        /**
		 * @param indicator the indicator to set
		 */
		public void setIndicator(Mapped<? extends Indicator> indicator) {
			this.indicator = indicator;
		}

		/**
         * @return the dimensionOptions
         */
        public Map<String, String> getDimensionOptions() {
        	return dimensionOptions;
        }

		/**
		 * @param dimensionOptions the dimensionOptions to set
		 */
		public void setDimensionOptions(Map<String, String> dimensionOptions) {
			this.dimensionOptions = dimensionOptions;
		}
	}
	
	
}
