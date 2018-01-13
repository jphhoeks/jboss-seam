package org.jboss.seam.jsf;

import java.io.Serializable;

import javax.el.MethodExpression;
import javax.faces.context.FacesContext;


/**
 * Nobody should be using MethodBinding anymore, but if they 
 * are, we need this.
 * 
 * @author Gavin King
 *
 */
@Deprecated
public class UnifiedELMethodBinding extends javax.faces.el.MethodBinding implements Serializable
{
   private static final long serialVersionUID = 5465825403188403025L;

private transient MethodExpression methodExpression;
   
   private String expressionString;
   private Class[] argTypes;

   public UnifiedELMethodBinding() {}
   
   public UnifiedELMethodBinding(String expressionString, Class[] argTypes)
   {
      this.expressionString = expressionString;
      this.argTypes = argTypes;
   }

   @Override
   public String getExpressionString()
   {
      return expressionString;
   }

   @Override
   public Class getType(FacesContext ctx) throws javax.faces.el.MethodNotFoundException
   {
      return getMethodExpression(ctx).getMethodInfo( ctx.getELContext() ).getReturnType();
   }

   @Override
   public Object invoke(FacesContext ctx, Object[] args) throws javax.faces.el.EvaluationException, javax.faces.el.MethodNotFoundException
   {
      return getMethodExpression(ctx).invoke( ctx.getELContext(), args);
   }

   @Override
   public String toString()
   {
      return getExpressionString();
   }

   private MethodExpression getMethodExpression(FacesContext ctx)
   {
      if (methodExpression==null)
      {
         // In JSF 1.1 EL (argTypes = null) == (argTypes = new Class[0]), but not in Unified EL
         methodExpression = ctx.getApplication().getExpressionFactory()
                  .createMethodExpression( ctx.getELContext(), expressionString, Object.class, argTypes == null ? new Class[0] : argTypes );
      }
      return methodExpression;
   }
}