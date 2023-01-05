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
  private final Feature litteral;
  
  private NotCstr(final Feature litteral) {
    this.litteral = litteral;
  }

  public static CrossConstraint of(final CrossConstraint child) {
    if (child instanceof TrueCstr)
      return new FalseCstr();
    else if (child instanceof FalseCstr)
      return new TrueCstr();
    else if (child instanceof NotCstr)
      return new LitteralCstr(((NotCstr) child).litteral);
    else if (child instanceof AndCstr) {
      AndCstr andCstr = (AndCstr) child;
      return OrCstr.of(NotCstr.of(andCstr.left),NotCstr.of(andCstr.right));
    }
    else if (child instanceof OrCstr) {
      OrCstr orCstr = (OrCstr) child;
      return AndCstr.of(NotCstr.of(orCstr.left),NotCstr.of(orCstr.right));
    }
    else if (child instanceof LitteralCstr) {
      LitteralCstr litteralCstr = (LitteralCstr) child;
      return new NotCstr(litteralCstr.getFeature());
    }
    else
      throw new IllegalStateException("There is no more possibility of constraint.");
  }

  @Override
  public Pair<Boolean,CrossConstraint> fixVariable(final Set<Feature> forced, final Set<Feature> forbidden) {
    if (forced.contains(litteral))
      return new Pair<Boolean, CrossConstraint>(true, new FalseCstr());
    else if (forbidden.contains(litteral))
      return new Pair<Boolean, CrossConstraint>(true, new TrueCstr());
    else
      return new Pair<Boolean, CrossConstraint>(false, this);
  }

  @Override
  public boolean isSatisfied(final Configuration configuration) {
    return !configuration.contains(litteral);
  }

  @Override
  public void postCPConstraint(final Map<Feature,BoolVar> featureToVar) {
    featureToVar.get(litteral).eq(0).post();
  }

  @Override
  public ReExpression getCPConstraint(final Map<Feature,BoolVar> featureToVar) {
    return featureToVar.get(litteral).not();
  }

  @Override
  public List<Clause> getEquivalentClauses(final StringIntLink link) {
    return new ArrayList<Clause>(List.of(Clause.ofFalseLit(link.getInt(litteral.getName()))));
  }

  @Override
  public Pair<Integer,Boolean> addTseitinClauses(final List<Clause> clauses, final StringIntLink link) {
    return new Pair<Integer,Boolean>(link.getInt(litteral.getName()),false);
  }

  @Override
  public Pair<Set<Feature>,Set<Feature>> forcedFeaturesForTrue() {
    return new Pair<Set<Feature>,Set<Feature>>(Set.of(),Set.of(litteral));
  }
  @Override
  public Pair<Set<Feature>,Set<Feature>> forcedFeaturesForFalse() {
    return new Pair<Set<Feature>,Set<Feature>>(Set.of(litteral),Set.of());
  }

  @Override
  public Set<Feature> getVariables() {
    return Set.of(litteral);
  }

  @Override
  public String toString() {
    return "NOT("+litteral+")";
  }

  @Override
  public String toUVL() {
    return "!"+litteral;
  }
}
