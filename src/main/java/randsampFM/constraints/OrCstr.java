package randsampFM.constraints;

import randsampFM.types.Feature;

import org.javatuples.Pair;

import java.util.Set;

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
  public Pair<Boolean,Constraint> fixVariable(final Feature feature) {
    Pair<Boolean, Constraint> leftFix = left.fixVariable(feature);
    Pair<Boolean, Constraint> rightFix = right.fixVariable(feature);
    return new Pair<Boolean, Constraint>(leftFix.getValue0() || rightFix.getValue0(), OrCstr.of(leftFix.getValue1(),rightFix.getValue1()));
  }

  @Override
  public String toString() {
    return "OR("+left.toString()+","+right.toString()+")";
  }
}
