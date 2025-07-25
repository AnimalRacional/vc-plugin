package com.example.examplemod;

import de.maxhenkel.voicechat.api.opus.OpusDecoder;

import java.nio.file.Path;
import java.util.UUID;

public class RecordedPlayer {
    public static final int RECORDING_SIZE = 1024*1024;
    private OpusDecoder decoder = null;
    private final short[] recording;
    private int currentRecordingIndex;
    private boolean isRecording = false;
    private final UUID uuid;
    public static Path audiosPath;

    public RecordedPlayer(UUID uuid) {
        this.uuid = uuid;
        this.recording = new short[RECORDING_SIZE];
    }

    public void stopRecording() {
        if (isRecording){
            isRecording = false;

            if (this.decoder != null) {
                this.decoder.close();
            }

            Path audioPath = audiosPath.resolve(getUuid().toString() + ".pcm");

            new AudioSaver(audioPath, currentRecordingIndex, recording).start();

            ExampleVoicechatPlugin.removeFromCache(audioPath);

        }
    }

    public void recordPacket(byte[] packet) {
        if (isRecording) {
            if (decoder == null) {
                ExampleMod.LOGGER.warn("Decoder is not initialized!");
                return;
            }
            try {
                short[] decodedPacket = decoder.decode(packet);
                if (decodedPacket.length + currentRecordingIndex < RECORDING_SIZE){
                    System.arraycopy(decodedPacket, 0, recording, currentRecordingIndex, decodedPacket.length);
                    currentRecordingIndex += decodedPacket.length;
                } else {
                    ExampleMod.LOGGER.warn("Recording buffer full!");
                    stopRecording();
                }
            } catch (Exception e) {
                ExampleMod.LOGGER.error("Error decoding packet: {}", e.getMessage());
            }
        }
    }

    public void startRecording() { // WARNING: Poderá ser melhor reniciar a gravação em vez de continuar caso este método seja chamado múltiplas vezes
        if (!isRecording) {
            decoder = ExampleMod.vcApi.createDecoder();
            currentRecordingIndex = 0;
            isRecording = true;
        }
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean isRecording() {
        return isRecording;
    }

}
