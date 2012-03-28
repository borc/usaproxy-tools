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

import org.antlr.runtime.*;

/**
 * The super class of the generated parser. It is extended by the generated
 * code because of the superClass optoin in the .g file.
 *
 * This class contains any helper functions used within the parser
 * grammar itself, as well as any overrides of the standard ANTLR Java
 * runtime methods, such as an implementation of a custom error reporting
 * method, symbol table populating methods and so on.
 *
 * @author Jim Idle - Temporal Wave LLC - jimi@temporal-wave.com
 */
public abstract class AbstractTParser

        extends Parser

{
    /**
     * Create a new parser instance, pre-supplying the input token stream.
     * 
     * @param input The stream of tokens that will be pulled from the lexer
     */
    protected AbstractTParser(TokenStream input) {
        super(input);
    }

    /**
     * Create a new parser instance, pre-supplying the input token stream
     * and the shared state.
     *
     * This is only used when a grammar is imported into another grammar, but
     * we must supply this constructor to satisfy the super class contract.
     *
     * @param input The stream of tokesn that will be pulled from the lexer
     * @param state The shared state object created by an interconnectd grammar
     */
    protected AbstractTParser(TokenStream input, RecognizerSharedState state) {
        super(input, state);
    }


    /**
     * Creates the error/warning message that we need to show users/IDEs when
     * ANTLR has found a parsing error, has recovered from it and is now
     * telling us that a parsing exception occurred.
     *
     * @param tokenNames token names as known by ANTLR (which we ignore)
     * @param e The exception that was thrown
     */
    @Override
    public void displayRecognitionError(String[] tokenNames, RecognitionException e) {

        // This is just a place holder that shows how to override this method
        //
        super.displayRecognitionError(tokenNames, e);
    }
}

