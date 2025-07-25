package com.example.examplemod;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class isRecordingCommand {
    public static final int PERMISSION_LEVEL = 2;
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("isRecording").requires((cmdSrc) -> {
            return cmdSrc.hasPermission(PERMISSION_LEVEL);
        }).executes((cmdSrc) -> {

            cmdSrc.getSource().sendSuccess(() -> {
                return Component.literal(ExampleVoicechatPlugin.isRecording ? "Recording" : "Not Recording");
            }, false);

            return 1;
        }));
    }
}
