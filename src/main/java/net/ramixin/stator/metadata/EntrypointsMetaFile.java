package net.ramixin.stator.metadata;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.ramixin.stator.entrypoints.EntrypointParameter;
import net.ramixin.stator.entrypoints.Phase;
import net.ramixin.stator.entrypoints.Side;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class EntrypointsMetaFile {

    private final Map<Side, Map<Phase, List<EntrypointData>>> entrypoints;

    private EntrypointsMetaFile(Map<Side, Map<Phase, List<EntrypointData>>> entrypoints) {
        this.entrypoints = entrypoints;
    }

    public static EntrypointsMetaFile read(Path path, Logger logger) throws StatorMetaFileException {
        try {
            return read(Files.newBufferedReader(path), logger, EntrypointsMetaFile.class.getClassLoader());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static EntrypointsMetaFile read(Reader reader, Logger logger, ClassLoader loader) throws StatorMetaFileException {
        try(reader) {
            JsonObject object = JsonParser.parseReader(reader).getAsJsonObject();
            int schema = object.get("schema").getAsInt();
            if(schema != 0) throw new IllegalArgumentException("Unsupported Schema version: " + schema);
            return readV0(object, logger, loader);
        } catch (Exception e) {
            throw new StatorMetaFileException("Failed to read metafile", e);
        }
    }

    public List<EntrypointData> getData(Side side, Phase phase) {
        if(!entrypoints.containsKey(side)) return List.of();
        Map<Phase, List<EntrypointData>> map = entrypoints.get(side);
        if(!map.containsKey(phase)) return List.of();
        return map.get(phase);
    }

    private static EntrypointsMetaFile readV0(JsonObject object, Logger logger, ClassLoader loader) {
        Map<Side, Map<Phase, List<EntrypointData>>> map = new HashMap<>();

        JsonObject sides = object.getAsJsonObject("sides");
        for(String key : sides.keySet()) {
            Side side = Side.valueOf(key);
            JsonObject sideObject = sides.getAsJsonObject(key);
            for(String key2 : sideObject.keySet()) {
                Phase phase = Phase.valueOf(key2);
                JsonArray list = sideObject.getAsJsonArray(key2);
                List<EntrypointData> data = extractV0Entrypoints(list, logger, loader);

                Map<Phase, List<EntrypointData>> phaseMap = map.computeIfAbsent(side, _ -> new HashMap<>());
                phaseMap.put(phase, data);
            }
        }
        return new EntrypointsMetaFile(map);
    }

    private static List<EntrypointData> extractV0Entrypoints(JsonArray array, Logger logger, ClassLoader loader) {
        List<EntrypointData> entrypointData = new ArrayList<>();
        for(int i = 0; i < array.size(); i++) {
            JsonObject entry = array.get(i).getAsJsonObject();
            String classPath = entry.get("classPath").getAsString();
            String methodName = entry.get("methodName").getAsString();
            JsonArray parametersArray = entry.getAsJsonArray("parameters");
            Class<?> clazz;
            try {
                clazz = Class.forName(classPath, false, loader);
            } catch (ClassNotFoundException e) {
                logger.error("Failed to get entrypoint class {}", classPath, e);
                continue;
            }
            try {

                EntrypointParameter[] parameters = new EntrypointParameter[parametersArray.size()];
                Class<?>[] parameterClasses = new Class[parametersArray.size()];
                for(int j = 0; j < parametersArray.size(); j++) {
                    parameters[j] = EntrypointParameter.valueOf(parametersArray.get(j).getAsString());
                    parameterClasses[j] = parameters[j].getClazz();
                }

                Method method = clazz.getDeclaredMethod(methodName, parameterClasses);
                method.setAccessible(true);

                entrypointData.add(new EntrypointData(method, parameters));
            } catch(ClassNotFoundException e) {
                logger.error("Failed to get parameter class {}", classPath, e);
            } catch (NoSuchMethodException e) {
                logger.error("Failed to get entrypoint method {}#{}", classPath, methodName, e);
            } catch (IllegalArgumentException e) {
                logger.error("Failed to get entrypoint parameter {}", parametersArray.get(0), e);
            }
        }
        return entrypointData;
    }

    public record EntrypointData(Method method, EntrypointParameter[] parameters) {}

}
