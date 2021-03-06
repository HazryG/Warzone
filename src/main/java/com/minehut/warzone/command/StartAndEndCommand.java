package com.minehut.warzone.command;

import com.google.common.base.Optional;
import com.minehut.warzone.chat.LocalizedChatMessage;
import com.minehut.warzone.module.modules.team.TeamModule;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.minehut.warzone.GameHandler;
import com.minehut.warzone.chat.ChatConstant;
import com.minehut.warzone.match.MatchState;
import com.minehut.warzone.module.modules.startTimer.StartTimer;
import com.minehut.warzone.module.modules.timeLimit.TimeLimit;
import com.minehut.warzone.util.ChatUtil;
import com.minehut.warzone.util.Teams;
import org.bukkit.command.CommandSender;

public class StartAndEndCommand {
    private static int timer;
    private static boolean waiting = false;

    @Command(aliases = {"start", "begin"}, desc = "Starts the match.", usage = "[time]", flags = "f")
    @CommandPermissions("cardinal.match.start")
    public static void start(CommandContext cmd, CommandSender sender) throws CommandException {
        if (GameHandler.getGameHandler().getMatch().getState().equals(MatchState.WAITING)) {
            int time = 600;
            if (cmd.argsLength() > 0) time = cmd.getInteger(0) * 20;
            GameHandler.getGameHandler().getMatch().start(time, cmd.hasFlag('f'));
        } else if (GameHandler.getGameHandler().getMatch().getState().equals(MatchState.STARTING)) {
            GameHandler.getGameHandler().getMatch().getModules().getModule(StartTimer.class).setTime(cmd.argsLength() > 0 ? cmd.getInteger(0) * 20 : 30 * 20);
            GameHandler.getGameHandler().getMatch().getModules().getModule(StartTimer.class).setForced(cmd.hasFlag('f'));
        } else if (GameHandler.getGameHandler().getMatch().getState().equals(MatchState.ENDED)) {
            throw new CommandException(new LocalizedChatMessage(ChatConstant.ERROR_NO_RESUME).getMessage(ChatUtil.getLocale(sender)));
        } else {
            throw new CommandException(new LocalizedChatMessage(ChatConstant.ERROR_NO_START).getMessage(ChatUtil.getLocale(sender)));
        }

    }

    @Command(aliases = {"end", "finish"}, desc = "Ends the match.", usage = "[team]", flags = "n")
    @CommandPermissions("cardinal.match.end")
    public static void end(CommandContext cmd, CommandSender sender) throws CommandException {
        if (!GameHandler.getGameHandler().getMatch().isRunning()) {
            throw new CommandException(ChatConstant.ERROR_NO_END.getMessage(ChatUtil.getLocale(sender)));
        }
        if (cmd.argsLength() > 0) {
            Optional<TeamModule> team = Teams.getTeamByName(cmd.getString(0));
            GameHandler.getGameHandler().getMatch().end(team.orNull());
        } else {
            if (cmd.hasFlag('n')) {
                GameHandler.getGameHandler().getMatch().end();
            } else {
                GameHandler.getGameHandler().getMatch().end(TimeLimit.getMatchWinner());
            }
        }
    }

}
