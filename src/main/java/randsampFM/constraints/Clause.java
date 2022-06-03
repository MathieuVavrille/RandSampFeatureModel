package randsampFM.constraints;

import randsampFM.MiniSat;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

import java.util.List;
import java.util.ArrayList;


/** i represent positive literal, -i represent negative literal */
public class Clause {
  private final List<Integer> clause;

  public Clause(final List<Integer> clause) {
    this.clause = new ArrayList<Integer>(clause);
  }

  public void addAll(final Clause c) {
    clause.addAll(c.clause);
  }

  public static Clause join(final Clause c1, final Clause c2) {
    List<Integer> joined = new ArrayList<Integer>(c1.clause);
    joined.addAll(c2.clause);
    return new Clause(joined);
  }

  public String toDimacs() {
    StringBuilder builder = new StringBuilder();
    for (int i : clause)
      builder.append(i+ " ");
    builder.append(0);
    return builder.toString();
  }

  public void addToMiniSat(final MiniSat sat) {
    TIntList satClause = new TIntArrayList(clause.size());
    for (int relLit : clause) {
      satClause.add(sat.makeLiteral(Math.abs(relLit)-1, relLit>0));
    }
    sat.addClause(satClause);
  }
  
}
