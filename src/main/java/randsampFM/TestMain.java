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
    
  }
}
