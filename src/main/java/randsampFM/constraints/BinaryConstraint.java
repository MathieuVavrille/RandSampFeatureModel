package randsampFM.constraints;

import randsampFM.types.Feature;

import java.util.HashSet;
import java.util.Set;

public abstract class BinaryConstraint extends Constraint {
  protected final Constraint left;
  protected final Constraint right;

  public BinaryConstraint(final Constraint left, final Constraint right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public Set<Feature> getVariables() {
    Set<Feature> leftVariables = new HashSet(left.getVariables());
    leftVariables.addAll(right.getVariables());
    return leftVariables;
  }
}
