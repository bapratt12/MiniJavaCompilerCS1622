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
import java.util.Map;
import java.util.HashMap;

import java_cup.runtime.Symbol;

import syntaxtree.*;
import visitor.Visitor;

public class IRVisitor implements Visitor {

   // quadruple parameters
   String op;
   Object arg1, arg2, result;
   public Quadruple quad;

   // temporary register number
   public int tempVar;

   // intermediate representation
   public ArrayList<Quadruple> IR;

   // labels
   public Map<Integer, String> labels;
   public int labelVar;

   // include 'this' as a param when calling from a non-static method
   private boolean inStatic = false;

   // constructor
   public IRVisitor() {
      tempVar = 0;
      IR = new ArrayList<Quadruple>();
      labels = new HashMap<Integer, String>();
      labelVar = 0;
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
      System.out.println("program");
      n.m.accept(this);
      for(int i = 0; i < n.cl.size(); i++) {
         n.cl.elementAt(i).accept(this);
      }
   }

   // Identifier i1,i2;
   // Statement s;
   public void visit(MainClass n) {
      System.out.println("mainclass");
      labels.put(0, "main");
      inStatic = true;
      n.i1.accept(this); n.i2.accept(this);
      n.s.accept(this);
      inStatic = false;
   }

   // Identifier i;
   // VarDeclList vl;
   // MethodDeclList ml;
   public void visit(ClassDeclSimple n) {
      System.out.println("classdeclsimple");
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
      System.out.println("classdeclextends");
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
      System.out.println("vardecl");
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
      System.out.println("methoddecl");
      n.t.accept(this);
      n.i.accept(this);
      labels.put(IR.size(), n.i.s);
      for(int i = 0; i < n.fl.size(); i++) {
         n.fl.elementAt(i).accept(this);
      }
      for(int i = 0; i < n.vl.size(); i++) {
         n.vl.elementAt(i).accept(this);
      }
      for(int i = 0; i < n.sl.size(); i++) {
         n.sl.elementAt(i).accept(this);
      }

      // return value
      if(n.e instanceof IdentifierExp || n.e instanceof IntegerLiteral
         || n.e instanceof True || n.e instanceof False) {
         IR.add(new Quadruple("RETURN", "", "", n.e));
      } else {
         int temp = tempVar;
         quad = new Quadruple("", "", "", "t"+(tempVar++));
         n.e.accept(this);
         IR.add(new Quadruple("RETURN", "", "", "t"+temp));
      }
   }

   // Type t;
   // Identifier i;
   public void visit(Formal n) {
      System.out.println("formal");
      n.t.accept(this);
      n.i.accept(this);
   }

   public void visit(IntArrayType n) {
      System.out.println("intarraytype");
   }

   public void visit(BooleanType n) {
      System.out.println("booleantype");
   }

   public void visit(IntegerType n) {
      System.out.println("integertype");
   }

   // String s;
   public void visit(IdentifierType n) {
      System.out.println("identifiertype");
   }

   // StatementList sl;
   public void visit(Block n) {
      System.out.println("block");
      for(int i = 0; i < n.sl.size(); i++) {
         n.sl.elementAt(i).accept(this);
      }
   }

   // Exp e;
   // Statement s1,s2;
   public void visit(If n) {
      System.out.println("if");

      String labelFalse = "L"+(labelVar++);
      if(n.e instanceof IdentifierExp || n.e instanceof True || n.e instanceof False) {
         IR.add(new Quadruple("IFFALSE", n.e, "", labelFalse));
      } else {
         Quadruple q = new Quadruple("IFFALSE", "t"+tempVar, "", labelFalse);
         quad = new Quadruple("", "", "", "t"+(tempVar++));
         n.e.accept(this);
         IR.add(q);
      }

      n.s1.accept(this);
      String labelTrue = "L"+(labelVar++);
      IR.add(new Quadruple("GOTO", "", "", labelTrue));

      labels.put(IR.size(), labelFalse);
      n.s2.accept(this);
      labels.put(IR.size(), labelTrue);
   }

   // Exp e;
   // Statement s;
   public void visit(While n) {
      System.out.println("while");
      
      String labelLoop = "L"+(labelVar++);
      labels.put(IR.size(), labelLoop);
      Quadruple q;
      if(n.e instanceof IdentifierExp || n.e instanceof True || n.e instanceof False) {
         q = new Quadruple("IFFALSE", n.e, "", "");
         IR.add(q);
      } else {
         q = new Quadruple("IFFALSE", "t"+tempVar, "", "");
         quad = new Quadruple("", "", "", "t"+(tempVar++));
         n.e.accept(this);
         IR.add(q);
      }

      n.s.accept(this);
      String labelBreak = "L"+(labelVar++);
      q.result = labelBreak;

      IR.add(new Quadruple("GOTO", "", "", labelLoop));
      labels.put(IR.size(), labelBreak);
   }

   // Exp e;
   public void visit(Print n) {
      System.out.println("print");

      if(n.e instanceof IntegerLiteral
         || n.e instanceof True || n.e instanceof False) {
         IR.add(new Quadruple("PARAM", "", "", n.e));
      } else {
         Quadruple q = new Quadruple("PARAM", "", "", "t"+tempVar);
         quad = new Quadruple("", "", "", "t"+(tempVar++));
         n.e.accept(this);
         IR.add(q);
      }
      IR.add(new Quadruple("CALL", "print", "1", ""));
   }

   // Identifier i;
   // Exp e;
   public void visit(Assign n) {
      System.out.println("assign");

      quad = new Quadruple("", "", "", n.i);
      if(n.e instanceof IdentifierExp || n.e instanceof IntegerLiteral 
         || n.e instanceof True || n.e instanceof False) {
         quad.op = ":=";
         quad.arg1 = n.e;
         IR.add(quad);
      } else {
         n.e.accept(this);
      }
   }

   // Identifier i;
   // Exp e1,e2;
   public void visit(ArrayAssign n) {
      System.out.println("arrayassign");

      Quadruple q = new Quadruple("[]=", "", "", n.i);

      if(n.e1 instanceof IdentifierExp || n.e1 instanceof IntegerLiteral) {
         q.arg1 = n.e1;
      } else {
         q.arg1 = "t"+tempVar;
         quad = new Quadruple("", "", "", "t"+(tempVar++));
         n.e1.accept(this);
      }

      if(n.e2 instanceof IdentifierExp || n.e2 instanceof IntegerLiteral) {
         q.arg2 = n.e2;
      } else {
         q.arg2 = "t"+tempVar;
         quad = new Quadruple("", "", "", "t"+(tempVar++));
         n.e2.accept(this);
      }

      IR.add(q);
   }

   // Exp e1,e2;
   public void visit(And n) {
      System.out.println("and");
      Quadruple q = quad;
      q.op = "&&";

      if(n.e1 instanceof IdentifierExp || n.e1 instanceof IntegerLiteral
         || n.e1 instanceof True || n.e1 instanceof False) {
         q.arg1 = n.e1;
      } else {
         q.arg1 = "t"+tempVar;
         quad = new Quadruple("", "", "", "t"+(tempVar++));
         n.e1.accept(this);
      }

      if(n.e2 instanceof IdentifierExp || n.e2 instanceof IntegerLiteral
         || n.e2 instanceof True || n.e2 instanceof False) {
         q.arg2 = n.e2;
      } else {
         q.arg2 = "t"+tempVar;
         quad = new Quadruple("", "", "", "t"+(tempVar++));
         n.e2.accept(this);
      }

      IR.add(q);
   }

   // Exp e1,e2;
   public void visit(LessThan n) {
      System.out.println("lessthan");
      Quadruple q = quad;
      q.op = "<";

      if(n.e1 instanceof IdentifierExp || n.e1 instanceof IntegerLiteral) {
         q.arg1 = n.e1;
      } else {
         q.arg1 = "t"+tempVar;
         quad = new Quadruple("", "", "", "t"+(tempVar++));
         n.e1.accept(this);
      }

      if(n.e2 instanceof IdentifierExp || n.e2 instanceof IntegerLiteral) {
         q.arg2 = n.e2;
      } else {
         q.arg2 = "t"+tempVar;
         quad = new Quadruple("", "", "", "t"+(tempVar++));
         n.e2.accept(this);
      }

      IR.add(q);
   }

   // Exp e1,e2;
   public void visit(Plus n) {
      System.out.println("plus");
      Quadruple q = quad;
      q.op = "+";

      if(n.e1 instanceof IdentifierExp || n.e1 instanceof IntegerLiteral) {
         q.arg1 = n.e1;
      } else {
         q.arg1 = "t"+tempVar;
         quad = new Quadruple("", "", "", "t"+(tempVar++));
         n.e1.accept(this);
      }

      if(n.e2 instanceof IdentifierExp || n.e2 instanceof IntegerLiteral) {
         q.arg2 = n.e2;
      } else {
         q.arg2 = "t"+tempVar;
         quad = new Quadruple("", "", "", "t"+(tempVar++));
         n.e2.accept(this);
      }

      IR.add(q);
   }

   // Exp e1,e2;
   public void visit(Minus n) {
      System.out.println("minus");
      Quadruple q = quad;
      q.op = "-";

      if(n.e1 instanceof IdentifierExp || n.e1 instanceof IntegerLiteral) {
         q.arg1 = n.e1;
      } else {
         q.arg1 = "t"+tempVar;
         quad = new Quadruple("", "", "", "t"+(tempVar++));
         n.e1.accept(this);
      }

      if(n.e2 instanceof IdentifierExp || n.e2 instanceof IntegerLiteral) {
         q.arg2 = n.e2;
      } else {
         q.arg2 = "t"+tempVar;
         quad = new Quadruple("", "", "", "t"+(tempVar++));
         n.e2.accept(this);
      }

      IR.add(q);
   }

   // Exp e1,e2;
   public void visit(Times n) {
      System.out.println("times");
      Quadruple q = quad;
      q.op = "*";

      if(n.e1 instanceof IdentifierExp || n.e1 instanceof IntegerLiteral) {
         q.arg1 = n.e1;
      } else {
         q.arg1 = "t"+tempVar;
         quad = new Quadruple("", "", "", "t"+(tempVar++));
         n.e1.accept(this);
      }

      if(n.e2 instanceof IdentifierExp || n.e2 instanceof IntegerLiteral) {
         q.arg2 = n.e2;
      } else {
         q.arg2 = "t"+tempVar;
         quad = new Quadruple("", "", "", "t"+(tempVar++));
         n.e2.accept(this);
      }

      IR.add(q);
   }

   // Exp e1,e2;
   public void visit(ArrayLookup n) {
      System.out.println("arraylookup");

      Quadruple q = quad;
      q.op = "=[]";
      if(n.e1 instanceof IdentifierExp) {
         q.arg1 = n.e1;
      }

      if(n.e2 instanceof IdentifierExp || n.e2 instanceof IntegerLiteral) {
         q.arg2 = n.e2;
      } else {
         q.arg2 = "t"+tempVar;
         quad = new Quadruple("", "", "", "t"+(tempVar++));
         n.e2.accept(this);
      }

      IR.add(q);
   }

   // Exp e;
   public void visit(ArrayLength n) {
      System.out.println("arraylength");

      quad.op = "length";
      quad.arg1 = n.e;
      IR.add(quad);
   }

   // Exp e;
   // Identifier i;
   // ExpList el;
   public void visit(Call n) {
      System.out.println("call");

      Quadruple q = quad;
      q.op = "CALL";
      q.arg1 = n.i;
      q.arg2 = n.el.size();

      for(int i = 0; i < n.el.size(); i++) {
         Object exp = n.el.elementAt(i);
         if(exp instanceof IdentifierExp || exp instanceof IntegerLiteral
            || exp instanceof True || exp instanceof False) {
            IR.add(new Quadruple("PARAM", "", "", exp));
         } else {
            int temp = tempVar;
            quad = new Quadruple("", "", "", "t"+(tempVar++));
            n.el.elementAt(i).accept(this);
            IR.add(new Quadruple("PARAM", "", "", "t"+temp));
         }
      }

      IR.add(q);
   }

   // int i;
   public void visit(IntegerLiteral n) {
      System.out.println("integerliteral");
   }

   public void visit(True n) {
      System.out.println("true");
   }

   public void visit(False n) {
      System.out.println("false");
   }

   // String s;
   public void visit(IdentifierExp n) {
      System.out.println("identifierexp");
   }

   public void visit(This n) {
      System.out.println("this");

      quad.result = "THIS";
      IR.add(quad);
   }

   // Exp e;
   public void visit(NewArray n) {
      System.out.println("newarray");

      Quadruple q = quad;
      q.op = "NEW";
      q.arg1 = "int";

      if(n.e instanceof IntegerLiteral || n.e instanceof IdentifierExp) {
         q.arg2 = n.e;
      } else {
         q.arg2 = "t"+tempVar;
         quad = new Quadruple("", "", "", "t"+(tempVar++));
         n.e.accept(this);
      }

      IR.add(q);
   }

   // Identifier i;
   public void visit(NewObject n) {
      System.out.println("newobject");
   }

   // Exp e;
   public void visit(Not n) {
      System.out.println("not");
      Quadruple q = quad;
      q.op = "!";

      if(n.e instanceof IdentifierExp || n.e instanceof IntegerLiteral
         || n.e instanceof True || n.e instanceof False) {
         q.arg1 = n.e;
      } else {
         q.arg1 = "t"+tempVar;
         quad = new Quadruple("", "", "", "t"+(tempVar++));
         n.e.accept(this);
      }

      IR.add(q);
   }

   // String s;
   public void visit(Identifier n) {
      System.out.println("identifier");
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
         for(int i = 0; i < visitor.IR.size(); i++) {
            String label = visitor.labels.get(i);

            if(label != null) {
               System.out.format("%6s: %s\n", label, visitor.IR.get(i));
            } else {
               System.out.println("        " + visitor.IR.get(i));
            }
         }

      } catch (IOException e) {
         System.err.println("Could not open file: " + args[0]);
      } catch (Exception e) {
         e.printStackTrace(System.err);
      }
   }
}