/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013-2015, Maxim Roncacé <mproncace@lapis.blue>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.caseif.ttt.command;

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.command.handler.CommandHandler;
import net.caseif.ttt.command.handler.arena.AddSpawnCommand;
import net.caseif.ttt.command.handler.arena.CreateArenaCommand;
import net.caseif.ttt.command.handler.arena.ImportCommand;
import net.caseif.ttt.command.handler.arena.ListSpawnsCommand;
import net.caseif.ttt.command.handler.arena.RemoveArenaCommand;
import net.caseif.ttt.command.handler.arena.RemoveSpawnCommand;
import net.caseif.ttt.command.handler.misc.DefaultCommand;
import net.caseif.ttt.command.handler.misc.HelpCommand;
import net.caseif.ttt.command.handler.misc.ReloadCommand;
import net.caseif.ttt.command.handler.player.BanCommand;
import net.caseif.ttt.command.handler.player.KickCommand;
import net.caseif.ttt.command.handler.player.PardonCommand;
import net.caseif.ttt.command.handler.player.RespawnCommand;
import net.caseif.ttt.command.handler.player.RoleCommand;
import net.caseif.ttt.command.handler.player.SlayCommand;
import net.caseif.ttt.command.handler.round.EndCommand;
import net.caseif.ttt.command.handler.round.PrepareCommand;
import net.caseif.ttt.command.handler.round.RolesCommand;
import net.caseif.ttt.command.handler.round.StartCommand;
import net.caseif.ttt.command.handler.use.JoinCommand;
import net.caseif.ttt.command.handler.use.LeaveCommand;
import net.caseif.ttt.command.handler.use.ListArenasCommand;
import net.caseif.ttt.util.Constants.Color;

import com.google.common.collect.ImmutableMap;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

public class CommandManager implements CommandExecutor {

    public static final ImmutableMap<String, CommandRef> commands;

    static {
        Map<String, CommandRef> map = new HashMap<>();

        // arena
        addRef(map, "addspawn", AddSpawnCommand.class, "superadmin", "[arena name] {[x] [y] [z]}", 2, true);
        addRef(map, "carena", CreateArenaCommand.class, "superadmin", "", 1, false);
        addRef(map, "import", ImportCommand.class, "superadmin", "[arena name]", 2, true);
        addRef(map, "listspawns", ListSpawnsCommand.class, "superadmin", "[arena name]", 2, true);
        addRef(map, "rarena", RemoveArenaCommand.class, "superadmin", "[arena name]", 2, true);
        addRef(map, "removespawn", RemoveSpawnCommand.class, "superadmin", "[arena name] [index]|[[x] [y] [z]]", 2,
                true);

        // misc
        addRef(map, "help", HelpCommand.class, null, "{command}", 1, true);
        addRef(map, "reload", ReloadCommand.class, "superadmin", "", 1, true);

        // player
        addRef(map, "ban", BanCommand.class, "admin", "[player name]", 2, true);
        addRef(map, "kick", KickCommand.class, "admin", "[player name]", 2, true);
        addRef(map, "pardon", PardonCommand.class, "admin", "[player name] {minutes}", 2, true);
        addRef(map, "respawn", RespawnCommand.class, "admin", "[player name]", 2, true);
        addRef(map, "role", RoleCommand.class, "admin", "[player name]", 2, true);
        addRef(map, "slay", SlayCommand.class, "admin", "[player name]", 2, true);

        // round
        addRef(map, "end", EndCommand.class, "admin", "[arena name] {victor (t/i)}", 2, true);
        addRef(map, "prepare", PrepareCommand.class, "admin", "[arena name]", 2, true);
        addRef(map, "roles", RolesCommand.class, "admin", "[arena name]", 2, true);
        addRef(map, "start", StartCommand.class, "admin", "[arena name]", 2, true);

        // use
        addRef(map, "join", JoinCommand.class, "use", "[arena name]", 2, false);
        addRef(map, "leave", LeaveCommand.class, "use", "", 1, false);
        addRef(map, "listarenas", ListArenasCommand.class, "use", "", 1, true);


        commands = ImmutableMap.copyOf(map);
    }

    private static void addRef(Map<String, CommandRef> map, String cmd, Class<? extends CommandHandler> clazz,
                               String perm, String usage, int minArgs, boolean consoleAllowed) {
        cmd = cmd.toLowerCase();
        map.put(cmd, new CommandRef(cmd, clazz, TTTCore.locale.getLocalizable("info.command.desc." + cmd),
                perm != null ? "ttt." + perm : null, "/ttt " + cmd + " " + usage, minArgs, consoleAllowed));
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (label.equalsIgnoreCase("ttt")) {
            if (args.length == 0) {
                new DefaultCommand(sender, args).handle();
                return true;
            }

            final String subCmd = args[0].equals("?") ? "help" : args[0].toLowerCase();

            if (commands.containsKey(subCmd)) {
                commands.get(subCmd).invoke(sender, args);
            } else {
                TTTCore.locale.getLocalizable("error.command.invalid-args")
                        .withPrefix(Color.ERROR).sendTo(sender);
            }

            return true;
        }

        return false;
    }

}
