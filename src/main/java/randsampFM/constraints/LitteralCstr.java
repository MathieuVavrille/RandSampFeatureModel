package randsampFM.constraints;

import randsampFM.types.*;

import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.expression.discrete.relational.ReExpression;

import org.javatuples.Pair;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class LitteralCstr extends CrossConstraint {
  private final Feature litteral;

  public LitteralCstr(final Feature litteral) {
    this.litteral = litteral;
  }

  @Override
  public Pair<Boolean,CrossConstraint> fixVariable(final Set<Feature> forced, final Set<Feature> forbidden) {
    if (forced.contains(litteral))
      return new Pair<Boolean, CrossConstraint>(true, new TrueCstr());
    else if (forbidden.contains(litteral))
      return new Pair<Boolean, CrossConstraint>(true, new FalseCstr());
    else
      return new Pair<Boolean, CrossConstraint>(false, this);
  }

  @Override
  public Set<Feature> getVariables() {
    return Set.of(litteral);
  }

  @Override
  public boolean isSatisfied(final Configuration configuration) {
    return configuration.contains(litteral);
  }

  @Override
  public ReExpression getCPConstraint(final Map<Feature,BoolVar> featureToVar) {
    return featureToVar.get(litteral);
  }

  @Override
  public Pair<Set<Feature>,Set<Feature>> forcedFeaturesForTrue() {
    return new Pair<Set<Feature>,Set<Feature>>(Set.of(litteral),Set.of());
  }
  @Override
  public Pair<Set<Feature>,Set<Feature>> forcedFeaturesForFalse() {
    return new Pair<Set<Feature>,Set<Feature>>(Set.of(),Set.of(litteral));
  }

  @Override
  public String toString() {
    return litteral.toString();
  }
}
