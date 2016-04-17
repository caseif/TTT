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

package net.caseif.ttt.util.helper.platform;

import static com.google.common.base.Preconditions.checkState;
import static net.caseif.ttt.TTTCore.getPlugin;

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.util.config.ConfigKey;
import net.caseif.ttt.util.constant.Color;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.Arrays;
import java.util.List;

public class BungeeHelper implements PluginMessageListener{

    private static final BungeeHelper INSTANCE;

    private static boolean startedInitializing = false;
    private static boolean support = false;

    static {
        INSTANCE = new BungeeHelper();
    }

    public static void initialize() {
        checkState(!startedInitializing, "BungeeHelper initialization cannot be called more than once");
        startedInitializing = true;

        if (TTTCore.config.get(ConfigKey.RETURN_SERVER).isEmpty()) {
            return;
        }

        registerBungeeChannel();
        sendPluginMessage("GetServers", null, Iterables.getFirst(PlayerHelper.getOnlinePlayers(), null));
    }

    public static boolean wasInitializationCalled() {
        return startedInitializing;
    }

    public static boolean hasSupport() {
        return support;
    }

    public static void sendPlayerToReturnServer(Player player) {
        sendPluginMessage("GetServers", null, player);
    }

    private static void sendPluginMessage(String subchannel, String content, Player player) {
        assert player != null;

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(subchannel);
        if (content != null) {
            out.writeUTF(content);
        }

        player.sendPluginMessage(TTTCore.getPlugin(), "BungeeCord", out.toByteArray());
    }

    private static void registerBungeeChannel() {
        getPlugin().getServer().getMessenger().registerOutgoingPluginChannel(getPlugin(), "BungeeCord");
        getPlugin().getServer().getMessenger().registerIncomingPluginChannel(getPlugin(), "BungeeCord", INSTANCE);
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subchannel = in.readUTF();

        if (subchannel.equals("GetServers")) {
            List<String> servers = Arrays.asList(in.readUTF().split(","));

            if (!support) { // still need to verify that server is valid
                if (!servers.contains(TTTCore.config.get(ConfigKey.RETURN_SERVER))) {
                    TTTCore.log.warning(TTTCore.locale.getLocalizable("error.bungee.configuration").localize());
                }
                support = true;
            } else {
                if (servers.contains(TTTCore.config.get(ConfigKey.RETURN_SERVER))) {
                    sendPluginMessage("Connect", TTTCore.config.get(ConfigKey.RETURN_SERVER), player);
                } else {
                    TTTCore.locale.getLocalizable("error.bungee.configuration").withPrefix(Color.ERROR)
                            .sendTo(player);
                    TTTCore.locale.getLocalizable("error.report").withPrefix(Color.ERROR).sendTo(player);
                }

            }
        }
    }

}
