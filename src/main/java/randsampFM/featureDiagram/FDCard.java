package randsampFM.featureDiagram;

import randsampFM.types.*;
import randsampFM.constraints.Clause;
import randsampFM.parser.StringIntLink;

import org.javatuples.Triplet;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.BoolVar;

import java.util.Map;
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
    throw new UnsupportedOperationException("Not Implemented");
    //Set<Feature> allFeatures = new HashSet<Feature>();
    /*for(FeatureDiagram child : children)
      allFeatures.addAll(child.getFeatures);*/
    //return allFeatures;
  }
  
  @Override
  public Triplet<BottomUpCase, FeatureDiagram, Set<Feature>> fixFeatures(final Set<Feature> forced, final Set<Feature> forbidden) {
    throw new UnsupportedOperationException("Not Implemented");
    //return null;
  }

  @Override
  public BoolVar addConstraints(final Model model, final Map<Feature,BoolVar> featureToVar) {
    throw new UnsupportedOperationException("Not Implemented");
  }
  @Override
  public void addTreeClauses(final List<Clause> clauses, final StringIntLink link) {
    throw new UnsupportedOperationException("Not Implemented");
  }

  @Override
  public BigInteger count() {
    throw new UnsupportedOperationException("Not Implemented");
    //return BigInteger.ONE;
  }

  @Override
  public ConfSet enumerate() {
    throw new UnsupportedOperationException("Not Implemented");
    //return ConfSet.singletonCS(new Feature("TODO"));
  }

  @Override
  public Configuration sample(final Random random) {
    throw new UnsupportedOperationException("Not Implemented");
    //return new Configuration();
  }

  @Override
  public String generateGraphvizEdges() {
    throw new UnsupportedOperationException("Not Implemented");
  }

  @Override
  public String toUVL(final String indentation) {
    throw new UnsupportedOperationException("Not Implemented");
  }
}
