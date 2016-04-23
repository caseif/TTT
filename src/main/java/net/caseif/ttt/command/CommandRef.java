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
import net.caseif.ttt.util.constant.Color;

import net.caseif.rosetta.Localizable;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Represents information regarding a specific command.
 */
public class CommandRef {

    private String cmd;
    private Constructor<? extends CommandHandler> constructor;
    private Localizable description;
    private String permission;
    private String usage;
    private int argCount;
    private boolean consoleAllowed;
    private String[] aliases;

    public CommandRef(String cmd, Class<? extends CommandHandler> handlerClass, Localizable desc, String perm,
                      String usage, int argCount, boolean consoleAllowed, String... aliases) {
        this.cmd = cmd;
        try {
            this.constructor = handlerClass.getConstructor(CommandSender.class, String[].class);
        } catch (NoSuchMethodException ex) {
            throw new AssertionError(ex);
        }
        this.description = desc;
        this.permission = perm;
        this.usage = usage;
        this.argCount = argCount;
        this.consoleAllowed = consoleAllowed;
        this.aliases = aliases;
    }

    public String getLabel() {
        return cmd;
    }

    public Constructor<? extends CommandHandler> getHandlerConstructor() {
        return constructor;
    }

    public Localizable getDescription() {
        return description;
    }

    public String getPermission() {
        return permission;
    }

    public String getUsage() {
        return usage;
    }

    public int getArgCount() {
        return argCount;
    }

    public boolean isConsoleAllowed() {
        return consoleAllowed;
    }

    public String[] getAliases() {
        return aliases;
    }

    public void invoke(CommandSender sender, String[] args) {
        if (!doAssertions(sender, args)) {
            return;
        }

        try {
            constructor.newInstance(sender, args).handle();
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new AssertionError(ex);
        } catch (InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }

    private boolean doAssertions(CommandSender sender, String[] args) {
        return assertPermission(sender) && assertPlayer(sender) && assertArgumentCount(sender, args);
    }

    /**
     * Asserts that the sender has permission to use a subcommand. Sends an error message if not.
     *
     * @return whether the sender has permission to use a subcommand
     */
    private boolean assertPermission(CommandSender sender) {
        if (getPermission() != null && !sender.hasPermission(getPermission())) {
            TTTCore.locale.getLocalizable("error.perms.generic").withPrefix(Color.ERROR)
                    .sendTo(sender);
            return false;
        }
        return true;
    }

    private boolean assertPlayer(CommandSender sender) {
        if (!isConsoleAllowed() && !(sender instanceof Player)) {
            TTTCore.locale.getLocalizable("error.command.ingame").withPrefix(Color.ERROR).sendTo(sender);
            return false;
        }
        return true;
    }

    private boolean assertArgumentCount(CommandSender sender, String[] args) {
        if (args.length < getArgCount()) {
            TTTCore.locale.getLocalizable("error.command.too-few-args").withPrefix(Color.ERROR)
                    .sendTo(sender);
            return false;
        }
        return true;
    }

}
