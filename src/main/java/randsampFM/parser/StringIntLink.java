package randsampFM.parser;

import randsampFM.types.Feature;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

/** Transform strings to integers, and store the mappings */
public class StringIntLink {

  private final Map<String,Integer> stringToInt = new HashMap<String,Integer>();
  private final List<String> intToString = new ArrayList<String>();

  public static StringIntLink fromSet(final Set<Feature> features) {
    StringIntLink res = new StringIntLink();
    for (Feature s : features)
      res.addString(s.getName());
    return res;
  }
  
  public void addString(final String s) {
    stringToInt.put(s, intToString.size()+1);
    intToString.add(s);
  }
  
  public String getString(final int i) {
    return intToString.get(i-1);
  }
  public int getInt(final String s) {
    return stringToInt.get(s);
  }

  public String toDimacsComments() {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < intToString.size(); i++) {
      builder.append("c "+ (i+1) + " " + intToString.get(i) + "\n");
    }
    return builder.toString();
  }

  public int size() {
    return intToString.size();
  }

  @Override
  public String toString() {
    return intToString.toString();
  }
}
