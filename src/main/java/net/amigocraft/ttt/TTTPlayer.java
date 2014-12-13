/*
 * TTT
 * Copyright (c) 2014, Maxim Roncacé <http://bitbucket.org/mproncace>
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
package net.amigocraft.ttt;

import net.amigocraft.mglib.api.MGPlayer;
import net.amigocraft.mglib.exception.PlayerOfflineException;
import net.amigocraft.ttt.managers.KarmaManager;
import org.bukkit.Bukkit;

public class TTTPlayer extends MGPlayer {

	private boolean discreet = false;
	private String killer;
	private int karma;
	private int dispKarma;
	private boolean teamKill = false;
	private double damageRed = 1;
	private boolean found = false;

	public TTTPlayer(String plugin, String name, String arena){
		super(plugin, name, arena);
		KarmaManager.loadKarma(name);
		karma = KarmaManager.playerKarma.get(name);
		dispKarma = KarmaManager.playerKarma.get(name);
		getBukkitPlayer().setCompassTarget(Bukkit.getWorlds().get(1).getSpawnLocation());
	}

	public boolean isDiscreet(){
		return discreet;
	}

	public String getKiller(){
		return killer;
	}

	public int getKarma(){
		return karma;
	}

	public int getDisplayKarma(){
		return dispKarma;
	}

	public double getDamageReduction(){
		return damageRed;
	}

	public boolean hasTeamKilled(){
		return teamKill;
	}

	public boolean isBodyFound(){
		return found;
	}

	public void setDiscreet(boolean discreet){
		this.discreet = discreet;
	}

	public void setKiller(String killer){
		this.killer = killer;
	}

	public void setKarma(int karma){
		this.karma = karma;
	}

	public void setDisplayKarma(int karma){
		this.dispKarma = karma;
	}

	public void setBodyFound(boolean found){
		this.found = found;
	}

	public void calculateDamageReduction(){
		// Below is an approximation of the original game's formula. It was calculated on a TI Nspire, so it may not be 100% accurate.
		double a = -1.5839260914526 * Math.pow(10, -7);
		double b = 2.591955951727 * Math.pow(10, -4);
		double c = -6.969034697 * Math.pow(10, -4);
		double d = 0.185644476098;
		int x = karma;
		this.damageRed = Math.round(a * Math.pow(x, 3) + b * Math.pow(x, 2) + c * x + d) / (double) 100;
		if (damageRed > 1){
			damageRed = 1;
		}
		else if (damageRed <= 0){
			damageRed = 0.01;
		}
	}

	public void addKarma(int karma){
		if (karma == 0 && Config.KARMA_ROUND_TO_ONE){
			karma = 1;
		}
		if (this.karma + karma < Main.maxKarma){
			this.karma += karma;
		}
		else if (this.karma < Main.maxKarma){
			this.karma = Main.maxKarma;
		}
		if (Config.KARMA_DEBUG){
			Main.kLog.info("[TTT Karma Debug] " + getName() + ": +" + karma + ". " + "New value: " + getKarma());
		}
	}

	public void subtractKarma(int karma){
		if (karma == 0 && Config.KARMA_ROUND_TO_ONE){
			karma = 1;
		}
		if (this.karma - karma < Config.KARMA_KICK){
			KarmaManager.handleKick(this);
		}
		else {
			this.karma -= karma;
			teamKill = true;
		}
		if (Config.KARMA_DEBUG){
			Main.kLog.info("[TTT Karma Debug] " + getName() + ": -" + karma + ". " + "New value: " + getKarma());
		}
	}

	@Override
	public void reset() throws PlayerOfflineException{
		super.reset();
		getBukkitPlayer().setCompassTarget(Bukkit.getWorlds().get(0).getSpawnLocation());
	}

	public boolean equals(Object p){
		if (p instanceof TTTPlayer){
			TTTPlayer t = (TTTPlayer) p;
			return getName().equals(t.getName()) && getArena().equals(t.getArena()) &&
					getTeam().equals(t.getTeam());
		}
		return false;
	}

	public int hashCode(){
		return 41 * (getName().hashCode() * 37 + getArena().hashCode() * 43 + getTeam().hashCode() * 53);
	}

	public boolean isTraitor(){
		return this.getTeam() != null && this.getTeam().equals("Traitor");
	}

}
