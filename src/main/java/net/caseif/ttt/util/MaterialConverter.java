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
package net.caseif.ttt.util;

import com.google.common.collect.BiMap;
import org.bukkit.Material;

/**
 * Utility class for converting between Minecraft names and Bukkit materials.
 * Stolen/repurposed from Pore.
 */
public final class MaterialConverter {

    private static final BiMapBuilder<Material, String> builder = BiMapBuilder.builder();

    public static final BiMap<Material, String> ITEM_TYPE_CONVERTER;

    static {
        addMaterial("STONE", "STONE");
        addMaterial("GRASS", "GRASS");
        addMaterial("DIRT", "DIRT");
        addMaterial("COBBLESTONE", "COBBLESTONE");
        addMaterial("WOOD", "PLANKS");
        addMaterial("SAPLING", "SAPLING");
        addMaterial("BEDROCK", "BEDROCK");
        addMaterial("SAND", "SAND");
        addMaterial("GRAVEL", "GRAVEL");
        addMaterial("GOLD_ORE", "GOLD_ORE");
        addMaterial("IRON_ORE", "IRON_ORE");
        addMaterial("COAL_ORE", "COAL_ORE");
        addMaterial("LOG", "LOG");
        addMaterial("LOG_2", "LOG2");
        addMaterial("LEAVES", "LEAVES");
        addMaterial("LEAVES_2", "LEAVES2");
        addMaterial("SPONGE", "SPONGE");
        addMaterial("GLASS", "GLASS");
        addMaterial("LAPIS_ORE", "LAPIS_ORE");
        addMaterial("LAPIS_BLOCK", "LAPIS_BLOCK"); // <3
        addMaterial("DISPENSER", "DISPENSER");
        addMaterial("SANDSTONE", "SANDSTONE");
        addMaterial("NOTE_BLOCK", "NOTEBLOCK");
        addMaterial("POWERED_RAIL", "GOLDEN_RAIL");
        addMaterial("DETECTOR_RAIL", "DETECTOR_RAIL");
        addMaterial("PISTON_STICKY_BASE", "STICKY_PISTON");
        addMaterial("WEB", "WEB");
        addMaterial("LONG_GRASS", "TALLGRASS");
        addMaterial("DEAD_BUSH", "DEADBUSH");
        addMaterial("PISTON_BASE", "PISTON");
        addMaterial("WOOL", "WOOL");
        addMaterial("YELLOW_FLOWER", "YELLOW_FLOWER");
        addMaterial("RED_ROSE", "RED_FLOWER");
        addMaterial("BROWN_MUSHROOM", "BROWN_MUSHROOM");
        addMaterial("RED_MUSHROOM", "RED_MUSHROOM");
        addMaterial("GOLD_BLOCK", "GOLD_BLOCK");
        addMaterial("IRON_BLOCK", "IRON_BLOCK");
        addMaterial("STEP", "STONE_SLAB");
        addMaterial("BRICK", "BRICK_BLOCK");
        addMaterial("TNT", "TNT");
        addMaterial("BOOKSHELF", "BOOKSHELF");
        addMaterial("MOSSY_COBBLESTONE", "MOSSY_COBBLESTONE");
        addMaterial("OBSIDIAN", "OBSIDIAN");
        addMaterial("TORCH", "TORCH");
        addMaterial("MOB_SPAWNER", "MOB_SPAWNER");
        addMaterial("WOOD_STAIRS", "OAK_STAIRS");
        addMaterial("CHEST", "CHEST");
        addMaterial("DIAMOND_ORE", "DIAMOND_ORE");
        addMaterial("DIAMOND_BLOCK", "DIAMOND_BLOCK");
        addMaterial("WORKBENCH", "CRAFTING_TABLE");
        addMaterial("SOIL", "FARMLAND");
        addMaterial("FURNACE", "FURNACE");
        addMaterial("BURNING_FURNACE", "LIT_FURNACE");
        addMaterial("LADDER", "LADDER");
        addMaterial("RAILS", "RAIL");
        addMaterial("COBBLESTONE_STAIRS", "STONE_STAIRS");
        addMaterial("LEVER", "LEVER");
        addMaterial("STONE_PLATE", "STONE_PRESSURE_PLATE");
        addMaterial("WOOD_PLATE", "WOODEN_PRESSURE_PLATE");
        addMaterial("REDSTONE_ORE", "REDSTONE_ORE");
        addMaterial("REDSTONE_TORCH_ON", "REDSTONE_TORCH");
        addMaterial("STONE_BUTTON", "STONE_BUTTON");
        addMaterial("SNOW", "SNOW_LAYER");
        addMaterial("ICE", "ICE");
        addMaterial("SNOW_BLOCK", "SNOW");
        addMaterial("CACTUS", "CACTUS");
        addMaterial("CLAY", "CLAY");
        addMaterial("JUKEBOX", "JUKEBOX");
        addMaterial("FENCE", "FENCE");
        addMaterial("SPRUCE_FENCE", "SPRUCE_FENCE");
        addMaterial("BIRCH_FENCE", "BIRCH_FENCE");
        addMaterial("JUNGLE_FENCE", "JUNGLE_FENCE");
        addMaterial("DARK_OAK_FENCE", "DARK_OAK_FENCE");
        addMaterial("ACACIA_FENCE", "ACACIA_FENCE");
        addMaterial("PUMPKIN", "PUMPKIN");
        addMaterial("NETHERRACK", "NETHERRACK");
        addMaterial("SOUL_SAND", "SOUL_SAND");
        addMaterial("GLOWSTONE", "GLOWSTONE");
        addMaterial("JACK_O_LANTERN", "LIT_PUMPKIN");
        addMaterial("TRAP_DOOR", "TRAPDOOR");
        addMaterial("MONSTER_EGGS", "MONSTER_EGG");
        addMaterial("SMOOTH_BRICK", "STONEBRICK");
        addMaterial("HUGE_MUSHROOM_1", "BROWN_MUSHROOM_BLOCK");
        addMaterial("HUGE_MUSHROOM_2", "RED_MUSHROOM_BLOCK");
        addMaterial("IRON_FENCE", "IRON_BARS");
        addMaterial("THIN_GLASS", "GLASS_PANE");
        addMaterial("MELON_BLOCK", "MELON_BLOCK");
        addMaterial("VINE", "VINE");
        addMaterial("FENCE_GATE", "FENCE_GATE");
        addMaterial("SPRUCE_FENCE_GATE", "SPRUCE_FENCE_GATE");
        addMaterial("BIRCH_FENCE_GATE", "BIRCH_FENCE_GATE");
        addMaterial("JUNGLE_FENCE_GATE", "JUNGLE_FENCE_GATE");
        addMaterial("DARK_OAK_FENCE_GATE", "DARK_OAK_FENCE_GATE");
        addMaterial("ACACIA_FENCE_GATE", "ACACIA_FENCE_GATE");
        addMaterial("BRICK_STAIRS", "BRICK_STAIRS");
        addMaterial("SMOOTH_STAIRS", "STONE_BRICK_STAIRS");
        addMaterial("MYCEL", "MYCELIUM");
        addMaterial("WATER_LILY", "WATERLILY");
        addMaterial("NETHER_BRICK", "NETHER_BRICK");
        addMaterial("NETHER_FENCE", "NETHER_BRICK_FENCE");
        addMaterial("NETHER_BRICK_STAIRS", "NETHER_BRICK_STAIRS");
        addMaterial("ENCHANTMENT_TABLE", "ENCHANTING_TABLE");
        addMaterial("ENDER_PORTAL_FRAME", "END_PORTAL_FRAME");
        addMaterial("ENDER_STONE", "END_STONE");
        addMaterial("DRAGON_EGG", "DRAGON_EGG");
        addMaterial("REDSTONE_LAMP_OFF", "REDSTONE_LAMP");
        addMaterial("WOOD_STEP", "WOODEN_SLAB");
        addMaterial("SANDSTONE_STAIRS", "SANDSTONE_STAIRS");
        addMaterial("EMERALD_ORE", "EMERALD_ORE");
        addMaterial("ENDER_CHEST", "ENDER_CHEST");
        addMaterial("TRIPWIRE_HOOK", "TRIPWIRE_HOOK");
        addMaterial("EMERALD_BLOCK", "EMERALD_BLOCK");
        addMaterial("SPRUCE_WOOD_STAIRS", "SPRUCE_STAIRS");
        addMaterial("BIRCH_WOOD_STAIRS", "BIRCH_STAIRS");
        addMaterial("JUNGLE_WOOD_STAIRS", "JUNGLE_STAIRS");
        addMaterial("COMMAND", "COMMAND_BLOCK");
        addMaterial("BEACON", "BEACON");
        addMaterial("COBBLE_WALL", "COBBLESTONE_WALL");
        addMaterial("WOOD_BUTTON", "WOODEN_BUTTON");
        addMaterial("ANVIL", "ANVIL");
        addMaterial("TRAPPED_CHEST", "TRAPPED_CHEST");
        addMaterial("GOLD_PLATE", "LIGHT_WEIGHTED_PRESSURE_PLATE");
        addMaterial("IRON_PLATE", "HEAVY_WEIGHTED_PRESSURE_PLATE");
        addMaterial("DAYLIGHT_DETECTOR", "DAYLIGHT_DETECTOR");
        addMaterial("REDSTONE_BLOCK", "REDSTONE_BLOCK");
        addMaterial("QUARTZ_ORE", "QUARTZ_ORE");
        addMaterial("HOPPER", "HOPPER");
        addMaterial("QUARTZ_BLOCK", "QUARTZ_BLOCK");
        addMaterial("QUARTZ_STAIRS", "QUARTZ_STAIRS");
        addMaterial("ACTIVATOR_RAIL", "ACTIVATOR_RAIL");
        addMaterial("DROPPER", "DROPPER");
        addMaterial("STAINED_CLAY", "STAINED_HARDENED_CLAY");
        addMaterial("BARRIER", "BARRIER");
        addMaterial("IRON_TRAPDOOR", "IRON_TRAPDOOR");
        addMaterial("HAY_BLOCK", "HAY_BLOCK");
        addMaterial("CARPET", "CARPET");
        addMaterial("HARD_CLAY", "HARDENED_CLAY");
        addMaterial("COAL_BLOCK", "COAL_BLOCK");
        addMaterial("PACKED_ICE", "PACKED_ICE");
        addMaterial("ACACIA_STAIRS", "ACACIA_STAIRS");
        addMaterial("DARK_OAK_STAIRS", "DARK_OAK_STAIRS");
        addMaterial("SLIME_BLOCK", "SLIME");
        addMaterial("DOUBLE_PLANT", "DOUBLE_PLANT");
        addMaterial("STAINED_GLASS", "STAINED_GLASS");
        addMaterial("STAINED_GLASS_PANE", "STAINED_GLASS_PANE");
        addMaterial("PRISMARINE", "PRISMARINE");
        addMaterial("SEA_LANTERN", "SEA_LANTERN");
        addMaterial("RED_SANDSTONE", "RED_SANDSTONE");
        addMaterial("RED_SANDSTONE_STAIRS", "RED_SANDSTONE_STAIRS");
        addMaterial("STONE_SLAB2", "STONE_SLAB2");
        addMaterial("IRON_SPADE", "IRON_SHOVEL");
        addMaterial("IRON_PICKAXE", "IRON_PICKAXE");
        addMaterial("IRON_AXE", "IRON_AXE");
        addMaterial("FLINT_AND_STEEL", "FLINT_AND_STEEL");
        addMaterial("APPLE", "APPLE");
        addMaterial("BOW", "BOW");
        addMaterial("ARROW", "ARROW");
        addMaterial("COAL", "COAL");
        addMaterial("DIAMOND", "DIAMOND");
        addMaterial("IRON_INGOT", "IRON_INGOT");
        addMaterial("GOLD_INGOT", "GOLD_INGOT");
        addMaterial("IRON_SWORD", "IRON_SWORD");
        addMaterial("WOOD_SWORD", "WOODEN_SWORD");
        addMaterial("WOOD_SPADE", "WOODEN_SHOVEL");
        addMaterial("WOOD_PICKAXE", "WOODEN_PICKAXE");
        addMaterial("WOOD_AXE", "WOODEN_AXE");
        addMaterial("STONE_SWORD", "STONE_SWORD");
        addMaterial("STONE_SPADE", "STONE_SHOVEL");
        addMaterial("STONE_PICKAXE", "STONE_PICKAXE");
        addMaterial("STONE_AXE", "STONE_AXE");
        addMaterial("DIAMOND_SWORD", "DIAMOND_SWORD");
        addMaterial("DIAMOND_SPADE", "DIAMOND_SHOVEL");
        addMaterial("DIAMOND_PICKAXE", "DIAMOND_PICKAXE");
        addMaterial("DIAMOND_AXE", "DIAMOND_AXE");
        addMaterial("STICK", "STICK");
        addMaterial("BOWL", "BOWL");
        addMaterial("MUSHROOM_SOUP", "MUSHROOM_STEW");
        addMaterial("GOLD_SWORD", "GOLDEN_SWORD");
        addMaterial("GOLD_SPADE", "GOLDEN_SHOVEL");
        addMaterial("GOLD_PICKAXE", "GOLDEN_PICKAXE");
        addMaterial("GOLD_AXE", "GOLDEN_AXE");
        addMaterial("STRING", "STRING");
        addMaterial("FEATHER", "FEATHER");
        addMaterial("SULPHUR", "GUNPOWDER");
        addMaterial("WOOD_HOE", "WOODEN_HOE");
        addMaterial("STONE_HOE", "STONE_HOE");
        addMaterial("IRON_HOE", "IRON_HOE");
        addMaterial("DIAMOND_HOE", "DIAMOND_HOE");
        addMaterial("GOLD_HOE", "GOLDEN_HOE");
        addMaterial("SEEDS", "WHEAT_SEEDS");
        addMaterial("WHEAT", "WHEAT");
        addMaterial("BREAD", "BREAD");
        addMaterial("LEATHER_HELMET", "LEATHER_HELMET");
        addMaterial("LEATHER_CHESTPLATE", "LEATHER_CHESTPLATE");
        addMaterial("LEATHER_LEGGINGS", "LEATHER_LEGGINGS");
        addMaterial("LEATHER_BOOTS", "LEATHER_BOOTS");
        addMaterial("CHAINMAIL_HELMET", "CHAINMAIL_HELMET");
        addMaterial("CHAINMAIL_CHESTPLATE", "CHAINMAIL_CHESTPLATE");
        addMaterial("CHAINMAIL_LEGGINGS", "CHAINMAIL_LEGGINGS");
        addMaterial("CHAINMAIL_BOOTS", "CHAINMAIL_BOOTS");
        addMaterial("IRON_HELMET", "IRON_HELMET");
        addMaterial("IRON_CHESTPLATE", "IRON_CHESTPLATE");
        addMaterial("IRON_LEGGINGS", "IRON_LEGGINGS");
        addMaterial("IRON_BOOTS", "IRON_BOOTS");
        addMaterial("DIAMOND_HELMET", "DIAMOND_HELMET");
        addMaterial("DIAMOND_CHESTPLATE", "DIAMOND_CHESTPLATE");
        addMaterial("DIAMOND_LEGGINGS", "DIAMOND_LEGGINGS");
        addMaterial("DIAMOND_BOOTS", "DIAMOND_BOOTS");
        addMaterial("GOLD_HELMET", "GOLDEN_HELMET");
        addMaterial("GOLD_CHESTPLATE", "GOLDEN_CHESTPLATE");
        addMaterial("GOLD_LEGGINGS", "GOLDEN_LEGGINGS");
        addMaterial("GOLD_BOOTS", "GOLDEN_BOOTS");
        addMaterial("FLINT", "FLINT");
        addMaterial("PORK", "PORKCHOP");
        addMaterial("GRILLED_PORK", "COOKED_PORKCHOP");
        addMaterial("PAINTING", "PAINTING");
        addMaterial("GOLDEN_APPLE", "GOLDEN_APPLE");
        addMaterial("SIGN", "SIGN");
        addMaterial("WOOD_DOOR", "WOODEN_DOOR");
        addMaterial("BUCKET", "BUCKET");
        addMaterial("WATER_BUCKET", "WATER_BUCKET");
        addMaterial("LAVA_BUCKET", "LAVA_BUCKET");
        addMaterial("MINECART", "MINECART");
        addMaterial("SADDLE", "SADDLE");
        addMaterial("IRON_DOOR", "IRON_DOOR");
        addMaterial("REDSTONE", "REDSTONE");
        addMaterial("SNOW_BALL", "SNOWBALL");
        addMaterial("BOAT", "BOAT");
        addMaterial("LEATHER", "LEATHER");
        addMaterial("MILK_BUCKET", "MILK_BUCKET");
        addMaterial("CLAY_BRICK", "BRICK");
        addMaterial("CLAY_BALL", "CLAY_BALL");
        addMaterial("SUGAR_CANE", "REEDS");
        addMaterial("PAPER", "PAPER");
        addMaterial("BOOK", "BOOK");
        addMaterial("SLIME_BALL", "SLIME_BALL");
        addMaterial("STORAGE_MINECART", "CHEST_MINECART");
        addMaterial("POWERED_MINECART", "FURNACE_MINECART");
        addMaterial("EGG", "EGG");
        addMaterial("COMPASS", "COMPASS");
        addMaterial("FISHING_ROD", "FISHING_ROD");
        addMaterial("WATCH", "CLOCK");
        addMaterial("GLOWSTONE_DUST", "GLOWSTONE_DUST");
        addMaterial("RAW_FISH", "FISH");
        addMaterial("COOKED_FISH", "COOKED_FISH");
        addMaterial("INK_SACK", "DYE");
        addMaterial("BONE", "BONE");
        addMaterial("SUGAR", "SUGAR");
        addMaterial("CAKE", "CAKE");
        addMaterial("BED", "BED");
        addMaterial("DIODE", "REPEATER");
        addMaterial("COOKIE", "COOKIE");
        addMaterial("MAP", "FILLED_MAP");
        addMaterial("SHEARS", "SHEARS");
        addMaterial("MELON", "MELON");
        addMaterial("PUMPKIN_SEEDS", "PUMPKIN_SEEDS");
        addMaterial("MELON_SEEDS", "MELON_SEEDS");
        addMaterial("RAW_BEEF", "BEEF");
        addMaterial("COOKED_BEEF", "COOKED_BEEF");
        addMaterial("RAW_CHICKEN", "CHICKEN");
        addMaterial("COOKED_CHICKEN", "COOKED_CHICKEN");
        addMaterial("ROTTEN_FLESH", "ROTTEN_FLESH");
        addMaterial("ENDER_PEARL", "ENDER_PEARL");
        addMaterial("BLAZE_ROD", "BLAZE_ROD");
        addMaterial("GHAST_TEAR", "GHAST_TEAR");
        addMaterial("GOLD_NUGGET", "GOLD_NUGGET");
        addMaterial("NETHER_WARTS", "NETHER_WART");
        addMaterial("POTION", "POTION");
        addMaterial("GLASS_BOTTLE", "GLASS_BOTTLE");
        addMaterial("SPIDER_EYE", "SPIDER_EYE");
        addMaterial("FERMENTED_SPIDER_EYE", "FERMENTED_SPIDER_EYE");
        addMaterial("BLAZE_POWDER", "BLAZE_POWDER");
        addMaterial("MAGMA_CREAM", "MAGMA_CREAM");
        addMaterial("BREWING_STAND_ITEM", "BREWING_STAND");
        addMaterial("CAULDRON_ITEM", "CAULDRON");
        addMaterial("EYE_OF_ENDER", "ENDER_EYE");
        addMaterial("SPECKLED_MELON", "SPECKLED_MELON");
        addMaterial("MONSTER_EGG", "SPAWN_EGG");
        addMaterial("EXP_BOTTLE", "EXPERIENCE_BOTTLE");
        addMaterial("FIREBALL", "FIRE_CHARGE");
        addMaterial("BOOK_AND_QUILL", "WRITABLE_BOOK");
        addMaterial("WRITTEN_BOOK", "WRITTEN_BOOK");
        addMaterial("EMERALD", "EMERALD");
        addMaterial("ITEM_FRAME", "ITEM_FRAME");
        addMaterial("FLOWER_POT_ITEM", "FLOWER_POT");
        addMaterial("CARROT_ITEM", "CARROT");
        addMaterial("POTATO_ITEM", "POTATO");
        addMaterial("BAKED_POTATO", "BAKED_POTATO");
        addMaterial("POISONOUS_POTATO", "POISONOUS_POTATO");
        addMaterial("EMPTY_MAP", "MAP");
        addMaterial("GOLDEN_CARROT", "GOLDEN_CARROT");
        addMaterial("SKULL_ITEM", "SKULL");
        addMaterial("CARROT_STICK", "CARROT_ON_A_STICK");
        addMaterial("NETHER_STAR", "NETHER_STAR");
        addMaterial("PUMPKIN_PIE", "PUMPKIN_PIE");
        addMaterial("FIREWORK", "FIREWORKS");
        addMaterial("FIREWORK_CHARGE", "FIREWORK_CHARGE");
        addMaterial("ENCHANTED_BOOK", "ENCHANTED_BOOK");
        addMaterial("REDSTONE_COMPARATOR", "COMPARATOR");
        addMaterial("NETHER_BRICK_ITEM", "NETHERBRICK");
        addMaterial("QUARTZ", "QUARTZ");
        addMaterial("EXPLOSIVE_MINECART", "TNT_MINECART");
        addMaterial("HOPPER_MINECART", "HOPPER_MINECART");
        addMaterial("PRISMARINE_SHARD", "PRISMARINE_SHARD");
        addMaterial("PRISMARINE_CRYSTALS", "PRISMARINE_CRYSTALS");
        addMaterial("RABBIT", "RABBIT");
        addMaterial("COOKED_RABBIT", "COOKED_RABBIT");
        addMaterial("RABBIT_STEW", "RABBIT_STEW");
        addMaterial("RABBIT_FOOT", "RABBIT_FOOT");
        addMaterial("RABBIT_HIDE", "RABBIT_HIDE");
        addMaterial("ARMOR_STAND", "ARMOR_STAND");
        addMaterial("IRON_BARDING", "IRON_HORSE_ARMOR");
        addMaterial("GOLD_BARDING", "GOLDEN_HORSE_ARMOR");
        addMaterial("DIAMOND_BARDING", "DIAMOND_HORSE_ARMOR");
        addMaterial("LEASH", "LEAD");
        addMaterial("NAME_TAG", "NAME_TAG");
        addMaterial("COMMAND_MINECART", "COMMAND_BLOCK_MINECART");
        addMaterial("MUTTON", "MUTTON");
        addMaterial("COOKED_MUTTON", "COOKED_MUTTON");
        addMaterial("BANNER", "BANNER");
        addMaterial("SPRUCE_DOOR_ITEM", "SPRUCE_DOOR");
        addMaterial("BIRCH_DOOR_ITEM", "BIRCH_DOOR");
        addMaterial("JUNGLE_DOOR_ITEM", "JUNGLE_DOOR");
        addMaterial("ACACIA_DOOR_ITEM", "ACACIA_DOOR");
        addMaterial("DARK_OAK_DOOR_ITEM", "DARK_OAK_DOOR");
        addMaterial("GOLD_RECORD", "RECORD_13");
        addMaterial("GREEN_RECORD", "RECORD_CAT");
        addMaterial("RECORD_3", "RECORD_BLOCKS");
        addMaterial("RECORD_4", "RECORD_CHIRP");
        addMaterial("RECORD_5", "RECORD_FAR");
        addMaterial("RECORD_6", "RECORD_MALL");
        addMaterial("RECORD_7", "RECORD_MELLOHI");
        addMaterial("RECORD_8", "RECORD_STAL");
        addMaterial("RECORD_9", "RECORD_STRAD");
        addMaterial("RECORD_10", "RECORD_WARD");
        addMaterial("RECORD_11", "RECORD_11");
        addMaterial("RECORD_12", "RECORD_WAIT");
        ITEM_TYPE_CONVERTER = builder.build();
    }

    public static String getNotchName(Material material) {
        return ITEM_TYPE_CONVERTER.get(material);
    }

    public static Material fromNotchName(String type) {
        return ITEM_TYPE_CONVERTER.inverse().get(type);
    }

    private static void addMaterial(String bukkitName, String notchName) {
        try {
            Material m = Material.valueOf(bukkitName);
            builder.put(m, notchName);
        } catch (IllegalArgumentException swallow){}
    }

}
