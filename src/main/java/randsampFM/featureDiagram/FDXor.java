package randsampFM.featureDiagram;

import randsampFM.types.*;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.BoolVar;

import org.javatuples.Triplet;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.math.BigInteger;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

public final class FDXor extends FeatureDiagram{

  final List<FeatureDiagram> children;
  
  public FDXor(final Feature label, final List<FeatureDiagram> children) {
    super(label);
    this.children = children;
  }
  
  public FDXor(final String label, final List<FeatureDiagram> children) {
    this(new Feature(label),children);
  }
	
  public static FDXor parse(String label, List<de.neominik.uvl.ast.Feature> rawChildren) {
    return new FDXor(label, rawChildren.stream().map(x -> parse(x)).collect(Collectors.toList()));
  }

  @Override
  public Set<Feature> getFeatures() {
    Set<Feature> allFeatures = new HashSet<Feature>();
    for(FeatureDiagram child : children)
      allFeatures.addAll(child.getFeatures());
    return allFeatures;
  }

  @Override
  public Triplet<BottomUpCase, FeatureDiagram, Set<Feature>> fixFeatures(final Set<Feature> forced, final Set<Feature> forbidden) {
    if (forbidden.contains(label)) {
      Set<Feature> newlyRemoved = new HashSet<Feature>(this.getFeatures());
      newlyRemoved.removeAll(forbidden);
      Set<Feature> newlyCopy = new HashSet<Feature>(newlyRemoved);
      newlyCopy.retainAll(forced);
      if (newlyCopy.size() > 0)
        return new Triplet<BottomUpCase, FeatureDiagram, Set<Feature>>(BottomUpCase.INCONSISTENT, null, null);
      else
        return new Triplet<BottomUpCase, FeatureDiagram, Set<Feature>>(BottomUpCase.EMPTY, null, newlyRemoved);
    }
    Set<Feature> newlyRemoved = new HashSet<Feature>();
    boolean isUnmodified = true; // when the FD has been modified
    FeatureDiagram mandatoryFD = null; // Store the only possible mandatory
    List<FeatureDiagram> newXors = new ArrayList<FeatureDiagram>(); // The new list of children
    for (FeatureDiagram child : children) {
      Triplet<BottomUpCase, FeatureDiagram, Set<Feature>> returnData = child.fixFeatures(forced, forbidden);
      switch (returnData.getValue0()) {
      case INCONSISTENT:
        return new Triplet<BottomUpCase, FeatureDiagram, Set<Feature>>(BottomUpCase.INCONSISTENT, null, null);
      case EMPTY: // just don't add the FD to the list of children
        isUnmodified = false;
        break;
      case MANDATORY_MODIFIED: case MANDATORY_UNMODIFIED:
        isUnmodified = false;
        if (mandatoryFD == null)
          mandatoryFD = returnData.getValue1();
        else // Two mandatory features on a XOR
          return new Triplet<BottomUpCase, FeatureDiagram, Set<Feature>>(BottomUpCase.INCONSISTENT, null, null);
        break;
      case MODIFIED:
        isUnmodified = false;
      case UNMODIFIED: // continued
        newXors.add(returnData.getValue1());
        break;
      }
      newlyRemoved.addAll(returnData.getValue2());
    }
    if (mandatoryFD == null && newXors.size() == 1) // if last Xor then it is a mandatory
      mandatoryFD = newXors.remove(0);
    if (mandatoryFD == null) {
      if (newXors.size() == 0)
        return new Triplet<BottomUpCase, FeatureDiagram, Set<Feature>>(BottomUpCase.EMPTY, null, newlyRemoved);
      else if (isUnmodified)
        return new Triplet<BottomUpCase, FeatureDiagram, Set<Feature>>(forced.contains(label) ? BottomUpCase.MANDATORY_UNMODIFIED : BottomUpCase.UNMODIFIED, this, newlyRemoved);
      else // modified
        return new Triplet<BottomUpCase, FeatureDiagram, Set<Feature>>(forced.contains(label) ? BottomUpCase.MANDATORY_MODIFIED : BottomUpCase.MODIFIED, new FDXor(label, newXors), newlyRemoved);
    }
    else {
      for (FeatureDiagram newXor : newXors)
        newlyRemoved.addAll(newXor.getFeatures());
      newlyRemoved.removeAll(forbidden);
      return new Triplet<BottomUpCase, FeatureDiagram, Set<Feature>>(BottomUpCase.MANDATORY_MODIFIED, new FDMandOpt(label, List.of(mandatoryFD), List.of()), newlyRemoved);
    }
  }

  @Override
  public BoolVar addConstraints(final Model model, final Map<Feature,BoolVar> featureToVar) {
    BoolVar mainVar = model.boolVar(label.getName());
    featureToVar.put(label, mainVar);
    BoolVar[] childrenVars = new BoolVar[children.size()];
    for (int i = 0; i < children.size(); i++) {
      childrenVars[i] = children.get(i).addConstraints(model, featureToVar);
    }
    model.sum(childrenVars, "=", mainVar).post();
    return mainVar;
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
  public Configuration sample(final Random random) {
    Configuration result = new Configuration(Set.of(this.label));
    result = result.union(this.choose(random).sample(random));
    return result;
  }
	
  private FeatureDiagram choose(final Random random){
    double r = random.nextDouble();
    BigDecimal nbConf = new BigDecimal(this.count());
    Object[] childs = children.stream().map(x -> new BigDecimal(x.count())).toArray();
    int i = 0;
    BigDecimal child;
    while(r >= 0) {
      child = (BigDecimal) childs[i];
      double p = child.divide(nbConf,PRECISION,RoundingMode.HALF_EVEN).doubleValue();
      r -= p;
      i++;
    }
    return children.get(i-1);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder(label.getName()+"-XOR(");
    for (FeatureDiagram child : children) {
      builder.append(child.toString());
      builder.append(" ");
    }
    builder.append(")");
    return builder.toString();
  }

  @Override
  public String generateGraphvizEdges() {
    StringBuilder builder = new StringBuilder(label.getName() + "[shape=diamond];\n");
    for (FeatureDiagram child : children) {
      builder.append(label.getName() + " -> " + child.label.getName() + " [arrowhead=none];\n");
      builder.append(child.generateGraphvizEdges());
    }
    return builder.toString();
  }
}
