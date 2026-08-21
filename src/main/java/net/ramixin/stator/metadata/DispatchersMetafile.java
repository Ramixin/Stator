package net.ramixin.stator.metadata;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.ramixin.stator.events.Event;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class DispatchersMetafile {

    private final Map<String, Map<Class<?>, Method>> dispatchers;

    private DispatchersMetafile(Map<String, Map<Class<?>, Method>> dispatchers) {
        this.dispatchers = dispatchers;
    }

    public static DispatchersMetafile read(Path path, Logger logger) throws StatorMetaFileException {
        try {
            return read(Files.newBufferedReader(path), logger, DispatchersMetafile.class.getClassLoader());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static DispatchersMetafile read(Reader reader, Logger logger, ClassLoader loader) throws StatorMetaFileException {
        try(reader) {
            JsonObject object = JsonParser.parseReader(reader).getAsJsonObject();
            int schema = object.get("schema").getAsInt();
            if(schema != 0) throw new IllegalArgumentException("Unsupported Schema version: " + schema);
            return readV0(object, logger, loader);
        } catch (Exception e) {
            throw new StatorMetaFileException("Failed to read metafile", e);
        }
    }

    private static DispatchersMetafile readV0(JsonObject object, Logger logger, ClassLoader classLoader) {
        Map<String, Map<Class<?>, Method>> dispatchers = new HashMap<>();
        JsonObject loaders = object.getAsJsonObject("loaders");
        for(String loaderPath : loaders.keySet()) {
            JsonObject loader = loaders.getAsJsonObject(loaderPath);
            Map<Class<?>, Method> map = new HashMap<>();
            for(String annotationPath : loader.keySet()) {
                JsonObject dispatcher = loader.getAsJsonObject(annotationPath);
                String classPath = dispatcher.get("classPath").getAsString();
                String methodName = dispatcher.get("methodName").getAsString();
                Class<?> annotation;
                try {
                    annotation = Class.forName(annotationPath);
                } catch (ClassNotFoundException e) {
                    logger.error("Failed to get annotation class {}", annotationPath, e);
                    continue;
                }
                try {
                    Class<?> dispatcherClass = Class.forName(classPath, false, classLoader);
                    Method dispatcherMethod = dispatcherClass.getDeclaredMethod(methodName, Event.class);
                    dispatcherMethod.setAccessible(true);
                    map.put(annotation, dispatcherMethod);
                } catch (ClassNotFoundException e) {
                    logger.error("Failed to get dispatcher class {}", classPath, e);
                } catch (NoSuchMethodException e) {
                    logger.error("Failed to get dispatcher method {}#{}", classPath, methodName, e);
                }
            }
            dispatchers.put(loaderPath, map);
        }
        return new DispatchersMetafile(dispatchers);
    }

    public Set<String> getLoaders() {
        return dispatchers.keySet();
    }

    public Set<Class<?>> getDispatcherAnnotations(String loader) {
        return dispatchers.get(loader).keySet();
    }

    public Optional<Method> getDispatcher(String loader, Class<?> annotation) {
        if(!dispatchers.containsKey(loader)) return Optional.empty();
        Map<Class<?>, Method> map = dispatchers.get(loader);
        return Optional.ofNullable(map.get(annotation));
    }

}
