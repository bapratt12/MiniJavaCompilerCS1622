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

   // optimize
   private boolean opt;

   // constructor
   public IRVisitor(boolean optFlag) {
      opt = optFlag;

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
      n.m.accept(this);
      for(int i = 0; i < n.cl.size(); i++) {
         n.cl.elementAt(i).accept(this);
      }
   }

   // Identifier i1,i2;
   // Statement s;
   public void visit(MainClass n) {
      labels.put(0, "main");
      IR.add(new Quadruple("LABEL", "", "", "main:"));
      inStatic = true;
      n.i1.accept(this); n.i2.accept(this);
      n.s.accept(this);
      IR.add(new Quadruple("END MAIN", "", "", ""));
      inStatic = false;
   }

   // Identifier i;
   // VarDeclList vl;
   // MethodDeclList ml;
   public void visit(ClassDeclSimple n) {
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
      n.t.accept(this);
      n.i.accept(this);
      labels.put(IR.size(), n.i.s);
      IR.add(new Quadruple("LABEL", "", "", n.i.s + ":"));
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
      n.t.accept(this);
      n.i.accept(this);
   }

   public void visit(IntArrayType n) {
   }

   public void visit(BooleanType n) {
   }

   public void visit(IntegerType n) {
   }

   // String s;
   public void visit(IdentifierType n) {
   }

   // StatementList sl;
   public void visit(Block n) {
      for(int i = 0; i < n.sl.size(); i++) {
         n.sl.elementAt(i).accept(this);
      }
   }

   // Exp e;
   // Statement s1,s2;
   public void visit(If n) {
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
      if(n.e instanceof IntegerLiteral
         || n.e instanceof True || n.e instanceof False) {
         IR.add(new Quadruple("PARAM", "", "", n.e));
      } else {
         Quadruple q = new Quadruple("PARAM", "", "", "t"+tempVar);
         quad = new Quadruple("", "", "", "t"+(tempVar++));
         n.e.accept(this);
         IR.add(q);
      }
      IR.add(new Quadruple("CALL", "_system_out_println", "1", ""));   }

   // Identifier i;
   // Exp e;
   public void visit(Assign n) {
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
      quad.op = "length";
      quad.arg1 = n.e;
      IR.add(quad);
   }

   // Exp e;
   // Identifier i;
   // ExpList el;
   public void visit(Call n) {
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
   }

   public void visit(True n) {
   }

   public void visit(False n) {
   }

   // String s;
   public void visit(IdentifierExp n) {
   }

   public void visit(This n) {
      quad.result = "THIS";
      IR.add(quad);
   }

   // Exp e;
   public void visit(NewArray n) {
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
   }

   // Exp e;
   public void visit(Not n) {
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
   }

   // generates IR and labels
   // optimizes if that's what you're into
   public void start(Program root) {
      // accept root node
      root.accept(this);

      // optimize
      if(opt) {
         // keep a list of instructions to be removed
         ArrayList<Quadruple> toRemove = new ArrayList<>();
         Map<Integer, Integer> moveLabels = new HashMap<>();

         // check each instruction
         for(int i = 0; i < IR.size(); i++) {
            Quadruple q = IR.get(i);
            
            /*
             * constant folding
             *
             * if a binary operation is performed on two constants,
             * compute the value now to aovid doing so at runtime
             */
            boolean arg1Constant = q.arg1 instanceof IntegerLiteral;
            boolean arg2Constant = q.arg2 instanceof IntegerLiteral;

            Object name = null;
            Object value = null;
            if(arg1Constant && arg2Constant) {
               if(q.op.equals("+")) {
                  q.op = ":=";
                  q.arg1 = ((IntegerLiteral)q.arg1).i + ((IntegerLiteral)q.arg2).i;
                  q.arg2 = "";

                  name = (q.result instanceof Identifier) ? ((Identifier)q.result).s : q.result;
                  value = q.arg1;
               } else if(q.op.equals("-")) {
                  q.op = ":=";
                  q.arg1 = ((IntegerLiteral)q.arg1).i - ((IntegerLiteral)q.arg2).i;
                  q.arg2 = "";

                  name = (q.result instanceof Identifier) ? ((Identifier)q.result).s : q.result;
                  value = q.arg1;
               } else if(q.op.equals("*")) {
                  q.op = ":=";
                  q.arg1 = ((IntegerLiteral)q.arg1).i * ((IntegerLiteral)q.arg2).i;
                  q.arg2 = "";

                  name = (q.result instanceof Identifier) ? ((Identifier)q.result).s : q.result;
                  value = q.arg1;
               } else if(q.op.equals("<")) {
                  q.op = ":=";
                  q.arg1 = ((IntegerLiteral)q.arg1).i < ((IntegerLiteral)q.arg2).i;
                  q.arg2 = "";

                  name = (q.result instanceof Identifier) ? ((Identifier)q.result).s : q.result;
                  value = q.arg1;
               }

               // if a constant expression has been encountered, remove it
               if(name != null) {
                  toRemove.add(q);

                  for(int line : labels.keySet()) {
                     if(line >= i) {
                        if(!moveLabels.containsKey(line)) {
                           moveLabels.put(line, 0);
                        }
                        moveLabels.put(line, moveLabels.get(line)+1);
                     }
                  }
               }

               // replace this variable with its value in all future uses until next assignment
               for(int j = i+1; j < IR.size(); j++) {
                  Quadruple n = IR.get(j);

                  Object r = (n.result instanceof Identifier) ? ((Identifier)n.result).s : n.result;
                  if(r.equals(name)) {
                     break;
                  }

                  Object a1 = (n.arg1 instanceof IdentifierExp) ? ((IdentifierExp)n.arg1).s : n.arg1;
                  if(a1.equals(name)) {
                     n.arg1 = value;
                  }

                  Object a2 = (n.arg2 instanceof IdentifierExp) ? ((IdentifierExp)n.arg2).s : n.arg2;
                  if(a2.equals(name)) {
                     n.arg2 = value;
                  }
               }
            }

            /*
             * algebraic simplification
             *
             * alter or remove any algebraic operations that will have no affect
             */
            Object constant = null;
            Object variable = null;
            if(arg1Constant && !arg2Constant) {
               constant = q.arg1;
               variable = q.arg2;
            } else if(!arg1Constant && arg2Constant) {
               constant = q.arg2;
               variable = q.arg1;
            }

            if(constant != null) {
               // track if a change has been made
               boolean changed = false;
               if(((IntegerLiteral)constant).i == 1 && q.op.equals("*")) {
                  // x * 1 || 1 * x
                  q.op = ":=";
                  constant = "";
                  q.arg1 = variable;
                  changed = true;
               } else if(((IntegerLiteral)constant).i == 0 && q.op.equals("+")) {
                  // x + 0 || 0 + x
                  q.op = ":=";
                  constant = "";
                  q.arg1 = variable;
                  changed = true;
               } else if(((IntegerLiteral)constant).i == 0 && q.op.equals("-")) {
                  // x - 0
                  q.op = ":=";
                  constant = "";
                  q.arg1 = variable;
                  changed = true;
               }

               // if the instruction is now x = x, remove it
               // must also fix all labels
               if(changed) {
                  String a1 = "";
                  if(q.arg1 instanceof Identifier) {
                     a1 = ((Identifier)q.arg1).s;
                  } else if(q.arg1 instanceof IdentifierExp) {
                     a1 = ((IdentifierExp)q.arg1).s;
                  }

                  String r = "";
                  if(q.result instanceof Identifier) {
                     r = ((Identifier)q.result).s;
                  } else if(q.result instanceof IdentifierExp) {
                     r = ((IdentifierExp)q.result).s;
                  }

                  if(a1.equals(r)) {
                     toRemove.add(q);

                     for(int line : labels.keySet()) {
                        if(line >= i) {
                           if(!moveLabels.containsKey(line)) {
                              moveLabels.put(line, 0);
                           }
                           moveLabels.put(line, moveLabels.get(line)+1);
                        }
                     }
                  }
               }
            }
         }

         // remove instructions
         for(int i = 0; i < toRemove.size(); i++) {
            IR.remove(toRemove.get(i));
         }

         // fix labels
         for(int i : moveLabels.keySet()) {
            int newLine = i - moveLabels.get(i);
            String label = labels.get(i);

            labels.remove(i);
            labels.put(newLine, label);
         }
      }
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

         boolean opt = false;
         if(args.length > 1 && args[1].equals("-O1")) {
            opt = true;
         }

         visitor = new IRVisitor(opt);
         visitor.start(root);

         System.out.println();
         for(int i = 0; i < visitor.IR.size(); i++) {
            String label = visitor.labels.get(i);

            if(label != null) {
               System.out.format("%6s: %s\n", label, visitor.IR.get(i));
            } else {
               System.out.println("        " + visitor.IR.get(i));
            }
         }
         System.out.println();

      } catch (IOException e) {
         System.err.println("Could not open file: " + args[0]);
      } catch (Exception e) {
         e.printStackTrace(System.err);
      }
   }
}