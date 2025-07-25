package com.example.examplemod;

import de.maxhenkel.voicechat.api.*;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import net.minecraft.server.level.ServerLevel;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@ForgeVoicechatPlugin
public class ExampleVoicechatPlugin implements VoicechatPlugin {
    public static String FAGGOT_CATEGORY = "faggots";
    public static final int RECORDING_SIZE = 1024*1024;
    private static short[] currentRecording;
    private static int currentRecordingIndex;
    private static ServerLevel currentRecordingLevel;
    public static boolean isRecording = false;
    private static OpusDecoder decoder = null;
    public static Path audiosPath = null;


    /**
     * @return the unique ID for this voice chat plugin
     */
    @Override
    public String getPluginId() {
        return ExampleMod.MOD_ID;
    }

    /**
     * Called when the voice chat initializes the plugin.
     *
     * @param api the voice chat API
     */
    @Override
    public void initialize(VoicechatApi api) {
        ExampleMod.LOGGER.info("Example voice chat plugin initialized!");
        ExampleMod.vcApi = api;
    }

    /**
     * Called once by the voice chat to register all events.
     *
     * @param registration the event registration
     */
    @Override
    public void registerEvents(EventRegistration registration) {
        // TODO register your events
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophonePacket, 100);
        registration.registerEvent(VoicechatServerStartedEvent.class, this::onServerStarted, 100);
    }

    private void onMicrophonePacket(MicrophonePacketEvent e){
        if (isRecording){

            if (decoder == null) {
                ExampleMod.LOGGER.warn("Decoder is not initialized!");
                return;
            }
            try {
                short[] packet = decoder.decode(e.getPacket().getOpusEncodedData());
                ExampleMod.LOGGER.info("Decoded {} samples", packet.length);

                if (packet.length + currentRecordingIndex < RECORDING_SIZE){
                    System.arraycopy(packet, 0, currentRecording, currentRecordingIndex, packet.length);
                    currentRecordingIndex += packet.length;
                    ExampleMod.LOGGER.info("Sucessfully added samples to currentRecording!");
                } else {
                    ExampleMod.LOGGER.warn("Recording buffer full!");
                    stopRecording();
                }
            } catch (Exception ex) {
                ExampleMod.LOGGER.error("Error decoding packet: {}", ex.getMessage());
            }
        }
    }

    private void onServerStarted(VoicechatServerStartedEvent event) {
        VoicechatServerApi api = event.getVoicechat();

        VolumeCategory faggots = api.volumeCategoryBuilder()
                .setId(FAGGOT_CATEGORY)
                .setName("Faggots")
                .setDescription("The volume of all faggots...")
                .setIcon(null)
                .build();

        api.registerVolumeCategory(faggots);
        currentRecording = new short[RECORDING_SIZE];
    }

    public static void stopRecording() {
        try{
            if (isRecording){
                isRecording = false;

                if (decoder != null) {
                    decoder.close();
                    decoder = null;
                }

                Path audioPath = audiosPath.resolve("audio.pcm");

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
                    dos.writeShort(currentRecording[i]);
                }
                dos.close();

                ExampleMod.LOGGER.info("Wrote recording to file");

            }
        }catch (IOException e){
            ExampleMod.LOGGER.error(e.getMessage());
        }
    }

    public static void startRecording(ServerLevel level) {
        currentRecordingIndex = 0;
        isRecording = true;
        currentRecordingLevel = level;
        if (audiosPath == null) {
            audiosPath = currentRecordingLevel.getLevel().getServer().getWorldPath(ExampleMod.AUDIOS);
        }
        decoder = ExampleMod.vcApi.createDecoder();
    }

}
