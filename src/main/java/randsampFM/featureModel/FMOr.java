package randsampFM.featureModel;

import java.util.stream.Collectors;

import randsampFM.types.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Random;

public class FMOr extends FeatureModel {

  final List<FeatureModel> children;
  
  public FMOr(final String label, final List<FeatureModel> children) {
    super(label);
    this.children = children;
  }
  
  public static FMOr parse(String label, List<de.neominik.uvl.ast.Feature> rawChildren) {
    return new FMOr(label, rawChildren.stream().map(x -> parseFeatureModel(x)).collect(Collectors.toList()));
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
      nbConfigurations = children.stream().map(x->x.count().add(BigInteger.ONE)).reduce(BigInteger.ONE, (a,b)-> a.multiply(b)).subtract(BigInteger.ONE);
    }
    return nbConfigurations;
  }

  @Override
  public ConfSet enumerate() {
    Conf rootConf = new Conf(Set.of(this.label)); 
    ConfSet root = new ConfSet(Set.of(rootConf));
    ConfSet result = root.expansion(children.stream().map(x -> x.enumerate().union(ConfSet.emptyCS())).reduce(ConfSet.emptyCS(),(a,b) -> a.expansion(b)));
    return result.without(rootConf);
  }

  @Override
  public Conf sample(final Random random) {
    double draw; 
    double bound;
    Conf result = new Conf();
    while(result.isEmpty()) {
      for(FeatureModel fm : children) {
        bound = (BigDecimal.ONE).divide(new BigDecimal(fm.count().add(BigInteger.ONE)),precision,RoundingMode.HALF_EVEN).doubleValue();
        draw = random.nextDouble();
        if(bound <= draw) {
          result = result.union(fm.sample(random));
        }
      }
    }
    return result.union(new Conf(Set.of(this.label)));
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
