package difftest;


import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorShuffle;


public class Diff1DVecOps {
  public static double[] diff1d(double[] data) {
    final int len = data.length;
    if (len < 2) return new double[0];
    final int rlen = len - 1;
    final double[] rval = new double[rlen];
    final VectorSpecies<Double> species = DoubleVector.SPECIES_PREFERRED;
    final int vecLen = species.length();
    final int nVecGroups = rlen / vecLen;
    final int leftover = rlen % vecLen;
    final int endOffset = nVecGroups * vecLen;
    if(nVecGroups > 0) {
      for(int gidx = 0; gidx < nVecGroups; ++gidx) {
	final int goff = gidx * vecLen;
	DoubleVector.fromArray(species, data, goff + 1)
	  .sub(DoubleVector.fromArray(species, data, goff))
	  .intoArray(rval,goff);
      }
    }
    for(int idx = endOffset; idx < rlen; ++idx ) {
      rval[idx] = data[idx+1] - data[idx];
    }
    return rval;
  }
}
