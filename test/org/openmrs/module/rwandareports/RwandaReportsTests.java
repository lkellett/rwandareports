package org.openmrs.module.rwandareports;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.OpenmrsObject;
import org.openmrs.Person;
import org.openmrs.PersonAttribute;
import org.openmrs.api.context.Context;
import org.openmrs.module.reportingobjectgroup.objectgroup.ObjectGroup;
import org.openmrs.module.reportingobjectgroup.objectgroup.query.service.ObjectGroupQueryService;
import org.openmrs.module.reportingobjectgroup.objectgroup.service.ObjectGroupDefinitionService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.util.Assert;


public class RwandaReportsTests extends BaseModuleContextSensitiveTest  {
	
	PersonAttribute pa = new PersonAttribute();
	
	@Override
    public Boolean useInMemoryDatabase(){
        return true;
    }
	

	@Test
    public void should_loadServices() {
		
		ObjectGroupQueryService eqs = Context.getService(ObjectGroupQueryService.class);
		ObjectGroupDefinitionService eds = Context.getService(ObjectGroupDefinitionService.class);

		Assert.notNull(eqs);
		Assert.notNull(eds);
       
    }
	
	/**
	 * runs a simple query returning encounter_id, patient_id, checks for instantiation of ObjectGroup of length > 0
	 */
	@Test
    public void should_testObjectGroupDefinitionService() {
		
		ObjectGroupQueryService eqs = Context.getService(ObjectGroupQueryService.class);
		String query = "select encounter_id, patient_id from encounter where location_id = :location";
		Map<String, Object> params = new HashMap<String, Object>();
		Location loc = Context.getLocationService().getLocation(1);
		Assert.notNull(loc);
		params.put("location", (Object) loc);
		ObjectGroup eg = eqs.executeSqlQuery(query, params);
		//System.out.println(eg.size());
		Assert.notNull(eg);
		Assert.isTrue(eg.size() > 1);
	 
    }
	
	
	//routine that I (dave) used to test using reflection to remove an item from its parent collection (PersonAttribute in this case)
	@Test
	public void should_testGenericsReflection(){
		
		//used to setup the test:
		Boolean replaced = false;	
		this.pa.setUuid(UUID.randomUUID().toString());
		this.pa.setVoided(false);
		Person person = new Person();
		person.addAttribute(pa);
		String uuid = pa.getUuid();
		
		
		//TODO: GENERALIZE:  replace 'this' with any instantiated OpenmrsObject:
		//If this was the sync PersonAttribute problem, we'd just have to pass in a hydrated SyncItem
		Object item = this.pa;
		Field[] f = item.getClass().getDeclaredFields(); //get the class fields for this class
		for (int k = 0; k < f.length; k++){
			Type fieldType = f[k].getGenericType();	//get the field type of the one field in this test class
			
			Method getter = getGetterMethod(item.getClass(), f[k].getName());
			OpenmrsObject oo = null;
			
			if (getter == null){
				//System.out.println("No getter found for " + f[k].getName());
				continue;
			}	
			
			try {
				oo = (OpenmrsObject) getter.invoke(item, null);  //HERE's where you pass in the real child class for 'this'
			} catch (Exception ex){
				ex.printStackTrace(System.out);
			}
			if (oo != null){

				Method[] methods =  getter.getReturnType().getDeclaredMethods();
				for (Method method : methods){
					Type type = method.getGenericReturnType();
					//return is a parameterizable and there are 0 arguments to method and the return is a Collection
					if (ParameterizedType.class.isAssignableFrom(type.getClass()) 
							&& method.getGenericParameterTypes().length == 0 
							&& method.getName().contains("get")) {
						ParameterizedType pt = (ParameterizedType) type;
						for (int i = 0 ; i < pt.getActualTypeArguments().length ; i++){
							Type t = pt.getActualTypeArguments()[i];
							// if the return type matches, and the return is not a Map
							if (item.getClass().equals(t)
									&& !pt.getRawType().toString().equals(java.util.Map.class.toString()) 
									&& java.util.Collection.class.isAssignableFrom((Class) pt.getRawType())){
								try {
									Object colObj =  (Object) method.invoke(oo, null);
									if (colObj != null){
										java.util.Collection collection = (java.util.Collection) colObj;
										Iterator it = collection.iterator();
										Collection replacementCollection;
										if (method.getReturnType().getCanonicalName().equals("java.util.List"))
											replacementCollection = new ArrayList();
										else
											replacementCollection = new HashSet();
										while (it.hasNext()){
											OpenmrsObject omrsobj = (OpenmrsObject) it.next();
											//now add to new collection:
											//TODO: GENERALIZE -- to delete 'item', you could compare to the UUID of 'item'
											if (omrsobj.getUuid() != null && !omrsobj.getUuid().equals("blah")){
												replacementCollection.add(omrsobj);
											}
										}
		
										try {	
												for (Method mInner : getter.getReturnType().getMethods()){
													if (mInner.getName().equals(StringUtils.replace(method.getName(), "get", "set", 1))){
														mInner.invoke(oo, replacementCollection);
														replaced = true;
													}
												}										
										} catch (Exception ex){
											ex.printStackTrace(System.out);
										}
									}
								} catch (Exception ex){
									ex.printStackTrace(System.out);
								}
							}
						}
					}
				}
			}
		}
		Assert.isTrue(replaced);
		Assert.isTrue(person.getActiveAttributes().get(0).getUuid().equals(uuid));
	}
	
	
//--------------------------------------------------------------------------------------------------------------------------------------------------------------------	
	
	
	//borrowed from sync
	public static String propCase(String text) {
		if ( text != null ) {
			return text.substring(0, 1).toUpperCase() + text.substring(1);
		} else {
			return null;
		}
	}
	
	//borrowd from sync
    public static Method getGetterMethod(Class objType, String propName) {
        String methodName = "get" + propCase(propName);
        return getPropertyAccessor(objType, methodName, null);
    }
	
	//borrowed from sync module:
	 private static Method getPropertyAccessor(Class objType, String methodName, Class propValType) {
			// need to try to get setter, both in this object, and its parent class 
			Method m = null;
	        boolean continueLoop = true;
	        
	        // Fix - CA - 22 Jan 2008 - extremely odd Java Bean convention that says getter/setter for fields
	        // where 2nd letter is capitalized (like "aIsToB") first letter stays lower in getter/setter methods
	        // like "getaIsToB()".  Hence we need to try that out too
	        String altMethodName = methodName.substring(0, 3) + methodName.substring(3, 4).toLowerCase() + methodName.substring(4);

	        try {
				Class[] setterParamClasses = null;
	            if (propValType != null) { //it is a setter
	                setterParamClasses = new Class[1];
	                setterParamClasses[0] = propValType;
	            }
				Class clazz = objType;
	    
	            // it could be that the setter method itself is in a superclass of objectClass/clazz, so loop through those
				while ( continueLoop && m == null && clazz != null && !clazz.equals(Object.class) ) {
					try {
						m = clazz.getMethod(methodName, setterParamClasses);
						continueLoop = false;
						break; //yahoo - we got it using exact type match
					} catch (SecurityException e) {
						m = null;
					} catch (NoSuchMethodException e) {
						m = null;
					}
					
					//not so lucky: try to find method by name, and then compare params for compatibility 
					//instead of looking for the exact method sig match 
	                Method[] mes = objType.getMethods();
	                for (Method me : mes) {
	                	if (me.getName().equals(methodName) || me.getName().equals(altMethodName) ) {
	                		Class[] meParamTypes = me.getParameterTypes();
	                		if (propValType != null && meParamTypes != null && meParamTypes.length == 1 && meParamTypes[0].isAssignableFrom(propValType)) {
	                			m = me;
	            				continueLoop = false; //aha! found it
	            				break;
	                		}
	                	}
	                }
	                
	                if ( continueLoop ) clazz = clazz.getSuperclass();
	    		}
	        }
	        catch(Exception ex) {
	            //whatever happened, we didn't find the method - return null
	            m = null;
	        }
	        
					
			return m;
		}


	public PersonAttribute getPa() {
		return pa;
	}


	public void setPa(PersonAttribute pa) {
		this.pa = pa;
	}
	 
	 
	
}
