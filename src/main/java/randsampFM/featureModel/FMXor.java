package randsampFM.featureModel;

import randsampFM.types.*;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.math.BigInteger;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

public final class FMXor extends FeatureModel{

  final List<FeatureModel> children;
  
  public FMXor(final String label, final List<FeatureModel> children) {
    super(label);
    this.children = children;
  }
	
  public static FMXor parse(String label, List<de.neominik.uvl.ast.Feature> rawChildren) {
    return new FMXor(label, rawChildren.stream().map(x -> parseFeatureModel(x)).collect(Collectors.toList()));
  }

  @Override
  public Set<Feature> getFeatures() {
    Set<Feature> allFeatures = new HashSet<Feature>();
    for(FeatureModel child : children)
      allFeatures.addAll(child.getFeatures());
    return allFeatures;
  }
  
  @Override
  public BigInteger count() {
    if(this.nbConfigurations == null) {
      nbConfigurations = children.stream().map(x->x.count()).reduce(BigInteger.ZERO, (a,b)-> a.add(b));
    }
    return nbConfigurations;
  }
  
  @Override
  public ConfSet enumerate() {
    ConfSet root = ConfSet.singletonCS(this.label);
    return root.expansion(children.stream().map(x -> x.enumerate()).reduce(new ConfSet(),(a,b) -> a.union(b)));
  }
	
  @Override
  public Conf sample(final Random random) {
    Conf result = new Conf(Set.of(this.label));
    /* a, b BigDecimal
     * a.divide(b, scale, rounding method)
     * */
    result = result.union(this.choose(random).sample(random));
    return result;
  }
	
  private FeatureModel choose(final Random random){
    double r = random.nextDouble();
    BigDecimal nbConf = new BigDecimal(this.count());
    Object[] childs = children.stream().map(x -> new BigDecimal(x.count())).toArray();
    int i = 0;
    BigDecimal child;
    while(r >= 0) {
      child = (BigDecimal) childs[i];
      double p = child.divide(nbConf,precision,RoundingMode.HALF_EVEN).doubleValue();
      r -= p;
      i++;
    }
    return children.get(i-1);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder("OR(");
    for (FeatureModel child : children) {
      builder.append(child.toString());
      builder.append(" ");
    }
    builder.append(")");
    return builder.toString();
  }
}
