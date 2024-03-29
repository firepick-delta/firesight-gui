package io.tinwhiskers.firesight.gui;

import io.tinwhiskers.firesight.gui.Pipeline.Stage;

import java.io.File;
import java.io.FileWriter;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.JsonPrimitive;

public class FireSight {
    private final Ops ops;
    
    public FireSight(Ops ops) {
        this.ops = ops;
    }
    
    public Map<Stage, File> generateOutputImages(Pipeline pipeline, File inputFile, File outputDirectory) {
        if (pipeline.getStages().size() < 1) {
            return null;
        }
        // get the selected image
        // serialize the pipeline, adding output stages between each stage
        // run firesight
        // load all the output images into the list
        File pipelineFile = new File(outputDirectory, "pipeline.json");
        try {
            System.out.println(pipelineFile.getAbsolutePath());
            FileWriter writer = new FileWriter(pipelineFile);
            Pipeline pipeline2 = clonePipelineWithOutputInjection(pipeline, inputFile, outputDirectory);
            writer.write(pipeline2.toJson().toString());
            writer.close();
            ProcessBuilder pb = new ProcessBuilder(
                    "firesight", 
                    "-i",
                    inputFile.getAbsolutePath(),
                    "-p",
                    pipelineFile.getAbsolutePath());
            pb.inheritIO();
            Process process = pb.start();
            int ret = process.waitFor();
            if (ret == 0) {
                // collect all the generated images
                Map<Stage, File> outputs = new LinkedHashMap<Stage, File>();
                for (Stage stage : pipeline.getStages()) {
                    File file = getOutputInjectionFile(stage, inputFile, outputDirectory);
                    outputs.put(stage, file);
                }
                return outputs;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private Pipeline clonePipelineWithOutputInjection(Pipeline pipeline, File inputFile, File outputDirectory) {
        Pipeline out = new Pipeline();
        for (Stage stage : pipeline.getStages()) {
            out.addStage(stage);
            Op op = ops.get("imwrite");
            Stage imWriteStage = new Stage(op);
            imWriteStage.setName("___" + stage.getName() + "__imwrite");
            JsonPrimitive path = new JsonPrimitive(
                    getOutputInjectionFile(stage, inputFile, outputDirectory).getAbsolutePath());
            imWriteStage.setParameterValue("path", path);
            imWriteStage.setParameterValueEnabled("path", true);
            out.addStage(imWriteStage);
        }
        return out;
    }
    
    private File getOutputInjectionFile(Stage stage, File inputFile, File outputDirectory) {
        return new File(outputDirectory, inputFile.getName() + "_" + stage.getName() + ".png");
    }
}
