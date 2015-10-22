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
package net.caseif.ttt.util.helper.platform;

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.util.Constants.Color;
import net.caseif.ttt.util.Constants.Role;

import net.caseif.flint.challenger.Challenger;
import net.caseif.flint.round.Round;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Static utility class for inventory-related functionality.
 */
public class InventoryHelper {

    private static final ItemStack ITEM_CROWBAR;
    private static final ItemStack ITEM_GUN;
    private static final ItemStack ITEM_AMMO;
    private static final ItemStack ITEM_DNA_SCANNER;

    static {
        ITEM_CROWBAR = new ItemStack(ConfigHelper.CROWBAR_ITEM, 1);
        ItemMeta cbMeta = ITEM_CROWBAR.getItemMeta();
        cbMeta.setDisplayName(Color.INFO + TTTCore.locale.getLocalizable("item.crowbar.name").localize());
        ITEM_CROWBAR.setItemMeta(cbMeta);

        ITEM_GUN = new ItemStack(ConfigHelper.GUN_ITEM, 1);
        ItemMeta gunMeta = ITEM_GUN.getItemMeta();
        gunMeta.setDisplayName(Color.INFO + TTTCore.locale.getLocalizable("item.gun.name").localize());
        ITEM_GUN.setItemMeta(gunMeta);

        ITEM_AMMO = new ItemStack(Material.ARROW, ConfigHelper.INITIAL_AMMO);

        ITEM_DNA_SCANNER = new ItemStack(Material.COMPASS, 1);
        ItemMeta dnaMeta = ITEM_DNA_SCANNER.getItemMeta();
        dnaMeta.setDisplayName(Color.INFO + TTTCore.locale.getLocalizable("item.dna-scanner.name").localize());
        ITEM_DNA_SCANNER.setItemMeta(dnaMeta);
    }

    public static void removeArrow(Inventory inv) {
        for (int i = 0; i < inv.getContents().length; i++) {
            ItemStack is = inv.getItem(i);
            if (is != null) {
                if (is.getType() == Material.ARROW) {
                    if (is.getAmount() == 1) {
                        inv.setItem(i, null);
                    } else if (is.getAmount() > 1) {
                        is.setAmount(is.getAmount() - 1);
                    }
                    break;
                }
            }
        }
    }

    public static void distributeItems(Round round) {
        for (Challenger ch : round.getChallengers()) {
            Player pl = Bukkit.getPlayer(ch.getUniqueId());
            assert pl != null;
            pl.getInventory().addItem(ITEM_CROWBAR, ITEM_GUN, ITEM_AMMO);
            if (ch.getMetadata().has(Role.DETECTIVE)) {
                pl.getInventory().addItem(ITEM_DNA_SCANNER);
            }
        }
    }

}
