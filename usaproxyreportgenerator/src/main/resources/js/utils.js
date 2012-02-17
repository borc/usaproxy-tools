/*
JS util functions. Mostly augmentations.
*/

String.prototype.padLeft = function( pPadChar, pTotalLength )
{
	return ( pTotalLength <= this.length ) ? this :
		( '' + pPadChar + this ).padLeft(pPadChar, pTotalLength);
};
