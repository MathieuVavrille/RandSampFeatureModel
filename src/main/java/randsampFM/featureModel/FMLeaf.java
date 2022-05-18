package randsampFM.featureModel;

import randsampFM.types.*;

import org.javatuples.Triplet;

import java.math.BigInteger;
import java.util.Set;
import java.util.Random;

public final class FMLeaf extends FeatureModel {
  
  public FMLeaf(String label) {
    super(label);
  }

  @Override
  public Set<Feature> getFeatures() {
    return Set.of(label);
  }

  @Override
  public Triplet<BottomUpCase, FeatureModel, Set<Feature>> fixFeatures(final Set<Feature> forced, final Set<Feature> forbidden) {
    if (forced.contains(label))
      return new Triplet<BottomUpCase, FeatureModel, Set<Feature>>(BottomUpCase.MANDATORY_UNMODIFIED, this, Set.of());
    else if (forbidden.contains(label))
      return new Triplet<BottomUpCase, FeatureModel, Set<Feature>>(BottomUpCase.EMPTY, null, Set.of());
    else
      return new Triplet<BottomUpCase, FeatureModel, Set<Feature>>(BottomUpCase.UNMODIFIED, this, Set.of());
  }

  @Override
  public BigInteger count() {
    if(this.nbConfigurations == null) {
      this.nbConfigurations = BigInteger.ONE;
    } 
    return nbConfigurations;
  }

  @Override
  public ConfSet enumerate() {
    return ConfSet.singletonCS(this.label);
  }

  @Override
  public Conf sample(final Random random) {
    return new Conf(Set.of(label));
  }

  @Override
  public String toString() {
    return label.toString();
  }
}
