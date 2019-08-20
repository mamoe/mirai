package net.mamoe.mirai.utils.config;

import net.mamoe.mirai.MiraiServer;
import net.mamoe.mirai.utils.Utils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * YAML-TYPE CONFIG
 * Thread SAFE
 * @author NaturalHG
 */
public class MiraiConfig extends MiraiConfigSection<Object>{

    private volatile File root;

    public MiraiConfig(File file){
        super();
        if(!file.toURI().getPath().contains(MiraiServer.getInstance().getParentFolder().getPath())){
            file = new File((MiraiServer.getInstance().getParentFolder().getPath() + "/" + file).replace("//","/"));
        }

        this.root = file;

        if(!file.exists()){
            try {
                if(!file.createNewFile()){
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.parse();
    }

    private MiraiConfig(){

    }

    public synchronized void save(){
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(dumperOptions);
        String content = yaml.dump(this);
        try {
            Utils.writeFile(this.root,content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void parse(){
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(dumperOptions);
        this.clear();
        try {
            Map<String,Object> content = yaml.loadAs(Utils.readFile(this.root), LinkedHashMap.class);
            if(content != null) {
                this.putAll(content);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
