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

package net.caseif.ttt.lobby;

import net.caseif.ttt.util.helper.gamemode.RoundHelper;

import net.caseif.flint.lobby.LobbySign;
import net.caseif.flint.lobby.populator.LobbySignPopulator;
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
        return RoundHelper.getTimeDisplay(sign.getArena().getRound().get(), false, ChatColor.DARK_PURPLE);
    }

    @Override
    public String fourth(LobbySign sign) {
        return defaultPop.fourth(sign);
    }

}
