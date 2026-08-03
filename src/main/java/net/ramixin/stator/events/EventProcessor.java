package net.ramixin.stator.events;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

@SupportedSourceVersion(SourceVersion.RELEASE_25)
@SupportedAnnotationTypes("*")
public final class EventProcessor extends AbstractProcessor {

    private final HashMap<String, List<Entry>> entries = new HashMap<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for(Element root : roundEnv.getRootElements())
            scan(root);

        if(!roundEnv.processingOver()) return false;
        if(entries.isEmpty()) return false;

        JsonObject object = new JsonObject();
        object.addProperty("schema", 0);
        JsonObject entriesByAnnotation = new JsonObject();
        for(Map.Entry<String, List<Entry>> entry : entries.entrySet()) {
            JsonArray array = new JsonArray();
            entry.getValue().forEach(e -> array.add(e.toJson()));
            entriesByAnnotation.add(entry.getKey(), array);
        }
        object.add("entries", entriesByAnnotation);
        Filer filer = processingEnv.getFiler();

        try {
            FileObject file = filer.createResource(
                    StandardLocation.CLASS_OUTPUT,
                    "",
                    "META-INF/stator/events.json"
            );
            Writer writer = file.openWriter();
            new GsonBuilder().setPrettyPrinting().create().toJson(object, writer);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    private void scan(Element element) {
        block: {
            if(element.getKind() != ElementKind.METHOD) break block;
            ExecutableElement method = (ExecutableElement) element;

            AnnotationMirror anno = null;
            for (AnnotationMirror mirror : method.getAnnotationMirrors()) {
                if(!isEvent(mirror)) continue;
                if(anno != null)
                    err("Only one event annotation is allowed per method", method);
                anno = mirror;
            }
            if(anno == null) break block;
            AnnotationMirror metaAnno = StatorEventAnnotation.Util.get((TypeElement) anno.getAnnotationType().asElement());
            if(metaAnno == null) {
                err("Event annotation must be annotated with @StatorEventAnnotation", anno.getAnnotationType().asElement());
                return;
            }
            if(!method.getModifiers().contains(Modifier.STATIC))
                err("Event methods must be static", method);
            if(method.getParameters().size() != 1)
                err("Event methods must have exactly one context parameter", method);

            TypeMirror expectedContextType = StatorEventAnnotation.Util.getContextMirror(metaAnno);
            TypeMirror expectedReturnType = StatorEventAnnotation.Util.getResultMirror(metaAnno);

            if(expectedContextType == null) {
                err("Failed to extract event context type", metaAnno.getAnnotationType().asElement());
                return;
            }
            if(expectedReturnType == null) {
                err("Failed to extract event return type", metaAnno.getAnnotationType().asElement());
                return;
            }

            Types types = processingEnv.getTypeUtils();

            TypeMirror actualContextType = method.getParameters().getFirst().asType();
            if(!types.isAssignable(actualContextType, expectedContextType)) {
                err("Event method parameter must be of type " + expectedContextType, method);
                return;
            }

            TypeMirror actualReturnType = method.getReturnType();
            if(!types.isAssignable(actualReturnType, expectedReturnType)) {
                err("Event method return type must be of type " + expectedReturnType, method);
                return;
            }

            addEntry(method, anno);
        }

        for (Element child : element.getEnclosedElements())
            scan(child);
    }

    private void err(String message, Element element) {
        processingEnv.getMessager().printMessage(
                Diagnostic.Kind.ERROR,
                message,
                element
        );
    }

    private boolean isEvent(AnnotationMirror mirror) {
        Element annotationType = mirror.getAnnotationType().asElement();

        for (AnnotationMirror meta : annotationType.getAnnotationMirrors()) {
            TypeElement metaType = (TypeElement) meta.getAnnotationType().asElement();
            Name name = metaType.getQualifiedName();
            if (name.contentEquals("net.ramixin.stator.events.StatorEventAnnotation"))
                return true;
        }
        return false;
    }

    private void addEntry(ExecutableElement method, AnnotationMirror anno) {
        TypeElement clazz = (TypeElement) method.getEnclosingElement();
        TypeElement annotation = (TypeElement) anno.getAnnotationType().asElement();
        String annotationPath = annotation.getQualifiedName().toString();
        String classPath = clazz.getQualifiedName().toString();
        String methodName = method.getSimpleName().toString();
        entries.computeIfAbsent(annotationPath, _ -> new ArrayList<>()).add(new Entry(classPath, methodName));
    }

    private record Entry(String classPath, String methodName) {

        public JsonObject toJson() {
            JsonObject object = new JsonObject();
            object.addProperty("classPath", classPath);
            object.addProperty("methodName", methodName);
            return object;
        }
    }
}
