package randsampFM.constraints;

import randsampFM.types.*;

import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.expression.discrete.relational.ReExpression;

import org.javatuples.Pair;

import java.util.Set;
import java.util.Map;

public class NotCstr extends CrossConstraint {
  private final CrossConstraint child;
  
  private NotCstr(final CrossConstraint child) {
    this.child = child;
  }

  public static CrossConstraint of(final CrossConstraint child) {
    if (child instanceof TrueCstr)
      return new FalseCstr();
    else if (child instanceof FalseCstr)
      return new TrueCstr();
    else if (child instanceof NotCstr)
      return ((NotCstr) child).child;
    else
      return new NotCstr(child);
  }

  @Override
  public Pair<Boolean,CrossConstraint> fixVariable(final Set<Feature> forced, final Set<Feature> forbidden) {
    Pair<Boolean, CrossConstraint> fix = child.fixVariable(forced,forbidden);
    return new Pair<Boolean, CrossConstraint>(fix.getValue0(), NotCstr.of(fix.getValue1()));
  }

  @Override
  public boolean isSatisfied(final Configuration configuration) {
    return !child.isSatisfied(configuration);
  }

  @Override
  public ReExpression getCPConstraint(final Map<Feature,BoolVar> featureToVar) {
    return child.getCPConstraint(featureToVar).not();
  }

  @Override
  public Pair<Set<Feature>,Set<Feature>> forcedFeaturesForTrue() {
    return child.forcedFeaturesForFalse();
  }
  @Override
  public Pair<Set<Feature>,Set<Feature>> forcedFeaturesForFalse() {
    return child.forcedFeaturesForTrue();
  }

  @Override
  public Set<Feature> getVariables() {
    return child.getVariables();
  }

  @Override
  public String toString() {
    return "NOT("+child.toString()+")";
  }
}
