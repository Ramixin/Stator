import com.google.testing.compile.Compilation;

import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public interface Util {

    static ClassLoader inMemoryLoader(Compilation compilation) throws IOException {
        Map<String, byte[]> classBytes = Util.extractClassBytes(compilation);
        return new ClassLoader(Util.class.getClassLoader()) {
            @Override
            protected Class<?> findClass(String name) throws ClassNotFoundException {
                byte[] bytes = classBytes.get(name);
                if (bytes == null) throw new ClassNotFoundException(name);
                return defineClass(name, bytes, 0, bytes.length);
            }
        };
    }

    private static Map<String, byte[]> extractClassBytes(Compilation compilation) throws IOException {
        Map<String, byte[]> result = new HashMap<>();
        for (JavaFileObject file : compilation.generatedFiles()) {
            if (file.getKind() == JavaFileObject.Kind.CLASS) {
                String binaryName = toBinaryName(file.toUri());
                InputStream stream = file.openInputStream();
                result.put(binaryName, stream.readAllBytes());
                stream.close();
            }
        }
        return result;
    }

    private static String toBinaryName(URI uri) {
        String path = uri.getPath();
        String withoutExt = path.substring(0, path.length() - ".class".length());
        int classOutputIdx = withoutExt.indexOf("CLASS_OUTPUT");
        String relevant = classOutputIdx >= 0
                ? withoutExt.substring(classOutputIdx + "CLASS_OUTPUT".length())
                : withoutExt;
        return relevant.replaceFirst("^/", "").replace('/', '.');
    }

}
