package net.ramixin.stator.metadata;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.ramixin.stator.events.StatorEventAnnotation;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class EventsMetaFile {

    private final Map<Class<?>, List<Method>> eventMap;

    private EventsMetaFile(Map<Class<?>, List<Method>> eventMap) {
        this.eventMap = eventMap;
    }

    public static EventsMetaFile read(Path path, Logger logger) throws StatorMetaFileException {
        try {
            return read(Files.newBufferedReader(path), logger, EventsMetaFile.class.getClassLoader());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static EventsMetaFile read(Reader reader, Logger logger, ClassLoader loader) throws StatorMetaFileException {
        try(reader) {
            JsonObject object = JsonParser.parseReader(reader).getAsJsonObject();
            int schema = object.get("schema").getAsInt();
            if(schema != 0) throw new IllegalArgumentException("Unsupported Schema version: " + schema);
            return readV0(object, logger, loader);
        } catch (Exception e) {
            throw new StatorMetaFileException("Failed to read metafile", e);
        }
    }

    private static EventsMetaFile readV0(JsonObject object, Logger logger, ClassLoader loader) {
        JsonObject entries = object.getAsJsonObject("entries");
        HashMap<Class<?>, List<Method>> events = new HashMap<>();

        for (String key : entries.keySet()) {
            Class<?> anno;
            try {
                anno = Class.forName(key);
            } catch (ClassNotFoundException e) {
                logger.error("Failed to get annotation class {}", key, e);
                continue;
            }
            StatorEventAnnotation annotation = anno.getAnnotation(StatorEventAnnotation.class);
            if(annotation == null) {
                logger.error("Failed to get load annotation {}", key);
                continue;
            }
            Class<?> contextClazz = annotation.context();
            List<Method> methods = new ArrayList<>();
            JsonArray jsonArray = entries.getAsJsonArray(key);
            for(int i = 0; i < jsonArray.size(); i++) {
                JsonObject entry = jsonArray.get(i).getAsJsonObject();
                String classPath = entry.get("classPath").getAsString();
                String methodName = entry.get("methodName").getAsString();
                try {
                    Class<?> clazz = Class.forName(classPath, false, loader);
                    Method method = clazz.getDeclaredMethod(methodName, contextClazz);
                    method.setAccessible(true);
                    methods.add(method);
                } catch (ClassNotFoundException e) {
                    logger.error("Failed to get event class {}", classPath, e);
                } catch (NoSuchMethodException e) {
                    logger.error("Failed to get event method {}#{}", classPath, methodName, e);
                }
            }
            events.put(anno, methods);
        }
        return new EventsMetaFile(events);
    }

    public List<Method> getEvents(Class<?> anno) {
        return eventMap.getOrDefault(anno, List.of());
    }

    public Set<Class<?>> getEventAnnotations() {
        return eventMap.keySet();
    }


}
