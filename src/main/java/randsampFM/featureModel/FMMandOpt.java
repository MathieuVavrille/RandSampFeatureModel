package randsampFM.featureModel;

import randsampFM.types.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Random;


public final class FMMandOpt extends FeatureModel {
			
  final List<FeatureModel> mandChildren;
  final List<FeatureModel> optChildren;
	
  public FMMandOpt(final String label, final List<FeatureModel> mandChildren , final List<FeatureModel> optChildren) {
    super(label);
    this.mandChildren = mandChildren;
    this.optChildren = optChildren;
  }
	
  public static FMMandOpt parse(String label, List<de.neominik.uvl.ast.Feature> rawMandChildren , List<de.neominik.uvl.ast.Feature> rawOptChildren) {
    return new FMMandOpt(label, rawMandChildren.stream().map(x -> parseFeatureModel(x)).collect(Collectors.toList()), 
                         rawOptChildren.stream().map(x -> parseFeatureModel(x)).collect(Collectors.toList()));
  }

  @Override
  public Set<Feature> getFeatures() {
    Set<Feature> allFeatures = new HashSet<Feature>();
    for(FeatureModel mandChild : mandChildren)
      allFeatures.addAll(mandChild.getFeatures());
    for(FeatureModel optChild : optChildren)
      allFeatures.addAll(optChild.getFeatures());
    return allFeatures;
  }

  @Override
  public Set<Feature> getChainedMandatoryFeatures() {
    Set<Feature> allMand = new HashSet<Feature>(Set.of(label));
    for(FeatureModel mandChild : mandChildren)
      allMand.addAll(mandChild.getChainedMandatoryFeatures());
    return allMand;
  }

  @Override
  public Triplet<BottomUpCase, FeatureModel, Set<Feature>> fixFeatures(final Set<Feature> forced, final Set<Feature> forbidden) {
    Set<Feature> newlyRemoved = new HashSet<Feature>();
    boolean isUnmodified = true;
    boolean propagateMandatory = false;
    List<FeatureModel> newMandatories = new ArrayList<FeatureModel>();
    List<FeatureModel> newOptionals = new ArrayList<FeatureModel>();
    // Mandatories 
    for (FeatureModel mandChild : mandChildren) {
      Triplet<BottomUpCase, FeatureModel, Set<Feature>> returnData = mandChild.fixFeatures(forced, forbidden);
      switch (returnData.getValue0()) {
      case INCONSISTENT: case EMPTY:
        return Triplet<BottomUpCase, FeatureModel, Set<Feature>>(BottomUpCase.INCONSISTENT, null, null);
      case MANDATORY_UNMODIFIED:
        propagateMandatory = true;
        break;
      case MANDATORY_MODIFIED:
        propagateMandatory = true;
      case MODIFIED: // continuation from MANDATORY_MODIFIED
        isUnmodified = false;
        break;
      }
      newMandatories.add(returnData.getValue1());
      newlyRemoved.addAll(returnData.getValue2());
    }
    // Optionals
    for (FeatureModel optChild : optChildren) {
      Triplet<BottomUpCase, FeatureModel, Set<Feature>> returnData = optChild.fixFeatures(forced, forbidden);

    }
    if (forced.contains(label)) {
      return new Triplet<BottomUpCase, FeatureModel, Set<Feature>>(BottomUpCase.MANDATORY, this, Set.of());
    }
    else if (forbidden.contains(label))
      return new Triplet<BottomUpCase, FeatureModel, Set<Feature>>(BottomUpCase.EMPTY, null, Set.of());
    else
      return new Triplet<BottomUpCase, FeatureModel, Set<Feature>>(BottomUpCase.NOTHING, this, Set.of());
  }

  @Override
  public BigInteger count() {
    if(this.nbConfigurations == null) {
      BigInteger optCount;
      BigInteger mandCount;
      if(optChildren.isEmpty())
        optCount = BigInteger.ONE;
      else
        optCount = optChildren.stream().map(x -> x.count().add(BigInteger.ONE)).reduce((a,b)->a.multiply(b)).get();
      if(mandChildren.isEmpty())
        mandCount = BigInteger.ONE;
      else
        mandCount = mandChildren.stream().map(x -> x.count()).reduce((a,b)->a.multiply(b)).get();
      this.nbConfigurations = mandCount.multiply(optCount);
    }
    return nbConfigurations;
  }
	
  public ConfSet enumerate() {
    ConfSet root = ConfSet.singletonCS(label);
    Stream<ConfSet> mandStream = Stream.empty(); 
    Stream<ConfSet> optStream = Stream.empty();	
    ConfSet result = new ConfSet();
    int nbEmpty = 0;
    // mandChildren and optChildren cannot be simultaneously empty
    if(mandChildren.isEmpty()) {
      result = ConfSet.expansion(optChildren.stream().map(x -> x.enumerate().union(ConfSet.emptyCS())).collect(Collectors.toList()));
      nbEmpty++;
    }
    else {
      mandStream = mandChildren.stream().map(x -> x.enumerate());
    }

    if(optChildren.isEmpty()) {
      result = ConfSet.expansion(mandChildren.stream().map(x -> x.enumerate()).collect(Collectors.toList()));
      nbEmpty++;
    }
    else {
      optStream = optChildren.stream().map(x -> x.enumerate().union(ConfSet.emptyCS()));
    }	
    switch(nbEmpty) {
    case 0:
      ConfSet tempMand = mandStream.reduce(ConfSet.emptyCS(), (a,b)->a.expansion(b));
      ConfSet tempOpt = optStream.reduce(ConfSet.emptyCS(), (a,b)->a.expansion(b));
      result = tempMand.expansion(tempOpt);
      break;
    case 1: 
      break;	
    default: // ~ case 0
      throw new NoSuchElementException("Both mandStream and optStrem cannot be empty");
    }
    return root.expansion(result);
  }
	
  public Conf sample(final Random random) {
    Conf result = new Conf(Set.of(this.label));
    for(FeatureModel fm : mandChildren) {
      result = result.union(fm.sample(random));
    }
    for(FeatureModel fm : optChildren) {
      double bound = (BigDecimal.ONE).divide(new BigDecimal(fm.count().add(BigInteger.ONE)),precision,RoundingMode.HALF_EVEN).doubleValue();
      double draw = random.nextDouble();
      if(bound <= draw) {
        result = result.union(fm.sample(random));
      }
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder("MandOpt(");
    for (FeatureModel mandChild : mandChildren) {
      builder.append(mandChild.toString());
      builder.append(" ");
    }
    builder.append("|");
    for (FeatureModel optChild : optChildren) {
      builder.append(" ");
      builder.append(optChild.toString());
    }
    builder.append(")");
    return builder.toString();
  }

}
