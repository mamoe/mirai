package net.mamoe.mirai.utils.config;

import org.ini4j.Profile;

import java.io.Closeable;
import java.util.Map;

public interface MiraiConfigSection extends Closeable {
    void saveAsSection(Profile.Section section);
}


