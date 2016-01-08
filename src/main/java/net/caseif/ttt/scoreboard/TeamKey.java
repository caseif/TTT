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
package net.caseif.ttt.scoreboard;

import com.google.common.base.Objects;

/**
 * Represents a hashable object used to denote the properties of a {@link Team}
 * in a map.
 */
class TeamKey {

    private boolean traitorBoard;
    private String role;
    private String aliveStatus;

    TeamKey(boolean isTraitorBoard, String role, String aliveStatus) {
        this.traitorBoard = isTraitorBoard;
        this.role = role;
        this.aliveStatus = aliveStatus;
    }

    boolean isTraitorBoard() {
        return traitorBoard;
    }

    String getRole() {
        return role;
    }

    String getAliveStatus() {
        return aliveStatus;
    }

    public boolean equals(Object obj) {
        if (obj instanceof TeamKey) {
            TeamKey tk = (TeamKey) obj;
            return traitorBoard == tk.traitorBoard && role.equals(tk.role) && aliveStatus.equals(tk.aliveStatus);
        }
        return false;
    }

    public int hashCode() {
        return Objects.hashCode(traitorBoard, role, aliveStatus);
    }

}
