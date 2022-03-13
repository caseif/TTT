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

package net.caseif.ttt.util.helper.platform;

import net.caseif.ttt.TTTCore;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Static utility class for NMS-related functionality. This class serves as an
 * abstraction layer for some under-the-hood hacks.
 */
public final class NmsHelper {

    private static final boolean NMS_SUPPORT;
    public static final String VERSION_STRING;

    // general classes for sending packets
    public static final Method CRAFT_PLAYER_GET_HANDLE;
    public static final Field ENTITY_PLAYER_PLAYER_CONNECTION;
    public static final Method PLAYER_CONNECTION_A_PACKET_PLAY_IN_CLIENT_COMMAND;
    public static final Object CLIENT_COMMAND_PACKET_INSTANCE;

    static {
        boolean nmsException = false;

        String[] array = Bukkit.getServer().getClass().getPackage().getName().split("\\.");
        VERSION_STRING = array.length == 4 ? array[3] + "." : "";

        Method m_CraftPlayer_getHandle = null;
        Field f_EntityPlayer_playerConnection = null;
        Method m_PlayerConnection_a_PacketPlayInClientCommand = null;
        Object clientCommandPacketInstance = null;
        try {
            // get method for recieving CraftPlayer's EntityPlayer
            m_CraftPlayer_getHandle = getCraftClass("entity.CraftPlayer").getMethod("getHandle");
            // get the PlayerConnection of the EntityPlayer
            Class<?> c_EntityPlayer = m_CraftPlayer_getHandle.getReturnType();
            
            // the PlayerConnection field changed names in 1.17, then at some point stopped being deobfuscated
            // so we just access by type to avoid a bunch of nonsense and hopefully prevent future breakage
            for (Field field : c_EntityPlayer.getDeclaredFields()) {
                if (field.getType().getSimpleName().equals("PlayerConnection")) {
                    if (f_EntityPlayer_playerConnection != null) {
                        throw new RuntimeException("EntityPlayer contains multiple fields of type PlayerConnection - "
                        + "refusing to proceed");
                    }
                    f_EntityPlayer_playerConnection = field;
                }
            }

            // method to send the packet
            Class<?> c_PlayerConnection = f_EntityPlayer_playerConnection.getType();
            Class<?> c_PacketPlayInClientCommand;
            try {
                c_PacketPlayInClientCommand = Class.forName("net.minecraft.network.protocol.game.PacketPlayInClientCommand");
            } catch (ClassNotFoundException ex) {
                c_PacketPlayInClientCommand = getVersionedNmsClass("PacketPlayInClientCommand");
            }
            m_PlayerConnection_a_PacketPlayInClientCommand = c_PlayerConnection.getMethod("a",
                    c_PacketPlayInClientCommand);

            try {
                @SuppressWarnings("rawtypes")
                Class<? extends Enum> c_EnumClientCommand;
                try {                    
                    // Spigot's NMS mappings were totally overhauled during 1.16.5
                    try {
                        String className = "net.minecraft.network.protocol.game.PacketPlayInClientCommand$EnumClientCommand";

                        // we do an indirect assignment to avoid compiler warnings
                        // (I don't want to disable warnings for the whole method)
                        @SuppressWarnings({"rawtypes", "unchecked"})
                        Class<? extends Enum> temp = (Class<? extends Enum>) Class.forName(className);
                        c_EnumClientCommand = temp;
                    } catch (ClassNotFoundException ex) { // 1.16.5 or earlier
                        String className = "PacketPlayInClientCommand$EnumClientCommand";
                        // this changed at some point in 1.8 to an inner class; I don't care to figure out exactly when
                        // also, same thing with the indirect assginment
                        @SuppressWarnings({"rawtypes", "unchecked"})
                        Class<? extends Enum> temp = (Class<? extends Enum>) getVersionedNmsClass(className);
                        c_EnumClientCommand = temp;
                    }

                } catch (ClassNotFoundException ex) { // older 1.8 builds/1.7
                    @SuppressWarnings({"rawtypes", "unchecked"})
                    Class<? extends Enum> temp = (Class<? extends Enum>) getVersionedNmsClass("EnumClientCommand");
                    c_EnumClientCommand = temp;
                }
                @SuppressWarnings("unchecked")
                Object performRespawn = Enum.valueOf(c_EnumClientCommand, "PERFORM_RESPAWN");
                clientCommandPacketInstance = c_PacketPlayInClientCommand
                        .getConstructor(performRespawn.getClass())
                        .newInstance(performRespawn);
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
                TTTCore.getInstance()
                        .logSevere(TTTCore.locale.getLocalizable("error.plugin.nms").localize());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            TTTCore.getInstance().logSevere(TTTCore.locale.getLocalizable("error.plugin.nms").localize());
            nmsException = true;
        }

        CRAFT_PLAYER_GET_HANDLE = m_CraftPlayer_getHandle;
        ENTITY_PLAYER_PLAYER_CONNECTION = f_EntityPlayer_playerConnection;
        PLAYER_CONNECTION_A_PACKET_PLAY_IN_CLIENT_COMMAND = m_PlayerConnection_a_PacketPlayInClientCommand;
        CLIENT_COMMAND_PACKET_INSTANCE = clientCommandPacketInstance;

        NMS_SUPPORT = !nmsException;
    }

    private NmsHelper() {
    }

    /**
     * Retrieves a class by the given name from the package
     * {@code net.minecraft.server.<PACKAGE_VERSION>}.
     *
     * @param name The name of the class to retrieve
     * @return The class object from the package {@code net.minecraft.server}
     * @throws ClassNotFoundException If the class does not exist in the package
     */
    private static Class<?> getVersionedNmsClass(String name) throws ClassNotFoundException {
        String className = "net.minecraft.server." + VERSION_STRING + name;
        return Class.forName(className);
    }

    /**
     * Retrieves a class by the given name from the package
     * {@code org.bukkit.craftbukkit}.
     *
     * @param name The name of the class to retrieve
     * @return The class object from the package {@code org.bukkit.craftbukkit}
     * @throws ClassNotFoundException If the class does not exist in the package
     */
    private static Class<?> getCraftClass(String name) throws ClassNotFoundException {
        String className = "org.bukkit.craftbukkit." + VERSION_STRING + name;
        return Class.forName(className);
    }

    /**
     * Sends a PlayInClientCommand packet to the given player.
     *
     * @param player The {@link Player} to send the packet to
     */
    public static void sendRespawnPacket(Player player) {
        if (NMS_SUPPORT) {
            try {
                Object nmsPlayer = CRAFT_PLAYER_GET_HANDLE.invoke(player);
                Object conn = ENTITY_PLAYER_PLAYER_CONNECTION.get(nmsPlayer);
                PLAYER_CONNECTION_A_PACKET_PLAY_IN_CLIENT_COMMAND.invoke(conn, CLIENT_COMMAND_PACKET_INSTANCE);
            } catch (IllegalAccessException | InvocationTargetException ex) {
                TTTCore.log.severe("Failed to force-respawn player " + player.getName());
                ex.printStackTrace();
            }
        }
    }

}
