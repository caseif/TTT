/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013-2019, Max Roncace <me@caseif.net>
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
import net.caseif.ttt.util.constant.StructureType;
import net.caseif.ttt.util.helper.data.CollectionsHelper;

import com.google.common.collect.Lists;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StructureCommand extends CommandHandler {

    public StructureCommand(CommandSender sender, String[] args) {
        super(sender, args);
    }

    @Override
    public void handle() {
        String arenaId = TTTCore.ARENA_EDITORS.get(((Player) sender).getUniqueId());
        if (arenaId == null) {
            TTTCore.locale.getLocalizable("error.arena.not-editing").withPrefix(Color.ALERT).sendTo(sender);
            return;
        }

        switch (args[1]) {
            case StructureType.TRAITOR_TESTER: {
                //TODO: implement
                break;
            }
            default: {
                TTTCore.locale.getLocalizable("error.arena.invalid-structure").withPrefix(Color.ALERT).sendTo(sender);
                TTTCore.locale.getLocalizable("error.arena.available-structures").withPrefix(Color.INFO)
                        .withReplacements(Color.SECONDARY + CollectionsHelper.prettyList(
                                Lists.newArrayList(StructureType.TRAITOR_TESTER)
                        ) + Color.INFO).sendTo(sender);
            }
        }
    }

}
