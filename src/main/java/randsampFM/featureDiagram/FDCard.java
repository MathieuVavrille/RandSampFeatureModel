package randsampFM.featureDiagram;

import randsampFM.types.*;

import org.javatuples.Triplet;

import java.util.Set;
import java.util.HashSet;
import java.util.Random;
import java.util.List;
import java.math.BigInteger;

public class FDCard extends FeatureDiagram {

  // TEMPORARY CLASS : TODO TOTALLY WRONG
  //private int lowerBound;
  //private int upperBound; 
  //private List<FeatureDiagram> children;
	
	
  public FDCard(String label, List<de.neominik.uvl.ast.Feature> rawChildren, int lb, int ub) {
    super(label);
    throw new UnsupportedOperationException("Cardinality is not yet implemented");
    //lowerBound = lb;
    //upperBound = ub;
    //this.children = rawChildren.stream().map(x -> parseFeatureDiagram(x,generator)).collect(Collectors.toList());
  }

  @Override
  public Set<Feature> getFeatures() {
    Set<Feature> allFeatures = new HashSet<Feature>();
    /*for(FeatureDiagram child : children)
      allFeatures.addAll(child.getFeatures);*/
    return allFeatures;
  }
  
  @Override
  public Triplet<BottomUpCase, FeatureDiagram, Set<Feature>> fixFeatures(final Set<Feature> forced, final Set<Feature> forbidden) {
    // TODO implement
    return null;
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
