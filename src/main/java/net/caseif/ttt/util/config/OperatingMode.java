/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013-2015, Maxim Roncace <mproncace@lapis.blue>
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
package net.caseif.ttt.util.config;

/**
 * Represents an available operating mode for the plugin.
 *
 * @author Max Roncace
 */
public enum OperatingMode {

    /**
     * The standard operating mode. Players may join rounds at will, and will be
     * ejected from a round upon it ending.
     */
    STANDARD,
    /**
     * An operating mode more faithful to the original gamemode. Players may
     * join rounds at will, but will not be ejected upon the round ending.
     * Instead, the arena will reset to its initial state and a new round will
     * begin.
     */
    CONTINUOUS,
    /**
     * An operating mode intended for configurations in which an entire server
     * is dedicated to the minigame. Only one round will exist on the server at
     * a given time, and players will be automatically entered into it upon
     * connecting. This mode also enables the {@link CycleMode arena cycling}
     * functionality.
     */
    DEDICATED

}
