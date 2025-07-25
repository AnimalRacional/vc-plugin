package com.example.examplemod;

import de.maxhenkel.voicechat.api.*;
import de.maxhenkel.voicechat.api.events.*;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ForgeVoicechatPlugin
public class ExampleVoicechatPlugin implements VoicechatPlugin {
    public static String FAGGOT_CATEGORY = "faggots";
    private static HashMap<UUID, RecordedPlayer> recordedPlayers;
    private static ConcurrentHashMap<Path, short[]> audioCache;

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
        audioCache = new ConcurrentHashMap<>();
    }

    public static void stopRecording(UUID uuid) {
        recordedPlayers.get(uuid).stopRecording();
        ExampleMod.LOGGER.info("Stopped recording for player: " + uuid.toString());
    }

    public static void startRecording(UUID uuid) {
        recordedPlayers.get(uuid).startRecording();
        ExampleMod.LOGGER.info("Recording started for player: " + uuid.toString());
    }

    public static RecordedPlayer getRecordedPlayer(UUID uuid) {
        return recordedPlayers.get(uuid);
    }

    public static ConcurrentHashMap<Path, short[]> getAudioCache() {
        return audioCache;
    }

    public static void removeFromCache(Path path){
        audioCache.remove(path);
    }

}
