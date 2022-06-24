package randsampFM.constraints;

import randsampFM.types.*;
import randsampFM.parser.StringIntLink;

import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.expression.discrete.relational.ReExpression;

import org.javatuples.Pair;

import java.util.List;
import java.util.Set;
import java.util.Map;

public class FalseCstr extends CrossConstraint {

  @Override
  public Pair<Boolean,CrossConstraint> fixVariable(final Set<Feature> forced, final Set<Feature> forbidden) {
    return new Pair<Boolean, CrossConstraint>(false, this);
  }

  @Override
  public Set<Feature> getVariables() {
    return Set.of();
  }

  @Override
  public boolean isSatisfied(final Configuration configuration) {
    return false;
  }

  @Override
  public ReExpression getCPConstraint(final Map<Feature,BoolVar> featureToVar) {
    throw new IllegalStateException("The boolean constants should have been removed");
  }

  @Override
  public List<Clause> getEquivalentClauses(final StringIntLink link) {
    throw new IllegalStateException("The boolean constants should have been removed");
  }
  @Override
  public Pair<Integer,Boolean> addTseitinClauses(final List<Clause> clauses, final StringIntLink link) {
    throw new IllegalStateException("The boolean constants should have been removed");
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
    return "FALSE";
  }

  @Override
  public String toUVL() {
    return "false";
  }
}
