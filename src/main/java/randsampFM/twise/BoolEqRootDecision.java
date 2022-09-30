package randsampFM.twise;

import randsampFM.types.Feature;

import org.chocosolver.memory.IStateBool;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.variables.BoolVar;

import org.javatuples.Pair;

import java.util.Map;

public class BoolEqRootDecision extends Decision<BoolVar> {

  private final BoolVar var;
  private final boolean value;

  private final Feature feature;
  private final FeatureWiseCombinations impossibleCombinations;

  private final IStateBool isFirstDecision;
  
  public BoolEqRootDecision(final BoolVar var, final boolean value, final IStateBool isFirstDecision, final Feature feature, final FeatureWiseCombinations impossibleCombinations) {
    super(2);
    this.var = var;
    this.value = value;
    this.isFirstDecision = isFirstDecision;
    this.feature = feature;
    this.impossibleCombinations = impossibleCombinations;
  }

  @Override
  public Boolean getDecisionValue() {
    return value;
  }

  @Override
  public void apply() throws ContradictionException {
    if (branch == 1) {
      var.getModel().getSolver().getEventObserver().pushDecisionLevel();
      if (value)
        var.setToTrue(this);
      else
        var.setToFalse(this);
      isFirstDecision.set(false);
    } else if (branch == 2) {
      var.eq(value ? 0 : 1).post();
      impossibleCombinations.set(feature, value);
      isFirstDecision.set(true);
    }
  }

  @Override
  protected void set(BoolVar var) {
    throw new IllegalStateException("Cannot reuse this decision");
  }

  @Override
  public void free() {}
}
