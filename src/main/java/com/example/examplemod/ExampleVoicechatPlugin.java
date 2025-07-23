package com.example.examplemod;

import de.maxhenkel.voicechat.api.*;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;
import de.maxhenkel.voicechat.api.mp3.Mp3Encoder;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import net.minecraft.server.level.ServerLevel;

import javax.sound.sampled.AudioFormat;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@ForgeVoicechatPlugin
public class ExampleVoicechatPlugin implements VoicechatPlugin {

    public static String FAGGOT_CATEGORY = "faggots";
    public static final int RECORDING_SIZE = 1024*1024;
    private static short[] currentRecording = new short[RECORDING_SIZE];
    private static int currentRecordingIndex;
    private static ServerLevel currentRecordingLevel;
    public static boolean isRecording = false;

    private static OpusDecoder decoder = null;


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

    }

    public static void stopRecording() {
        try{
            if (isRecording){
                isRecording = false;

                if (decoder != null) {
                    decoder.close();
                    decoder = null;
                }

                Path audiosPath = currentRecordingLevel.getLevel().getServer().getWorldPath(ExampleMod.AUDIOS);
                Path audioPath = audiosPath.resolve("audio.mp3");

                Files.deleteIfExists(audioPath);
                Files.createFile(audioPath);

                short[] audioToEncode = new short[currentRecordingIndex];
                System.arraycopy(currentRecording, 0, audioToEncode, 0, currentRecordingIndex);

                FileOutputStream fos = new FileOutputStream(audioPath.toString());
                Mp3Encoder encoder = ExampleMod.vcApi.createMp3Encoder(new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                        48000, 16, 1, 2, 48000, false),
                        112, 1, fos);
                assert encoder != null;
                encoder.encode(audioToEncode);
                encoder.close();
                fos.close();

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
        currentRecording = new short[RECORDING_SIZE];
        decoder = ExampleMod.vcApi.createDecoder();
    }

}
