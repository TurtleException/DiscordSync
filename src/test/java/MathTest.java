public class MathTest {
    public static void main(String[] args) {
        test(87);
        test(265);
        test(100);
        test(1);
        test(0);
        test(-1);
        test(2000000000);
    }

    private static void test(int n) {
        System.out.println("Result of " + n + " is " + ceilPow10(n));
    }

    private static int ceilPow10(int n) {
        return (int) Math.pow(10, (int) Math.log10(n - 1) + 1);
    }
}
