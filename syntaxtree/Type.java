package syntaxtree;
import visitor.Visitor;
import visitor.TypeVisitor;

public abstract class Type {
  public int line, col;
  public abstract void accept(Visitor v);
  public abstract Type accept(TypeVisitor v);
}
