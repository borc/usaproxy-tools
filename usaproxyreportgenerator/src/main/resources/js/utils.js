/**
 * JS util functions. Mostly augmentations.
*/

/**
 * This function augments the native String object.
 * Returns a left-padded string. Leaves the original string intact.
 * First parameter is the padding character, and it is expected to be exactly 
 * one character long, but using longer paddings should work just as well.
 * Second parameter is the targeted total length of the padded string.
 */
String.prototype.padLeft = function( pPadChar, pTotalLength )
{
	return ( pTotalLength <= this.length ) ? this :
		( pPadChar + this ).padLeft(pPadChar, pTotalLength);
};
