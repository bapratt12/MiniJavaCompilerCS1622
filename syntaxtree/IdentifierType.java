package syntaxtree;
import visitor.Visitor;
import visitor.TypeVisitor;

public class IdentifierType extends Type {
  public String s;

  public IdentifierType(String as){
    s=as;
  }
  
  public IdentifierType(String as, int l, int c) {
    s=as; line=l; col=c;
  }

  public void accept(Visitor v) {
    v.visit(this);
  }

  public Type accept(TypeVisitor v) {
    return v.visit(this);
  }
}
