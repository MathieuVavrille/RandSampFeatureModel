package randsampFM;

import randsampFM.featureDiagram.*;
import randsampFM.constraints.*;
import randsampFM.types.*;

import de.neominik.uvl.ast.UVLModel;
import de.neominik.uvl.UVLParser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

public class TestMain {
  public static void main(String[] args) {
    String path = "./models/truc.uvl";
    FeatureModel fm = FeatureModel.parse(path);
    /*UVLModel model = loadModel(path);
    de.neominik.uvl.ast.Feature rootFeature = model.getRootFeatures()[0];
    FeatureDiagram fm = FeatureDiagram.parseFeatureDiagram(rootFeature);
    List<Feature> allFeatures = new ArrayList<Feature>(fm.getFeatures());
    System.out.println(fm.toString());
    System.out.println(allFeatures);
    FeatureDiagram fm2 = fm.fixFeatures(Set.of(),Set.of(allFeatures.get(4))).getValue1();
    System.out.println(fm2.fixFeatures(Set.of(),Set.of(allFeatures.get(5))));*/
  }
}
