/**
 * @author Collin Barth
 *
 * Quadruple.java
 * represents an IR statement in three-address code
 */

import syntaxtree.*;

public class Quadruple {
   int type;

   String op;
   Object arg1;
   Object arg2;
   Object result;

   public Quadruple(String op, Object a1, Object a2, Object r) {
      this.op = op;
      this.arg1 = a1;
      this.arg2 = a2;
      this.result = r;
   }

   public String toString() {
      String str = "";

      String a1, a2, r;

      if(arg1 instanceof IntegerLiteral) {
         a1 = Integer.toString(((IntegerLiteral)arg1).i);
      } else if(arg1 instanceof IdentifierExp) {
         a1 = ((IdentifierExp)arg1).s;
      } else {
         a1 = (String)arg1;
      }

      if(arg2 instanceof IntegerLiteral) {
         a2 = Integer.toString(((IntegerLiteral)arg2).i);
      } else if(arg2 instanceof IdentifierExp) {
         a2 = ((IdentifierExp)arg2).s;
      } else {
         a2 = (String)arg2;
      }

      if(result instanceof Identifier) {
         r = ((Identifier)result).s;
      } else {
         r = (String)result;
      }

      if(type == IRVisitor.ASSIGN) {
         str = r + " := " + a1 + " " + op + " " + a2;
      }

      return str;

      //return "{ " + op + " " + arg1 + " " + arg2 + " " + result + " }";

      //return str;
   }
}