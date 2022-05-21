package randsampFM.splittedFM;

import randsampFM.types.*;
import randsampFM.featureDiagram.FeatureDiagram;
import randsampFM.FMSampleCountEnum;

import java.util.List;
import java.util.Random;
import java.math.BigInteger;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class SplittedFDList implements FMSampleCountEnum {

  private final List<FeatureDiagram> splittedFDs;
  private BigInteger nbConfigurations = null;

  public SplittedFDList(final List<FeatureDiagram> splittedFDs) {
    this.splittedFDs = splittedFDs;
  }

  
  public BigInteger count() {
    if (nbConfigurations == null)
      nbConfigurations = splittedFDs.stream().map(x->x.count()).reduce(BigInteger.ZERO, (a,b)-> a.add(b)); // Same as counting a XOR
    return nbConfigurations;
  }
  
  public ConfSet enumerate() {
    return splittedFDs.stream().map(x -> x.enumerate()).reduce(new ConfSet(),(a,b) -> a.union(b));
  }
  
  public Configuration sample(final Random random) {
    double r = random.nextDouble();
    BigDecimal nbConf = new BigDecimal(this.count());
    int i = 0;
    while(r >= 0) {
      double p = new BigDecimal(splittedFDs.get(i).count()).divide(nbConf,PRECISION,RoundingMode.HALF_EVEN).doubleValue();
      r -= p;
      i++;
    }
    return splittedFDs.get(i-1).sample(random);
  }
  
}
