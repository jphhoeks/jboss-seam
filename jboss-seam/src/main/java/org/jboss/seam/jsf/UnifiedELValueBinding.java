package org.jboss.seam.jsf;

import java.io.Serializable;

import javax.el.ValueExpression;
import javax.faces.context.FacesContext;


/**
 * Nobody should be using ValueBinding anymore, but if they 
 * are, we need this.
 * 
 * @author Gavin King
 *
 */

@Deprecated
public class UnifiedELValueBinding extends javax.faces.el.ValueBinding implements Serializable
{
   private static final long serialVersionUID = -7555716855004738587L;

private transient ValueExpression valueExpression;
   
   private String expressionString;

   public UnifiedELValueBinding(String expressionString)
   {
      this.expressionString = expressionString;
   }

   public UnifiedELValueBinding() {}
   
   @Override
   public String getExpressionString()
   {
      return expressionString;
   }

   @Override
   public Class getType(FacesContext ctx) throws javax.faces.el.EvaluationException, javax.faces.el.PropertyNotFoundException {
      return getValueExpression(ctx).getType( ctx.getELContext() );
   }

   @Override
   public Object getValue(FacesContext ctx) throws javax.faces.el.EvaluationException, javax.faces.el.PropertyNotFoundException {
   	return getValueExpression(ctx).getValue( ctx.getELContext() );
   }

   @Override
   public boolean isReadOnly(FacesContext ctx) throws javax.faces.el.EvaluationException, javax.faces.el.PropertyNotFoundException {
   	return getValueExpression(ctx).isReadOnly( ctx.getELContext() );
   }

   @Override
   public void setValue(FacesContext ctx, Object value) throws javax.faces.el.EvaluationException, javax.faces.el.PropertyNotFoundException {
      getValueExpression(ctx).setValue( ctx.getELContext(), value);
   }
   
   @Override
   public String toString()
   {
      return getExpressionString();
   }

   private ValueExpression getValueExpression(FacesContext ctx)
   {
      if (valueExpression==null)
      {
         valueExpression = ctx.getApplication().getExpressionFactory()
                  .createValueExpression( ctx.getELContext(), expressionString, Object.class );
      }
      return valueExpression;
   }
}