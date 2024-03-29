package io.tinwhiskers.firesight.gui;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Ops implements Iterable<Op> {
    private JsonParser parser = new JsonParser();
    private Map<String, Op> ops = new HashMap<String, Op>();
    
    public Ops() {
        for (JsonElement e : (JsonArray) parseResource("ops/ops.json")) {
            String filename = e.getAsString();
            JsonObject o = (JsonObject) parseResource("ops/" + filename);
            if (o == null) {
                System.out.println("Failed to parse: " + filename);
                continue;
            }
            Op op = new Op(o);
            ops.put(op.getName(), op);
        }
    }
    
    private JsonElement parseResource(String name) {
        try {
            return parser.parse(new InputStreamReader(ClassLoader.getSystemResourceAsStream(name)));
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public Op get(String name) {
        return ops.get(name);
    }

    public Iterator<Op> iterator() {
        return ops.values().iterator();
    }
}
