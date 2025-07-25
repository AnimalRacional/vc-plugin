package com.example.examplemod;

import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.audiochannel.EntityAudioChannel;

import java.nio.file.Path;

public class AudioPlayer extends Thread{
    private final Path path;
    private final VoicechatServerApi api;
    private final EntityAudioChannel channel;
    public AudioPlayer(Path path, VoicechatServerApi api, EntityAudioChannel channel) {
        this.path = path;
        this.api = api;
        this.channel = channel;
    }

    @Override
    public void run() {
        short[] recording;
        try {
            recording = new AudioReader(path).read().get();
        } catch (Exception e) {
            ExampleMod.LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }

        de.maxhenkel.voicechat.api.audiochannel.AudioPlayer playerAudioPlayer = api.createAudioPlayer(channel, api.createEncoder(), recording);
        ExampleMod.LOGGER.info("AudioPlayer Created");

        playerAudioPlayer.startPlaying();
        ExampleMod.LOGGER.info("Playing Audio...");
    }
}
