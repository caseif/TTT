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

package net.caseif.ttt.command.handler.arena;

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.command.handler.CommandHandler;
import net.caseif.ttt.util.constant.Color;
import net.caseif.ttt.util.constant.MetadataKey;
import net.caseif.ttt.util.constant.Stage;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import net.caseif.flint.arena.Arena;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class EditArenaCommand extends CommandHandler {

    public EditArenaCommand(CommandSender sender, String[] args) {
        super(sender, args);
    }

    @Override
    public void handle() {
        if (TTTCore.ARENA_EDITORS.containsKey(((Player) sender).getUniqueId())) {
            TTTCore.locale.getLocalizable("error.arena.already-editing").withPrefix(Color.ALERT)
                    .withReplacements(Color.EM + TTTCore.ARENA_EDITORS.get(((Player) sender).getUniqueId())
                            + Color.ALERT).sendTo(sender);
        }

        String arenaId = args[1];
        Optional<Arena> arena = TTTCore.mg.getArena(arenaId);
        if (!arena.isPresent()) {
            TTTCore.locale.getLocalizable("error.arena.dne").withPrefix(Color.ALERT).sendTo(sender);
            return;
        }

        if (arena.get().getMetadata().containsKey(MetadataKey.Arena.EDITOR)) {
            TTTCore.locale.getLocalizable("error.arena.has-editor").withPrefix(Color.ALERT)
                    .withReplacements(Color.EM + Bukkit.getPlayer(arena.get()
                            .getMetadata().<UUID>get(MetadataKey.Arena.EDITOR).get()).getName() + Color.ALERT)
                    .sendTo(sender);
            return;
        }

        if (arena.get().getRound().isPresent() && arena.get().getRound().get().getChallengers().size() > 0
                && arena.get().getRound().get().getLifecycleStage() != Stage.EDITING) {
            TTTCore.locale.getLocalizable("error.arena.in-progress-editing").withPrefix(Color.ALERT)
                    .withReplacements(Color.EM + "/ttt end " + arena.get().getId() + Color.ALERT,
                            Color.EM + "/ttt kickall " + arena.get().getId() + Color.ALERT).sendTo(sender);
            return;
        }

        TTTCore.ARENA_EDITORS.put(((Player) sender).getUniqueId(), arena.get().getId());
        arena.get().getMetadata().set(MetadataKey.Arena.EDITOR, ((Player) sender).getUniqueId());
        arena.get().getOrCreateRound(ImmutableSet.of(Stage.EDITING)).addChallenger(((Player) sender).getUniqueId());
    }

}
