import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import net.ramixin.stator.entrypoints.EntrypointProcessor;
import net.ramixin.stator.events.EventProcessor;
import net.ramixin.stator.events.dispatching.DispatcherProcessor;
import net.ramixin.stator.metadata.DispatchersMetafile;
import net.ramixin.stator.metadata.EntrypointsMetaFile;
import net.ramixin.stator.metadata.EventsMetaFile;
import net.ramixin.stator.metadata.StatorMetaFileException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.util.Optional;

import static com.google.testing.compile.CompilationSubject.assertThat;

public class TestProcessors {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestProcessors.class.getName());

    @Test
    public void testEventProcessor() throws IOException, StatorMetaFileException {
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
        LOGGER.info(json);

        ClassLoader loader = Util.inMemoryLoader(compilation);

        EventsMetaFile file = EventsMetaFile.read(generated.openReader(false), LOGGER, loader);
        LOGGER.info("{}", file);
    }

    @Test
    public void testInitializerProcessor() throws IOException, StatorMetaFileException {
        JavaFileObject source = JavaFileObjects.forSourceString(
                "test.EntrypointClass",
                """
               package test;
               \s
               import net.ramixin.stator.entrypoints.Entrypoint;
               import net.ramixin.stator.networking.ClientNetworking;
               import net.ramixin.stator.registration.ClientRegistration;
               import net.ramixin.stator.Platform;
               import net.ramixin.stator.entrypoints.Side;
               import net.ramixin.stator.entrypoints.Phase;
               \s
               public class EntrypointClass {
                   \s
                   @Entrypoint(side = Side.CLIENT, phase = Phase.INIT)
                   public static void onInitialize(ClientNetworking networking, ClientRegistration registration, Platform platform) { }
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
        LOGGER.info(json);

        ClassLoader loader = Util.inMemoryLoader(compilation);

        EntrypointsMetaFile file = EntrypointsMetaFile.read(generated.openReader(false), LOGGER, loader);
        LOGGER.info("{}", file);
    }

    @Test
    public void testDispatcherProcessor() throws IOException, StatorMetaFileException {
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
        LOGGER.info(json);

        ClassLoader loader = Util.inMemoryLoader(compilation);

        DispatchersMetafile file = DispatchersMetafile.read(generated.openReader(false), LOGGER, loader);
        LOGGER.info("{}", file);
    }

}
