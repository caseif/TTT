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

import java.lang.reflect.Field;

public abstract class MaterialHelper {

    private static final MaterialHelper INSTANCE;

    static {
        if (TTTCore.getInstance().isLegacyMinecraftVersion()) {
            INSTANCE = new LegacyMaterialHelper();
        } else {
            INSTANCE = new ModernMaterialHelper();
        }
    }

    public static MaterialHelper instance() {
        return INSTANCE;
    }

    public Material CLOCK;
    public Material IRON_HORSE_ARMOR;
    public Material LEAD;

    public abstract boolean isBed(Material material);

    public abstract boolean isStandingSign(Material material);

    public abstract boolean isLiquid(Material material);

    public abstract ItemStack createRoleWool(String role);

    @SuppressWarnings("deprecation")
    private static class LegacyMaterialHelper extends MaterialHelper {

        private static Material BED_BLOCK;
        private static Material IRON_BARDING;
        private static Material LEASH;
        private static Material SIGN_POST;
        private static Material STATIONARY_LAVA;
        private static Material STATIONARY_WATER;
        private static Material WATCH;
        private static Material WOOL;

        private static final ImmutableSet<Material> FLUIDS;

        private static final ImmutableMap<String, Integer> WOOL_DURABILITY_MAP;

        static {
            for (Field field : LegacyMaterialHelper.class.getDeclaredFields()) {
                if (field.getType() == Material.class) {
                    try {
                        field.set(null, lookup(field.getName()));
                    } catch (IllegalAccessException ex) {
                        throw new RuntimeException("Failed to set legacy material field " + field.getName() + ".");
                    }
                }
            }

            FLUIDS = ImmutableSet.of(
                    Material.WATER, Material.LAVA,
                    STATIONARY_WATER, STATIONARY_LAVA
            );

            WOOL_DURABILITY_MAP = ImmutableMap.of(
                    Role.DETECTIVE, 11,
                    Role.INNOCENT, 5,
                    Role.TRAITOR, 14
            );
        }

        private static Material lookup(String id) {
            return Material.valueOf(id);
        }

        private LegacyMaterialHelper() {
            CLOCK = WATCH;
            IRON_HORSE_ARMOR = IRON_BARDING;
            LEAD = LEASH;
        }

        @Override
        public boolean isBed(Material material) {
            return material == BED_BLOCK;
        }

        @Override
        public boolean isStandingSign(Material material) {
            return material == SIGN_POST;
        }

        @Override
        public boolean isLiquid(Material material) {
            return FLUIDS.contains(material);
        }

        @Override
        public ItemStack createRoleWool(String role) {
            ItemStack roleId = new ItemStack(WOOL, 1);

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
            IRON_HORSE_ARMOR = Material.IRON_HORSE_ARMOR;
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
