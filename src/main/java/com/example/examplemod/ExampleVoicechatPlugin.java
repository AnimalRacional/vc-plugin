package com.example.examplemod;

import de.maxhenkel.voicechat.api.*;
import de.maxhenkel.voicechat.api.events.*;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.UUID;

@ForgeVoicechatPlugin
public class ExampleVoicechatPlugin implements VoicechatPlugin {
    public static String FAGGOT_CATEGORY = "faggots";
    private static HashMap<UUID, RecordedPlayer> recordedPlayers;
    private static HashMap<Path, short[]> audioCache;

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
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophonePacket, 100);
        registration.registerEvent(VoicechatServerStartedEvent.class, this::onServerStarted, 100);
        registration.registerEvent(PlayerConnectedEvent.class, this::onPlayerConnected, 100);
        registration.registerEvent(PlayerDisconnectedEvent.class, this::onPlayerDisconnected, 100);
    }

    private void onMicrophonePacket(MicrophonePacketEvent e){
        if (e.getSenderConnection() != null){
            RecordedPlayer recordedPlayer = recordedPlayers.get(e.getSenderConnection().getPlayer().getUuid());
            recordedPlayer.recordPacket(e.getPacket().getOpusEncodedData());
        }
    }

    private void onPlayerConnected(PlayerConnectedEvent e){
        UUID playerUuid = e.getConnection().getPlayer().getUuid();
        recordedPlayers.put(playerUuid, new RecordedPlayer(playerUuid));
    }

    private void onPlayerDisconnected(PlayerDisconnectedEvent e){
        stopRecording(e.getPlayerUuid());
        recordedPlayers.remove(e.getPlayerUuid());
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
        recordedPlayers = new HashMap<>();
        audioCache = new HashMap<>();
    }

    public static void stopRecording(UUID uuid) {
        recordedPlayers.get(uuid).stopRecording();
    }

    public static void startRecording(UUID uuid) {
        recordedPlayers.get(uuid).startRecording();
    }

    public static RecordedPlayer getRecordedPlayer(UUID uuid) {
        return recordedPlayers.get(uuid);
    }

    public static short[] getAudio(Path path) {
        if (audioCache.containsKey(path)) {
            return audioCache.get(path);
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
                return audio;

            } catch (Exception e) {
                ExampleMod.LOGGER.error(e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }

    public static void removeFromCache(Path path){
        audioCache.remove(path);
    }

}
