package randsampFM.constraints;

import randsampFM.types.Feature;

import org.javatuples.Pair;

import java.util.Set;
import java.util.HashSet;

public class OrCstr extends BinaryConstraint {
  
  private OrCstr(final Constraint left, final Constraint right) {
    super(left,right);
  }

  public static Constraint of(final Constraint left, final Constraint right) {
    if (left instanceof TrueCstr || right instanceof TrueCstr)
      return new TrueCstr();
    else if (left instanceof FalseCstr)
      return right;
    else if (right instanceof FalseCstr)
      return left;
    else
      return new OrCstr(left, right);
  }

  @Override
  public Pair<Boolean,Constraint> fixVariable(final Set<Feature> forced, final Set<Feature> forbidden) {
    Pair<Boolean, Constraint> leftFix = left.fixVariable(forced,forbidden);
    Pair<Boolean, Constraint> rightFix = right.fixVariable(forced,forbidden);
    return new Pair<Boolean, Constraint>(leftFix.getValue0() || rightFix.getValue0(), OrCstr.of(leftFix.getValue1(),rightFix.getValue1()));
  }

  @Override
  public Pair<Set<Feature>,Set<Feature>> forcedFeaturesForTrue() {
    return new Pair<Set<Feature>,Set<Feature>>(Set.of(),Set.of());
  }
  @Override
  public Pair<Set<Feature>,Set<Feature>> forcedFeaturesForFalse() {
    Pair<Set<Feature>,Set<Feature>> leftFeatures = left.forcedFeaturesForFalse();
    Pair<Set<Feature>,Set<Feature>> rightFeatures = right.forcedFeaturesForFalse();
    Set<Feature> leftForced = new HashSet<Feature>(leftFeatures.getValue0());
    leftForced.addAll(rightFeatures.getValue0());
    Set<Feature> leftForbidden = new HashSet<Feature>(leftFeatures.getValue1());
    leftForbidden.addAll(rightFeatures.getValue1());
    return new Pair<Set<Feature>,Set<Feature>>(leftForced, leftForbidden);
  }

  @Override
  public String toString() {
    return "OR("+left.toString()+","+right.toString()+")";
  }
}
