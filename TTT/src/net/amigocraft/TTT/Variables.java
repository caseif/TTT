package net.amigocraft.TTT;

public class Variables {
	
	public static double DETECTIVE_RATIO;
	public static int MAXIMUM_PLAYERS;
	public static double TRAITOR_RATIO;
	public static int MINIMUM_PLAYERS_FOR_DETECTIVE;
	public static int CROWBAR_DAMAGE;
	public static boolean GUNS_OUTSIDE_ARENAS;
	public static boolean REQUIRE_AMMO_FOR_GUNS;
	public static boolean KARMA_PERSISTENCE;
	public static int DEFAULT_KARMA;
	public static int MAX_KARMA;
	public static int KARMA_HEAL;
	public static int KARMA_CLEAN_BONUS;
	public static double KARMA_CLEAN_HALF;
	public static double DAMAGE_PENALTY;
	public static int KILL_PENALTY;
	public static int T_DAMAGE_REWARD;
	public static int TBONUS;
	public static boolean KARMA_ROUND_TO_ONE;
	public static int KARMA_KICK;
	public static boolean KARMA_BAN;
	public static int KARMA_BAN_TIME;
	public static boolean KARMA_DEBUG;
	public static boolean VERBOSE_LOGGING;
	public static boolean DAMAGE_REDUCTION;
	public static boolean ENABLE_AUTO_UPDATE;
	public static boolean ENABLE_METRICS;
	public static String LOCALIZATION;
	public static boolean UNKNOWN_BUILD_WARNING;
	public static boolean UNSTABLE_BUILD_WARNING;
	public static String CONFIG_VERSION;
	public static int MINIMUM_PLAYERS;
	public static int TIME_LIMIT;
	public static int SETUP_TIME;

	public static void initialize(){
		TIME_LIMIT = getInt("time-limit");
		SETUP_TIME = getInt("setup-time");
		MINIMUM_PLAYERS = getInt("minimum-players");
		MAXIMUM_PLAYERS = getInt("maximum-players");
		TRAITOR_RATIO = getDouble("traitor-ratio");
		DETECTIVE_RATIO = getDouble("detective-ratio");
		MINIMUM_PLAYERS_FOR_DETECTIVE = getInt("minimum-players-for-detective");
		CROWBAR_DAMAGE = getInt("crowbar-damage");
		GUNS_OUTSIDE_ARENAS = getBoolean("guns-outside-arenas");
		REQUIRE_AMMO_FOR_GUNS = getBoolean("require-ammo-for-guns");
		KARMA_PERSISTENCE = getBoolean("karma-persistence");
		DEFAULT_KARMA = getInt("default-karma");
		MAX_KARMA = getInt("max-karma");
		KARMA_HEAL = getInt("karma-heal");
		KARMA_CLEAN_BONUS = getInt("karma-clean-bonus");
		KARMA_CLEAN_HALF = getDouble("karma-clean-half");
		DAMAGE_PENALTY = getDouble("damage-penalty");
		KILL_PENALTY = getInt("kill-penalty");
		T_DAMAGE_REWARD = getInt("t-damage-reward");
		TBONUS = getInt("tbonus");
		KARMA_ROUND_TO_ONE = getBoolean("karma-round-to-one");
		KARMA_KICK = getInt("karma-kick");
		KARMA_BAN = getBoolean("karma-ban");
		KARMA_BAN_TIME = getInt("karma-ban-time");
		DAMAGE_REDUCTION = getBoolean("damage-reduction");
		KARMA_DEBUG = getBoolean("karma-debug");
		VERBOSE_LOGGING = getBoolean("verbose-logging");
		ENABLE_AUTO_UPDATE = getBoolean("enable-auto-update");
		ENABLE_METRICS = getBoolean("enable-metrics");
		LOCALIZATION = getString("localization");
		UNKNOWN_BUILD_WARNING = getBoolean("unknown-build-warning");
		UNSTABLE_BUILD_WARNING = getBoolean("unstable-build-warning");
		CONFIG_VERSION = getString("config-version");
	}

	public static String getString(String a) {
		return TTT.plugin.getConfig().getString(a);
	}

	public static boolean getBoolean(String a) {
		return TTT.plugin.getConfig().getBoolean(a);
	}

	public static int getInt(String a) {
		return TTT.plugin.getConfig().getInt(a);
	}

	public static double getDouble(String a) {
		return TTT.plugin.getConfig().getDouble(a);
	}
}