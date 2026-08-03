import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import net.ramixin.stator.entrypoints.EntrypointProcessor;
import net.ramixin.stator.events.EventProcessor;
import net.ramixin.stator.events.dispatching.DispatcherProcessor;
import org.junit.Test;

import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.util.Optional;

import static com.google.testing.compile.CompilationSubject.assertThat;

public class TestProcessors {

    @Test
    public void testEventProcessor() throws IOException {
        JavaFileObject source = JavaFileObjects.forSourceString(
                "test.MyEvents",
                """
                package test;
               \s
                import net.ramixin.stator.events.annotations.BlockBrokenEvent;
                import net.ramixin.stator.events.contexts.BlockBrokenContext;
                import net.ramixin.stator.events.annotations.PlayerJoinedServerEvent;
                import net.ramixin.stator.events.contexts.PlayerJoinedServerContext;
       \s
                public class MyEvents {
                    @BlockBrokenEvent
                    public static void onEvent(BlockBrokenContext ctx) {}
                   \s
                    @PlayerJoinedServerEvent
                    public static void onEvent2(PlayerJoinedServerContext ctx) {}
                   \s
                    @BlockBrokenEvent
                    public static void onEvent3(BlockBrokenContext ctx) {}
                }
               \s"""
        );

        Compilation compilation = Compiler.javac()
                .withProcessors(new EventProcessor())
                .compile(source);

        assertThat(compilation)
                .succeeded();

        Optional<JavaFileObject> maybeFile = compilation.generatedFile(
                StandardLocation.CLASS_OUTPUT,
                "",
                "META-INF/stator/events.json"
        );
        assert maybeFile.isPresent() : "No file generated";

        JavaFileObject generated = maybeFile.get();
        String json = generated.getCharContent(false).toString();
        System.out.println(json);
    }

    @Test
    public void testInitializerProcessor() throws IOException {
        JavaFileObject source = JavaFileObjects.forSourceString(
                "test.EntrypointClass",
                """
               package test;
               \s
               import net.ramixin.stator.entrypointData.Entrypoint;
               import net.ramixin.stator.entrypointData.Entrypoint.Side;
               import net.ramixin.stator.networking.ClientNetworking;
               import net.ramixin.stator.registration.ClientRegistration;
               import net.ramixin.stator.Platform;
               \s
               public class EntrypointClass {
                   \s
                   @Entrypoint(Entrypoint.Side.CLIENT)
                   public void onInitialize(ClientNetworking networking, ClientRegistration registration, Platform platform) { }
                   \s
                   \s
               }"""
        );

        Compilation compilation = Compiler.javac()
                .withProcessors(new EntrypointProcessor())
                .compile(source);

        assertThat(compilation)
                .succeeded();

        Optional<JavaFileObject> maybeFile = compilation.generatedFile(
                StandardLocation.CLASS_OUTPUT,
                "",
                "META-INF/stator/initializers.json"
        );
        assert maybeFile.isPresent() : "No file generated";

        JavaFileObject generated = maybeFile.get();
        String json = generated.getCharContent(false).toString();
        System.out.println(json);
    }

    @Test
    public void testDispatcherProcessor() throws IOException {
        JavaFileObject source = JavaFileObjects.forSourceString(
                "test.DispatcherClass",
                """
               package test;
               \s
               import net.ramixin.stator.events.dispatching.Dispatcher;
               import net.ramixin.stator.events.Event;
               import net.ramixin.stator.events.contexts.BlockBrokenContext;
               import net.ramixin.stator.events.annotations.BlockBrokenEvent;
               import net.ramixin.stator.events.annotations.PlayerJoinedServerEvent;
               import net.ramixin.stator.events.contexts.PlayerJoinedServerContext;
               import net.ramixin.stator.Platform;
               \s
               public class DispatcherClass {
                   \s
                   @Dispatcher(event = BlockBrokenEvent.class, loader = Platform.FABRIC)
                   public static void blockBrokenDispatcher(Event<BlockBrokenContext, Void> event) { }
                   \s
                   @Dispatcher(event = BlockBrokenEvent.class, loader = Platform.NEOFORGE)
                   public static void otherBlockBrokenEvent(Event<BlockBrokenContext, Void> event) { }
                   \s
               }"""
        );

        Compilation compilation = Compiler.javac()
                .withProcessors(new DispatcherProcessor())
                .compile(source);

        assertThat(compilation)
                .succeeded();

        Optional<JavaFileObject> maybeFile = compilation.generatedFile(
                StandardLocation.CLASS_OUTPUT,
                "",
                "META-INF/stator/dispatchers.json"
        );
        assert maybeFile.isPresent() : "No file generated";

        JavaFileObject generated = maybeFile.get();
        String json = generated.getCharContent(false).toString();
        System.out.println(json);
    }

}
