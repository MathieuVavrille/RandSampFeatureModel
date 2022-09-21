package randsampFM.twise;

import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.variables.BoolVar;


public class FailDecision extends Decision<BoolVar> {

  private BoolVar var;
  
  public FailDecision(final BoolVar var) {
    super(1);
    this.var = var;
  }
  
  @Override
  public void apply() throws ContradictionException {
    var.instantiateTo(-1, this);
  }

  @Override
  protected void set(BoolVar var) {
    this.var = var;
  }

  @Override
  public void free() {}

  @Override
  public BoolVar getDecisionValue() {
    return var;
  }
}
