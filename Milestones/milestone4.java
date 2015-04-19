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
        j = new Test2().Hello(5);  // 7
        y = x + j;  // 9
        return y;   // return 9
    }
    
    public int Hello(int x){
        x = x + 2;
        return x;
    }
}