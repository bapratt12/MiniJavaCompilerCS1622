class Test {
   public static void main(String[] args) {
      System.out.println(new Test2().Start(0));
   }
}

class Test2 {
   public int Start(int y) {
      int a;
      int b;
      int c;

      a = 0;
      b = 0;
      c = 0;

      // algebraic simplification
      b = 1 * b;  // removed
      b = b + 0;  // removed
      b = b - 0;  // removed

      a = 1 * b;  // a := b
      a = b + 0;  // a := b
      a = b - 0;  // a := b

      // constant folding
      c = 1 + 1;  // removed
      y = c;      // y := 2

      return y;
   }
}