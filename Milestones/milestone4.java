class Test {
    public static void main(String[] args) {
        System.out.println(new Test2().Start(0));
    }
}
class Test2 {
    public int Start(int y) {
        //Arbitrary straight-line code
        int x;
        int j;
        x = 1+1;    // 2
        j = 4 - x;  // 2
        y = x + j;  // 4
        return y;   // return 4
    }
}