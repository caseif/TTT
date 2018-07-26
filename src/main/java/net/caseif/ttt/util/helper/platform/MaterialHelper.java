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

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.util.constant.Role;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.bukkit.Material;
import org.bukkit.block.data.type.Bed;
import org.bukkit.inventory.ItemStack;

public abstract class MaterialHelper {

    private static final MaterialHelper INSTANCE = TTTCore.getInstance().isLegacyMinecraftVersion()
            ? new LegacyMaterialHelper()
            : new ModernMaterialHelper();

    public static MaterialHelper instance() {
        return INSTANCE;
    }

    public Material CLOCK;
    public Material LEAD;

    public abstract boolean isBed(Material material);

    public abstract boolean isStandingSign(Material material);

    public abstract boolean isLiquid(Material material);

    public abstract ItemStack createRoleWool(String role);

    @SuppressWarnings("deprecation")
    private static class LegacyMaterialHelper extends MaterialHelper {

        private static final ImmutableSet<Material> FLUIDS = ImmutableSet.of(
                Material.WATER, Material.LAVA,
                Material.LEGACY_STATIONARY_WATER, Material.LEGACY_STATIONARY_LAVA
        );

        private static final ImmutableMap<String, Integer> WOOL_DURABILITY_MAP = ImmutableMap.of(
                Role.DETECTIVE, 11,
                Role.INNOCENT, 5,
                Role.TRAITOR, 14
        );

        private LegacyMaterialHelper() {
            CLOCK = Material.LEGACY_WATCH;
            LEAD = Material.LEGACY_LEASH;
        }

        @Override
        public boolean isBed(Material material) {
            return material == Material.LEGACY_BED_BLOCK;
        }

        @Override
        public boolean isStandingSign(Material material) {
            return material == Material.LEGACY_SIGN_POST;
        }

        @Override
        public boolean isLiquid(Material material) {
            return FLUIDS.contains(material);
        }

        @Override
        public ItemStack createRoleWool(String role) {
            ItemStack roleId = new ItemStack(Material.LEGACY_WOOL, 1);

            short durability = WOOL_DURABILITY_MAP.getOrDefault(role, -1).shortValue();

            if (durability == -1) {
                throw new AssertionError("Bad role \"" + role + "\"");
            }

            roleId.setDurability(durability);

            return roleId;
        }
    }

    private static class ModernMaterialHelper extends MaterialHelper {

        private static final ImmutableMap<String, Material> WOOL_MAP = ImmutableMap.of(
                Role.DETECTIVE, Material.BLUE_WOOL,
                Role.INNOCENT, Material.GREEN_WOOL,
                Role.TRAITOR, Material.RED_WOOL
        );

        private ModernMaterialHelper() {
            CLOCK = Material.CLOCK;
            LEAD = Material.LEAD;
        }

        @Override
        public boolean isBed(Material material) {
            return material.createBlockData() instanceof Bed;
        }

        @Override
        public boolean isStandingSign(Material material) {
            return material == Material.SIGN;
        }

        @Override
        public boolean isLiquid(Material material) {
            return material == Material.WATER || material == Material.LAVA;
        }

        @Override
        public ItemStack createRoleWool(String role) {
            Material wool = WOOL_MAP.get(role);

            if (wool == null) {
                throw new AssertionError("Bad role \"" + role + "\"");
            }

            return new ItemStack(wool, 1);
        }
    }

}
