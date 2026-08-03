package net.ramixin.stator.metadata;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.ramixin.stator.entrypoints.Entrypoint;
import net.ramixin.stator.entrypoints.EntrypointParameter;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public final class EntrypointsMetaFile {

    private final List<EntrypointData> clientList;
    private final List<EntrypointData> serverList;
    private final List<EntrypointData> commonList;

    private EntrypointsMetaFile(List<EntrypointData> clientList, List<EntrypointData> serverList, List<EntrypointData> commonList) {
        this.clientList = clientList;
        this.serverList = serverList;
        this.commonList = commonList;
    }

    public static EntrypointsMetaFile read(File file, Logger logger) throws StatorMetaFileException {
        try(FileReader reader = new FileReader(file)) {
            JsonObject object = JsonParser.parseReader(reader).getAsJsonObject();
            int schema = object.get("schema").getAsInt();
            if(schema != 0) throw new IllegalArgumentException("Unsupported Schema version: " + schema);
            return readV0(object, logger);
        } catch (Exception e) {
            throw new StatorMetaFileException("Failed to read metafile", e);
        }
    }

    public List<EntrypointData> getSideList(Entrypoint.Side side) {
        return switch (side) {
            case CLIENT -> clientList;
            case SERVER -> serverList;
            case COMMON -> commonList;
        };
    }

    private static EntrypointsMetaFile readV0(JsonObject object, Logger logger) {
        return new EntrypointsMetaFile(
                extractV0entrypoints(object.getAsJsonArray("client"), logger),
                extractV0entrypoints(object.getAsJsonArray("server"), logger),
                extractV0entrypoints(object.getAsJsonArray("common"), logger)
        );
    }

    private static List<EntrypointData> extractV0entrypoints(JsonArray array, Logger logger) {
        List<EntrypointData> entrypointData = new ArrayList<>();
        for(int i = 0; i < array.size(); i++) {
            JsonObject entry = array.get(i).getAsJsonObject();
            String classPath = entry.get("classPath").getAsString();
            String methodName = entry.get("methodName").getAsString();
            JsonArray parametersArray = entry.getAsJsonArray("parameters");
            try {
                Class<?> clazz = Class.forName(classPath);
                Method method = clazz.getDeclaredMethod(methodName);
                method.setAccessible(true);
                EntrypointParameter[] parameters = new EntrypointParameter[parametersArray.size()];
                for(int j = 0; j < parametersArray.size(); j++) {
                    parameters[j] = EntrypointParameter.valueOf(parametersArray.get(j).getAsString());
                }
                entrypointData.add(new EntrypointData(method, parameters));
            } catch (ClassNotFoundException e) {
                logger.error("Failed to get entrypoint class {}", classPath, e);
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
