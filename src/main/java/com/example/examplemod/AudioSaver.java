package com.example.examplemod;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class AudioSaver extends Thread{
    private final Path path;
    private final int index;
    private final short[] recording;

    public AudioSaver(Path path, int index, short[] recording) {
        this.path = path;
        this.index = index;
        this.recording = recording;
    }
    @Override
    public void run() {
        try{
            Files.deleteIfExists(path);
            Files.createFile(path);

            DataOutputStream dos = new DataOutputStream(new FileOutputStream(path.toString()));
            for (int i=0; i< index; i++) {
                dos.writeShort(recording[i]);
            }
            dos.close();

            ExampleMod.LOGGER.info("Wrote recording to file");
        } catch (IOException e){
            ExampleMod.LOGGER.error(e.getMessage());
        }
    }
}
