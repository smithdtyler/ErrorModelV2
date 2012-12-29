package org.osate.xtext.aadl2.errormodel.validation;
 
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EPackage;
import org.osate.xtext.aadl2.properties.validation.PropertiesJavaValidator;

public class AbstractErrorModelJavaValidator extends PropertiesJavaValidator {

	@Override
	protected List<EPackage> getEPackages() {
	    List<EPackage> result = new ArrayList<EPackage>();
	    result.add(org.osate.xtext.aadl2.errormodel.errorModel.ErrorModelPackage.eINSTANCE);
	    result.add(EPackage.Registry.INSTANCE.getEPackage("http://aadl.info/AADL/2.0"));
		return result;
	}

}
