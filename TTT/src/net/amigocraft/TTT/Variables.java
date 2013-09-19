package net.amigocraft.TTT;

public class Variables {

	private TTT plugin;
	public static double detective_ratio;
	public static int maximum_players;
	public static double traitor_ratio;
	public static int minimum_players_for_detective;
	public static int crowbar_damage;
	public static boolean guns_outside_arenas;
	public static boolean require_ammo_for_guns;
	public static boolean karma_persistence;
	public static int default_karma;
	public static int max_karma;
	public static int karma_heal;
	public static int karma_clean_bonus;
	public static double karma_clean_half;
	public static double damage_penalty;
	public static int kill_penalty;
	public static int t_damage_reward;
	public static int tbonus;
	public static boolean karma_round_to_one;
	public static int karma_kick;
	public static boolean karma_ban;
	public static int karma_ban_time;
	public static boolean karma_debug;
	public static boolean verbose_logging;
	public static boolean damage_reduction;
	public static boolean enable_auto_update;
	public static boolean enable_metrics;
	public static String localization;
	public static boolean unknown_build_warning;
	public static boolean unstable_build_warning;
	public static String config_version;
	public static int minimum_players;
	public static int time_limit;
	public static int setup_time;

	public Variables(TTT plugin) {
		this.plugin = plugin;
		InitialiseVariables();
	}

	public void InitialiseVariables() {
		time_limit = getInt("time-limit");
		setup_time = getInt("setup-time");
		minimum_players = getInt("minimum-players");
		maximum_players = getInt("maximum-players");
		traitor_ratio = getDouble("traitor-ratio");
		detective_ratio = getDouble("detective-ratio");
		minimum_players_for_detective = getInt("minimum-players-for-detective");
		crowbar_damage = getInt("crowbar-damage");
		guns_outside_arenas = getBoolean("guns-outside-arenas");
		require_ammo_for_guns = getBoolean("require-ammo-for-guns");
		karma_persistence = getBoolean("karma-persistence");
		default_karma = getInt("default-karma");
		max_karma = getInt("max-karma");
		karma_heal = getInt("karma-heal");
		karma_clean_bonus = getInt("karma-clean-bonus");
		karma_clean_half = getDouble("karma-clean-half");
		damage_penalty = getDouble("damage-penalty");
		kill_penalty = getInt("kill-penalty");
		t_damage_reward = getInt("t-damage-reward");
		tbonus = getInt("tbonus");
		karma_round_to_one = getBoolean("karma-round-to-one");
		karma_kick = getInt("karma-kick");
		karma_ban = getBoolean("karma-ban");
		karma_ban_time = getInt("karma-ban-time");
		damage_reduction = getBoolean("damage-reduction");
		karma_debug = getBoolean("karma-debug");
		verbose_logging = getBoolean("verbose-logging");
		enable_auto_update = getBoolean("enable-auto-update");
		enable_metrics = getBoolean("enable-metrics");
		localization = getString("localization");
		unknown_build_warning = getBoolean("unknown-build-warning");
		unstable_build_warning = getBoolean("unstable-build-warning");
		config_version = getString("config-version");
	}

	public String getString(String a) {
		return plugin.getConfig().getString(a);
	}

	public boolean getBoolean(String a) {
		return plugin.getConfig().getBoolean(a);
	}

	public int getInt(String a) {
		return plugin.getConfig().getInt(a);
	}

	public double getDouble(String a) {
		return plugin.getConfig().getDouble(a);
	}
}