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
  public Pair<Boolean,Constraint> fixVariable(final Feature feature, final boolean value) {
    Pair<Boolean, Constraint> leftFix = left.fixVariable(feature, value);
    Pair<Boolean, Constraint> rightFix = right.fixVariable(feature, value);
    return new Pair<Boolean, Constraint>(leftFix.getValue0() || rightFix.getValue0(), AndCstr.of(leftFix.getValue1(),rightFix.getValue1()));
  }

  @Override
  public String toString() {
    return "AND("+left.toString()+","+right.toString()+")";
  }
}
