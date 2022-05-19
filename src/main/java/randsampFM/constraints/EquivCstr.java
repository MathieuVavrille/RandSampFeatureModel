package randsampFM.constraints;

import randsampFM.types.Feature;

import org.javatuples.Pair;

import java.util.Set;

/**
00 | 1
01 | 0
10 | 0
11 | 1
*/
public class EquivCstr extends BinaryConstraint {
  
  private EquivCstr(final Constraint left, final Constraint right) {
    super(left,right);
  }

  public static Constraint of(final Constraint left, final Constraint right) {
    if (left instanceof TrueCstr)
      return right;
    else if (right instanceof TrueCstr)
      return left;
    else if (left instanceof FalseCstr)
      return NotCstr.of(right);
    else if (right instanceof FalseCstr)
      return NotCstr.of(left);
    return new EquivCstr(left, right);
  }

  @Override
  public Pair<Boolean,Constraint> fixVariable(final Set<Feature> forced, final Set<Feature> forbidden) {
    Pair<Boolean, Constraint> leftFix = left.fixVariable(forced,forbidden);
    Pair<Boolean, Constraint> rightFix = right.fixVariable(forced,forbidden);
    return new Pair<Boolean, Constraint>(leftFix.getValue0() || rightFix.getValue0(), EquivCstr.of(leftFix.getValue1(),rightFix.getValue1()));
  }

  @Override
  public Pair<Set<Feature>,Set<Feature>> forcedFeaturesForTrue() {
    return new Pair<Set<Feature>,Set<Feature>>(Set.of(),Set.of());
  }
  @Override
  public Pair<Set<Feature>,Set<Feature>> forcedFeaturesForFalse() {
    return new Pair<Set<Feature>,Set<Feature>>(Set.of(),Set.of());
  }

  @Override
  public String toString() {
    return "EQUIV("+left.toString()+","+right.toString()+")";
  }
}
