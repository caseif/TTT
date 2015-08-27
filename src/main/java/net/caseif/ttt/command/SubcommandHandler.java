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
import net.caseif.ttt.util.Constants.Color;

import org.bukkit.command.CommandSender;

public abstract class SubcommandHandler {

    protected CommandSender sender;
    protected String[] args;
    protected String perm;

    public SubcommandHandler(CommandSender sender, String[] args, String perm) {
        this.sender = sender;
        this.args = args;
        this.perm = perm;
    }

    public abstract void handle();

    /**
     * Asserts that the sender has permission to use a subcommand. Sends an error message if not.
     *
     * @return whether the sender has permission to use a subcommand
     */
    public boolean assertPermission() {
        if (perm != null && !sender.hasPermission(perm)) {
            TTTCore.locale.getLocalizable("error.perms.generic").withPrefix(Color.ERROR.toString())
                    .sendTo(sender);
            return false;
        }
        return true;
    }

    /**
     * Retrieves the usage for this subcommand from the plugin.yml file.
     *
     * @return the usage for this subcommand, or null if not specified
     */
    public String getUsage() {
        return CommandManager.getUsage(args[0]);
    }

    public void sendUsage() {
        TTTCore.locale.getLocalizable("fragment.usage").withPrefix(Color.INFO.toString()).withReplacements(getUsage())
                .sendTo(sender);
    }
}
