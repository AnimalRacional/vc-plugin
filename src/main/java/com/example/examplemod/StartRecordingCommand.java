package com.example.examplemod;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class StartRecordingCommand {
    public static final int PERMISSION_LEVEL = 2;
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("startRecording").requires((cmdSrc) -> {
            return cmdSrc.hasPermission(PERMISSION_LEVEL);
        }).executes((cmdSrc) -> {

            ExampleVoicechatPlugin.startRecording(cmdSrc.getSource().getLevel());
            cmdSrc.getSource().sendSuccess(() -> {
                return Component.literal("Started Recording...");
            }, false);

            return 1;
        }));
    }
}
