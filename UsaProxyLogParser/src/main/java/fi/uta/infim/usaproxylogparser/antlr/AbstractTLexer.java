/*
 * UsaProxyLogParser - Java API for UsaProxy-fork logs
 *  Copyright (C) 2012 Teemu Pääkkönen - University of Tampere
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package fi.uta.infim.usaproxylogparser.antlr;

import org.antlr.runtime.CharStream;
import org.antlr.runtime.Lexer;
import org.antlr.runtime.RecognizerSharedState;

/**

 * This is the super class for the lexer. It is extended by the lexer class
 * generated from TLexer.g.
 *
 * Do not place code and declarations in the lexer .g files, use
 * a superclass like this and place all the support methods and
 * error overrides etc in the super class. This way you will keep
 * the lexer grammar clean and hunky dory.
 *
 * @author Jim Idle - Temporal Wave LLC (jimi@idle.ws)
 */
public abstract class AbstractTLexer
    extends Lexer

{
    /**
     * Default constructor for the lexer, when you do not yet know what
     * the character stream to be provided is.
     */
    public AbstractTLexer() {
    }

    /**
     * Create a new instance of the lexer using the given character stream as
     * the input to lex into tokens.
     *
     * @param input A valid character stream that contains the ruleSrc code you
     *              wish to compile (or lex at least)
     */
    public AbstractTLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }

    /**
     * Internal constructor for ANTLR - do not use.
     *
     * @param input The character stream we are going to lex
     * @param state The shared state object, shared between all lexer comonents
     */
    public AbstractTLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);
    }

}

