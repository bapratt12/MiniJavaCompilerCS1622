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

   private String findName(Object n) {
      if(n instanceof IntegerLiteral) {
         return ""+((IntegerLiteral)n).i;
      } else if(n instanceof Identifier) {
         return ((Identifier)n).s;
      } else if(n instanceof IdentifierExp) {
         return ((IdentifierExp)n).s;
      } else {
         return n.toString();
      }
   }

   public String toString() {
      String str = "";
      String a1 = findName(arg1);
      String a2 = findName(arg2);
      String r = findName(result);

      if(op.equals("&&") || op.equals("<") || op.equals("+") || op.equals("-") || op.equals("*")) {
         str = r + " := " + a1 + " " + op + " " + a2;
      } else if(op.equals("CALL")) {
         str = r + " := " + op + " " + a1 + ", " + a2;
      } else if(op.equals("PARAM")) {
         str = op + " " + r;
      }

      return str;
   }
}