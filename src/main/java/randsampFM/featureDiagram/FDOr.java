package randsampFM.featureDiagram;

import randsampFM.types.*;

import org.javatuples.Triplet;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Random;
import java.util.stream.Collectors;

public class FDOr extends FeatureDiagram {

  final List<FeatureDiagram> children;
  
  public FDOr(final Feature label, final List<FeatureDiagram> children) {
    super(label);
    this.children = children;
  }
  
  public FDOr(final String label, final List<FeatureDiagram> children) {
    this(new Feature(label), children);
  }
  
  public static FDOr parse(String label, List<de.neominik.uvl.ast.Feature> rawChildren) {
    return new FDOr(label, rawChildren.stream().map(x -> parse(x)).collect(Collectors.toList()));
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
    List<FeatureDiagram> newMandatories = new ArrayList<FeatureDiagram>();
    List<FeatureDiagram> newOptionals = new ArrayList<FeatureDiagram>();
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
        newMandatories.add(returnData.getValue1());
        break;
      case MODIFIED:
        isUnmodified = false;
      case UNMODIFIED: // continued
        newOptionals.add(returnData.getValue1());
        break;
      }
      newlyRemoved.addAll(returnData.getValue2());
    }
    if (newMandatories.size() == 0 && newOptionals.size() == 0) // No more children
      return new Triplet<BottomUpCase, FeatureDiagram, Set<Feature>>(BottomUpCase.EMPTY, null, newlyRemoved);
    if (newMandatories.size() == 0 && newOptionals.size() == 1) // Make the last Or mandatory
      newMandatories.add(newOptionals.remove(0));
    if (newMandatories.size() == 0) { // then newOptionals.size() >= 2
      if (isUnmodified)
        return new Triplet<BottomUpCase, FeatureDiagram, Set<Feature>>(BottomUpCase.UNMODIFIED, this, newlyRemoved);
      else
        return new Triplet<BottomUpCase, FeatureDiagram, Set<Feature>>(BottomUpCase.MODIFIED, new FDOr(label, newOptionals), newlyRemoved);
    }
    else { // Necessarily modified and mandOpt, and there is at least one mand, so the OR is valid
      return new Triplet<BottomUpCase, FeatureDiagram, Set<Feature>>(BottomUpCase.MANDATORY_MODIFIED, new FDMandOpt(label, newMandatories, newOptionals), newlyRemoved);
    }
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
      for(FeatureDiagram fm : children) {
        bound = (BigDecimal.ONE).divide(new BigDecimal(fm.count().add(BigInteger.ONE)),PRECISION,RoundingMode.HALF_EVEN).doubleValue();
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
    StringBuilder builder = new StringBuilder(label.getName() + "-OR(");
    for (FeatureDiagram child : children) {
      builder.append(child.toString());
      builder.append(" ");
    }
    builder.append(")");
    return builder.toString();
  }
}
