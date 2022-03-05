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
import net.caseif.ttt.util.constant.Role;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

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
    public Material OAK_WALL_SIGN;

    public abstract boolean isBed(Material material);

    public abstract boolean isStandingSign(Material material);

    public abstract boolean isWallSign(Material material);

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
        private static Material WALL_SIGN;
        private static Material WATCH;
        private static Material WOOL;

        private static final ImmutableSet<Material> FLUIDS;

        private static final ImmutableMap<String, Integer> WOOL_DURABILITY_MAP;

        static {
            for (Field field : LegacyMaterialHelper.class.getDeclaredFields()) {
                if (field.getType() == Material.class) {
                    try {
                        field.set(null, Material.valueOf(field.getName()));
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

        private LegacyMaterialHelper() {
            CLOCK = WATCH;
            IRON_HORSE_ARMOR = IRON_BARDING;
            LEAD = LEASH;
            OAK_WALL_SIGN = WALL_SIGN;
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
        public boolean isWallSign(Material material) {
            return material == WALL_SIGN;
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
        // this class is free to depend on the new behavior of
        // Material#matchMaterial introduced by Spigot 1.13 since it's only used
        // for Minecraft 1.13 and higher

        private static ImmutableList<Material> getMaterialList(String... ids) {
            return Arrays.stream(ids)
                .map(Material::matchMaterial)
                .filter(Predicates.notNull())
                .collect(ImmutableList.toImmutableList());
        }

        private static final ImmutableList<Material> STANDING_SIGN_TYPES = getMaterialList(
            "minecraft:acacia_sign",
            "minecraft:birch_sign",
            "minecraft:dark_oak_sign",
            "minecraft:jungle_sign",
            "minecraft:oak_sign",
            "minecraft:spruce_sign",
            "minecraft:warped_sign",
            "minecraft:crimson_sign"
        );

        private static final ImmutableList<Material> WALL_SIGN_TYPES = getMaterialList(
            "minecraft:acacia_wall_sign",
            "minecraft:birch_wall_sign",
            "minecraft:dark_oak_wall_sign",
            "minecraft:jungle_wall_sign",
            "minecraft:oak_wall_sign",
            "minecraft:spruce_wall_sign",
            "minecraft:warped_wall_sign",
            "minecraft:crimson_wall_sign"
        );

        private static final ImmutableMap<String, Material> WOOL_MAP = ImmutableMap.of(
                Role.DETECTIVE, Material.matchMaterial("minecraft:blue_wool"),
                Role.INNOCENT, Material.matchMaterial("minecraft:green_wool"),
                Role.TRAITOR, Material.matchMaterial("minecraft:red_wool")
        );

        private static final ImmutableList<Material> FLUIDS = getMaterialList(
            "minecraft:water",
            "minecraft:lava"
        );

        private static final Class<?> c_blockdata_Bed;
        private static final Method m_Material_createBlockData;

        static {
            try {
                c_blockdata_Bed = Class.forName("org.bukkit.block.data.type.Bed");
                m_Material_createBlockData = Material.class.getMethod("createBlockData");
            } catch (ClassNotFoundException | NoSuchMethodException ex) {
                throw new RuntimeException("Reflection lookup failed in 1.13+ material helper", ex);
            }
        }

        private ModernMaterialHelper() {
            CLOCK = Material.matchMaterial("minecraft:clock");
            IRON_HORSE_ARMOR = Material.matchMaterial("minecraft:iron_horse_armor");
            LEAD = Material.matchMaterial("minecraft:lead");
            OAK_WALL_SIGN = Material.matchMaterial("minecraft:oak_wall_sign");
        }

        @Override
        public boolean isBed(Material material) {
            try {
                Object blockData = m_Material_createBlockData.invoke(material);

                if (blockData == null) {
                    return false;
                }

                return c_blockdata_Bed.isAssignableFrom(blockData.getClass());
            } catch (IllegalAccessException ex) {
                throw new RuntimeException("Reflection invocation failed in 1.13+ material helper", ex);
            } catch (InvocationTargetException ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public boolean isStandingSign(Material material) {
            return STANDING_SIGN_TYPES.stream().anyMatch(Predicates.equalTo(material));
        }

        @Override
        public boolean isWallSign(Material material) {
            return WALL_SIGN_TYPES.stream().anyMatch(Predicates.equalTo(material));
        }

        @Override
        public boolean isLiquid(Material material) {
            return FLUIDS.stream().anyMatch(Predicates.equalTo(material));
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
