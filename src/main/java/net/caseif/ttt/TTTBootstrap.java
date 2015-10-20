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
package net.caseif.ttt;

import static net.caseif.ttt.util.Constants.MIN_FLINT_VERSION;

import net.caseif.ttt.command.SpecialCommandManager;
import net.caseif.ttt.listeners.SpecialPlayerListener;
import net.caseif.ttt.util.helper.ConfigHelper;

import net.caseif.rosetta.LocaleManager;
import net.gravitydevelopment.updater.Updater;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

import java.io.File;
import java.io.IOException;

public class TTTBootstrap extends JavaPlugin {

    private static final int CURSEFORGE_PROJECT_ID = 52474;
    private static final int STEEL_CURSEFORGE_PROJECT_ID = 95203;

    public static TTTBootstrap INSTANCE;

    public static LocaleManager locale;
    public static boolean STEEL = true;

    public TTTBootstrap() {
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        STEEL = Bukkit.getPluginManager().isPluginEnabled("Steel");
        locale = new LocaleManager(this);
        initializeUpdater();
        initializeMetrics();
        if (!STEEL) {
            fail();
            return;
        }
        new TTTCore(this, locale).initialize();
    }

    @Override
    public void onDisable() {
        if (STEEL) {
            TTTCore.getInstance().deinitialize();
        }
    }

    @Override
    public File getFile() {
        return super.getFile();
    }

    public void fail() {
        getLogger().warning(locale.getLocalizable("error.plugin.flint")
                .withReplacements(MIN_FLINT_VERSION + "").localize());
        Bukkit.getPluginManager().registerEvents(new SpecialPlayerListener(), this);
        getCommand("ttt").setExecutor(new SpecialCommandManager());
    }

    private void initializeMetrics() {
        if (ConfigHelper.ENABLE_METRICS) {
            try {
                Metrics metrics = new Metrics(this);
                Metrics.Graph graph = metrics.createGraph("Steel Version");
                graph.addPlotter(new Metrics.Plotter(
                        Bukkit.getPluginManager().getPlugin("Steel").getDescription().getVersion()
                ) {
                    public int getValue() {
                        return 1;
                    }
                });
                metrics.addGraph(graph);
                metrics.start();
            } catch (IOException ex) {
                if (ConfigHelper.VERBOSE_LOGGING) {
                    getLogger().warning(locale.getLocalizable("error.plugin.mcstats").localize());
                }
            }
        }
    }

    private void initializeUpdater() {
        if (ConfigHelper.ENABLE_AUTO_UPDATE) {
            new Updater(this, CURSEFORGE_PROJECT_ID, getFile(), Updater.UpdateType.DEFAULT,
                    new TTTUpdateCallback(), true);

            if (!STEEL) {
                new Updater(this, STEEL_CURSEFORGE_PROJECT_ID, new File(getDataFolder().getParentFile(), "Steel.jar"),
                        Updater.UpdateType.DEFAULT,
                        new SteelUpdateCallback(), true);
            }
        }
    }

    private class SteelUpdateCallback implements Updater.UpdateCallback {
        @Override
        public void onFinish(Updater updater) {
            if (updater.getResult() == Updater.UpdateResult.SUCCESS) {
            getLogger().info(locale.getLocalizable("info.plugin.installed-steel").localize());
        } else if (updater.getResult() == Updater.UpdateResult.FAIL_APIKEY
                    || updater.getResult() == Updater.UpdateResult.FAIL_BADID
                    || updater.getResult() == Updater.UpdateResult.FAIL_DBO
                    || updater.getResult() == Updater.UpdateResult.FAIL_DOWNLOAD
                    || updater.getResult() == Updater.UpdateResult.FAIL_NOVERSION) {
                getLogger().info(locale.getLocalizable("error.plugin.update-fail").localize());
            }
        }
    }

    private class TTTUpdateCallback implements Updater.UpdateCallback {
        @Override
        public void onFinish(Updater updater) {
            if (updater.getResult() == Updater.UpdateResult.SUCCESS) {
                getLogger().info(locale.getLocalizable("info.plugin.updated").localize());
            } else if (updater.getResult() == Updater.UpdateResult.FAIL_APIKEY
                    || updater.getResult() == Updater.UpdateResult.FAIL_BADID
                    || updater.getResult() == Updater.UpdateResult.FAIL_DBO
                    || updater.getResult() == Updater.UpdateResult.FAIL_DOWNLOAD
                    || updater.getResult() == Updater.UpdateResult.FAIL_NOVERSION) {
                getLogger().info(locale.getLocalizable("error.plugin.update-fail").localize());
            }
        }
    }

}
