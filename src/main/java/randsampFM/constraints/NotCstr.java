package randsampFM.constraints;

import randsampFM.types.*;
import randsampFM.parser.StringIntLink;

import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.expression.discrete.relational.ReExpression;

import org.javatuples.Pair;

import java.util.List;
import java.util.ArrayList;
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
    else if (child instanceof AndCstr) {
      AndCstr andCstr = (AndCstr) child;
      return OrCstr.of(NotCstr.of(andCstr.left),NotCstr.of(andCstr.right));
    }
    else if (child instanceof OrCstr) {
      OrCstr orCstr = (OrCstr) child;
      return AndCstr.of(NotCstr.of(orCstr.left),NotCstr.of(orCstr.right));
    }
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
  public List<Clause> getEquivalentClauses(final StringIntLink link) {
    if (child instanceof LitteralCstr)
      return new ArrayList<Clause>(List.of(Clause.ofFalseLit(link.getInt(((LitteralCstr) child).getFeature().getName()))));
    else
      throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public Pair<Integer,Boolean> addTseitinClauses(final List<Clause> clauses, final StringIntLink link) {
    final Pair<Integer,Boolean> childLit = child.addTseitinClauses(clauses, link);
    return new Pair<Integer,Boolean>(childLit.getValue0(), !childLit.getValue1());
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

  @Override
  public String toUVL() {
    return "!"+child.toUVL();
  }
}
