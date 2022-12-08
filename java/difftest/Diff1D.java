package difftest;


public class Diff1D {
  public static double[] diff1d(double[] input) {
    final int len = input.length;
    if (len < 2) return new double[0];
    double lval = input[0];
    final int rlen = len - 1;
    final double[] retval = new double[rlen];
    for(int idx = 0; idx < rlen; ++idx) {
      final double ii = input[idx+1];
      retval[idx] = ii - lval;
      lval = ii;
    }
    return retval;
  }
}
