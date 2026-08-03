package net.ramixin.stator.events.dispatching;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.ramixin.stator.events.StatorEventAnnotation;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SupportedSourceVersion(SourceVersion.RELEASE_25)
@SupportedAnnotationTypes("net.ramixin.stator.events.dispatching.Dispatcher")
public final class DispatcherProcessor extends AbstractProcessor {

    private final HashMap<String, HashMap<String, Entry>> dispatchers = new HashMap<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Elements elements = processingEnv.getElementUtils();
        Types types = processingEnv.getTypeUtils();

        TypeElement dispatcher = elements.getTypeElement("net.ramixin.stator.events.dispatching.Dispatcher");
        TypeMirror event = elements.getTypeElement("net.ramixin.stator.events.Event").asType();
        TypeMirror voidType = elements.getTypeElement("java.lang.Void").asType();

        for(Element element : roundEnv.getElementsAnnotatedWith(dispatcher))
            scan(element, types, event, voidType);

        if(!roundEnv.processingOver()) return true;

        JsonObject object = new JsonObject();
        object.addProperty("schema", 0);
        JsonObject loaders = new JsonObject();
        for(Map.Entry<String, HashMap<String, Entry>> entry : dispatchers.entrySet()) {
            JsonObject dispatchers = new JsonObject();
            for(Map.Entry<String, Entry> dispatcherEntry : entry.getValue().entrySet()) {
                dispatchers.add(dispatcherEntry.getKey(), dispatcherEntry.getValue().toJson());
            }
            loaders.add(entry.getKey(), dispatchers);
        }
        object.add("dispatchers", loaders);

        Filer filer = processingEnv.getFiler();
        try {
            FileObject file = filer.createResource(
                    StandardLocation.CLASS_OUTPUT,
                    "",
                    "META-INF/stator/dispatchers.json"
            );
            Writer writer = file.openWriter();
            new GsonBuilder().setPrettyPrinting().create().toJson(object, writer);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    private void scan(Element element, Types types, TypeMirror event, TypeMirror voidType) {
        ExecutableElement method = (ExecutableElement) element;
        if(!method.getModifiers().contains(Modifier.STATIC)) {
            err("Dispatcher methods must be static", method);
            return;
        }
        AnnotationMirror mirror = getDispatcherAnnotation(element);
        if(mirror == null) {
            err("Could not find @Dispatcher annotation", element);
            return;
        }
        TypeMirror eventType = Dispatcher.Util.getEvent(mirror);
        String loader = Dispatcher.Util.getLoader(mirror);
        if(eventType == null) {
            err("Could not extract event type from @Dispatcher annotation", element);
        }
        AnnotationMirror metaAnno = StatorEventAnnotation.Util.get((TypeElement) types.asElement(eventType));
        if(metaAnno == null) {
            err("Event annotation must be annotated with @StatorEventAnnotation", element);
            return;
        }
        TypeMirror expectedContextType = StatorEventAnnotation.Util.getContextMirror(metaAnno);
        TypeMirror expectedReturnType = StatorEventAnnotation.Util.getResultMirror(metaAnno);
        if(expectedContextType == null) {
            err("Failed to extract event context type", element);
            return;
        }
        if(expectedReturnType == null) {
            err("Failed to extract event return type", element);
            return;
        }

        List<? extends VariableElement> parameters = method.getParameters();
        if(parameters.size() != 1) {
            err("Dispatcher methods must have exactly one parameter", element);
            return;
        }
        TypeMirror parameterType = parameters.getFirst().asType();
        if(!(parameterType instanceof DeclaredType declaredType)) {
            err("Dispatcher method parameter must be a declared type", element);
            return;
        }
        if(!types.isAssignable(declaredType.asElement().asType(), event)) {
            err("Dispatcher method parameter must be of type " + expectedContextType, element);
            return;
        }
        List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
        TypeMirror actualContextType = typeArguments.getFirst();
        TypeMirror actualReturnType = typeArguments.getLast();

        if(!types.isSameType(actualContextType, expectedContextType)) {
            err("Dispatcher method parameter must be of type " + expectedContextType, element);
            return;
        }
        boolean voidReturn = types.isSameType(actualReturnType, voidType) && expectedReturnType.getKind() == TypeKind.VOID;
        if(!voidReturn && !types.isSameType(actualReturnType, expectedReturnType)) {
            err("Dispatcher event parameter return type must be of type " + expectedReturnType, element);
            return;
        }
        addEntry(method, (TypeElement) types.asElement(eventType), loader);
    }

    private void addEntry(ExecutableElement method, TypeElement eventElement, String loader) {
        TypeElement clazz = (TypeElement) method.getEnclosingElement();
        String annotationPath = eventElement.getQualifiedName().toString();
        String classPath = clazz.getQualifiedName().toString();
        String methodName = method.getSimpleName().toString();

        HashMap<String, Entry> hashmap = dispatchers.computeIfAbsent(loader, _ -> new HashMap<>());
        if(hashmap.containsKey(annotationPath)) {
            err("Duplicate dispatcher for event " + annotationPath, method);
            return;
        }
        hashmap.put(annotationPath, new Entry(classPath, methodName));
    }

    private AnnotationMirror getDispatcherAnnotation(Element element) {
        for(AnnotationMirror mirror : element.getAnnotationMirrors()) {
            TypeElement type = (TypeElement) mirror.getAnnotationType().asElement();
            if(type.getQualifiedName().contentEquals("net.ramixin.stator.events.dispatching.Dispatcher"))
                return mirror;
        }
        return null;
    }

    private void err(String message, Element element) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, element);
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
