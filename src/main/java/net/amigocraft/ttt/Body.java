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

import net.amigocraft.mglib.api.Location3D;
// the class

public class Body {
	private String player;
	private String arena;
	private String team;
	private Location3D l;
	private long time;

	public Body(String player, String arena, String team, Location3D l, long time){
		this.player = player;
		this.arena = arena;
		this.team = team;
		this.l = l;
		this.time = time;
	}

	public String getPlayer(){
		return player;
	}

	public String getArena(){
		return arena;
	}

	public String getTeam(){
		return team;
	}

	public Location3D getLocation(){
		return l;
	}

	public long getTime(){
		return time;
	}

	public boolean equals(Object b){
		return player.equals(((Body) b).getPlayer()) && arena.equals(((Body) b).getArena()) &&
				team.equals(((Body) b).getTeam()) && l.equals(((Body) b).getLocation());
	}

	public int hashCode(){
		return 41 * (41 + player.hashCode() + l.hashCode());
	}
}