package randsampFM.constraints;

import randsampFM.types.Feature;

import org.javatuples.Pair;

import java.util.Set;

public class AndCstr extends BinaryConstraint {
  
  private AndCstr(final Constraint left, final Constraint right) {
    super(left,right);
  }

  public static Constraint of(final Constraint left, final Constraint right) {
    if (left instanceof FalseCstr || right instanceof FalseCstr)
      return new FalseCstr();
    else if (left instanceof TrueCstr)
      return right;
    else if (right instanceof TrueCstr)
      return left;
    return new AndCstr(left,right);
  }

  @Override
  public Pair<Boolean,Constraint> fixVariable(final Set<Feature> forced, final Set<Feature> forbidden) {
    Pair<Boolean, Constraint> leftFix = left.fixVariable(forced,forbidden);
    Pair<Boolean, Constraint> rightFix = right.fixVariable(forced,forbidden);
    return new Pair<Boolean, Constraint>(leftFix.getValue0() || rightFix.getValue0(), AndCstr.of(leftFix.getValue1(),rightFix.getValue1()));
  }

  @Override
  public Pair<Set<Feature>,Set<Feature>> forcedFeaturesForTrue() {
    Pair<Set<Feature>,Set<Feature>> leftFeatures = left.forcedFeaturesForTrue();
    Pair<Set<Feature>,Set<Feature>> rightFeatures = right.forcedFeaturesForTrue();
    Set<Feature> leftForced = new HashSet<Feature>(leftFeatures.getValue0());
    leftForced.addAll(rightFeatures.getValue0());
    Set<Feature> leftForbidden = new HashSet<Feature>(leftFeatures.getValue1());
    leftForbidden.addAll(rightFeatures.getValue1());
    return Pair<Set<Feature>,Set<Feature>>(leftForced, leftForbidden);
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
