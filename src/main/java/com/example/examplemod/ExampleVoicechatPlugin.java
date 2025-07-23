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
        //ExampleMod.LOGGER.info("packet! " + e.getPacket().isWhispering());
        if (isRecording){
            // Creates a new decoder instance with 48kHz mono
            OpusDecoder decoder = ExampleMod.vcApi.createDecoder();
            ExampleMod.LOGGER.info("Decoded Created!");

            // Decodes the encoded audio
            short[] packet = decoder.decode(e.getPacket().getOpusEncodedData());
            ExampleMod.LOGGER.info("Audio Decoded!");

            // Resets the decoder state
            decoder.resetState();

            // Closes the decoder - Not calling this will cause a memory leak!
            decoder.close();

            if (packet.length + currentRecordingIndex < RECORDING_SIZE){
                System.arraycopy(packet, 0, currentRecording, currentRecordingIndex, packet.length);
                currentRecordingIndex += packet.length;
                ExampleMod.LOGGER.info("Success!");
            } else {
                stopRecording();
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
                Path audiosPath = currentRecordingLevel.getLevel().getServer().getWorldPath(ExampleMod.AUDIOS);
                Path audioPath = audiosPath.resolve("audio.raw");

                Files.deleteIfExists(audioPath);
                Files.createFile(audioPath);

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
    }

}
