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

import static net.caseif.ttt.listeners.wizard.WizardListener.WIZARDS;
import static net.caseif.ttt.listeners.wizard.WizardListener.WIZARD_INFO;

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.command.handler.CommandHandler;
import net.caseif.ttt.util.constant.Color;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreateArenaCommand extends CommandHandler {

    public CreateArenaCommand(CommandSender sender, String[] args) {
        super(sender, args);
    }

    @Override
    public void handle() {
        if (!WIZARDS.containsKey(((Player) sender).getUniqueId())) {
            WIZARDS.put(((Player) sender).getUniqueId(), 0);
            WIZARD_INFO.put(((Player) sender).getUniqueId(), new Object[4]);
            TTTCore.locale.getLocalizable("info.personal.arena.create.welcome").withPrefix(Color.INFO).sendTo(sender);
            TTTCore.locale.getLocalizable("info.personal.arena.create.exit-note").withPrefix(Color.INFO)
                    .withReplacements(TTTCore.locale.getLocalizable("info.personal.arena.create.cancel-keyword")
                            .withPrefix(Color.EM).withSuffix(Color.INFO)).sendTo(sender);
        } else {
            TTTCore.locale.getLocalizable("error.arena.create.already").withPrefix(Color.ALERT).sendTo(sender);
        }
    }

}
