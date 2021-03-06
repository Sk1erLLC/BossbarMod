package club.sk1er.bossbarcustomizer.forge;

import club.sk1er.bossbarcustomizer.asm.GuiIngameTransformer;
import club.sk1er.bossbarcustomizer.forge.transform.ITransformer;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.Collection;
import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

public class ClassTransformer implements IClassTransformer {

    private static final Logger LOGGER = LogManager.getLogger("BossbarCustomizer");
    private final Multimap<String, ITransformer> transformerMap = ArrayListMultimap.create();

    public ClassTransformer() {
        registerTransformer(new GuiIngameTransformer());
    }

    private void registerTransformer(ITransformer transformer) {
        for (String cls : transformer.getClassName()) {
            transformerMap.put(cls, transformer);
        }
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (bytes == null) return null;

        Collection<ITransformer> transformers = transformerMap.get(transformedName);
        if (transformers.isEmpty()) return bytes;

        ClassReader reader = new ClassReader(bytes);
        ClassNode node = new ClassNode();
        reader.accept(node, ClassReader.EXPAND_FRAMES);

        for (ITransformer transformer : transformers) {
            transformer.transform(node, transformedName);
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

        try {
            node.accept(writer);
        } catch (Throwable t) {
            LOGGER.error("Exception when transforming " + transformedName + " : " + t.getClass().getSimpleName(), t);
        }

        return writer.toByteArray();
    }
}
