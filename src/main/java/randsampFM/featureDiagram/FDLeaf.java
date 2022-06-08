package randsampFM.featureDiagram;

import randsampFM.types.*;
import randsampFM.constraints.Clause;
import randsampFM.parser.StringIntLink;
import randsampFM.MiniSat;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.BoolVar;

import org.javatuples.Triplet;

import java.math.BigInteger;
import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.Random;

public final class FDLeaf extends FeatureDiagram {
  
  public FDLeaf(final Feature label) {
    super(label);
  }
  
  public FDLeaf(String label) {
    this(new Feature(label));
  }

  @Override
  public Set<Feature> getFeatures() {
    return Set.of(label);
  }

  @Override
  public Triplet<BottomUpCase, FeatureDiagram, Set<Feature>> fixFeatures(final Set<Feature> forced, final Set<Feature> forbidden) {
    if (forced.contains(label))
      return new Triplet<BottomUpCase, FeatureDiagram, Set<Feature>>(BottomUpCase.MANDATORY_UNMODIFIED, this, Set.of());
    else if (forbidden.contains(label))
      return new Triplet<BottomUpCase, FeatureDiagram, Set<Feature>>(BottomUpCase.EMPTY, null, Set.of());
    else
      return new Triplet<BottomUpCase, FeatureDiagram, Set<Feature>>(BottomUpCase.UNMODIFIED, this, Set.of());
  }

  @Override
  public BoolVar addConstraints(final Model model, final Map<Feature,BoolVar> featureToVar) {
    BoolVar mainVar = model.boolVar(label.getName());
    featureToVar.put(label, mainVar);
    return mainVar;
  }

  @Override
  public void addTreeClauses(final List<Clause> clauses, final StringIntLink link) {}


  @Override
  public BigInteger count() {
    if(this.nbConfigurations == null) {
      this.nbConfigurations = BigInteger.ONE;
    } 
    return nbConfigurations;
  }
  
  @Override
  public BigInteger countAssigned(final List<MiniSat.Boolean> assignment, final StringIntLink silink) {
    if (assignment.get(silink.getInt(label.getName())) == MiniSat.Boolean.lFalse)
      return BigInteger.ZERO;
    return BigInteger.ONE;
  }

  @Override
  public ConfSet enumerate() {
    return ConfSet.singletonCS(this.label);
  }

  @Override
  public Configuration sample(final Random random) {
    return new Configuration(Set.of(label));
  }

  @Override
  public String toString() {
    return label.toString();
  }

  @Override
  public String toUVL(final String baseIndentation) {
    return baseIndentation + label + "\n";
  }

  @Override
  public String generateGraphvizEdges() {
    return "";
  }
}
