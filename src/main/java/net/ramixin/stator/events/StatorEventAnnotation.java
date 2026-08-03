package net.ramixin.stator.events;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.*;
import java.util.Map;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface StatorEventAnnotation {

    Class<?> context();

    Class<?> result();


    interface Util {

        static TypeMirror getContextMirror(AnnotationMirror mirror) {
            Map<? extends ExecutableElement, ? extends AnnotationValue> values = mirror.getElementValues();
            for(Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : values.entrySet()) {
                String name = entry.getKey().getSimpleName().toString();
                if(name.equals("context")) return (TypeMirror) entry.getValue().getValue();
            }
            return null;
        }

        static TypeMirror getResultMirror(AnnotationMirror mirror) {
            Map<? extends ExecutableElement, ? extends AnnotationValue> values = mirror.getElementValues();
            for(Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : values.entrySet()) {
                String name = entry.getKey().getSimpleName().toString();
                if(name.equals("result")) return (TypeMirror) entry.getValue().getValue();
            }
            return null;
        }

        static AnnotationMirror get(TypeElement element) {
            for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
                TypeElement type =
                        (TypeElement) mirror.getAnnotationType().asElement();

                if (type.getQualifiedName()
                        .contentEquals("net.ramixin.stator.events.StatorEventAnnotation")) {
                    return mirror;
                }
            }
            return null;
        }

    }

}
