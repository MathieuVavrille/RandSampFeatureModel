package randsampFM.splittedFM;

import randsampFM.types.*;
import randsampFM.featureDiagram.FeatureDiagram;
import randsampFM.FMSampleCountEnum;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import java.util.Comparator;
import java.math.BigInteger;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class SplittedFDList implements FMSampleCountEnum {

  private final List<FeatureDiagram> splittedFDs;
  private BigInteger nbConfigurations = null;

  public SplittedFDList(final List<FeatureDiagram> splittedFDs) {
    this.splittedFDs = splittedFDs;
    this.splittedFDs.sort(new Comparator<FeatureDiagram>() {
        @Override
        public int compare(FeatureDiagram a, FeatureDiagram b) {
          return b.count().compareTo(a.count());
        }
      });
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

  public void saveGraphvizToFolder(final String folderName) {
    int cpt = 0;
    for (FeatureDiagram fd : splittedFDs) {
      fd.saveGraphvizToFile(folderName + "/out-"+(cpt++)+".dot");
      System.out.println(cpt-1 + " " + fd.count());
    }
  }

  public Map<BigInteger,Integer> countsRepartition() {
    Map<BigInteger,Integer> counts = new HashMap<BigInteger,Integer>();
    for (FeatureDiagram fd : splittedFDs) {
      BigInteger c = fd.count();
      counts.put(c, counts.getOrDefault(c,0) + 1);
    }
    return counts;
  }

  public FeatureDiagram get(final int id) {
    return splittedFDs.get(id);
  }

  public int size() {
    return splittedFDs.size();
  }

  public List<FeatureDiagram> getFDs() {
    return splittedFDs;
  }

  @Override
  public String toString() {
    return splittedFDs.toString();
  }
}
