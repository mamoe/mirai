package net.mamoe.mirai.utils.config;
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class MiraiConfigSection<T> extends MiraiAbstractConfigSection<String, T>{

    public MiraiConfigSection(){

    }


    public Integer getInt(String key){
        return Integer.valueOf(this.get(key).toString());
    }

    public Double getDouble(String key){
        return Double.valueOf(this.get(key).toString());
    }

    public Float getFloat(String key){
        return Float.valueOf(this.get(key).toString());
    }

    public String getString(String key){
        return String.valueOf(this.get(key));
    }

    @SuppressWarnings("unchecked")
    public <D extends T> MiraiConfigSection<D> getTypedSection(String key){
        var content = (SortedMap<String,D>) this.get(key);
        return new MiraiConfigSection<>(){{
           setContent(Collections.synchronizedSortedMap(content));
        }};
    }

    @SuppressWarnings("unchecked")
    public MiraiConfigSection<Object> getSection(String key){
        var content = (SortedMap<String,Object>) this.get(key);
        return new MiraiConfigSection<>(){{
            setContent(Collections.synchronizedSortedMap(content));
        }};
    }

}
