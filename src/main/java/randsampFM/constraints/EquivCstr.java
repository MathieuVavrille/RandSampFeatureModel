package randsampFM.constraints;

import randsampFM.types.*;
import randsampFM.parser.StringIntLink;

import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.expression.discrete.relational.ReExpression;

import org.javatuples.Pair;

import java.util.List;
import java.util.Set;
import java.util.Map;

/**
00 | 1
01 | 0
10 | 0
11 | 1
*/
public class EquivCstr extends BinaryCrossConstraint {
  
  private EquivCstr(final CrossConstraint left, final CrossConstraint right) {
    super(left,right);
  }

  public static CrossConstraint of(final CrossConstraint left, final CrossConstraint right) {
    if (left instanceof TrueCstr)
      return right;
    else if (right instanceof TrueCstr)
      return left;
    else if (left instanceof FalseCstr)
      return NotCstr.of(right);
    else if (right instanceof FalseCstr)
      return NotCstr.of(left);
    return new EquivCstr(left, right);
  }

  @Override
  public Pair<Boolean,CrossConstraint> fixVariable(final Set<Feature> forced, final Set<Feature> forbidden) {
    Pair<Boolean, CrossConstraint> leftFix = left.fixVariable(forced,forbidden);
    Pair<Boolean, CrossConstraint> rightFix = right.fixVariable(forced,forbidden);
    return new Pair<Boolean, CrossConstraint>(leftFix.getValue0() || rightFix.getValue0(), EquivCstr.of(leftFix.getValue1(),rightFix.getValue1()));
  }

  @Override
  public boolean isSatisfied(final Configuration configuration) {
    return left.isSatisfied(configuration) == right.isSatisfied(configuration);
  }

  @Override
  public ReExpression getCPConstraint(final Map<Feature,BoolVar> featureToVar) {
    return left.getCPConstraint(featureToVar).iff(right.getCPConstraint(featureToVar));
  }

  @Override
  public List<Clause> getEquivalentClauses(final StringIntLink link) {
    throw new UnsupportedOperationException("Not Implemented");
  }

  @Override
  public Pair<Integer,Boolean> addTseitinClauses(final List<Clause> clauses, final StringIntLink link) {
    final Pair<Integer,Boolean> leftLit = left.addTseitinClauses(clauses, link);
    final Pair<Integer,Boolean> rightLit = right.addTseitinClauses(clauses, link);
    final int newVar = link.createDummy();
    clauses.add(Clause.ofLit(leftLit.getValue0(), !leftLit.getValue1(), rightLit.getValue0(), !rightLit.getValue1(), newVar, true));
    clauses.add(Clause.ofLit(leftLit.getValue0(), leftLit.getValue1(), rightLit.getValue0(), rightLit.getValue1(), newVar, true));
    clauses.add(Clause.ofLit(leftLit.getValue0(), leftLit.getValue1(), rightLit.getValue0(), !rightLit.getValue1(), newVar, false));
    clauses.add(Clause.ofLit(leftLit.getValue0(), !leftLit.getValue1(), rightLit.getValue0(), rightLit.getValue1(), newVar, false));
    return new Pair<Integer,Boolean>(newVar, true);
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
    return "EQUIV("+left.toString()+","+right.toString()+")";
  }

  @Override
  public String toUVL() {
    return "("+left.toUVL()+" <=> "+right.toString()+")";
  }
}
