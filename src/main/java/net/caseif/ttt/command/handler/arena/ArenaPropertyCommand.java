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

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import net.caseif.flint.arena.Arena;
import net.caseif.flint.metadata.persist.PersistentMetadata;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class ArenaPropertyCommand extends CommandHandler {

    private static final ImmutableMap<String, Class<?>> VALID_PROPERTIES = ImmutableMap.<String, Class<?>>of(
            MetadataKey.Arena.PROPERTY_MIN_PLAYERS, Integer.class,
            MetadataKey.Arena.PROPERTY_MAX_PLAYERS, Integer.class
    );

    public ArenaPropertyCommand(CommandSender sender, String[] args) {
        super(sender, args);
    }

    @Override
    public void handle() {
        if (args.length > 4) {
            printInvalidArgsError();
            return;
        }

        Optional<Arena> arena = TTTCore.mg.getArena(args[1]);
        if (!arena.isPresent()) {
            TTTCore.locale.getLocalizable("error.arena.dne").sendTo(sender);
            return;
        }

        String propKey = args[2].toLowerCase();

        if (!VALID_PROPERTIES.containsKey((propKey))) {
            TTTCore.locale.getLocalizable("error.arena.setprop.invalid")
                    .withReplacements(Color.EM + propKey + Color.INFO).sendTo(sender);
        }

        Optional<PersistentMetadata> propStruct
                = arena.get().getPersistentMetadata().get(MetadataKey.Arena.PROPERTY_CAT);

        if (args.length == 3) {
            Optional<?> prop;
            if (!propStruct.isPresent() || (prop = propStruct.get().get(propKey)).isPresent()) {
                sender.sendMessage(Color.INFO + propKey + ": " + ChatColor.DARK_GRAY + ChatColor.ITALIC
                        + TTTCore.locale.getLocalizable("fragment.not-set").localizeFor(sender));
                return;
            }
            sender.sendMessage(Color.INFO + propKey + ": " + Color.SECONDARY + prop.get().toString());
        } else if (args.length == 4) {
            if (!propStruct.isPresent()) {
                propStruct = Optional
                        .of(arena.get().getPersistentMetadata().createStructure(MetadataKey.Arena.PROPERTY_CAT));
            }

            Object value;
            if (VALID_PROPERTIES.get(propKey) == Integer.class) {
                try {
                    propStruct.get().set(propKey, value = Integer.parseInt(args[3]));
                } catch (NumberFormatException ex) {
                    TTTCore.locale.getLocalizable("error.arena.setprop.invalid-type")
                            .withReplacements(Color.EM + propKey + Color.INFO).sendTo(sender);
                    return;
                }
            } else {
                value = null; // should never execute
            }

            TTTCore.locale.getLocalizable("info.personal.arena.setprop.success")
                    .withReplacements(Color.EM + propKey + Color.INFO, Color.EM + value + Color.INFO,
                            Color.EM + arena.get().getId() + Color.INFO).sendTo(sender);
        }
    }

}
