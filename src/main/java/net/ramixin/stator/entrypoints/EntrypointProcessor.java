package net.ramixin.stator.entrypoints;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SupportedSourceVersion(SourceVersion.RELEASE_25)
@SupportedAnnotationTypes("net.ramixin.stator.entrypoints.Entrypoint")
public final class EntrypointProcessor extends AbstractProcessor {

    private final List<Entry> clientList = new ArrayList<>();
    private final List<Entry> serverList = new ArrayList<>();
    private final List<Entry> commonList = new ArrayList<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Elements elements = processingEnv.getElementUtils();
        Types types = processingEnv.getTypeUtils();

        TypeElement entrypoint = elements.getTypeElement("net.ramixin.stator.entrypoints.Entrypoint");

        TypeMirror networkingType = EntrypointParameter.NETWORKING.getType(elements);
        TypeMirror platformType = EntrypointParameter.PLATFORM.getType(elements);
        TypeMirror registrationType = EntrypointParameter.REGISTRATION.getType(elements);

        TypeMirror clientRegistrationType = EntrypointParameter.CLIENT_REGISTRATION.getType(elements);
        TypeMirror clientNetworkingType = EntrypointParameter.CLIENT_NETWORKING.getType(elements);

        for(Element methodElement : roundEnv.getElementsAnnotatedWith(entrypoint)) {
            if(methodElement.getKind() != ElementKind.METHOD) {
                err("Initializer annotation can only be used on methods", methodElement);
                continue;
            }
            if(!methodElement.getModifiers().contains(Modifier.STATIC)) {
                err("Initializer methods must be static", methodElement);
                continue;
            }
            AnnotationMirror anno = getMetaAnnotation(methodElement);
            if(anno == null) {
                err("Initializer annotation must be annotated with @Entrypoint", methodElement);
                continue;
            }
            Map<? extends ExecutableElement, ? extends AnnotationValue> values = anno.getElementValues();
            Entrypoint.Side side = null;
            for(Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : values.entrySet()) {
                String name = entry.getKey().getSimpleName().toString();
                if(name.equals("value")) side = Entrypoint.Side.valueOf(entry.getValue().getValue().toString());
                else err("Unknown initializer annotation attribute: " + name, anno.getAnnotationType().asElement());
            }
            if(side == null) {
                err("Failed to extract initializer side", anno.getAnnotationType().asElement());
                continue;
            }

            ExecutableElement method = (ExecutableElement) methodElement;
            List<EntrypointParameter> parameters = new ArrayList<>();
            for(VariableElement parameter : method.getParameters()) {
                TypeMirror type = parameter.asType();

                if(types.isAssignable(type, clientNetworkingType)) {
                    if(side != Entrypoint.Side.CLIENT) {
                        err("Client Networking cannot be used on a non-client side initializer", parameter);
                        continue;
                    }
                    parameters.add(EntrypointParameter.CLIENT_NETWORKING);
                }
                else if(types.isAssignable(type, networkingType)) {
                    if(side == Entrypoint.Side.CLIENT) {
                        err("Networking cannot be used on a client side initializer", parameter);
                        continue;
                    }
                    parameters.add(EntrypointParameter.NETWORKING);
                }
                else if(types.isAssignable(type, registrationType)) {
                    if(side == Entrypoint.Side.CLIENT) {
                        err("Registration cannot be used on a client side initializer", parameter);
                        continue;
                    }
                    parameters.add(EntrypointParameter.REGISTRATION);
                }
                else if(types.isAssignable(type, clientRegistrationType)) {
                    if(side != Entrypoint.Side.CLIENT) {
                        err("Client Registration cannot be used on a non-client side initializer", parameter);
                        continue;
                    }
                    parameters.add(EntrypointParameter.CLIENT_REGISTRATION);
                }
                else if(types.isAssignable(type, platformType)) parameters.add(EntrypointParameter.PLATFORM);
                else err("Invalid initializer parameter type: " + type, parameter);
            }

            Entry entry = new Entry(method.getEnclosingElement().toString(), method.getSimpleName().toString(), parameters);
            switch (side) {
                case CLIENT -> clientList.add(entry);
                case SERVER -> serverList.add(entry);
                case COMMON -> commonList.add(entry);
            }
        }

        if(!roundEnv.processingOver()) return true;

        JsonObject object = new JsonObject();
        object.addProperty("schema", 0);
        JsonArray client = new JsonArray();
        for(Entry entry : clientList) client.add(entry.toJson());
        JsonArray server = new JsonArray();
        for(Entry entry : serverList) server.add(entry.toJson());
        JsonArray common = new JsonArray();
        for(Entry entry : commonList) common.add(entry.toJson());
        object.add("CLIENT", client);
        object.add("SERVER", server);
        object.add("COMMON", common);

        Filer filer = processingEnv.getFiler();
        try {
            FileObject file = filer.createResource(
                    StandardLocation.CLASS_OUTPUT,
                    "",
                    "META-INF/stator/initializers.json"
            );
            Writer writer = file.openWriter();
            new GsonBuilder().setPrettyPrinting().create().toJson(object, writer);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    private AnnotationMirror getMetaAnnotation(Element element) {
        for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
            TypeElement type =
                    (TypeElement) mirror.getAnnotationType().asElement();

            if (type.getQualifiedName()
                    .contentEquals("net.ramixin.stator.entrypoints.Entrypoint")) {
                return mirror;
            }
        }

        return null;
    }

    private void err(String message, Element element) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, element);
    }

    private record Entry(String classPath, String methodName, List<EntrypointParameter> parameters) {

        public JsonObject toJson() {
            JsonObject object = new JsonObject();
            object.addProperty("classPath", classPath);
            object.addProperty("methodName", methodName);
            JsonArray parametersArray = new JsonArray();
            for(EntrypointParameter parameter : parameters) {
                parametersArray.add(parameter.name());
            }
            object.add("parameters", parametersArray);
            return object;
        }

    }
}
