/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.ejb.plugins.cmp.ejbql;

/**
 * This abstract syntax node represents a negation opperator.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.1.1.1 $
 */                            
public class ASTNegation extends SimpleNode {
  public ASTNegation(int id) {
    super(id);
  }

  public ASTNegation(EJBQLParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(JBossQLParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
