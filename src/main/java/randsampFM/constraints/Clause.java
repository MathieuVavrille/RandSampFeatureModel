package randsampFM.constraints;


import java.util.List;
import java.util.ArrayList;

public class Clause {
  private final List<Integer> clause;

  public Clause(final List<Integer> clause) {
    this.clause = clause;
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
}
