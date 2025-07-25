package com.example.examplemod;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AudioReader {
    private final Path path;
    private static ConcurrentHashMap<Path, short[]> audioCache;

    public AudioReader(Path path){
        this.path = path;
        audioCache = ExampleVoicechatPlugin.getAudioCache();
    }

    public Future<short[]> read() {
        short[] audioToPlay;
        if (audioCache.containsKey(path)) {
            audioToPlay = audioCache.get(path);
        } else {
            try {
                File file = path.toFile();

                int numberOfShorts = (int)(file.length() / 2); // each short = 2 bytes
                short[] audio = new short[numberOfShorts];
                ExampleMod.LOGGER.info("Short Array Size: " + audio.length);

                DataInputStream dis = new DataInputStream(new FileInputStream(file));
                for (int i = 0; i < numberOfShorts; i++) {
                    audio[i] = dis.readShort();
                }
                dis.close();
                ExampleMod.LOGGER.info("Read from the file!");

                audioCache.put(path, audio);
                audioToPlay = audio;

            } catch (Exception e) {
                ExampleMod.LOGGER.error(e.getMessage());
                throw new RuntimeException(e);
            }
        }
        return Executors.newSingleThreadExecutor().submit(() -> {
            // Read the audio file and store it in the cache
            return audioToPlay;
        });
    }
}
