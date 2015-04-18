package visitor;

import syntaxtree.*;

import java.util.HashMap;
import java.util.ArrayList;

public class BuildSymbolTableVisitor implements Visitor {
  
  private HashMap<String, ArrayList<String>> root = new HashMap<String,ArrayList<String>>();
  private Tree symbolTable = new Tree(root);
  private Node currentNode;
  private String curIden = null;
  private String expStr = null;
  private ArrayList<String> temp;
	
  
  public Tree getSymbolTable(){
	return symbolTable;
  }  

  // MainClass m;
  // ClassDeclList cl;
  public void visit(Program n) {
	System.err.println("\nBuild Symbol Table Visitor\n");
	symbolTable.setRootName(n.m.i1.s);
	currentNode = symbolTable.getRoot();
    Node cur = currentNode;
    
	currentNode = cur;
    for ( int i = 0; i < n.cl.size(); i++ ) {
        n.cl.elementAt(i).accept(this);
		currentNode = cur;
    }
	n.m.accept(this);
  }
  
  // Identifier i1,i2;
  // Statement s;
  public void visit(MainClass n) {
    //add main and args ids to top level
    temp = new ArrayList<>();
    temp.add("class");
	currentNode.getData().put(n.i1.s, temp);
    n.i1.accept(this);
    temp = new ArrayList<>();
    temp.add("String[]");
	currentNode.getData().put(n.i1.s, temp);
    n.i2.accept(this);
	
	//create main level and traverse main statements in this level
	Node main = new Node("main", currentNode);
	currentNode.addChild(main);
	Node cur = currentNode;
	currentNode = main;
    n.s.accept(this);
	currentNode = cur;
  }
  
  // Identifier i;
  // VarDeclList vl;
  // MethodDeclList ml;
  public void visit(ClassDeclSimple n) {
    //check if class iden has been used
    if(currentNode.getData().containsKey(n.i.s)){
		System.err.println("Multiply defined class identifier " + n.i.s + " at line " + n.i.line + ", character " + n.i.col);
	}
    temp = new ArrayList<>();
    temp.add("class");
    currentNode.getData().put(n.i.s, temp);
    n.i.accept(this);
	
	Node classNode = new Node(n.i.s, "class", currentNode);
	
	Node cur = currentNode;
	currentNode = classNode;
	
    for ( int i = 0; i < n.vl.size(); i++ ) {
        n.vl.elementAt(i).accept(this);
		currentNode = classNode;
    }
	for(int i = 0; i < n.ml.size(); i++){
		if(currentNode.parentsContains(n.ml.elementAt(i).i.s)){
			System.err.println("Multiply defined identifier " + n.ml.elementAt(i).i.s + " at line " + n.ml.elementAt(i).i.line + ", character " + n.ml.elementAt(i).i.col);
		}
		//System.err.println("About to add method " + n.ml.elementAt(i).i.s);
        temp = new ArrayList<>();
        temp.add("method");
		currentNode.getData().put(n.ml.elementAt(i).i.s, temp);
	}
    for ( int i = 0; i < n.ml.size(); i++ ) {
        n.ml.elementAt(i).accept(this);
		currentNode = classNode;
    }
	currentNode = cur;
	currentNode.addChild(classNode);
  }
 
  // Identifier i;
  // Identifier j;
  // VarDeclList vl;
  // MethodDeclList ml;
  public void visit(ClassDeclExtends n) {
    if(currentNode.getData().containsKey(n.i.s)){
		System.err.println("Multiply defined identifier " + n.i.s + " at line " + n.i.line + ", character " + n.i.col);
	}
    temp = new ArrayList<>();
    temp.add("class");
    currentNode.getData().put(n.i.s, temp);
    n.i.accept(this);
    n.j.accept(this);
	
	Node classNode = new Node(n.i.s, "class", currentNode);
	
	Node cur = currentNode;
	currentNode = classNode;
	
    for ( int i = 0; i < n.vl.size(); i++ ) {
        n.vl.elementAt(i).accept(this);
		currentNode = classNode;
    }
	for(int i = 0; i < n.ml.size(); i++){
		if(currentNode.parentsContains(n.ml.elementAt(i).i.s)){
			System.err.println("Multiply defined identifier " + n.ml.elementAt(i).i.s + " at line " + n.ml.elementAt(i).i.line + ", character " + n.ml.elementAt(i).i.col);
		}
		//System.err.println("About to add method " + n.ml.elementAt(i).i.s);
        temp = new ArrayList<>();
        temp.add("method");
		currentNode.getData().put(n.ml.elementAt(i).i.s, temp);
	}
    for ( int i = 0; i < n.ml.size(); i++ ) {
        n.ml.elementAt(i).accept(this);
		currentNode = classNode;
    }
	currentNode = cur;
	currentNode.addChild(classNode);
  }

  // Type t;
  // Identifier i;
  public void visit(VarDecl n) {
	if(currentNode.parentsContains(n.i.s)){
		System.err.println("Multiply defined identifier " + n.i.s + " at line " + n.i.line + ", character " + n.i.col);
	}
    curIden = n.i.s;
    n.t.accept(this);
    n.i.accept(this);
  }

  // Type t;
  // Identifier i;
  // FormalList fl;
  // VarDeclList vl;
  // StatementList sl;
  // Exp e;
  public void visit(MethodDecl n) {
	/*if(currentNode.parentsContains(n.i.s)){
		System.err.println("Multiply defined identifier " + n.i.s + " at line ");
	}*/
  
	curIden = n.i.s;
    n.t.accept(this);
	/*System.err.println("About to add method " + n.i.s);
	currentNode.getData().put(n.i.s, "method");*/
    n.i.accept(this);
	
	Node methodNode = new Node(n.i.s, "method", currentNode);
	
	Node cur = currentNode;
	currentNode = methodNode;
	
    for ( int i = 0; i < n.fl.size(); i++ ) {
        n.fl.elementAt(i).accept(this);
		currentNode = methodNode;
    }
    for ( int i = 0; i < n.vl.size(); i++ ) {
        n.vl.elementAt(i).accept(this);
		currentNode = methodNode;
    }
    for ( int i = 0; i < n.sl.size(); i++ ) {
        n.sl.elementAt(i).accept(this);
		currentNode = methodNode;
    }
    n.e.accept(this);
	currentNode = cur;
	currentNode.addChild(methodNode);
  }

  // Type t;
  // Identifier i;
  public void visit(Formal n) {
    if(currentNode.parentsContains(n.i.s)){
		System.err.println("Multiply defined identifier " + n.i.s + " at line " + n.i.line + ", character " + n.i.col);
	}
    if(currentNode == null) System.err.println("curentNode is null");
    if(currentNode.params == null) System.err.println("curentNode.params is null");
    if(n.i.s == null) System.err.println("n.i.s is null");
    currentNode.params.add(n.i.s);
    curIden = n.i.s;
    n.t.accept(this);
    n.i.accept(this);
  }

  public void visit(IntArrayType n) {
    temp = new ArrayList<>();
    temp.add("intArr");
	currentNode.getData().put(curIden, temp);
  }

  public void visit(BooleanType n) {
    temp = new ArrayList<>();
    temp.add("boolean");
	currentNode.getData().put(curIden, temp);
  }

  public void visit(IntegerType n) {
    temp = new ArrayList<>();
    temp.add("int");
	currentNode.getData().put(curIden, temp);
  }

  // String s;
  public void visit(IdentifierType n) {
    temp = new ArrayList<>();
    temp.add(n.s);
	currentNode.getData().put(curIden, temp);
  }

  // StatementList sl;
  public void visit(Block n) {
    for ( int i = 0; i < n.sl.size(); i++ ) {
        n.sl.elementAt(i).accept(this);
    }
  }

  // Exp e;
  // Statement s1,s2;
  public void visit(If n) {
    n.e.accept(this);
	n.s1.accept(this);
	n.s2.accept(this);
  }

  // Exp e;
  // Statement s;
  public void visit(While n) {
    n.e.accept(this);
    n.s.accept(this);
  }

  // Exp e;
  public void visit(Print n) {
    n.e.accept(this);
  }
  
  // Identifier i;
  // Exp e;
  public void visit(Assign n) {
	if(!currentNode.parentsContains(n.i.s)){
		System.err.println("Use of undefined identifier " + n.i.s + " at line " + n.i.line + ", character " + n.i.col);
	}
    n.i.accept(this);
    n.e.accept(this);
  }

  // Identifier i;
  // Exp e1,e2;
  public void visit(ArrayAssign n) {
	if(!currentNode.parentsContains(n.i.s)){
		System.err.println("Use of undefined identifier " + n.i.s + " at line " + n.i.line + ", character " + n.i.col);
	}
    n.i.accept(this);
    n.e1.accept(this);
    n.e2.accept(this);
  }

  // Exp e1,e2;
  public void visit(And n) {
    n.e1.accept(this);
    n.e2.accept(this);
  }

  // Exp e1,e2;
  public void visit(LessThan n) {
    n.e1.accept(this);
    n.e2.accept(this);
  }

  // Exp e1,e2;
  public void visit(Plus n) {
    n.e1.accept(this);
    n.e2.accept(this);
  }

  // Exp e1,e2;
  public void visit(Minus n) {
    n.e1.accept(this);
    n.e2.accept(this);
  }

  // Exp e1,e2;
  public void visit(Times n) {
    n.e1.accept(this);
    n.e2.accept(this);
  }

  // Exp e1,e2;
  public void visit(ArrayLookup n) {
    n.e1.accept(this);
    n.e2.accept(this);
  }

  // Exp e;
  public void visit(ArrayLength n) {
    n.e.accept(this);
  }

  // Exp e;
  // Identifier i;
  // ExpList el;
  public void visit(Call n) {
	String className = new String();
	n.e.accept(this);
	if(n.e instanceof This){
		className = currentNode.getParent().getName();
	}
    else className = expStr;
	/*Node root = symbolTable.getRoot();
	Node node = new Node("node");
	
	System.err.println("ClassName is " + className);
	for(int k = 0; k < root.getChildren().size(); k++){
		if(root.getChildren().get(k).getName().equals(className)){
			node = root.getChildren().get(k);
			break;
		}
	}
	boolean found = false;
	for(String str: node.getData().keySet()){
		if(str.equals(n.i.s)) found = true;
	}
	if(!found){
		System.err.println("Use of undefined identifier " + n.i.s + " at line ");
	}*/
	Node node = symbolTable.search(n.i.s, symbolTable.search(className, symbolTable.getRoot()));
	if(node == null){
		if(!currentNode.parentsContains(n.i.s)) System.err.println("Use of undefined identifier " + n.i.s + " at line " + n.i.line + ", character " + n.i.col);
	}
    n.i.accept(this);
    for ( int i = 0; i < n.el.size(); i++ ) {
        n.el.elementAt(i).accept(this);
    }
  }

  // int i;
  public void visit(IntegerLiteral n) {
  }

  public void visit(True n) {
  }

  public void visit(False n) {
  }

  // String s;
  public void visit(IdentifierExp n) {
	if(!currentNode.parentsContains(n.s)){
		System.err.println("Use of undefined identifier " + n.s + " at line " + n.line + ", character " + n.col);
	}
	expStr = n.s;
  }

  public void visit(This n) {
  }

  // Exp e;
  public void visit(NewArray n) {
    n.e.accept(this);
  }

  // Identifier i;
  public void visit(NewObject n) {
	expStr = n.i.s;
  }

  // Exp e;
  public void visit(Not n) {
    n.e.accept(this);
  }

  // String s;
  public void visit(Identifier n) {
  }
}