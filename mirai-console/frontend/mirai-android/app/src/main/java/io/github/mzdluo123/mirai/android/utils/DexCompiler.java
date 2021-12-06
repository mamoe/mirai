package io.github.mzdluo123.mirai.android.utils;

import android.util.Log;

import com.android.tools.r8.CompilationFailedException;
import com.android.tools.r8.D8;
import com.android.tools.r8.D8Command;
import com.android.tools.r8.OutputMode;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class DexCompiler {
    private File tempDir, pluginDir;

    public DexCompiler(File fileDir, File cache) {
        tempDir = cache;
        if (!tempDir.exists()){
            tempDir.mkdir();
        }
        pluginDir = new File(fileDir.getAbsolutePath(), "plugins");
    }

    public File compile(File jarFile, Boolean desugaring) throws CompilationFailedException, IOException {
        String outName = jarFile.getName().substring(0, jarFile.getName().length() - 4) +
                "-android.jar";
        File outFile = new File(tempDir, outName).getAbsoluteFile();
//        if (!outFile.exists()) {
//            outFile.createNewFile();
//        }
        Log.e("输出路径", outFile.getAbsolutePath());
        D8Command.Builder command = D8Command.builder()
                .addProgramFiles(jarFile.getAbsoluteFile().toPath())
                .setOutput(outFile.toPath(), OutputMode.DexIndexed)
                .setMinApiLevel(26);

        if (!desugaring) {
            command.setDisableDesugaring(true);
        }

//        String[] cmd = new String[3];
//        cmd[0] = "--output";
//        cmd[1] = outFile.getAbsolutePath();
//        cmd[2] = jarFile.getAbsolutePath();
//        D8Command command = D8Command.parse(cmd, Origin.root()).build();
        D8.run(command.build());
        return outFile;
    }

    public void copyResourcesAndMove(File origin, File newFile) throws IOException {
        ZipFile originZip = new ZipFile(origin);
        ZipFile newZip = new ZipFile(newFile);
        ArrayList<File> resources = new ArrayList<>();
        originZip.getFileHeaders().forEach(i -> {
            try {
                originZip.extractFile(i, tempDir.getAbsolutePath());
            } catch (ZipException e) {
                e.printStackTrace();
            }
        });

        for (File file : tempDir.listFiles()) {
            if (file.isFile() && !file.getName().equals(newFile.getName())) {
                resources.add(file);
            }
        }
        newZip.addFiles(resources);
        newZip.addFolder(new File(tempDir, "META-INF"));
        newFile.renameTo(new File(pluginDir, newFile.getName()));
    }
}
