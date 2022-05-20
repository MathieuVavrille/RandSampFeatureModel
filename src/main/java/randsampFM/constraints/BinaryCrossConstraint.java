package randsampFM.constraints;

import randsampFM.types.Feature;

import java.util.HashSet;
import java.util.Set;

public abstract class BinaryCrossConstraint extends CrossConstraint {
  protected final CrossConstraint left;
  protected final CrossConstraint right;

  public BinaryCrossConstraint(final CrossConstraint left, final CrossConstraint right) {
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
