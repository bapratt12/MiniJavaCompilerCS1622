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
      int x;
      int y;
      int z ;
      boolean q;

      // algebraic simplification
      x = 1 * x;  // removed
      x = x + 0;  // removed
      x = x - 0;  // removed

      y = 1 * x;  // y := x
      y = x + 0;  // y := x
      y = x - 0;  // y := x

      // constant folding
      x = 1 + 1;  // removed
      y = x;      // y := 2

      x = 0;
      while(x < 5) {
         x = x + 1;
      }

      return y;
   }
}