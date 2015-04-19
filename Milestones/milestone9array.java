class Test {
   public static void main(String[] args) {
      System.out.println(new Test2().Start(10));
   }
}

class Test2 {
   public int Start(int y) {
      int[] x;
      int[] y;
      int i;

      x = new int[10];
      y = new int[10];

      i = 5;

      x[i] = x.length;
      y[i] = x[i];

      return y[i];
   }
}