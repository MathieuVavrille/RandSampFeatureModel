package randsampFM;

import randsampFM.constraints.Constraint;
import randsampFM.featureDiagram.FeatureDiagram;

import de.neominik.uvl.ast.UVLModel;
import de.neominik.uvl.UVLParser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class FeatureModel {
  private final FeatureDiagram featureDiagram;
  private final List<Constraint> constraints;

  public FeatureModel(final FeatureDiagram featureDiagram, final List<Constraint> constraints) {
    this.featureDiagram = featureDiagram;
    this.constraints = constraints;
  }

  public static FeatureModel parse(final String fileName) {
    try {
      return FeatureModel.parse((UVLModel) UVLParser.parse(Files.readString(Path.of(fileName))));
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(0);
      return null;
    }
  }

  public static FeatureModel parse(final UVLModel uvlModel) {
    List<Constraint> constraints = new ArrayList<Constraint>();
    for (Object cstr : uvlModel.getConstraints())
      constraints.add(Constraint.fromUVLConstraint(cstr));
    return new FeatureModel(FeatureDiagram.parse(uvlModel.getRootFeatures()[0]), constraints);
  }
}
