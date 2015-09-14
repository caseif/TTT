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
package net.caseif.ttt.command.admin;

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.command.SubcommandHandler;
import net.caseif.ttt.util.Constants;
import net.caseif.ttt.util.Constants.Color;

import com.google.common.base.Optional;
import net.caseif.flint.arena.Arena;
import net.caseif.flint.round.Round;
import org.bukkit.command.CommandSender;

public class StartCommand extends SubcommandHandler {

    public StartCommand(CommandSender sender, String[] args) {
        super(sender, args, "ttt.admin.start");
    }

    @Override
    public void handle() {
        if (assertPermission()) {
            if (args.length > 1) {
                String arenaName = args[1];
                Optional<Arena> arena = TTTCore.mg.getArena(arenaName);
                if (arena.isPresent()) {
                    if (arena.get().getRound().isPresent()) {
                        Round round = arena.get().getRound().get();
                        if (round.getChallengers().size() > 1) {
                            round.setLifecycleStage(Constants.Stage.PLAYING);
                            round.setTime(0);
                            TTTCore.locale.getLocalizable("info.personal.arena.set-stage.playing.success")
                                    .withPrefix(Color.INFO.toString())
                                    .withReplacements(Color.ARENA + arena.get().getName() + Color.INFO).sendTo(sender);
                        } else {
                            TTTCore.locale.getLocalizable("error.arena.too-few-players")
                                    .withPrefix(Color.ERROR.toString())
                                    .sendTo(sender);
                        }
                    } else {
                        TTTCore.locale.getLocalizable("error.round.dne").withPrefix(Color.ERROR.toString())
                                .withReplacements(Color.ARENA + arena.get().getName() + Color.INFO).sendTo(sender);
                    }
                } else {
                    TTTCore.locale.getLocalizable("error.round.dne").withPrefix(Color.ERROR.toString())
                            .withReplacements(Color.ARENA + arena.get().getName() + Color.INFO).sendTo(sender);
                }
            } else {
                TTTCore.locale.getLocalizable("error.command.too-few-args").withPrefix(Color.ERROR.toString())
                        .sendTo(sender);
                sendUsage();
            }
        }
    }
}
