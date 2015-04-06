package syntaxtree;
import visitor.Visitor;
import visitor.TypeVisitor;

public class Identifier {
  public String s;
  public int line, col;

  public Identifier(String as, int l, int c) { 
    s=as; line = l; col = c;
  }

  public void accept(Visitor v) {
    v.visit(this);
  }

  public Type accept(TypeVisitor v) {
    return v.visit(this);
  }

  public String toString(){
    return s;
  }
}
