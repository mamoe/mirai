package net.mamoe.mirai.utils.setting;


import org.ini4j.Profile;

import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * @author NaturalHG
 */
public class MiraiSettingListSection extends Vector<Object> implements MiraiSettingSection {
    private Lock lock = new ReentrantLock();

    @SuppressWarnings("unchecked")
    public <T> T getAs(int index){
        return (T)super.get(index);
    }

    public int getInt(int index){
        return this.getAs(index);
    }

    public int getDouble(int index){
        return this.getAs(index);
    }

    public int getString(int index){
        return this.getAs(index);
    }

    public int getFloat(int index) {
        return this.getAs(index);
    }

    @Override
    public synchronized void saveAsSection(Profile.Section section) {
        section.clear();
        AtomicInteger integer = new AtomicInteger(0);
        this.forEach(a -> {
            section.put(String.valueOf(integer.getAndAdd(1)),a);
        });
    }

    @Override
    public void close() throws IOException {

    }

}
