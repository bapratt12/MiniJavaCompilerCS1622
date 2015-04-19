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
        x = 1+1;
        j = 4 - 3;
        y = x + j;
        if(y < 10){
            y = x;
        }
        else {
            y = j;
        }
        return y;
    }
}