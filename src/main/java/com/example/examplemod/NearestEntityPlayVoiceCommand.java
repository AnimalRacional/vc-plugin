package com.example.examplemod;

import com.mojang.brigadier.CommandDispatcher;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.audiochannel.AudioPlayer;
import de.maxhenkel.voicechat.api.audiochannel.EntityAudioChannel;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.UUID;

public class NearestEntityPlayVoiceCommand {
    public static final int PERMISSION_LEVEL = 2;
    public static final int bbX = 5;
    public static final int bbY = 5;
    public static final int bbZ = 5;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("playVoiceNearestEntity").requires((cmdSrc) -> {
            return cmdSrc.hasPermission(PERMISSION_LEVEL);
        }).executes((cmdSrc) -> {
            if (ExampleMod.vcApi instanceof VoicechatServerApi api){
                ServerLevel level = cmdSrc.getSource().getLevel();
                Player player = cmdSrc.getSource().getPlayerOrException();
                Vec3 playerPos = cmdSrc.getSource().getPosition();
                AABB aabb = new AABB(playerPos.x + bbX, playerPos.y + bbY, playerPos.z + bbZ,
                        playerPos.x - bbX,  playerPos.y -bbY,  playerPos.z -bbZ);

                Entity closestEntity = level.getNearestEntity(Pig.class, TargetingConditions.DEFAULT,
                        null, player.getX(), player.getY(), player.getZ(), aabb);

                if (closestEntity != null){
                    ExampleMod.LOGGER.info("Entity: " + closestEntity.getName());
                    String category = ExampleVoicechatPlugin.FAGGOT_CATEGORY;
                    UUID channelID = UUID.randomUUID();

                    EntityAudioChannel channel = api.createEntityAudioChannel(channelID, api.fromEntity(closestEntity));
                    if (channel == null) {
                        ExampleMod.LOGGER.error("Couldn't create channel");
                        return 0;
                    }
                    channel.setCategory(category); // The category of the audio channel
                    channel.setDistance(20); // The distance in which the audio channel can be heard

                    try {
                        Path audioPath = ExampleVoicechatPlugin.audiosPath.resolve("audio.pcm");
                        ExampleMod.LOGGER.info("Audio Path: " + audioPath);

                        File file = audioPath.toFile();

                        int numberOfShorts = (int)(file.length() / 2); // each short = 2 bytes
                        short[] decoded = new short[numberOfShorts];
                        ExampleMod.LOGGER.info("Short Array Size: " + decoded.length);

                        DataInputStream dis = new DataInputStream(new FileInputStream(file));
                        for (int i = 0; i < numberOfShorts; i++) {
                            decoded[i] = dis.readShort();
                        }
                        dis.close();
                        ExampleMod.LOGGER.info("Read from the file!");

                        AudioPlayer playerAudioPlayer = api.createAudioPlayer(channel, api.createEncoder(), decoded);
                        ExampleMod.LOGGER.info("AudioPlayer Created");

                        playerAudioPlayer.startPlaying();
                        ExampleMod.LOGGER.info("Playing Audio...");
                    } catch (Exception e) {
                        ExampleMod.LOGGER.error(e.getMessage());
                        throw new RuntimeException(e);
                    }


                } else{
                    ExampleMod.LOGGER.warn("No Entity Found");
                }

                return 1;
            }
            return 0;
        }));
    }

}
