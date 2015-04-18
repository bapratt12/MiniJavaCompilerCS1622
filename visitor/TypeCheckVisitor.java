package visitor;

import syntaxtree.*;

public class TypeCheckVisitor implements TypeVisitor {

	private Tree symbolTable;
	Node currentNode;

  // MainClass m;
  // ClassDeclList cl;
  public Type visit(Program n) {
	System.err.println("\nType Check Visitor\n");
	symbolTable = n.symbolTable;
	currentNode = symbolTable.getRoot();
	Node cur = currentNode;
	currentNode = symbolTable.search("main", currentNode);
    n.m.accept(this);
	currentNode = cur;
    for ( int i = 0; i < n.cl.size(); i++ ) {
        n.cl.elementAt(i).accept(this);
		currentNode = cur;
    }
    return null;
  }
  
  // Identifier i1,i2;
  // Statement s;
  public Type visit(MainClass n) {
    n.i1.accept(this);
    //n.i2.accept(this);
    n.s.accept(this);
    return null;
  }
  
  // Identifier i;
  // VarDeclList vl;
  // MethodDeclList ml;
  public Type visit(ClassDeclSimple n) {
	Node cur = currentNode;
	Node classNode = symbolTable.search(n.i.s, currentNode);
	currentNode = classNode;
    n.i.accept(this);
    for ( int i = 0; i < n.vl.size(); i++ ) {
        n.vl.elementAt(i).accept(this);
		currentNode = classNode;
    }
    for ( int i = 0; i < n.ml.size(); i++ ) {
        n.ml.elementAt(i).accept(this);
		currentNode = classNode;
    }
	currentNode = cur;
    return null;
  }
 
  // Identifier i;
  // Identifier j;
  // VarDeclList vl;
  // MethodDeclList ml;
  public Type visit(ClassDeclExtends n) {
	Node cur = currentNode;
	Node classNode = symbolTable.search(n.i.s, currentNode);
	currentNode = classNode;
    n.i.accept(this);
    n.j.accept(this);
    for ( int i = 0; i < n.vl.size(); i++ ) {
        n.vl.elementAt(i).accept(this);
		currentNode = classNode;
    }
    for ( int i = 0; i < n.ml.size(); i++ ) {
        n.ml.elementAt(i).accept(this);
		currentNode = classNode;
    }
	currentNode = cur;
    return null;
  }

  // Type t;
  // Identifier i;
  public Type visit(VarDecl n) {
    n.t.accept(this);
    n.i.accept(this);
    return null;
  }

  // Type t;
  // Identifier i;
  // FormalList fl;
  // VarDeclList vl;
  // StatementList sl;
  // Exp e;
  public Type visit(MethodDecl n) {
	Node cur = currentNode;
	Node methodNode = symbolTable.search(n.i.s, currentNode);
	currentNode = methodNode;
    n.t.accept(this);
    n.i.accept(this);
    for ( int i = 0; i < n.fl.size(); i++ ) {
        n.fl.elementAt(i).accept(this);
    }
    for ( int i = 0; i < n.vl.size(); i++ ) {
        n.vl.elementAt(i).accept(this);
    }
    for ( int i = 0; i < n.sl.size(); i++ ) {
        n.sl.elementAt(i).accept(this);
    }
    n.e.accept(this);
	currentNode = cur;
    return null;
  }

  // Type t;
  // Identifier i;
  public Type visit(Formal n) {
    n.t.accept(this);
    n.i.accept(this);
    return null;
  }

  public Type visit(IntArrayType n) {
    return new IntArrayType();
  }

  public Type visit(BooleanType n) {
    return new BooleanType();
  }

  public Type visit(IntegerType n) {
    return new IntegerType();
  }

  // String s;
  public Type visit(IdentifierType n) {
	Node node = currentNode.parentsSearch(n.s);
	String type = node.getData().get(n.s).get(0);
	if(type.equals("boolean")){
		return new BooleanType();
	}
	else if(type.equals("int")){
		return new IntegerType();
	}
	else if(type.equals("intArr")){
		return new IntArrayType();
	}
	else{
		return new IdentifierType(type);
	}
  }

  // StatementList sl;
  public Type visit(Block n) {
    for ( int i = 0; i < n.sl.size(); i++ ) {
        n.sl.elementAt(i).accept(this);
    }
    return null;
  }

  // Exp e;
  // Statement s1,s2;
  public Type visit(If n) {
    Type t = n.e.accept(this);
	if(!(t instanceof BooleanType)){
		System.err.println("Non-boolean expression used as the condition of if statement at line " + t.line + ", character " + t.col);
	}
    n.s1.accept(this);
    n.s2.accept(this);
    return null;
  }

  // Exp e;
  // Statement s;
  public Type visit(While n) {
    Type t = n.e.accept(this);
	if(!(t instanceof BooleanType)){
		System.err.println("Non-boolean expression used as the condition of while statement at line " + t.line + ", character " + t.col);
	}
    n.s.accept(this);
    return null;
  }

  // Exp e;
  public Type visit(Print n) {
    Type t = n.e.accept(this);
	if(!(t instanceof IntegerType)){
		System.err.println("Call of method System.out.println does not match its declared signature at line " + t.line + ", character " + t.col);
	}
    return null;
  }
  
  // Identifier i;
  // Exp e;
  public Type visit(Assign n) {
	Node node = symbolTable.search(n.i.s, symbolTable.getRoot());
	if((node != null) || (n.i.s.equals("this"))){
		System.err.println("Invalid l-value: " + n.i.s + " cannot be assigned to, at line " + n.i.line + ", character " + n.i.col);
	}
    Type t1 = n.i.accept(this);
    Type t2 = n.e.accept(this);
	IdentifierType t3 = new IdentifierType("null");
	IdentifierType t4 = new IdentifierType("temp");
	if(t1 instanceof IdentifierType){
		t3 = (IdentifierType) t1;
	}
	if(t2 instanceof IdentifierType){
		t4 = (IdentifierType) t2;
		Node temp = symbolTable.search(t4.s, symbolTable.getRoot());
		//System.err.println("IDType " + t4.s);
		if(temp != null && temp.getParent().getParent() != null) System.err.println("Invalid r-value: " + t4.s + " cannot be assigned from, at line " + t4.line + ", character " + t4.col);
	}
	if(!((t1 instanceof IntegerType) && (t2 instanceof IntegerType)) && !((t1 instanceof BooleanType) && (t2 instanceof BooleanType)) &&
	!((t1 instanceof IntArrayType) && (t2 instanceof IntArrayType)) && !(t3.s.equals(t4.s))){
		System.err.println("Type mismatch during assignment at line " + n.i.line + ", character " + n.i.col);
	}
    return null;
  }

  // Identifier i;
  // Exp e1,e2;
  public Type visit(ArrayAssign n) {
    Type t1 = n.i.accept(this);
    Type t2 = n.e1.accept(this);
	if(!(t2 instanceof IntegerType)){
		System.err.println("Attempt to index array with non-integer at line " + t2.line + ", character " + t2.col);
	}
    Type t3 = n.e2.accept(this);
	if(!(t3 instanceof IntegerType)){
		System.err.println("Type mismatch during array assignment at line " + t3.line + ", character " + t3.col);
	}
    return null;
  }

  // Exp e1,e2;
  public Type visit(And n) {
    Type t1 = n.e1.accept(this);
    Type t2 = n.e2.accept(this);
	boolean check1 = checkIfValid(t1);
	boolean check2 = checkIfValid(t2);
	if(!(check1 && check2)){
		System.err.println("Invalid operands for and operator at line " + t1.line + ", character " + t1.col);
	}
	if(!(t1 instanceof BooleanType) || !(t2 instanceof BooleanType)){
		System.err.println("Attempt to use boolean operator and on non-boolean operators at line " + t1.line + ", character " + t1.col);
	}
    return new BooleanType(t1.line, t1.col);
  }

  // Exp e1,e2;
  public Type visit(LessThan n) {
    Type t1 = n.e1.accept(this);
    Type t2 = n.e2.accept(this);
	boolean check1 = checkIfValid(t1);
	boolean check2 = checkIfValid(t2);
	if(!(check1 && check2)){
		System.err.println("Invalid operands for less than operator at line " + t1.line + ", character " + t1.col);
	}
	if(!(t1 instanceof IntegerType) || !(t2 instanceof IntegerType)){
		System.err.println("Non-integer operand for operator less than at line " + t1.line + ", character " + t1.col);
	}
    return new BooleanType(t1.line, t1.col);
  }

  // Exp e1,e2;
  public Type visit(Plus n) {
    Type t1 = n.e1.accept(this);
    Type t2 = n.e2.accept(this);
	boolean check1 = checkIfValid(t1);
	boolean check2 = checkIfValid(t2);
	if(!(check1 && check2)){
		System.err.println("Invalid operands for plus operator at line " + t1.line + ", character " + t1.col);
	}
	if(!(t1 instanceof IntegerType) || !(t2 instanceof IntegerType)){
		System.err.println("Non-integer operand for operator plus at line " + t1.line + ", character " + t1.col);
	}
    return new IntegerType(t1.line, t1.col);
  }

  // Exp e1,e2;
  public Type visit(Minus n) {
    Type t1 = n.e1.accept(this);
    Type t2 = n.e2.accept(this);
	boolean check1 = checkIfValid(t1);
	boolean check2 = checkIfValid(t2);
	if(!(check1 && check2)){
		System.err.println("Invalid operands for minus operator at line " + t1.line + ", character " + t1.col);
	}
	if(!(t1 instanceof IntegerType) || !(t2 instanceof IntegerType)){
		System.err.println("Non-integer operand for operator minus at line " + t1.line + ", character " + t1.col);
	}
	return new IntegerType(t1.line, t1.col);
  }

  // Exp e1,e2;
  public Type visit(Times n) {
    Type t1 = n.e1.accept(this);
    Type t2 = n.e2.accept(this);
	boolean check1 = checkIfValid(t1);
	boolean check2 = checkIfValid(t2);
	if(!(check1 && check2)){
		System.err.println("Invalid operands for minus operator at line " + t1.line + ", character " + t1.col);
	}
	if(!(t1 instanceof IntegerType) || !(t2 instanceof IntegerType)){
		System.err.println("Non-integer operand for operator times at line " + t1.line + ", character " + t1.col);
	}
    return new IntegerType(t1.line, t1.col);
  }

  // Exp e1,e2;
  public Type visit(ArrayLookup n) {
    Type t1 = n.e1.accept(this);
	if(!(t1 instanceof IntArrayType)){
		System.err.println("Attempt to use square brackets on non-array at line " + t1.line + ", character " + t1.col);
	}
    Type t2 = n.e2.accept(this);
	if(!(t2 instanceof IntegerType)){
		System.err.println("Attempt to index array with non-integer at line " + t1.line + ", character " + t1.col);
	}
    return new IntegerType(t1.line, t1.col);
  }

  // Exp e;
  public Type visit(ArrayLength n) {
    Type t = n.e.accept(this);
	if(!(t instanceof IntArrayType)){
		System.err.println("Length property only applies to arrays, line " + t.line + ", character " + t.col);
	}
    return new IntegerType(t.line, t.col);
  }

  // Exp e;
  // Identifier i;
  // ExpList el;
  public Type visit(Call n) {
    Type t = n.e.accept(this);
	Node node = symbolTable.search(n.i.s, symbolTable.getRoot());
	if(node == null){
		System.err.println("Attempt to call a non-method at line " + n.i.line + ", character " + n.i.col);
	}
	else if(!node.getType().equals("method")){
		System.err.println("Attempt to call a non-method at line " + n.i.line + ", character " + n.i.col);
	}
    //n.i.accept(this);
    for ( int i = 0; i < n.el.size(); i++ ) {
        n.el.elementAt(i).accept(this);
    }
    return new IntegerType(t.line, t.col);
  }

  // int i;
  public Type visit(IntegerLiteral n) {
    return new IntegerType(n.line, n.col);
  }

  // int line, col;
  public Type visit(True n) {
    return new BooleanType(n.line, n.col);
  }

  // int line, col;
  public Type visit(False n) {
    return new BooleanType(n.line, n.col);
  }

  // String s;
  // int line, col;
  public Type visit(IdentifierExp n) {
	/*Node node = symbolTable.search(n.s, symbolTable.getRoot());
	if(node != null){
		System.err.println("Invalid r-value at line ");
	}*/
    Node node = currentNode.parentsSearch(n.s);
	String type = null;
	if(node != null){
		type = node.getData().get(n.s).get(0);
	}
	else{
		node = symbolTable.search(n.s, symbolTable.getRoot());
		if(node != null) type = node.getName();
	}
	if(type.equals("boolean")){
		return new BooleanType(n.line, n.col);
	}
	else if(type.equals("int")){
		return new IntegerType(n.line, n.col);
	}
	else if(type.equals("intArr")){
		return new IntArrayType(n.line, n.col);
	}
	else{
		return new IdentifierType(type, n.line, n.col);
	}
  }
  
  //int line, col;
  public Type visit(This n) {
	if(currentNode.getName().equals("main")){
		System.err.println("Illegal use of keyword 'this' in static method at line " + n.line + ", character " + n.col);
	}
    return new IdentifierType(currentNode.getName(), n.line, n.col);
  }

  // Exp e;
  public Type visit(NewArray n) {
    Type t = n.e.accept(this);
	if(!(t instanceof IntegerType)){
		System.err.println("Attempt to set array size with non-integer at line  " + t.line + ", character " + t.col);
	}
    return new IntArrayType(t.line, t.col);
  }

  // Identifier i;
  public Type visit(NewObject n) {
    return new IdentifierType(n.i.s, n.i.line, n.i.col);
  }

  // Exp e;
  public Type visit(Not n) {
	Type t = n.e.accept(this);
	boolean check = checkIfValid(t);
	if(!check){
		System.err.println("Invalid operands for not operator at line " + t.line + ", character " + t.col);
	}
    if(!(t instanceof BooleanType)){
		System.err.println("Attempt to use boolean operator not on non-boolean operators at line " + t.line + ", character " + t.col);
	}
    return new BooleanType(t.line, t.col);
  }

  // String s;
  // int line, col;
  public Type visit(Identifier n) {
    Node node = currentNode.parentsSearch(n.s);
	if(node == null){
		return new IdentifierType(n.s, n.line, n.col);
	}
	String type = node.getData().get(n.s).get(0);
	if(type.equals("boolean")){
		return new BooleanType(n.line, n.col);
	}
	else if(type.equals("int")){
		return new IntegerType(n.line, n.col);
	}
	else if(type.equals("intArr")){
		return new IntArrayType(n.line, n.col);
	}
	else{
		return new IdentifierType(type, n.line, n.col);
	}
  }
  
  public boolean checkIfValid(Type t){
	IdentifierType t1;
	Node n = null;
	if(t instanceof IdentifierType){
		t1 = (IdentifierType) t;
		n = symbolTable.search(t1.s, symbolTable.getRoot());
		if(n != null) return false;
		else return true;
	}
	return true;

  }
  
}