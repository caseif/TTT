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

package net.caseif.ttt.lobby;

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.util.config.ConfigKey;
import net.caseif.ttt.util.constant.MetadataKey;
import net.caseif.ttt.util.constant.Stage;

import net.caseif.flint.lobby.LobbySign;
import net.caseif.flint.lobby.populator.LobbySignPopulator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class StatusLobbySignPopulator implements LobbySignPopulator {

    public static final int SIGN_HASTE_SWITCH_PERIOD = 4;

    private LobbySignPopulator defaultPop;

    public StatusLobbySignPopulator(LobbySignPopulator defaultPop) {
        this.defaultPop = defaultPop;
    }

    @Override
    public String first(LobbySign sign) {
        return defaultPop.first(sign);
    }

    @Override
    public String second(LobbySign sign) {
        return defaultPop.second(sign);
    }

    @Override
    public String third(LobbySign sign) {
        if (!sign.getArena().getRound().isPresent()) {
            return "";
        }
        if (!TTTCore.config.get(ConfigKey.HASTE)
                || sign.getArena().getRound().get().getLifecycleStage() != Stage.PLAYING
                || sign.getArena().getRound().get().getRemainingTime() == -1) {
            return defaultPop.third(sign);
        }
        if (System.currentTimeMillis() % (SIGN_HASTE_SWITCH_PERIOD * 1000 * 2)
                < SIGN_HASTE_SWITCH_PERIOD * 1000) {
            long seconds = sign.getArena().getRound().get().getRemainingTime();
            seconds -= sign.getArena().getRound().get().getMetadata().<Integer>get(MetadataKey.Round.HASTE_TIME).or(0);
            return ChatColor.DARK_PURPLE + ""
                    + seconds / 60 + ":" + (seconds % 60 >= 10 ? seconds % 60 : "0" + seconds % 60);
        } else {
            return ChatColor.RED + TTTCore.locale.getLocalizable("fragment.haste-mode").localize();
        }
    }

    @Override
    public String fourth(LobbySign sign) {
        return defaultPop.fourth(sign);
    }

}
