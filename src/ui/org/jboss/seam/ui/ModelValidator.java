package org.jboss.seam.ui;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidValue;
import org.jboss.seam.Component;
import org.jboss.seam.Seam;
import org.jboss.seam.core.FacesMessages;
import org.jboss.seam.core.ResourceBundle;

public class ModelValidator implements Validator
{

   public void validate(FacesContext context, UIComponent component, Object value)
         throws ValidatorException
   {
      ValueBinding valueBinding = component.getValueBinding("value");
      if (valueBinding==null)
      {
         throw new RuntimeException("component has no value attribute: " + component.getId());
      }
      String propertyExpression = valueBinding.getExpressionString();
      int sep = propertyExpression.lastIndexOf('.');
      if (sep<=0) 
      {
         throw new RuntimeException("not an attribute value binding: " + propertyExpression);
      }
      String modelExpression = propertyExpression.substring(0, sep) + '}';

      Object model = context.getApplication().createValueBinding(modelExpression).getValue(context);

      String propertyName = propertyExpression.substring( modelExpression.length() , propertyExpression.length()-1 );
      
      Class modelClass = model.getClass();
      if ( modelClass.getName().contains("CGLIB") ) 
      {
         modelClass = modelClass.getSuperclass();
      }
      ClassValidator validator;
      String componentName = Seam.getComponentName(modelClass);
      if (componentName==null)
      {
         java.util.ResourceBundle bundle = ResourceBundle.instance();
         validator = bundle==null ? 
               new ClassValidator(modelClass) : 
               new ClassValidator(modelClass, bundle);
      }
      else
      {
         validator = Component.forName(componentName).getValidator();
      }
      InvalidValue[] ivs = validator.getPotentialInvalidValues(propertyName, value);
      if ( ivs.length!=0 )
      {
         throw new ValidatorException( FacesMessages.createFacesMessage( FacesMessage.SEVERITY_WARN, ivs[0].getMessage() ) );
      }
   }

}
