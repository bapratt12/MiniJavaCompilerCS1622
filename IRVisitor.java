/**
 * @author Collin Barth
 *
 * IRVisitor.java
 * visits each node of an abstract syntax tree and
 * generates the corresponding three address code
 */

import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;

import java_cup.runtime.Symbol;

import syntaxtree.*;
import visitor.Visitor;

public class IRVisitor implements Visitor {

   // statement types
   public static final int ASSIGN           = 1;
   public static final int UNARY_ASSIGN     = 2;
   public static final int COPY             = 3;
   public static final int UNCOND_JUMP      = 4;
   public static final int COND_JUMP        = 5;
   public static final int PARAM            = 6;
   public static final int CALL             = 7;
   public static final int RETURN           = 8;
   public static final int IND_ASSIGN_OTHER = 9;
   public static final int IND_ASSIGN_SELF  = 10;
   public static final int NEW              = 11;
   public static final int NEW_ARRAY        = 12;
   public static final int LENGTH           = 13;

   // quadruple parameters
   String op;
   Object arg1, arg2, result;

   // temporary register number
   public int tempVar;

   // intermediate representation
   public ArrayList<Quadruple> IR;

   // include 'this' as a param when calling from a non-static method
   private boolean inStatic = false;

   // constructor
   public IRVisitor() {
      tempVar = 0;
      IR = new ArrayList<Quadruple>();
      clear();
   }

   // clear quadruple parameters
   public void clear() {
      op = "";
      arg1 = ""; arg2 = "";
      result = "";
   }

   // MainClass m;
   // ClassDeclList cl;
   public void visit(Program n) {
      System.out.println("Program");
      n.m.accept(this);
      for(int i = 0; i < n.cl.size(); i++) {
         n.cl.elementAt(i).accept(this);
      }
   }

   // Identifier i1,i2;
   // Statement s;
   public void visit(MainClass n) {
      System.out.println("MainClass");
      inStatic = true;
      n.i1.accept(this); n.i2.accept(this);
      n.s.accept(this);
      inStatic = false;
   }

   // Identifier i;
   // VarDeclList vl;
   // MethodDeclList ml;
   public void visit(ClassDeclSimple n) {
      System.out.println("ClassDeclSimple");
      n.i.accept(this);
      for(int i = 0; i < n.vl.size(); i++) {
         n.vl.elementAt(i).accept(this);
      }
      for(int i = 0; i < n.ml.size(); i++) {
         n.ml.elementAt(i).accept(this);
      }
   }

   // Identifier i;
   // Identifier j;
   // VarDeclList vl;
   // MethodDeclList ml;
   public void visit(ClassDeclExtends n) {
      System.out.println("ClassDeclExtends");
      n.i.accept(this); n.j.accept(this);
      for(int i = 0; i < n.vl.size(); i++) {
         n.vl.elementAt(i).accept(this);
      }
      for(int i = 0; i < n.ml.size(); i++) {
         n.ml.elementAt(i).accept(this);
      }
   }

   // Type t;
   // Identifier i;
   public void visit(VarDecl n) {
      System.out.println("VarDecl");
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
      System.out.println("MethodDecl");
      n.t.accept(this);
      n.i.accept(this);
      for(int i = 0; i < n.fl.size(); i++) {
         n.fl.elementAt(i).accept(this);
      }
      for(int i = 0; i < n.vl.size(); i++) {
         n.vl.elementAt(i).accept(this);
      }
      for(int i = 0; i < n.sl.size(); i++) {
         n.sl.elementAt(i).accept(this);
      }
      n.e.accept(this);
   }

   // Type t;
   // Identifier i;
   public void visit(Formal n) {
      System.out.println("Formal");
      n.t.accept(this);
      n.i.accept(this);
   }

   public void visit(IntArrayType n) {
      System.out.println("IntArrayType");
   }

   public void visit(BooleanType n) {
      System.out.println("BooleanType");
   }

   public void visit(IntegerType n) {
      System.out.println("IntegerType");
   }

   // String s;
   public void visit(IdentifierType n) {
      System.out.println("IdentifierType");
   }

   // StatementList sl;
   public void visit(Block n) {
      System.out.println("Block");
      for(int i = 0; i < n.sl.size(); i++) {
         n.sl.elementAt(i).accept(this);
      }
   }

   // Exp e;
   // Statement s1,s2;
   public void visit(If n) {
      System.out.println("If");
      n.e.accept(this);
      n.s1.accept(this); n.s2.accept(this);
   }

   // Exp e;
   // Statement s;
   public void visit(While n) {
      System.out.println("While");
      n.e.accept(this);
      n.s.accept(this);
   }

   // Exp e;
   public void visit(Print n) {
      System.out.println("Print");
      n.e.accept(this);
   }

   // Identifier i;
   // Exp e;
   public void visit(Assign n) {
      System.out.println("Assign");
      n.i.accept(this);

      result = n.i.s;
      n.e.accept(this);
   }

   // Identifier i;
   // Exp e1,e2;
   public void visit(ArrayAssign n) {
      System.out.println("ArrayAssign");
      n.i.accept(this);
      n.e1.accept(this); n.e2.accept(this);
   }

   // Exp e1,e2;
   public void visit(And n) {
      System.out.println("And");
      n.e1.accept(this); n.e2.accept(this);
   }

   // Exp e1,e2;
   public void visit(LessThan n) {
      System.out.println("LessThan");
      n.e1.accept(this); n.e2.accept(this);
   }

   // Exp e1,e2;
   public void visit(Plus n) {
      System.out.println("Plus");
      //n.e1.accept(this); n.e2.accept(this);

      Object temp = result;

      if(n.e1 instanceof IntegerLiteral || n.e1 instanceof IdentifierExp) {
         arg1 = n.e1;
      } else {
         arg1 = "t"+(tempVar++);
         result = arg1;
         n.e1.accept(this);
      }

      if(n.e2 instanceof IntegerLiteral || n.e2 instanceof IdentifierExp) {
         arg2 = n.e2;
      } else {
         arg2 = "t"+(tempVar++);
         result = arg2;
         n.e2.accept(this);
      }

      result = temp;
      Quadruple quad = new Quadruple("+", arg1, arg2, result);
      quad.type = ASSIGN;
      IR.add(quad);
   }

   // Exp e1,e2;
   public void visit(Minus n) {
      System.out.println("Minus");
      n.e1.accept(this); n.e2.accept(this);
   }

   // Exp e1,e2;
   public void visit(Times n) {
      System.out.println("Times");
      n.e1.accept(this); n.e2.accept(this);
   }

   // Exp e1,e2;
   public void visit(ArrayLookup n) {
      System.out.println("ArrayLookup");
      n.e1.accept(this); n.e2.accept(this);
   }

   // Exp e;
   public void visit(ArrayLength n) {
      System.out.println("ArrayLength");
      n.e.accept(this);
   }

   // Exp e;
   // Identifier i;
   // ExpList el;
   public void visit(Call n) {
      System.out.println("Call");
      n.e.accept(this);
      n.i.accept(this);
      for(int i = 0; i < n.el.size(); i++) {
         n.el.elementAt(i).accept(this);
      }
   }

   // int i;
   public void visit(IntegerLiteral n) {
      System.out.println("IntegerLiteral");
   }

   public void visit(True n) {
      System.out.println("True");
   }

   public void visit(False n) {
      System.out.println("False");
   }

   // String s;
   public void visit(IdentifierExp n) {
      System.out.println("IdentifierExp");
   }

   public void visit(This n) {
      System.out.println("This");
   }

   // Exp e;
   public void visit(NewArray n) {
      System.out.println("NewArray");
      n.e.accept(this);
   }

   // Identifier i;
   public void visit(NewObject n) {
      System.out.println("NewObject");
      n.i.accept(this);
   }

   // Exp e;
   public void visit(Not n) {
      System.out.println("Not");
      n.e.accept(this);
   }

   // String s;
   public void visit(Identifier n) {
      System.out.println("Identifier");
   }

   public static void main(String args[]) {
      MiniJavaLexer lexer;
      MiniJavaParser parser;
      Symbol parse_tree;
      Program root;
      IRVisitor visitor;

      try {
         lexer = new MiniJavaLexer(new FileReader(args[0]));
         parser = new MiniJavaParser(lexer);

         parse_tree = parser.parse();
         root = (Program) parse_tree.value;

         visitor = new IRVisitor();
         root.accept(visitor);

         System.out.println("\n~~~~~~~~~IR~~~~~~~~~");
         for(Quadruple q : visitor.IR) {
            System.out.println(q);
         }

      } catch (IOException e) {
         System.err.println("Could not open file: " + args[0]);
      } catch (Exception e) {
         e.printStackTrace(System.err);
      }
   }
}