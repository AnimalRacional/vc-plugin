package com.example.examplemod;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
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
        try{
            if (isRecording){
                isRecording = false;

                if (this.decoder != null) {
                    this.decoder.close();
                }

                Path audioPath = audiosPath.resolve(getUuid().toString() + ".pcm");

                Files.deleteIfExists(audioPath);
                Files.createFile(audioPath);

                /*
                try (VAD vad = new VAD()) {
                    boolean isSpeech = vad.isSpeech(pcm);
                    ExampleMod.LOGGER.info("is speech: {}", isSpeech);
                }
                 */

                DataOutputStream dos = new DataOutputStream(new FileOutputStream(audioPath.toString()));
                for (int i=0; i< currentRecordingIndex; i++) {
                    dos.writeShort(recording[i]);
                }
                dos.close();

                ExampleMod.LOGGER.info("Wrote recording to file");

                ExampleVoicechatPlugin.removeFromCache(audioPath);

            }
        }catch (IOException e){
            ExampleMod.LOGGER.error(e.getMessage());
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
