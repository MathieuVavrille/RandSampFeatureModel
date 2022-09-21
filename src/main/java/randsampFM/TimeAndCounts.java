package randsampFM;

import randsampFM.types.Feature;
import java.util.Map;
import java.math.BigInteger;

public class TimeAndCounts {

  public final long weightTime;
  public final long parseTime;
  public final BigInteger count;
  public final Map<Feature,BigInteger> leafCounts;

  public TimeAndCounts(final long weightTime, final long parseTime, final BigInteger count, final Map<Feature,BigInteger> leafCounts) {
    this.weightTime = weightTime;
    this.parseTime = parseTime;
    this.count = count;
    this.leafCounts = leafCounts;
  }
}
