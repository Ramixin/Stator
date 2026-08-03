package net.ramixin.stator.events.dispatching;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.*;
import java.util.Map;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface Dispatcher {

    Class<? extends Annotation> event();

    String loader();

    interface Util {

        static TypeMirror getEvent(AnnotationMirror mirror) {
            Map<? extends ExecutableElement, ? extends AnnotationValue> values = mirror.getElementValues();
            for(Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : values.entrySet()) {
                String name = entry.getKey().getSimpleName().toString();
                if(name.equals("event")) return (TypeMirror) entry.getValue().getValue();
            }
            return null;
        }

        static String getLoader(AnnotationMirror mirror) {
            Map<? extends ExecutableElement, ? extends AnnotationValue> values = mirror.getElementValues();
            for(Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : values.entrySet()) {
                String name = entry.getKey().getSimpleName().toString();
                if(name.equals("loader")) return (String) entry.getValue().getValue();
            }
            return null;
        }

    }

}
