package syntaxtree;
import visitor.Visitor;
import visitor.TypeVisitor;

public class IntegerLiteral extends Exp {
  public int i, line, col;

  public IntegerLiteral(int ai, int l, int c) {
    i=ai; line = l; col = c;
  }

  public void accept(Visitor v) {
    v.visit(this);
  }

  public Type accept(TypeVisitor v) {
    return v.visit(this);
  }
}
