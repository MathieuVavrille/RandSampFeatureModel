package randsampFM.constraints;

import randsampFM.types.*;

import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.expression.discrete.relational.ReExpression;

import org.javatuples.Pair;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;

public class AndCstr extends BinaryCrossConstraint {
  
  private AndCstr(final CrossConstraint left, final CrossConstraint right) {
    super(left,right);
  }

  public static CrossConstraint of(final CrossConstraint left, final CrossConstraint right) {
    if (left instanceof FalseCstr || right instanceof FalseCstr)
      return new FalseCstr();
    else if (left instanceof TrueCstr)
      return right;
    else if (right instanceof TrueCstr)
      return left;
    return new AndCstr(left,right);
  }

  @Override
  public Pair<Boolean,CrossConstraint> fixVariable(final Set<Feature> forced, final Set<Feature> forbidden) {
    Pair<Boolean, CrossConstraint> leftFix = left.fixVariable(forced,forbidden);
    Pair<Boolean, CrossConstraint> rightFix = right.fixVariable(forced,forbidden);
    return new Pair<Boolean, CrossConstraint>(leftFix.getValue0() || rightFix.getValue0(), AndCstr.of(leftFix.getValue1(),rightFix.getValue1()));
  }

  @Override
  public boolean isSatisfied(final Configuration configuration) {
    return left.isSatisfied(configuration) && right.isSatisfied(configuration);
  }

  @Override
  public ReExpression getCPConstraint(final Map<Feature,BoolVar> featureToVar) {
    return left.getCPConstraint(featureToVar).and(right.getCPConstraint(featureToVar));
  }

  @Override
  public Pair<Set<Feature>,Set<Feature>> forcedFeaturesForTrue() {
    Pair<Set<Feature>,Set<Feature>> leftFeatures = left.forcedFeaturesForTrue();
    Pair<Set<Feature>,Set<Feature>> rightFeatures = right.forcedFeaturesForTrue();
    Set<Feature> leftForced = new HashSet<Feature>(leftFeatures.getValue0());
    leftForced.addAll(rightFeatures.getValue0());
    Set<Feature> leftForbidden = new HashSet<Feature>(leftFeatures.getValue1());
    leftForbidden.addAll(rightFeatures.getValue1());
    return new Pair<Set<Feature>,Set<Feature>>(leftForced, leftForbidden);
  }
  @Override
  public Pair<Set<Feature>,Set<Feature>> forcedFeaturesForFalse() {
    return new Pair<Set<Feature>,Set<Feature>>(Set.of(),Set.of());
  }

  @Override
  public String toString() {
    return "AND("+left.toString()+","+right.toString()+")";
  }
}
