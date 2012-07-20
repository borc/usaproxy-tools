/*! Copyright (c) 2010 Brandon Aaron (http://brandonaaron.net)
 * Copyright (c) 2012 Teemu Pääkkönen, University of Tampere
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

;(function($, document, window, undefined) {

$.fn.overlaps = function(selector, obstruct) {
    
    var obstruct = obstruct || false;
    
    // if nothing is passed, act as a filter
    if (arguments.length === 0) {
        return this.pushStack(filterOverlaps(this));
    }
    // otherwise compare selected elements against passed eleemnts
    else {
        return isOverlapping(this, $(selector), obstruct);
    }
};

$.expr[':'].overlaps = function(elem, i, m, array) {
    return isOverlapping([elem], array);
};
$.expr[':'].overlapping = $.expr[':'].overlaps;

function filterOverlaps(collection) {
    var dims   = getDims(collection),
        stack  = [],
        index1 = 0,
        index2 = 0,
        length = dims.length;
    
    for (; index1 < length; index1++) {
        for (index2 = 0; index2 < length; index2++) {
            if (index1 === index2) { 
                continue;
            }
            if (checkOverlap(dims[index1], dims[index2])) {
                stack.push(collection[index2]);
            }
        }
    }
    
    return $.unique(stack);
}

function isOverlapping(collection1, collection2, obstruct) {
    var obstruct = obstruct || false;
    var dims1   = getDims(collection1),
        dims2   = getDims(collection2),
        index1  = 0,
        index2  = 0,
        length1 = dims1.length,
        length2 = dims2.length;

    for (; index1 < length1; index1++) {
        for (index2 = 0; index2 < length2; index2++) {
            if (collection1[index1] === collection2[index2]) {
                continue;
            }
            if ( obstruct ? checkObstruct( collection1[index1], collection1[index2] ) : 
                    checkOverlap(dims1[index1], dims2[index2])) {
                return true;
            }
        }
    }
    
    return false;
}

function getDims(elems) {
    var dims = [], i = 0, offset, elem;
    
    while ((elem = elems[i++])) {
        offset = $(elem).offset();
        dims.push({
            top: 		offset.top,
            left: 		offset.left,
            width: 		elem.offsetWidth,
            height: 	elem.offsetHeight
        });
    }
    
    return dims;
}

function checkOverlap(dims1, dims2) {
    var x1 = dims1.left, y1 = dims1.top,
        w1 = dims1.width, h1 = dims1.height,
        x2 = dims2.left, y2 = dims2.top,
        w2 = dims2.width, h2 = dims2.height;
    return !(y2 + h2 < y1 || y1 + h1 < y2 || x2 + w2 < x1 || x1 + w1 < x2);
}

/**
 * Check whether an element completely obstructs another from the view.
 * Return values:
 *   1  <- el1 obstructs el2
 *  -1  <- el2 obstructs el1
 *   0  <- no obstruction
 */
function checkObstruct( el1, el2 ) {
	var dims = getDims( [el1, el2] );
	var dims1 = dims[0];
	var dims2 = dims[1];
	if ( !checkOverlap( dims1, dims2 ) ) return 0; // no obstruction if no overlap
	var stackOrder = stacksOnTopOf( el1, el2 );
	var elementBelow = stackOrder ? dims2 : dims1;
	var elementOnTop = stackOrder ? dims1 : dims2;
	if ( elementBelow.left >= elementOnTop.left && 
			elementBelow.top >= elementOnTop.top && 
			elementBelow.left + elementBelow.width <= elementOnTop.left + elementOnTop.width &&
			elementBelow.top + elementBelow.height <= elementOnTop.top + elementOnTop.height )
	{
		return elementBelow === dims2 ? 1 : -1;
	}
    return 0;
}

/**
 * Checks whether el1 stacks on top of el2
 */
function stacksOnTopOf( el1, el2 ) {
	var jqEl1 = $(el1), jqEl2 = $(el2);
	var el1Z = jqEl1.css( 'z-index' ), el2Z = jqEl2.css( 'z-index' ); 
	if ( el1Z > el2Z ) return true;
	if ( el1Z < el2Z ) return false;
	
	// Same Z-index means that we must check the elements' locations
	// in the DOM tree. Elements are stack according to the order they 
	// appear in the markup so that later appearing elements are stacked on top.
	var ind1 = getIdx( el1 );
	var ind2 = getIdx( el2 );
	var indices = [ ind1, ind2 ];
	indices.sort();
	return ind1 == indices[ 1 ]; // the element that sorts after the other is on top
}

var alpha = "1234567890qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM";
alpha = alpha.split("");
alpha.sort();

/**
 * Returns an alphabetically sortable path from the document root for an element.
 * eg. AABFH
 * @param el
 * @returns {String}
 */
function getIdx( el ) {
	var jqEl = $(el);
	var parent = jqEl.parent();
	if ( parent.length == 0 ) return '' + posIdxToAlpha( jqEl.index() );
	return getIdx( parent.get(0) ) + posIdxToAlpha( jqEl.index() );
}

function posIdxToAlpha( pos ) {
	++pos; // add one to compensate for zero-based indexing, removed later
	// The last character of the array is used as a special character for cases
	// where the position > alpha.length. Therefore we only use characters up to
	// alpha.length - 1.
	var roundCount = Math.floor( (pos - 1) / (alpha.length - 1) );
	var lastAlpha = alpha[ (pos - 1) % (alpha.length - 1) ];
	return (new Array( roundCount + 1 )).join( alpha[ alpha.length - 1 ] ) + lastAlpha;
}

})(jQuery, document, window);