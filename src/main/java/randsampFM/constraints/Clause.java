package randsampFM.constraints;

import randsampFM.MiniSat;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

import java.util.List;
import java.util.Map;
import java.util.HashMap;


/** i represent positive literal, -i represent negative literal */
public class Clause {
  private final Map<Integer,Boolean> clause = new HashMap<Integer,Boolean>();

  private Clause() {}
  private Clause(final Map<Integer,Boolean> clause) {
    this.clause.putAll(clause);
  }

  /** Constructors helpers */
  private static Clause ofLit(final int var, final boolean val) {
    Clause res = new Clause();
    res.clause.put(var, val);
    return res;
  }
  public static Clause ofTrueLit(final int var) {
    return Clause.ofLit(var, true);
  }
  public static Clause ofFalseLit(final int var) {
    return Clause.ofLit(var, false);
  }
  /** Does not check for tautology */
  public static Clause impl(final int a, final int b) {
    Clause res = Clause.ofFalseLit(a);
    res.clause.put(b,true);
    return res;
  }
  /** Does not check for tautology */
  public static Clause notBoth(final int a, final int b) {
    Clause res = Clause.ofFalseLit(a);
    res.clause.put(b,false);
    return res;
  }

  /** Does not check for tautology */
  public void addTrueLit(final int var) {
    clause.put(var, true);
  }
  /** Does not check for tautology */
  public void addFalseLit(final int var) {
    clause.put(var, false);
  }
  
  public boolean addAll(final Clause c) {
    for (Map.Entry<Integer,Boolean> es : c.clause.entrySet()) {
      if (clause.containsKey(es.getKey()) && clause.get(es.getKey()) == !es.getValue())
        return true;
      clause.put(es.getKey(),es.getValue());
    }
    return false;
  }

  public static Clause join(final Clause c1, final Clause c2) {
    Clause joined = new Clause(c1.clause);
    if (joined.addAll(c2))
      return null;
    return joined;
  }

  public String toDimacs() {
    StringBuilder builder = new StringBuilder();
    for (Map.Entry<Integer,Boolean> es : clause.entrySet()) {
      if (es.getValue())
        builder.append(es.getKey()+1+ " ");
      else
        builder.append(-es.getKey()-1 + " ");
    }
    builder.append(0);
    return builder.toString();
  }

  public void addToMiniSat(final MiniSat sat) {
    addToMiniSat(sat, false);
  }
  
  /** Adds the clause to a MiniSat instance.
   * @param sat the model
   * @param isBranching a boolean to set to true if the model should branch on the constraint
   */
  public void addToMiniSat(final MiniSat sat, final boolean isBranching) {
    TIntList satClause = new TIntArrayList(clause.size());
    for (Map.Entry<Integer,Boolean> es : clause.entrySet()) {
      satClause.add(sat.makeLiteral(es.getKey(), es.getValue()));
    }
    if (isBranching)
      sat.addBranchingClause(satClause);
    else
      sat.addClause(satClause);
  }
  
}
