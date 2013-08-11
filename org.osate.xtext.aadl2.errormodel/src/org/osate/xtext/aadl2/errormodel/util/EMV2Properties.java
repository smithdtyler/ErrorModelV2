package org.osate.xtext.aadl2.errormodel.util;

import java.util.List;
import java.util.Stack;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.osate.aadl2.BasicPropertyAssociation;
import org.osate.aadl2.ComponentCategory;
import org.osate.aadl2.ContainedNamedElement;
import org.osate.aadl2.Element;
import org.osate.aadl2.EnumerationLiteral;
import org.osate.aadl2.ModalPropertyValue;
import org.osate.aadl2.NamedElement;
import org.osate.aadl2.NamedValue;
import org.osate.aadl2.Property;
import org.osate.aadl2.PropertyAssociation;
import org.osate.aadl2.PropertyExpression;
import org.osate.aadl2.RealLiteral;
import org.osate.aadl2.RecordValue;
import org.osate.aadl2.instance.ComponentInstance;
import org.osate.aadl2.modelsupport.util.AadlUtil;
import org.osate.xtext.aadl2.errormodel.errorModel.ErrorBehaviorStateMachine;
import org.osate.xtext.aadl2.errormodel.errorModel.ErrorModelLibrary;
import org.osate.xtext.aadl2.errormodel.errorModel.ErrorModelSubclause;
import org.osate.xtext.aadl2.errormodel.errorModel.TypeSet;
import org.osate.xtext.aadl2.properties.util.InstanceModelUtil;

public class EMV2Properties {

	public static final String INVALID_OCCURRENCE_TYPE = "unknown_distribution";

	public static EList<ContainedNamedElement> getHazardProperty(ComponentInstance ci, Element target, TypeSet ts){
		EList<ContainedNamedElement> result =  getProperty("EMV2::hazard",ci,target,ts);
		return result;
	}

	/**
	 * Retrieve the value of the OccurenceDistribution property of the
	 * EMV2 property. You can use it like this:
	 * 	ContainedNamedElement PA = EM2Util.getOccurenceDistributionProperty(instance,null,ee,null);
	 * res = EM2Util.getOccurenceValue (PA);
	 * 
	 * @see		Util
	 * @param 	ci				ComponentInstance object that contains the property
	 * @param localContext		the context or null
	 * @param target			the property
	 * @param ts				corresponding typeset or null
	 * @return
	 */
	public static EList<ContainedNamedElement> getOccurenceDistributionProperty(ComponentInstance ci, NamedElement target, TypeSet ts){
		EList<ContainedNamedElement> result =  getProperty("EMV2::OccurrenceDistribution",ci,target,ts);
		
		if (result.size() == 0)
		{
			if ((ci.getComponentClassifier().getCategory() == ComponentCategory.PROCESS)||
					(ci.getComponentClassifier().getCategory() == ComponentCategory.ABSTRACT) ||
					(ci.getComponentClassifier().getCategory() == ComponentCategory.VIRTUAL_PROCESSOR) ||
					(ci.getComponentClassifier().getCategory() == ComponentCategory.SYSTEM))
			{
				List<ComponentInstance> cpus = InstanceModelUtil.getProcessorBinding(ci);
				ComponentInstance cpu = cpus.isEmpty() ? null : cpus.get(0);
				if (cpu != null)
				{
					return getOccurenceDistributionProperty(cpu,  target, ts);
				}
			}
			
		}
		return result;
	}

	/**
	 * Retrieve the type/kind of distribution associated
	 * with the Occurrence property
	 * See PRISM converter to see how it is used.
	 *
	 * @param PAContainmentPath string value describing the distribution get from getOccurenceDistributionProperty
	 */
	public static String getOccurenceType (final ContainedNamedElement PAContainmentPath)
	{
		if (PAContainmentPath == null)
		{
			return "unknown_distribution";
		}
		
		for (ModalPropertyValue modalPropertyValue : AadlUtil.getContainingPropertyAssociation(PAContainmentPath).getOwnedValues()) {
			PropertyExpression val = modalPropertyValue.getOwnedValue();
			if (val instanceof RecordValue){
	
				RecordValue rv = (RecordValue)val;
				EList<BasicPropertyAssociation> fields = rv.getOwnedFieldValues();
				// for all error types/aliases in type set or the element identified in the containment clause 
				for (BasicPropertyAssociation bpa : fields)
				{
					if (bpa.getProperty().getName().equalsIgnoreCase("distribution"))
					{
						if (bpa.getValue() instanceof NamedValue)
						{
							EnumerationLiteral el = (EnumerationLiteral)((NamedValue)bpa.getValue()).getNamedValue();
							return (el.getName().toLowerCase());
							
							//RealLiteral rl = (NamedValue)bpa.getValue();
							//result = rl.getScaledValue();
						}
					}
				}
			}
		}
		return "unknown_distribution";
	}

	/**
	 * Retrieve the value associated with a containment path
	 * See RDB action to see how it is used.
	 *
	 * @param PAContainmentPath value get from getOccurenceDistributionProperty
	 */
	public static double getOccurenceValue (final ContainedNamedElement PAContainmentPath)
	{
		double result;
		
		result = 0;
		
		if (PAContainmentPath == null)
		{
			return 0;
		}
		
		for (ModalPropertyValue modalPropertyValue : AadlUtil.getContainingPropertyAssociation(PAContainmentPath).getOwnedValues()) {
			PropertyExpression val = modalPropertyValue.getOwnedValue();
			if (val instanceof RecordValue){
	
				RecordValue rv = (RecordValue)val;
				EList<BasicPropertyAssociation> fields = rv.getOwnedFieldValues();
				// for all error types/aliases in type set or the element identified in the containment clause 
				for (BasicPropertyAssociation bpa : fields)
				{
					if (bpa.getProperty().getName().equalsIgnoreCase("probabilityvalue"))
					{
						if (bpa.getValue() instanceof RealLiteral)
						{
							RealLiteral rl = (RealLiteral)bpa.getValue();
							result = rl.getScaledValue();
						}
					}
				}
			}
		}
		return result;
	}

	/**
	 * get enclosing object within the error annex that has a properties section..
	 * ErrorModelLibrary, ErrorBehaviorStateMachine, and ErrorModelSubclause have properties sections
	 * @param element declarative model element or error annex element
	 * @return ErrorModelLibrary, ErrorBehaviorStateMachine, ErrorModelSubclause
	 */
	public static EList<PropertyAssociation> getPropertiesInContext(Element element) {
		EObject container = element;
		while (container != null ){
			if (container instanceof ErrorModelSubclause ){
				return ((ErrorModelSubclause)container).getProperties();
			}
			if (container instanceof ErrorModelLibrary ){
				return ((ErrorModelLibrary)container).getProperties();
			}
			if (container instanceof ErrorBehaviorStateMachine ){
				return ((ErrorBehaviorStateMachine)container).getProperties();
			}
			container = container.eContainer();
		}
		return null;
	}

	/** return list of property associations that meet the property name
	 * 
	 * @param props property association list from the properties section
	 * @param propertyName name of property
	 * @return EList<PropertyAssociation>
	 */
	public static List<PropertyAssociation> getProperty(EList<PropertyAssociation> props,String propertyName){
		List<PropertyAssociation> result = new BasicEList<PropertyAssociation>();
		for (PropertyAssociation propertyAssociation : props) {
			Property prop = propertyAssociation.getProperty();
			String name = prop.getQualifiedName();
			if (propertyName.equalsIgnoreCase(name)){
				result.add(propertyAssociation);
			}
		}
		return result;
	}

	/**
	 * retrieve an error model property (such as Hazard) attached to an error model element.
	 * @param props list of property associations from the properties section in the error model of ci
	 * @param propertyName name of property we are looking for
	 * @param target the error model element
	 * @param ciStack stack of nested CI below the ci of the props; those names may show up in the path
	 * @return list of paths
	 */
	public static EList<ContainedNamedElement> getProperty(EList<PropertyAssociation> props,String propertyName, Element target,
			 Stack<ComponentInstance> ciStack, TypeSet ts){
		if (props.isEmpty()  ) return new BasicEList<ContainedNamedElement>();
		EList<ContainedNamedElement> result = new BasicEList<ContainedNamedElement>();
		for (PropertyAssociation propertyAssociation : props) {
			Property prop = propertyAssociation.getProperty();
			String name = prop.getQualifiedName();
			if (propertyName.equalsIgnoreCase(name)){
				ContainedNamedElement res = EMV2Util.isErrorModelElementProperty(propertyAssociation, target,ciStack,ts);
				if (res!=null)
				result.add(res);
			}
		}
		return result;
	}

	/**
	 * retrieve an error model property (such as Hazard) attached to an error model element.
	 * @param props list of property associations from the properties section in the error model
	 * @param propertyName name of property we are looking for
	 * @param target the error model element
	 * @param ciStack stack of nested CI
	 * @return property association
	 */
	public static EList<ContainedNamedElement> getProperty(EList<PropertyAssociation> props,String propertyName, Element target,
			 TypeSet ts){
		if (props == null) return null;
		EList<ContainedNamedElement> result = new BasicEList<ContainedNamedElement>();
		for (PropertyAssociation propertyAssociation : props) {
			Property prop = propertyAssociation.getProperty();
			String name = prop.getQualifiedName();
			if (propertyName.equalsIgnoreCase(name)){
				ContainedNamedElement res = EMV2Util.isErrorModelElementProperty(propertyAssociation, target,null,ts);
				if (res!=null)
				result.add(res);
			}
		}
		return result;
	}

	/**
	 * find property by first looking for it down the component instance hierarchy to ci
	 * Then try to find it in the local context if not null. The context is the context object of the referenced target.
	 * For example: the transition for a state reference.
	 * Finally, we look for the property at the definition site of the referenced object.
	 * NOTE: the target element may actually be contained in a different context from the context in which we are interested in its property.
	 * For example, we are interested in the occurrence distribution for an error state. It actually is contained in an error behavior state machine.
	 * We are interested in its property value in the context of of the reference to the state, e.g., an error source.
	 * In some cases we are interested in the state as it is associated with the component via the use behavior clause.
	 * In that case there is no local context for the state reference.
	 * @param propertyName String
	 * @param ci ComponentInstance the component with the error model element, whose property we are retrieving
	 * @param target Element the target object in the error model whose property we retrieve (the element may carry an error type)
	 * @param ts Type Set null or any error type in the type set as part of the target error model element
	 * @return
	 */
	public static EList<ContainedNamedElement> getProperty(String propertyName, ComponentInstance ci,Element target, TypeSet ts){
		EList<ContainedNamedElement> result = getPropertyInInstanceHierarchy(propertyName,ci,target, ts);
		if (result.isEmpty()){
			// look up in context of target definition
			// for example: for a state reference the properties section of the EBSM that defines the state
			EList<PropertyAssociation> props = getPropertiesInContext(target);
			if (props != null) {
				result = getProperty(props, propertyName, target,ts);
			}
		}
		return result;
	}

	/**
	 * recurse up the component hierarchy to look for the PA from the outside in.
	 * @param propertyName
	 * @param ci the component instance whose subclause property section we are looking for the proeprty
	 * @param target
	 * @param localContext
	 * @param ciStack stack of CIS that are down the hierarchy towards the target emv2 subclause
	 * @param ts
	 * @return
	 */
	private static EList<ContainedNamedElement> getPropertyInInstanceHierarchy(String propertyName, ComponentInstance ci,Element target, 
			Stack<ComponentInstance> ciStack, TypeSet ts){
		if (ci != null ) {
			if (ci.getContainingComponentInstance() != null){
				ciStack.push(ci);
				EList<ContainedNamedElement> result = getPropertyInInstanceHierarchy(propertyName, ci.getContainingComponentInstance(), target,ciStack,ts);
				ciStack.pop();
				return result;
			} else {
				EList<ErrorModelSubclause> emslist = EMV2Util.getAllContainingClassifierEMV2Subclauses(ci);
				for (ErrorModelSubclause ems : emslist) {
					EList<PropertyAssociation> props = ems.getProperties();
					EList<ContainedNamedElement>result = getProperty(props, propertyName, target, ciStack,ts);
					if (!result.isEmpty()){
						return result;
					}
				}
			}
		}
		return new BasicEList<ContainedNamedElement>();
	}

	/**
	 * retrieve an error model property (such as Hazard) attached to an error model element based on contained property associations
	 * in the annex subclause properties section.
	 * Here we come down the component instance hierarchy to find the outmost property association
	 * in the properties section of the annex subclauses. Those are the ones that can override down the component hierarchy.
	 * @param propertyName name of property we are looking for
	 * @param ci component instance whose EM element has the property
	 * @param target the error model element (which is optionally followed by a type that is contained in the typeset ts
	 * @param ts the type set
	 * @return Containmentpath of the PA that matches the parameters.
	 * we return the path because the PA applies to more than element
	 */
	public static EList<ContainedNamedElement> getPropertyInInstanceHierarchy(String propertyName, ComponentInstance ci,Element target, 
			TypeSet ts){
		Stack<ComponentInstance> ciStack = new Stack<ComponentInstance>();
		return getPropertyInInstanceHierarchy(propertyName,ci,target,ciStack, ts);
	}
	

	public static EList<ContainedNamedElement> getSeverityProperty(ComponentInstance ci, Element target, TypeSet ts){
		EList<ContainedNamedElement> result = EMV2Properties.getProperty("MILSTD882::Severity",ci,target,ts);
		if (result==null)result = EMV2Properties.getProperty("ARP4761::Severity",ci,target,ts);
		if (result==null)result = EMV2Properties.getProperty("EMV2::Severity",ci,target,ts);
		return result;
	}
	
	public static EList<ContainedNamedElement> getLikelihoodProperty(ComponentInstance ci, Element target, TypeSet ts){
		EList<ContainedNamedElement> result = EMV2Properties.getProperty("MILSTD882::Likelihood",ci,target,ts);
		if (result==null)result = EMV2Properties.getProperty("ARP4761::Likelihood",ci,target,ts);
		if (result==null)result = EMV2Properties.getProperty("EMV2::Likelihood",ci,target,ts);
		return result;
	}
	


}