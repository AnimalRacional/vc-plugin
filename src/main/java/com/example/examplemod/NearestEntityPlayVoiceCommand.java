package com.example.examplemod;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.audiochannel.AudioPlayer;
import de.maxhenkel.voicechat.api.audiochannel.EntityAudioChannel;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.nio.file.Path;
import java.util.Collection;
import java.util.UUID;

public class NearestEntityPlayVoiceCommand {
    public static final int PERMISSION_LEVEL = 2;
    private static final int CHANNEL_DISTANCE = 20;
    public static final int bbX = 5;
    public static final int bbY = 5;
    public static final int bbZ = 5;


    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("playVoiceNearestEntity").requires((cmdSrc) -> {
            return cmdSrc.hasPermission(PERMISSION_LEVEL);
        }).then(Commands.argument("targets", GameProfileArgument.gameProfile()).suggests((cmdSrc, suggestionsBuilder) -> {
            PlayerList playerlist = cmdSrc.getSource().getServer().getPlayerList();

            return SharedSuggestionProvider.suggest(playerlist.getPlayers().stream().map((player) -> {
                return player.getGameProfile().getName();
            }), suggestionsBuilder);
        }).executes((cmdSrc) -> {
            if (ExampleMod.vcApi instanceof VoicechatServerApi api){

                ServerLevel level = cmdSrc.getSource().getLevel();
                Vec3 srcPos = cmdSrc.getSource().getPosition();
                AABB aabb = new AABB(srcPos.x + bbX, srcPos.y + bbY, srcPos.z + bbZ,
                        srcPos.x - bbX,  srcPos.y -bbY,  srcPos.z -bbZ);
                Entity nearestEntity = level.getNearestEntity(Pig.class, TargetingConditions.DEFAULT,
                        null, srcPos.x, srcPos.y, srcPos.z, aabb);

                if (nearestEntity != null){
                    ExampleMod.LOGGER.info("Entity: " + nearestEntity.getName());
                    String category = ExampleVoicechatPlugin.FAGGOT_CATEGORY;
                    Collection<GameProfile> targets = GameProfileArgument.getGameProfiles(cmdSrc, "targets");

                    for (GameProfile target : targets) {
                        UUID channelID = UUID.randomUUID();
                        EntityAudioChannel channel = createChannel(api, channelID, category, nearestEntity);
                        short[] recording = getAudio(target.getId());
                        playAudio(recording, api, channel);
                    }

                } else{
                    ExampleMod.LOGGER.warn("No Entity Found");
                }

                return 1;
            }
            return 0;
        })));
    }

    private static short[] getAudio(UUID uuid) {
        Path audioPath = RecordedPlayer.audiosPath.resolve(uuid.toString() + ".pcm");
        ExampleMod.LOGGER.info("Audio Path: " + audioPath);
        return ExampleVoicechatPlugin.getAudio(audioPath);
    }

    private static void playAudio(short[] recording, VoicechatServerApi api, EntityAudioChannel channel) {
        AudioPlayer playerAudioPlayer = api.createAudioPlayer(channel, api.createEncoder(), recording);
        ExampleMod.LOGGER.info("AudioPlayer Created");

        playerAudioPlayer.startPlaying();
        ExampleMod.LOGGER.info("Playing Audio...");
    }

    private static EntityAudioChannel createChannel(VoicechatServerApi api, UUID channelID, String category, Entity nearestEntity) {
        EntityAudioChannel channel = api.createEntityAudioChannel(channelID, api.fromEntity(nearestEntity));
        if (channel == null) {
            ExampleMod.LOGGER.error("Couldn't create channel");
            return null;
        }
        channel.setCategory(category); // The category of the audio channel
        channel.setDistance(NearestEntityPlayVoiceCommand.CHANNEL_DISTANCE); // The distance in which the audio channel can be heard
        return channel;
    }

}
