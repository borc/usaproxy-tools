;(function( UsaProxy, document, window, undefined ) {
	UsaProxy.plugins.monocle = {
		setup : function() {},
		events : [ {
		          bindTo : function() { 
		        	  return UsaProxy.jQuery( '.monelem_box' ).get(); },
		          events : function() { 
		        	  return [ 'monocle:turn' ]; },
		          onTrigger : function( event ) {
		        	  var reader = event.target.dom.properties.reader;
		        	  var place = reader.getPlace();
		        	  return {
		        		  pageNumber : place.pageNumber(),
		        		  bottom : 100.0 * place.percentAtBottomOfPage(),
		        		  top : 100.0 * place.percentAtTopOfPage()
		        	  }; },
		          deferBind : function( bindFunc ) {
	        		  UsaProxy.jQuery( 'div' ).one( 'monocle:loaded', function() {
	        			  bindFunc( [ this ] );
	        		  } );
		          }
		}
		]	
	};
} )( UsaProxy, document, window );