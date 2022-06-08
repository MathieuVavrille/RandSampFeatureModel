package randsampFM.constraints;

import randsampFM.types.*;
import randsampFM.parser.StringIntLink;

import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.expression.discrete.relational.ReExpression;

import org.javatuples.Pair;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;

public class OrCstr extends BinaryCrossConstraint {
  
  private OrCstr(final CrossConstraint left, final CrossConstraint right) {
    super(left,right);
  }

  public static CrossConstraint of(final CrossConstraint left, final CrossConstraint right) {
    if (left instanceof TrueCstr || right instanceof TrueCstr)
      return new TrueCstr();
    else if (left instanceof FalseCstr)
      return right;
    else if (right instanceof FalseCstr)
      return left;
    else
      return new OrCstr(left, right);
  }

  @Override
  public Pair<Boolean,CrossConstraint> fixVariable(final Set<Feature> forced, final Set<Feature> forbidden) {
    Pair<Boolean, CrossConstraint> leftFix = left.fixVariable(forced,forbidden);
    Pair<Boolean, CrossConstraint> rightFix = right.fixVariable(forced,forbidden);
    return new Pair<Boolean, CrossConstraint>(leftFix.getValue0() || rightFix.getValue0(), OrCstr.of(leftFix.getValue1(),rightFix.getValue1()));
  }

  @Override
  public boolean isSatisfied(final Configuration configuration) {
    return left.isSatisfied(configuration) || right.isSatisfied(configuration);
  }

  @Override
  public ReExpression getCPConstraint(final Map<Feature,BoolVar> featureToVar) {
    return left.getCPConstraint(featureToVar).or(right.getCPConstraint(featureToVar));
  }

  @Override
  public List<Clause> getEquivalentClauses(final StringIntLink link) {
    List<Clause> leftClauses = left.getEquivalentClauses(link);
    List<Clause> rightClauses = right.getEquivalentClauses(link);
    // TODO improve maybe if I find something good
    /*if (leftClauses.size() == 1) {
      for (Clause cl : rightClauses)
        cl.addAll(leftClauses.get(0));
      return rightClauses;
    }
    if (rightClauses.size() == 1) {
      for (Clause cl : leftClauses)
        cl.addAll(rightClauses.get(0));
      return leftClauses;
      }*/
    List<Clause> newClauses = new ArrayList<Clause>();
    for (Clause leftClause : leftClauses)
      for (Clause rightClause : rightClauses) {
        Clause mergedClause = Clause.join(leftClause,rightClause);
        if (mergedClause != null)
          newClauses.add(mergedClause);
      }
    System.out.println(leftClauses.size() + " " + rightClauses.size() + " " + newClauses.size());
    return newClauses;
  }

  @Override
  public Pair<Set<Feature>,Set<Feature>> forcedFeaturesForTrue() {
    return new Pair<Set<Feature>,Set<Feature>>(Set.of(),Set.of());
  }
  @Override
  public Pair<Set<Feature>,Set<Feature>> forcedFeaturesForFalse() {
    Pair<Set<Feature>,Set<Feature>> leftFeatures = left.forcedFeaturesForFalse();
    Pair<Set<Feature>,Set<Feature>> rightFeatures = right.forcedFeaturesForFalse();
    Set<Feature> leftForced = new HashSet<Feature>(leftFeatures.getValue0());
    leftForced.addAll(rightFeatures.getValue0());
    Set<Feature> leftForbidden = new HashSet<Feature>(leftFeatures.getValue1());
    leftForbidden.addAll(rightFeatures.getValue1());
    return new Pair<Set<Feature>,Set<Feature>>(leftForced, leftForbidden);
  }

  @Override
  public String toString() {
    return "OR("+left.toString()+","+right.toString()+")";
  }

  @Override
  public String toUVL() {
    return "("+left.toUVL()+" | "+right.toUVL()+")";
  }
}
