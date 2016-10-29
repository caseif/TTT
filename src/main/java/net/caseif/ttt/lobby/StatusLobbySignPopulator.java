package net.caseif.ttt.lobby;

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.util.config.ConfigKey;

import net.caseif.flint.lobby.LobbySign;
import net.caseif.flint.lobby.populator.LobbySignPopulator;
import org.bukkit.ChatColor;

public class StatusLobbySignPopulator implements LobbySignPopulator {

    private static final int SIGN_HASTE_SWITCH_PERIOD = 4;

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
        if (!TTTCore.config.get(ConfigKey.HASTE)) {
            return defaultPop.third(sign);
        }
        if (!sign.getArena().getRound().isPresent()) {
            return "";
        }
        return sign.getArena().getRound().get().getTime() / (SIGN_HASTE_SWITCH_PERIOD * 2) < SIGN_HASTE_SWITCH_PERIOD
                ? defaultPop.third(sign)
                : ChatColor.RED + "(Haste Mode)";
    }

    @Override
    public String fourth(LobbySign sign) {
        return defaultPop.fourth(sign);
    }

}
