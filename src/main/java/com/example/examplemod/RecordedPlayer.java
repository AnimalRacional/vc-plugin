package com.example.examplemod;

import de.maxhenkel.voicechat.api.opus.OpusDecoder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class RecordedPlayer {
    public static final int RECORDING_SIZE = 1024*1024;
    private OpusDecoder decoder = null;
    private final short[] recording;
    private int currentRecordingIndex;
    private long recordsCount;
    private boolean isRecording = false;
    private final UUID uuid;
    private final Path userPath;
    public static Path audiosPath;
    public static final int RECORDING_LIMIT = 50;

    public RecordedPlayer(UUID uuid) {
        this.uuid = uuid;
        this.recording = new short[RECORDING_SIZE];
        userPath = audiosPath.resolve(uuid.toString());
        recordsCount = 0;
        if(!Files.exists(userPath)){
            try {
                Files.createDirectory(userPath);
            } catch (IOException e) {
                ExampleMod.LOGGER.error("Error creating directory " + userPath);
                return;
            }
        }
        try{
            // TODO isto le muitos ficheiros duma vez, talvez usar DirectoryStream
            File[] files = userPath.toFile().listFiles();
            if(files == null){
                ExampleMod.LOGGER.error("User path {} not found!", userPath);
                throw new RuntimeException(String.format("User path %s not found!", userPath));
            }
            for(File f : files){
                String name = f.getName();
                if(name.startsWith(uuid.toString())){
                    String ending = name.substring(name.lastIndexOf('-')+1, name.lastIndexOf('.'));
                    try {
                        int num = Integer.parseInt(ending);
                        if(num > recordsCount){
                            recordsCount = num;
                        }
                    } catch(NumberFormatException e){
                        ExampleMod.LOGGER.warn("Invalid audio found in folder {}", uuid);
                    }
                } else {
                    ExampleMod.LOGGER.warn("Unknown audio found in folder {}", uuid);
                }
            }
        } catch(Exception e){
            ExampleMod.LOGGER.error("error on recordedplayer: {}", e.getMessage());
        }
        updateRecordingCount();
    }

    private void updateRecordingCount(){
        recordsCount++;
        recordsCount %= RECORDING_LIMIT;
    }

    public Path getAudio(int index){
        return userPath.resolve(uuid.toString() + "-" + index + ".pcm");
    }

    public void stopRecording() {
        if (isRecording){
            isRecording = false;

            if (this.decoder != null) {
                this.decoder.close();
            }

            Path audioPath = userPath.resolve(getUuid().toString() + "-" + recordsCount + ".pcm");

            new AudioSaver(audioPath, currentRecordingIndex, recording).start();

            ExampleVoicechatPlugin.removeFromCache(audioPath);
            updateRecordingCount();
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
