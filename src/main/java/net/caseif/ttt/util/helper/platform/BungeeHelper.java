package net.caseif.ttt.util.helper.platform;

import static net.caseif.ttt.TTTCore.getPlugin;

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.util.Constants;

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

    private static boolean configVerified = false;

    static {
        INSTANCE = new BungeeHelper();

        registerBungeeChannel();
        sendPluginMessage("GetServers", null, Iterables.getFirst(PlayerHelper.getOnlinePlayers(), null));
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
            ByteArrayDataInput in = ByteStreams.newDataInput(message);
            String subchannel = in.readUTF();

            if (subchannel.equals("GetServers")) {
                List<String> servers = Arrays.asList(in.readUTF().split(","));

                if (!configVerified) { // still need to verify that server is valid
                    if (!servers.contains(TTTCore.config.RETURN_SERVER)) {
                        TTTCore.log.warning(TTTCore.locale.getLocalizable("error.bungee.configuration").localize());
                    }
                    configVerified = true;
                } else {
                    if (servers.contains(TTTCore.config.RETURN_SERVER)) {
                        sendPluginMessage("Connect", TTTCore.config.RETURN_SERVER, player);
                    } else {
                        TTTCore.locale.getLocalizable("error.bungee.configuration").withPrefix(Constants.Color.ERROR)
                                .sendTo(player);
                        TTTCore.locale.getLocalizable("error.report").withPrefix(Constants.Color.ERROR).sendTo(player);
                    }

                }
            }
        }
    }

}
