package net.ramixin.stator.entrypoints;

import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

public enum EntrypointParameter {

    CLIENT_NETWORKING("networking.ClientNetworking"),
    NETWORKING("networking.Networking"),
    CLIENT_REGISTRATION("registration.ClientRegistration"),
    REGISTRATION("registration.Registration"),
    PLATFORM("Platform")

    ;

    private final String classPath;

    EntrypointParameter(String classPath) {
        this.classPath = "net.ramixin.stator." + classPath;
    }

    public TypeMirror getType(Elements elements) {
        return elements.getTypeElement(this.classPath).asType();
    }

    public Class<?> getClazz() throws ClassNotFoundException {
        return Class.forName(this.classPath);
    }
}
