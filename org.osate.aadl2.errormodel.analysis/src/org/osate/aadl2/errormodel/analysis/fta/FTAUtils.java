package org.osate.aadl2.errormodel.analysis.fta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.osate.aadl2.BasicPropertyAssociation;
import org.osate.aadl2.Classifier;
import org.osate.aadl2.ContainedNamedElement;
import org.osate.aadl2.Feature;
import org.osate.aadl2.ListValue;
import org.osate.aadl2.ModalPropertyValue;
import org.osate.aadl2.NamedElement;
import org.osate.aadl2.PropertyExpression;
import org.osate.aadl2.RecordValue;
import org.osate.aadl2.StringLiteral;
import org.osate.aadl2.Subcomponent;
import org.osate.aadl2.instance.ComponentInstance;
import org.osate.aadl2.modelsupport.util.AadlUtil;
import org.osate.aadl2.util.OsateDebug;
import org.osate.xtext.aadl2.errormodel.errorModel.CompositeState;
import org.osate.xtext.aadl2.errormodel.errorModel.ConditionElement;
import org.osate.xtext.aadl2.errormodel.errorModel.ConditionExpression;
import org.osate.xtext.aadl2.errormodel.errorModel.ErrorBehaviorState;
import org.osate.xtext.aadl2.errormodel.errorModel.ErrorBehaviorTransition;
import org.osate.xtext.aadl2.errormodel.errorModel.ErrorEvent;
import org.osate.xtext.aadl2.errormodel.errorModel.ErrorModelSubclause;
import org.osate.xtext.aadl2.errormodel.errorModel.ErrorPropagation;
import org.osate.xtext.aadl2.errormodel.errorModel.EventOrPropagation;
import org.osate.xtext.aadl2.errormodel.errorModel.FeatureorPPReference;
import org.osate.xtext.aadl2.errormodel.errorModel.OutgoingPropagationCondition;
import org.osate.xtext.aadl2.errormodel.errorModel.SAndExpression;
import org.osate.xtext.aadl2.errormodel.errorModel.SOrExpression;
import org.osate.xtext.aadl2.errormodel.errorModel.SubcomponentElement;
import org.osate.xtext.aadl2.errormodel.errorModel.TypeSet;
import org.osate.xtext.aadl2.errormodel.errorModel.TypeToken;
import org.osate.xtext.aadl2.errormodel.util.AnalysisModel;
import org.osate.xtext.aadl2.errormodel.util.EMV2Properties;
import org.osate.xtext.aadl2.errormodel.util.EMV2Util;
import org.osate.xtext.aadl2.errormodel.util.PropagationPathEnd;
import org.osate.xtext.aadl2.properties.util.GetProperties;

public class FTAUtils 
{
	private static List<String>        currentHandledStates;
	private static AnalysisModel       currentAnalysisModel;
	
	
	public static void init (ComponentInstance root)
	{
		currentHandledStates = new ArrayList<String>();
		currentAnalysisModel = new AnalysisModel(root);
	}
	
	private static ComponentInstance findInstance (EList<ComponentInstance> instances, String name)
	{
		for (ComponentInstance ci : instances)
		{
			if (ci.getName().equalsIgnoreCase(name))
			{
				return ci;
			}
		}
//		OsateDebug.osateDebug("[FTAUtils] Did not find the instance " + name);
		return null;
	}
	
	
	public static String getStateHash (ComponentInstance ci,ErrorBehaviorState ebs )
	{
		return ("##" + ci.getName() + ebs.getName()+ "##");
	}
	
	
	private static List<String> getInvolvedSubcomponents (final ConditionExpression cond,  final EList<ComponentInstance> componentInstances)
	{
		List<String> result = new ArrayList<String>();
		if (cond instanceof SOrExpression)
		{
			SOrExpression sor = (SOrExpression) cond;
			
			for (ConditionExpression conditionExpression : sor.getOperands())
			{
				if (conditionExpression instanceof ConditionElement)
				{
					ConditionElement conditionElement;
					
					conditionElement = (ConditionElement) conditionExpression;
					for (SubcomponentElement subcomponentElement : conditionElement.getSubcomponents())
					{
						Subcomponent subcomponent = subcomponentElement.getSubcomponent();
						//OsateDebug.osateDebug("cond = " + cond + "      subcomponent=" + subcomponent);
						ComponentInstance relatedInstance = findInstance(componentInstances, subcomponent.getName());
						if (relatedInstance != null)
						{
							result.add (relatedInstance.getName());
						}
					}
				}
			}
		}
		if (cond instanceof SAndExpression)
		{
			SAndExpression sae = (SAndExpression) cond;
	
			for (ConditionExpression conditionExpression : sae.getOperands())
			{
				if (conditionExpression instanceof ConditionElement)
				{
					ConditionElement conditionElement;
					
					conditionElement = (ConditionElement) conditionExpression;
					for (SubcomponentElement subcomponentElement : conditionElement.getSubcomponents())
					{
						Subcomponent subcomponent = subcomponentElement.getSubcomponent();
						//OsateDebug.osateDebug("cond = " + cond + "      subcomponent=" + subcomponent);
						ComponentInstance relatedInstance = findInstance(componentInstances, subcomponent.getName());
						if (relatedInstance != null)
						{
							result.add (relatedInstance.getName());
						}
					}
				}
			}
		}
		return result;
	}
	
	public static void findPropagationSource (NamedElement feature, List<ComponentInstance> componentInstances)
	{
		for (ComponentInstance ci : componentInstances)
		{
//			for (Feature f : ci.getComponentClassifier().getAllFeatures())
//			{

				for (ErrorPropagation ep : EMV2Util.getAllOutgoingErrorPropagations(ci.getComponentClassifier()))
				{
						OsateDebug.osateDebug("debug on " + ci);
				}
		//	}
		}
	}
	
	public static String buildHash (ComponentInstance instance, ErrorBehaviorState ebs)
	{
		return "##" + instance + "##" + instance.getComponentClassifier().getName() + "##" + ebs.getName() + "##";
	}
	
	public static List<Event> findIncomingPropagations (ComponentInstance relatedInstance, ConditionElement conditionElement, EList<ComponentInstance> componentInstances)
	{
		List<Event> propagations;
		Classifier cl;
		
		propagations = new ArrayList<Event>();
		
		
		cl = relatedInstance.getComponentClassifier();
//		OsateDebug.osateDebug("[FTAUtils] findIncomingPropagations on: " + relatedInstance);
//		OsateDebug.osateDebug("relatedInstance" + relatedInstance);
//		OsateDebug.osateDebug("condition state" + conditionElement.getState());
//		OsateDebug.osateDebug("Classifier=" + cl);
		
		if (EMV2Util.hasComponentErrorBehaviorTransitions(relatedInstance))
		{
			Collection<ErrorBehaviorTransition> transitions = EMV2Util.getAllErrorBehaviorTransitions(cl);
			for (ErrorBehaviorTransition t : transitions)
			{
//				OsateDebug.osateDebug( " trans = " + t + "|" + t.getName());
//				OsateDebug.osateDebug("target="  + t.getTarget().getName());
//				OsateDebug.osateDebug("condition="  + t.getCondition());
				if (t.getCondition() instanceof ConditionElement)
				{
					ConditionElement ce = (ConditionElement) t.getCondition();
					if (ce.getIncoming() != null)
					{
						
						EventOrPropagation eop = ce.getIncoming();
						if (eop instanceof ErrorPropagation)
						{
							
							/*
							 * Set the propagation name.
							 */
							String newEventName = null;
							boolean handleSubcomponent = false;
							ErrorPropagation ep = (ErrorPropagation) ce.getIncoming();
							Event newEvent;
							

							//OsateDebug.osateDebug("relatedinstance=" + relatedInstance);
							//OsateDebug.osateDebug("ep=" + ep);
							for (PropagationPathEnd ppe : currentAnalysisModel.getAllPropagationSourceEnds(relatedInstance, ep))
							{
								//OsateDebug.osateDebug("ppe=" + ppe.getErrorPropagation().getFeatureorPPRefs());
								ComponentInstance ciSource = ppe.getComponentInstance();
								//OsateDebug.osateDebug("bla" + ciSource);
								for (OutgoingPropagationCondition opc : EMV2Util.getAllOutgoingPropagationConditions(ciSource))
								{
									//OsateDebug.osateDebug("opc outgoing" + opc.getOutgoing());
									//OsateDebug.osateDebug("ppe getprep " + ppe.getErrorPropagation());
									if (opc.getOutgoing() == ppe.getErrorPropagation())
									{
										ErrorBehaviorState ebs = opc.getState();
										
										newEventName = ciSource.getName() + "/" + ebs.getName();
										
										newEvent = new Event();
										newEvent.setName (newEventName);
										newEvent.setEventType(EventType.NORMAL);
										
										fillEventWithProperties (newEvent, relatedInstance, ebs);
										//OsateDebug.osateDebug("ep="  + ep);
										int nTransitions = 0;
										for (ErrorBehaviorTransition ebt : EMV2Util.getAllErrorBehaviorTransitions(ciSource))
										{
											if (ebt.getTarget() == ebs)
											{
												nTransitions++;
												Event subEvent = new Event();
												subEvent.setName ("sub");
												subEvent.setEventType(EventType.EVENT);
												handleCondition (subEvent,ebs,ciSource,ebt.getCondition(),componentInstances, true);

												newEvent.addSubEvent(subEvent);
											}
										}
										if (nTransitions > 1)
										{
											newEvent.setEventType(EventType.OR);
										}
										propagations.add(newEvent);
										
									}
								}
							}
							
							if (handleSubcomponent == false)
							{
								/*
								 * First, we try to put the type name as the propagation
								 * name.
								 */
								TypeSet ts = ce.getConstraint();
								if (ts != null)
								{
									for (TypeToken tt : ts.getTypeTokens())
									{
										//OsateDebug.osateDebug("tt="  + tt.getType().get(0).getName());
										
										newEventName = tt.getType().get(0).getName();
										newEvent = new Event();
										if (ep.getFeatureorPPRefs().size() > 0)
										{
											FeatureorPPReference fppr = ep.getFeatureorPPRefs().get(0);
											Feature f = (Feature) fppr.getFeatureorPP();
											if (f != null)
											{
												newEventName += " on " + f.getName();
											}
										}
										else
										{
											
										}
										
										OsateDebug.osateDebug("EVENT NAME1" + newEventName);
										newEvent.setName (newEventName);
										newEvent.setEventType(EventType.EVENT);
										propagations.add(newEvent);
										fillEventWithProperties (newEvent, relatedInstance, ep,ts);
//										OsateDebug.osateDebug("[FTAUtils] findIncomingPropagations ep="  + ep);
									}
								}
								
								/*
								 * Then, if we fail to get the type name, we retrieve
								 * the associated feature name.
								 */
								List<FeatureorPPReference> features = ep.getFeatureorPPRefs();
								for (FeatureorPPReference fe : features)
								{
									//OsateDebug.osateDebug("fe="  + fe.getFeatureorPP());
									if (newEventName == null)
									{
										newEventName = fe.getFeatureorPP().getName();
	
										newEvent = new Event();
										newEvent.setName (newEventName);
										newEvent.setEventType(EventType.EVENT);
										OsateDebug.osateDebug("EVENT NAME2" + newEventName);
										fillEventWithProperties (newEvent, relatedInstance, fe.getFeatureorPP());
										propagations.add(newEvent);
										//OsateDebug.osateDebug("ep="  + ep);
									}
								}
							}

						}
						
						if (eop instanceof ErrorEvent)
						{
							
							ErrorEvent ev = (ErrorEvent) ce.getIncoming();
							//OsateDebug.osateDebug("ev="  + ev);
							Event newEvent;
							/*
							 * Set the propagation name.
							 */
							String newEventName = null;
							
							/*
							 * First, we try to put the type name as the propagation
							 * name.
							 */
							TypeSet ts = ev.getTypeSet();
							if ((ts != null) && (ts.getTypeTokens() != null))
							{
								for (TypeToken tt : ts.getTypeTokens())
								{
									//OsateDebug.osateDebug("tt="  + tt.getType().get(0).getName());
									
									newEventName = tt.getType().get(0).getName();
									//OsateDebug.osateDebug("eventname="  + newEventName);

									if (newEventName != null)
									{
										newEvent = new Event ();
										newEvent.setName (newEventName);
										newEvent.setEventType(EventType.EVENT);
										OsateDebug.osateDebug("EVENT NAME3" + newEventName);
										propagations.add(newEvent);
										fillEventWithProperties (newEvent, relatedInstance, eop);
									}
								}
							}
							

							
							/*
							 * Then, if we fail to get the type name, we retrieve
							 * the associated event name.
							 */

							if (newEventName == null)
							{
								newEvent = new Event ();
								newEventName = ev.getName();
								newEvent.setName (newEventName);
								newEvent.setEventType(EventType.EVENT);
								OsateDebug.osateDebug("EVENT NAME4" + newEventName);
								propagations.add(newEvent);
								fillEventWithProperties (newEvent, relatedInstance, ev);
								
							}
							

						}

					}
				}
			}
		}
		return propagations;
	}

	
	public static void handleCondition (Event event, 
			                            final ErrorBehaviorState resultingBehaviorState, 
			                            final ComponentInstance relatedComponentInstance,
			                            final ConditionExpression cond, 
			                            final EList<ComponentInstance> componentInstances,
			                            final boolean exploreRelativeCompositeStates)
	{
		
		//OsateDebug.osateDebug("[FTAUtils] handleCondition on " + relatedComponentInstance.getName() +"/" + resultingBehaviorState.getName());
		if (cond instanceof ConditionElement)
		{
			ConditionElement conditionElement;
			
			conditionElement = (ConditionElement) cond;

			ErrorBehaviorState behaviorState = conditionElement.getState();

			/**
			 * First, we see if the condition is bound to any other subcomponent
			 * So, if it comes from an incoming propagation
			 */
			if (conditionElement.getSubcomponents().size() == 0)
			{
				if (conditionElement.getIncoming() instanceof ErrorPropagation)
				{
					List<Event> toAdd = new ArrayList<Event>();
					Event tmpEvent;
					
					ErrorPropagation ep = (ErrorPropagation) conditionElement.getIncoming();
					
					if (ep.getFeatureorPPRefs().size() == 0)
					{
						EMV2Util.getPrintName(ep.getTypeSet());
						event.setEventType(EventType.EVENT);
						event.setName ("processor" + EMV2Util.getPrintName(ep.getTypeSet()));
						event.setDescription("processor binding error, receive error type " + EMV2Util.getPrintName(conditionElement.getConstraint()));
					}
					else
					{
						for (FeatureorPPReference fppr : ep.getFeatureorPPRefs())
						{
								//OsateDebug.osateDebug("BLA" + fppr.getFeatureorPP());
							tmpEvent = new Event();
							tmpEvent.setEventType(EventType.EVENT);
							tmpEvent.setName(fppr.getFeatureorPP().getName());
							OsateDebug.osateDebug("EVENT NAME5" + fppr.getFeatureorPP().getName());
							fillEventWithProperties (tmpEvent, relatedComponentInstance, ep);
	
							toAdd.add(tmpEvent);
						}
					}
					
					if (exploreRelativeCompositeStates)
					{
						for (CompositeState cs : EMV2Util.getAllCompositeStates(relatedComponentInstance))
						{
							if (cs.getState() == resultingBehaviorState)
							{
								tmpEvent = new Event();
	
								handleCondition (tmpEvent, resultingBehaviorState, relatedComponentInstance, cs.getCondition(), componentInstances, true);
								toAdd.add(tmpEvent);
								
								//OsateDebug.osateDebug("BLA2=" + cs.getState());
	
							}
						}
					}
					
					if (toAdd.size() > 1)
					{
						event.setEventType(EventType.OR);
						for (Event e : toAdd)
						{
							event.addSubEvent(e);
						}
					}
					if (toAdd.size() == 1)
					{
						event.setName(toAdd.get(0).getName());
						event.setEventType(toAdd.get(0).getEventType());
					}
						

				}
				if (conditionElement.getIncoming() instanceof ErrorEvent)
				{
					ErrorEvent ee = (ErrorEvent) conditionElement.getIncoming();
					event.setEventType(EventType.EVENT);
					fillEventWithProperties (event, relatedComponentInstance, ee,null);
					
					OsateDebug.osateDebug("EVENT NAME5" +ee.getName());
					event.setName(ee.getName());
				}
			}
			else
			{
			/**
			 * Here, the condition is associated to some subcomponent so we assume
			 * it is associated with another composite error states. 
			 */
				for (SubcomponentElement subcomponentElement : conditionElement.getSubcomponents())
				{
					Subcomponent subcomponent = subcomponentElement.getSubcomponent();
					//OsateDebug.osateDebug("cond = " + cond + "      subcomponent=" + subcomponent);
					ComponentInstance relatedInstance = findInstance(componentInstances, subcomponent.getName());
					
					if (relatedInstance == null)
					{
						continue;
					}
					ErrorBehaviorState ebsTarget = conditionElement.getState();
					String targetStateName = ebsTarget.getName();
					if (conditionElement.getConstraint() != null)
					{
						
						targetStateName += conditionElement.getConstraint().getTypeTokens().get(0).getType().get(0).getName();
					}
//					OsateDebug.osateDebug("[FTAUtils] handleCondition targetStateName=" + targetStateName + " on " + relatedInstance.getName());
					EList<CompositeState> emslist = EMV2Util.getAllCompositeStates(relatedInstance);
					
					if (!emslist.isEmpty())
					{
						fillCompositeBehavior(event, emslist, targetStateName, relatedInstance, componentInstances);
						//	public static void fillCompositeBehavior (Event ftaEvent, CompositeErrorBehavior compositeErrorBehavior, String stateName, ComponentInstance relatedInstance, final EList<ComponentInstance> componentInstances)
						
					}
					else
					{
						if (behaviorState != null)
						{
							
							if (event.getEventType() != EventType.NORMAL)
							{
								Event resultEvent = new Event();
								resultEvent.setEventType(EventType.NORMAL);
								fillEventWithProperties (resultEvent, relatedInstance, behaviorState);
								fillFTAEventfromEventState(resultEvent, behaviorState, relatedInstance, componentInstances);
								event.addSubEvent(resultEvent);
								
							}
							else
							{
								fillFTAEventfromEventState(event, behaviorState, relatedInstance, componentInstances);
								List<Event> propagations = findIncomingPropagations (relatedInstance, conditionElement, componentInstances);
								for (Event tmpEvent : propagations)
								{
									event.addSubEvent(tmpEvent);
								}
							}
						}
					}
				}
			}
		}
		
		
		if (cond instanceof SOrExpression)
		{
			SOrExpression sor = (SOrExpression)cond;
			
			if ((event.getEventType() != EventType.NORMAL) && (event.getEventType() != EventType.OR))
			{
				OsateDebug.osateDebug("[FTAUtils] Inconsistent composite behavior, expecting an OR condition");
			}
			event.setEventType(EventType.OR);
			
			String desc;
			desc = "\"occurrence (OR) of one event";
			if(getInvolvedSubcomponents (cond, componentInstances).size() > 0)
			{
				boolean first = true;
				desc += " (from ";
				for (String s : getInvolvedSubcomponents (cond, componentInstances))
				{
					
					if (first == false)
					{
						desc += ", ";
					}
					desc += s;
					first = false;
				}
				desc += ")";
			}
			desc += "\"";

			event.setDescription(desc);
						
			for (ConditionExpression conditionExpression : sor.getOperands())
			{
				//OsateDebug.osateDebug("      operand=" + conditionExpression);
				//result += handleCondition (conditionExpression, componentInstances);
				Event resultEvent = new Event ();
				handleCondition(resultEvent, resultingBehaviorState, relatedComponentInstance, conditionExpression, componentInstances, exploreRelativeCompositeStates);
				event.addSubEvent(resultEvent);
			}
		}
		
		if (cond instanceof SAndExpression)
		{
			SAndExpression sae = (SAndExpression) cond;
			if ((event.getEventType() != EventType.NORMAL) && (event.getEventType() != EventType.AND))
			{
				OsateDebug.osateDebug("[FTAUtils] Inconsistent composite behavior, expecting an AND condition");
			}
			event.setEventType(EventType.AND);
			String desc;
			desc = "\"combination (AND) of events";
			if(getInvolvedSubcomponents (cond, componentInstances).size() > 0)
			{
				boolean first = true;
				desc += " (from ";
				for (String s : getInvolvedSubcomponents (cond, componentInstances))
				{
					
					if (first == false)
					{
						desc += ", ";
					}
					desc += s;
					first = false;
				}
				desc += ")";
			}
			desc += "\"";
			event.setDescription(desc);
						
			for (ConditionExpression conditionExpression : sae.getOperands())
			{
				Event resultEvent = new Event ();
				handleCondition(resultEvent, resultingBehaviorState, relatedComponentInstance, conditionExpression, componentInstances, exploreRelativeCompositeStates);
				if (conditionExpression instanceof SAndExpression)
				{
					for (Event e : resultEvent.getSubEvents())
					{
						event.addSubEvent(e);
					}
						
				}
				else
				{
					event.addSubEvent(resultEvent);
				}
			}
		}
	}
	
	
	public static void fillCompositeBehavior (Event ftaEvent, EList<CompositeState> states, String stateName, ComponentInstance relatedInstance, final EList<ComponentInstance> componentInstances)
	{
		int nBranches = 0;
		for (CompositeState state : states)
		{
			if (state.getState().getName().equalsIgnoreCase(stateName))
			{
				nBranches++;
			}
		}
		
		if (nBranches > 1)
		{
			String desc;
			desc = "Switch to " + stateName + " due to the occurence of one of the following condition";
			ftaEvent.setEventType(EventType.OR);
			
			ftaEvent.setName (stateName);
			ftaEvent.setDescription(desc);
			ftaEvent.setIdentifier (stateName);
		}
		
		for (CompositeState state : states)
		{
			
			String originalStateName = state.getState().getName();
			if (state.getTypedToken() != null)
			{
				
				originalStateName += state.getTypedToken().getType().get(0).getName();
			}
			
//			OsateDebug.osateDebug("[FTAUtils] fillCompositeBehavior on " + relatedInstance.getName() +  " looking for="+ stateName +"|browsing state name=" + originalStateName);
			
			if (originalStateName.equalsIgnoreCase(stateName))
			{
				
				ErrorBehaviorState ebs = (ErrorBehaviorState) state.getState();

				Event targetEvent;
				if (nBranches > 1)
				{
					targetEvent = new Event();
				}
				else
				{
					targetEvent = ftaEvent;
				}
				
				fillEventWithProperties (targetEvent, relatedInstance, ebs);
				
				FTAUtils.fillFTAEventfromEventState (targetEvent, ebs, relatedInstance, componentInstances);

				FTAUtils.handleCondition (targetEvent, ebs, relatedInstance, state.getCondition(), componentInstances, false);
				if (nBranches > 1)
				{
					ftaEvent.addSubEvent(targetEvent);
				}
			}
			

		}
	}
	
	public static void fillEventWithProperties (Event event, ComponentInstance ci, NamedElement ne)
	{
		fillEventWithProperties (event, ci, ne, null);
	}
	
	public static void fillEventWithProperties (Event event, ComponentInstance ci, NamedElement ne, TypeSet ts)
	{
		EList<ContainedNamedElement> PA = EMV2Properties.getOccurenceDistributionProperty(ci,ne,ts);
		
		if (!PA.isEmpty()){
			double prob = EMV2Properties.getOccurenceValue (PA.get(0));
			event.setProbability(prob);
		}
		
		if (EMV2Properties.getDescription (ne, ci) != null)
		{
			event.setDescription("\"" + EMV2Properties.getDescription (ne, ci) + "\"");
		}
	}
	
	public static void fillFTAEventfromEventState (Event event, ErrorBehaviorState behaviorState, ComponentInstance relatedComponentInstance, final EList<ComponentInstance> componentInstances)
	{
		if (event == null)
		{
			OsateDebug.osateDebug("[FTAUtils] fillFTAEventfromEventState null event");
			return;
		}
		
		if (behaviorState == null)
		{
			OsateDebug.osateDebug("[FTAUtils] fillFTAEventfromEventState null event");
			return;
		}
		
		if (relatedComponentInstance == null)
		{
			OsateDebug.osateDebug("[FTAUtils] fillFTAEventfromEventState null event");
			return;
		}
				
		event.setName(behaviorState.getName() + "/" + relatedComponentInstance.getName()); 
		fillEventWithProperties (event, relatedComponentInstance, behaviorState, behaviorState.getTypeSet());		
	}
	
}
