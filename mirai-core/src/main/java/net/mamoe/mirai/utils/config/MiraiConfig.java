package net.mamoe.mirai.utils.config;

import net.mamoe.mirai.MiraiServer;
import net.mamoe.mirai.utils.Utils;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.util.*;

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
            Utils.writeFile(this.root, content);
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
        try {
            return yaml.loadAs(Utils.readFile(file), LinkedHashMap.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new LinkedHashMap<>();
    }


}
