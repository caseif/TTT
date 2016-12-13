/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013-2016, Max Roncace <me@caseif.net>
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
import net.caseif.ttt.command.handler.arena.ArenaInfoCommand;
import net.caseif.ttt.command.handler.arena.ArenaPropertyCommand;
import net.caseif.ttt.command.handler.arena.CreateArenaCommand;
import net.caseif.ttt.command.handler.arena.DoneCommand;
import net.caseif.ttt.command.handler.arena.EditArenaCommand;
import net.caseif.ttt.command.handler.arena.ImportCommand;
import net.caseif.ttt.command.handler.arena.ListSpawnsCommand;
import net.caseif.ttt.command.handler.arena.RemoveArenaCommand;
import net.caseif.ttt.command.handler.arena.RemoveSpawnCommand;
import net.caseif.ttt.command.handler.misc.DefaultCommand;
import net.caseif.ttt.command.handler.misc.HelpCommand;
import net.caseif.ttt.command.handler.misc.XyzzyCommand;
import net.caseif.ttt.command.handler.player.BanCommand;
import net.caseif.ttt.command.handler.player.KickCommand;
import net.caseif.ttt.command.handler.player.PardonCommand;
import net.caseif.ttt.command.handler.player.RespawnCommand;
import net.caseif.ttt.command.handler.player.RoleCommand;
import net.caseif.ttt.command.handler.player.SlayCommand;
import net.caseif.ttt.command.handler.round.EndCommand;
import net.caseif.ttt.command.handler.round.ForceEndCommand;
import net.caseif.ttt.command.handler.round.PrepareCommand;
import net.caseif.ttt.command.handler.round.RolesCommand;
import net.caseif.ttt.command.handler.round.StartCommand;
import net.caseif.ttt.command.handler.use.JoinCommand;
import net.caseif.ttt.command.handler.use.LeaveCommand;
import net.caseif.ttt.command.handler.use.ListArenasCommand;
import net.caseif.ttt.util.constant.Color;

import com.google.common.collect.ImmutableMap;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.LinkedHashMap;
import java.util.Map;

public class CommandManager implements CommandExecutor {

    public static final ImmutableMap<String, CommandRef> commands;

    static {
        Map<String, CommandRef> map = new LinkedHashMap<>();

        // use
        addRef(map, "join", JoinCommand.class, "use", "[arena name]", 1, false);
        addRef(map, "leave", LeaveCommand.class, "use", "", 1, false);
        addRef(map, "listarenas", ListArenasCommand.class, "use", "", 1, true);

        // arena
        addRef(map, "arenainfo", ArenaInfoCommand.class, "admin", "[arena name]", 1, true);
        addRef(map, "arenaprop", ArenaPropertyCommand.class, "superadmin", "[arena name] [property] [value]", 3, true);
        addRef(map, "createarena", CreateArenaCommand.class, "superadmin", "", 1, false, "carena");
        addRef(map, "editarena", EditArenaCommand.class, "superadmin", "", 2, false);
        addRef(map, "done", DoneCommand.class, "superadmin", "", 1, false);
        addRef(map, "import", ImportCommand.class, "superadmin", "[arena name]", 2, true);
        addRef(map, "removearena", RemoveArenaCommand.class, "superadmin", "[arena name]", 2, true, "rarena");
        addRef(map, "listspawns", ListSpawnsCommand.class, "superadmin", "[arena name]", 2, true);
        addRef(map, "addspawn", AddSpawnCommand.class, "superadmin", "[arena name] {[x] [y] [z]}", 2, true);
        addRef(map, "removespawn", RemoveSpawnCommand.class, "superadmin", "[arena name] [index]|[[x] [y] [z]]", 2,
                true);

        // player
        addRef(map, "role", RoleCommand.class, "admin", "[player name]", 2, true);
        addRef(map, "slay", SlayCommand.class, "admin", "[player name]", 2, true);
        addRef(map, "respawn", RespawnCommand.class, "admin", "[player name]", 2, true);
        addRef(map, "kick", KickCommand.class, "admin", "[player name]", 2, true);
        addRef(map, "ban", BanCommand.class, "admin", "[player name]", 2, true);
        addRef(map, "pardon", PardonCommand.class, "admin", "[player name] {minutes}", 2, true);

        // round
        addRef(map, "roles", RolesCommand.class, "admin", "[arena name]", 2, true);
        addRef(map, "prepare", PrepareCommand.class, "admin", "[arena name]", 2, true);
        addRef(map, "start", StartCommand.class, "admin", "[arena name]", 2, true);
        addRef(map, "end", EndCommand.class, "admin", "[arena name] {victor (t/i)}", 2, true);
        addRef(map, "forceend", ForceEndCommand.class, "admin", "[arena name] {victor (t/i)}", 2, true);

        // misc
        addRef(map, "help", HelpCommand.class, null, "{command}", 1, true, "?");
        addRef(map, "xyzzy", XyzzyCommand.class, null, "", 1, true, true);

        commands = ImmutableMap.copyOf(map);
    }

    private static void addRef(Map<String, CommandRef> map, String cmd, Class<? extends CommandHandler> clazz,
                               String perm, String usage, int minArgs, boolean consoleAllowed, boolean hidden,
                               String... aliases) {
        cmd = cmd.toLowerCase();
        CommandRef cr = new CommandRef(cmd, clazz, TTTCore.locale.getLocalizable("info.command.desc." + cmd),
                perm != null ? "ttt." + perm : null, "/ttt " + cmd + " " + usage, minArgs, consoleAllowed, aliases);
        map.put(cmd, cr);
        for (String alias : aliases) {
            map.put(alias, cr);
        }
    }

    private static void addRef(Map<String, CommandRef> map, String cmd, Class<? extends CommandHandler> clazz,
                               String perm, String usage, int minArgs, boolean consoleAllowed, String... aliases) {
        addRef(map, cmd, clazz, perm, usage, minArgs, consoleAllowed, false, aliases);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (label.equalsIgnoreCase("ttt")) {
            if (args.length == 0) {
                new DefaultCommand(sender, args).handle();
                return true;
            }

            final String subCmd = args[0].toLowerCase();

            if (commands.containsKey(subCmd)) {
                commands.get(subCmd).invoke(sender, args);
            } else {
                TTTCore.locale.getLocalizable("error.command.invalid-args")
                        .withPrefix(Color.ALERT).sendTo(sender);
            }

            return true;
        }

        return false;
    }

}
