package net.mamoe.mirai.utils.config;

import kotlin.io.FilesKt;
import net.mamoe.mirai.MiraiServer;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * YAML-TYPE CONFIG
 * Thread SAFE
 *
 * @author NaturalHG
 */
public class MiraiConfig extends MiraiConfigSection<Object> {

    private final File root;

    public MiraiConfig(@NotNull String configName) {
        this(new File(MiraiServer.getInstance().getParentFolder(), Objects.requireNonNull(configName)));
    }

    public MiraiConfig(@NotNull File file) {
        super(parse(Objects.requireNonNull(file)));
        this.root = file;
    }

    public synchronized void save() {
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(dumperOptions);
        String content = yaml.dump(this.sortedMap);
        try {
            new ByteArrayInputStream(content.getBytes()).transferTo(new FileOutputStream(this.root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private static LinkedHashMap<String,Object> parse(File file) {
        if (!file.toURI().getPath().contains(MiraiServer.getInstance().getParentFolder().getPath())) {
            file = new File(MiraiServer.getInstance().getParentFolder().getPath(), file.getName());
        }
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    return new LinkedHashMap<>();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return new LinkedHashMap<>();
            }
        }
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(dumperOptions);
        return yaml.loadAs(String.join("\n", FilesKt.readLines(file, Charset.defaultCharset())), LinkedHashMap.class);
    }



}
