package randsampFM.twise;

import randsampFM.types.Feature;

import org.chocosolver.memory.IStateBool;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.variables.BoolVar;

import org.javatuples.Pair;

import java.util.Map;

public class IntEqRootDecision extends Decision<BoolVar> {

  private final BoolVar var;
  private final boolean value;

  private final Feature feature;
  private final Map<Feature, Pair<CombinationStatus, CombinationStatus>> combinations;

  private final IStateBool isFirstDecision;
  
  public IntEqRootDecision(final BoolVar var, final boolean value, final IStateBool isFirstDecision, final Feature feature, final Map<Feature, Pair<CombinationStatus, CombinationStatus>> combinations) {
    super(2);
    this.var = var;
    this.value = value;
    this.isFirstDecision = isFirstDecision;
    this.feature = feature;
    this.combinations = combinations;
  }

  @Override
  public Integer getDecisionValue() {
    return value ? 1 : 0;
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
      if (value) {
        var.eq(0).post();
        CombinationStatus other = combinations.get(feature).getValue0();
        combinations.put(feature, new Pair<CombinationStatus, CombinationStatus>(other, CombinationStatus.IMPOSSIBLE));
      }
      else {
        var.eq(1).post();
        CombinationStatus other = combinations.get(feature).getValue0();
        combinations.put(feature, new Pair<CombinationStatus, CombinationStatus>(CombinationStatus.IMPOSSIBLE, other));
      }
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
