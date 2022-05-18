package randsampFM.featureModel;

import randsampFM.types.Conf;
import randsampFM.types.ConfSet;
import randsampFM.types.Feature;

import java.util.Set;
import java.util.HashSet;
import java.util.Random;
import java.util.List;
import java.math.BigInteger;

public class FMCard extends FeatureModel{

  // TEMPORARY CLASS : TODO TOTALLY WRONG
  //private int lowerBound;
  //private int upperBound; 
  //private List<FeatureModel> children;
	
	
  public FMCard(String label, List<de.neominik.uvl.ast.Feature> rawChildren, int lb, int ub) {
    super(label);
    throw new UnsupportedOperationException("Cardinality is not yet implemented");
    //lowerBound = lb;
    //upperBound = ub;
    //this.children = rawChildren.stream().map(x -> parseFeatureModel(x,generator)).collect(Collectors.toList());
  }

  @Override
  public Set<Feature> getFeatures() {
    Set<Feature> allFeatures = new HashSet<Feature>();
    /*for(FeatureModel child : children)
      allFeatures.addAll(child.getFeatures);*/
    return allFeatures;
  }

  @Override
  public BigInteger count() {
    return BigInteger.ONE;
  }

  @Override
  public ConfSet enumerate() {
    return ConfSet.singletonCS(new Feature("TODO"));
  }

  @Override
  public Conf sample(final Random random) {
    return new Conf();
  }
	
}
