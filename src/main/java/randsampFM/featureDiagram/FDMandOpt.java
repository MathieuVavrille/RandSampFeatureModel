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
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;


public final class FDMandOpt extends FeatureDiagram {
			
  final List<FeatureDiagram> mandChildren;
  final List<FeatureDiagram> optChildren;
	
  public FDMandOpt(final Feature label, final List<FeatureDiagram> mandChildren , final List<FeatureDiagram> optChildren) {
    super(label);
    this.mandChildren = mandChildren;
    this.optChildren = optChildren;
  }
	
  public FDMandOpt(final String label, final List<FeatureDiagram> mandChildren , final List<FeatureDiagram> optChildren) {
    super(label);
    this.mandChildren = mandChildren;
    this.optChildren = optChildren;
  }
	
  public static FDMandOpt parse(String label, List<de.neominik.uvl.ast.Feature> rawMandChildren , List<de.neominik.uvl.ast.Feature> rawOptChildren) {
    return new FDMandOpt(label, rawMandChildren.stream().map(x -> parse(x)).collect(Collectors.toList()), 
                         rawOptChildren.stream().map(x -> parse(x)).collect(Collectors.toList()));
  }

  @Override
  public Set<Feature> getFeatures() {
    Set<Feature> allFeatures = new HashSet<Feature>();
    for(FeatureDiagram mandChild : mandChildren)
      allFeatures.addAll(mandChild.getFeatures());
    for(FeatureDiagram optChild : optChildren)
      allFeatures.addAll(optChild.getFeatures());
    return allFeatures;
  }

  @Override
  public Set<Feature> getChainedMandatoryFeatures() {
    Set<Feature> allMand = new HashSet<Feature>(Set.of(label));
    for(FeatureDiagram mandChild : mandChildren)
      allMand.addAll(mandChild.getChainedMandatoryFeatures());
    return allMand;
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
    else {
      Set<Feature> newlyRemoved = new HashSet<Feature>();
      boolean isUnmodified = true; // when the FD has been modified
      boolean propagateMandatory = forced.contains(label); // when we have to propagate the mandatory
      boolean isEmpty = false; // when a mandatory feature is empty (i.e. false)
      List<FeatureDiagram> newMandatories = new ArrayList<FeatureDiagram>();
      List<FeatureDiagram> newOptionals = new ArrayList<FeatureDiagram>();
      // Mandatories 
      for (FeatureDiagram mandChild : mandChildren) {
        Triplet<BottomUpCase, FeatureDiagram, Set<Feature>> returnData = mandChild.fixFeatures(forced, forbidden);
        switch (returnData.getValue0()) {
        case INCONSISTENT:
          return new Triplet<BottomUpCase, FeatureDiagram, Set<Feature>>(BottomUpCase.INCONSISTENT, null, null);
        case EMPTY:
          isEmpty = true;
          break;
        case MANDATORY_UNMODIFIED:
          propagateMandatory = true;
          break;
        case MANDATORY_MODIFIED:
          propagateMandatory = true;
          isUnmodified = false;
          break;
        case MODIFIED:
          isUnmodified = false;
          break;
        case UNMODIFIED:
          break;
        }
        newMandatories.add(returnData.getValue1());
        newlyRemoved.addAll(returnData.getValue2());
      }
      // Optionals
      for (FeatureDiagram optChild : optChildren) {
        Triplet<BottomUpCase, FeatureDiagram, Set<Feature>> returnData = optChild.fixFeatures(forced, forbidden);
        switch (returnData.getValue0()) {
        case INCONSISTENT:
          return new Triplet<BottomUpCase, FeatureDiagram, Set<Feature>>(BottomUpCase.INCONSISTENT, null, null);
        case EMPTY:
          isUnmodified = false;
          break;
        case MANDATORY_MODIFIED: case MANDATORY_UNMODIFIED:
          propagateMandatory = true;
          isUnmodified = false;
          newMandatories.add(returnData.getValue1());
          break;
        case MODIFIED:
          isUnmodified = false;
        case UNMODIFIED:// Continuation from MODIFIED
          newOptionals.add(returnData.getValue1());
          break;
        }
        newlyRemoved.addAll(returnData.getValue2());
      }
      if (isUnmodified) {
        return new Triplet<BottomUpCase, FeatureDiagram, Set<Feature>>(propagateMandatory ? BottomUpCase.MANDATORY_UNMODIFIED : BottomUpCase.UNMODIFIED, this, newlyRemoved);
      }
      else if (isEmpty) {
        for (FeatureDiagram mandChild : mandChildren)
          newlyRemoved.addAll(mandChild.getFeatures());
        newlyRemoved.removeAll(forbidden); // remove
        return new Triplet<BottomUpCase, FeatureDiagram, Set<Feature>>(BottomUpCase.EMPTY, null, newlyRemoved);
      }
      else {
        if (newMandatories.size() == 0 && newOptionals.size() == 0)
          return new Triplet<BottomUpCase, FeatureDiagram, Set<Feature>>(propagateMandatory ? BottomUpCase.MANDATORY_MODIFIED : BottomUpCase.MODIFIED, new FDLeaf(label), newlyRemoved);
        else
          return new Triplet<BottomUpCase, FeatureDiagram, Set<Feature>>(propagateMandatory ? BottomUpCase.MANDATORY_MODIFIED : BottomUpCase.MODIFIED, new FDMandOpt(label, newMandatories, newOptionals), newlyRemoved);
      }
    }
  }

  @Override
  public BoolVar addConstraints(final Model model, final Map<Feature,BoolVar> featureToVar) {
    BoolVar mainVar = model.boolVar(label.getName());
    featureToVar.put(label, mainVar);
    for (FeatureDiagram mandChild : mandChildren) {
      BoolVar mandVar = mandChild.addConstraints(model, featureToVar);
      mandVar.eq(mainVar).post();
    }
    for (FeatureDiagram optChild : optChildren) {
      BoolVar optVar = optChild.addConstraints(model, featureToVar);
      optVar.le(mainVar).post();
    }
    return mainVar;
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
	
  public Configuration sample(final Random random) {
    Configuration result = new Configuration(Set.of(this.label));
    for(FeatureDiagram fm : mandChildren) {
      result = result.union(fm.sample(random));
    }
    for(FeatureDiagram fm : optChildren) {
      double bound = (BigDecimal.ONE).divide(new BigDecimal(fm.count().add(BigInteger.ONE)),PRECISION,RoundingMode.HALF_EVEN).doubleValue();
      double draw = random.nextDouble();
      if(bound <= draw) {
        result = result.union(fm.sample(random));
      }
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder(label.getName() + "-MandOpt(");
    for (FeatureDiagram mandChild : mandChildren) {
      builder.append(mandChild.toString());
      builder.append(" ");
    }
    builder.append("|");
    for (FeatureDiagram optChild : optChildren) {
      builder.append(" ");
      builder.append(optChild.toString());
    }
    builder.append(")");
    return builder.toString();
  }

  @Override
  public String toUVL(final String baseIndentation) {
    StringBuilder builder = new StringBuilder(baseIndentation + label + "\n");
    builder.append("  " + baseIndentation + "mandatory" + "\n");
    for (FeatureDiagram mand : mandChildren)
      builder.append(mand.toUVL("    "+baseIndentation));
    builder.append("  " + baseIndentation + "optional" + "\n");
    for (FeatureDiagram opt : optChildren)
      builder.append(opt.toUVL("    "+baseIndentation));
    return builder.toString();
  }

  @Override
  public String generateGraphvizEdges() {
    StringBuilder builder = new StringBuilder(label.getName() + "[shape=square];\n");
    for (FeatureDiagram mandChild : mandChildren) {
      builder.append(label.getName() + " -> " + mandChild.label.getName() + " [arrowhead=dot];\n");
      builder.append(mandChild.generateGraphvizEdges());
    }
    for (FeatureDiagram optChild : optChildren) {
      builder.append(label.getName() + " -> " + optChild.label.getName() + " [arrowhead=odot];\n");
      builder.append(optChild.generateGraphvizEdges());
    }
    return builder.toString();
  }

}
