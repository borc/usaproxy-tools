/**
 * Main JS application
*/

/**
 * Create a closure for the main app. Takes jQuery as argument.
 */
(function($)
{
	// Main application object init
	var initApp = function()
	{
		USAPROXYREPORT.visiblePlot = null; // ID of visible plot (http traffic id)
		USAPROXYREPORT.hideFilteredItems = true; // Hide/show only filtered items?
	}
	initApp();
	
	// Function for showing floating tooltips
	var showTooltip = function(x, y, contents) {
        $('<div id="tooltip">' + contents + '</div>').css({
            position : 'absolute',
            display : 'none',
            top : y + 5,
            left : x + 5,
            border : '1px solid #fdd',
            padding : '2px',
            'background-color' : '#fee',
            opacity : 0.80
        }).appendTo("body").show();
    };
    
    // The UsaProxy session
    var session = USAPROXYREPORT.session;
	
    // Function for creating a HTML table from a JS object.
    // Keys in first column. Values in second column.
    var createHTMLTableFromObject = function( object )
	{
		var tableHTML = '<table>';
		for ( var detail in object )
		{
			tableHTML += '<tr><td>' + detail + '</td><td>';
			if ( object[ detail ].constructor === Date )
			{
				tableHTML += object[ detail ].toLocaleString();
			} 
			else if ( typeof( object[ detail ] ) === 'object' )
			{
				tableHTML += createHTMLTableFromObject( object[ detail ] );
			}
			else
			{
				tableHTML += object[ detail ];
			}
			tableHTML += '</td></tr>';
		}
		tableHTML += '</table>';
		return tableHTML;
	};
    
    // Function for opening a dialog window with details of a DOM element
	var showElementDetails = function( httpTrafficId, dataseries )
	{
		var details = session.httptraffics[ httpTrafficId ].domElements.details[ dataseries.elementDomId ];
		
		var detailsTable = {
			'Node name': details.nodeName,
			'DOM path': details.path
		};
		
		var tableHTML = createHTMLTableFromObject( detailsTable );
		
		var appearancesPlaceholder = $('<div />').css( {
			width: '80%',
			height: '50px',
			margin: '1em',
			padding: 0
		} );
		
		$( '<div title="Element details" />' )  
		.css({
			'font-size': '80%'
		})
		.append( $( tableHTML ) )
		.append( $( '<h3 class="title">Appearances</h3>' ) )
		.append( appearancesPlaceholder )
		.append( $( '<h3 class="title">Contents</h3>' ) )
		.append( $( '<p>' + unescape(details.content) + '</p>' ) )
		.dialog({
			buttons: {
				"OK": function() { $(this).dialog("close"); } 
			},
			width: Math.floor( 0.8 * $( window ).width() )
		});
		
		$.plot( appearancesPlaceholder, [ {data: dataseries.data} ], {
				xaxis : {
					mode: 'time',
					min: dataseries.xaxis.min,
					max: dataseries.xaxis.max
                },
	            yaxis : {
	            	show: false,
	                transform : function(v) {
	                	return -v;
	                },
	                inverseTransform : function(v) {
	                	return -v;
	                }
	            },
	            series: {
	            	lines: {
	            		show: true,
	            		lineWidth: 2
	            	},
	            	points: {
	            		show: true,
	            		radius: 5
	            	},
	            	shadowSize: 0
	            }
	    } );
	};
	
	var initHTTPTrafficFilters = function( pTrafficObject, pForce )
	{
		pTrafficObject.filteredElements = pTrafficObject.filteredElements && !pForce ? pTrafficObject.filteredElements : []; 
		pTrafficObject.filteredTypes = pTrafficObject.filteredTypes && !pForce ? pTrafficObject.filteredTypes : [];
	};
	
	/**
	 * Filters handling
	 */
	var openFiltersWindow = function()
	{
		var httpTraffic = session.httptraffics[ USAPROXYREPORT.visiblePlot ];
		
		// Initialize filtered elements and types arrays if not already inited
		initHTTPTrafficFilters( httpTraffic, false );
		
		var elementHTMLList = "";
		
		var dialogElement = $( '<div title="Filtering settings for traffic ID ' + 
				USAPROXYREPORT.visiblePlot + '" />' )
				.css({
					'font-size': '80%'
				});
		
		var elementTypeList = $( '<ul class="filter-list" />' );
		var processedElementTypes = [];
		
		var listElement = $( '<ul class="filter-list" />' );
		
		for ( var i in httpTraffic.domElements.details )
		{
			var elementDetails = httpTraffic.domElements.details[ i ];
			
			// New element type?
			if ( !processedElementTypes[ elementDetails.nodeName ] )
			{
				var typeListItem = $( '<li class="filter-list-item" />' );
			
				// Id of type filter checkbox element
				var typeid = 'type-filter-checkbox-' + USAPROXYREPORT.visiblePlot + '-' + elementDetails.nodeName;
				
				processedElementTypes[ elementDetails.nodeName ] = 
					$( '<input type="checkbox" id="' + typeid +	'" />' )
						.click( (function( type )
						{
							return function()
							{
								listElement.find('input').trigger( 'toggle-all', [this.checked, type] )
								httpTraffic.filteredTypes[ type ] = this.checked;
								$( this ).parent().removeClass( 'ui-state-highlight' ).attr( 'title', '' );
							};
						})( elementDetails.nodeName ) )
						.each( function()
						{
							if ( httpTraffic.filteredTypes[ elementDetails.nodeName ] ) this.checked = true;
						} )
						.appendTo( typeListItem );
				$( '<label for="' + typeid + '">' + elementDetails.nodeName + 
						'</label>' ).appendTo( typeListItem );
				
				typeListItem.appendTo( elementTypeList );
			}
			
			var halfCheckTypeCheckbox = function( pJQInputElement )
			{
				pJQInputElement
					.attr( 'checked', true )
					.parent()
						.addClass( 'ui-state-highlight' )
						.attr( 'title', 'Separate element filters are active' )	;
			};
			
			var id = 'filter-checkbox-' + USAPROXYREPORT.visiblePlot + '-' + i;
			
			var listItem = $( '<li class="filter-list-item" />' );
			
			$( '<input type="checkbox" id="' + id +	'" />' )
				.click( (function( i, elementType )
				{
					return function() 
					{
						httpTraffic.filteredElements[ i ] = this.checked;
						halfCheckTypeCheckbox( processedElementTypes[ elementType ] );
					};
				})( i, elementDetails.nodeName ) )
				.bind( 'toggle-all', (function( i, thistype )
				{
					return function( event, checked, thattype )
					{
						if ( thistype === thattype )
						{
							this.checked = checked;
							httpTraffic.filteredElements[ i ] = this.checked;
						}
					};
				})( i, elementDetails.nodeName ))
				.each( function()
				{
					// No need to bind loop variables. This is executed immediately.
					if ( httpTraffic.filteredElements[ i ] )
					{
						this.checked = true;
						halfCheckTypeCheckbox( processedElementTypes[ elementDetails.nodeName ] );
					}
				})
				.appendTo( listItem );
			
			$( '<label for="' + id + '">' + elementDetails.nodeName + ': ' + elementDetails.path + 
					( elementDetails.content ? " (" + elementDetails.content.substr(0,30) + ")" : "" ) + 
					'</label>' ).appendTo( listItem );
			
			listItem.appendTo( listElement );
		}
		
		// Create tabs and add the element to the dialog
		$( '<div>' +
				'<ul>' +
					'<li><a href="#filterByType">Filter by type</a></li>' +
					'<li><a href="#filterByElement">Filter by element</a></li>' +
				'</ul>' +
			'</div>')
			.append( 
					$( '<div id="filterByType" />' ).append( elementTypeList ) 
				)
			.append( 
					$( '<div id="filterByElement" />' ).append( listElement ) 
				)
			.appendTo( dialogElement )
			.tabs();
		
		dialogElement.dialog({
				buttons: {
					"OK": function() { $(this).dialog("close"); } 
				},
				width: Math.floor( 0.8 * $( window ).width() ),
				modal: true,
				close: function()
				{
					$( document ).trigger( 'reinit-plot', USAPROXYREPORT.visiblePlot );
					$( this ).dialog( 'destroy' ).remove();
				}
			});
	};
	
	/**
	 * Returns a modified copy of the sightings object filtered by user-selected filters
	 */
	var getFilteredElementSightings = function( httpTrafficId )
	{
		var httpTraffic = session.httptraffics[httpTrafficId];
		if ( !httpTraffic.filteredElements ) return httpTraffic.domElements.sightings; // no filters?
		var filteredSightings = [];
		for ( var i in httpTraffic.domElements.sightings )
		{
			var sighting = httpTraffic.domElements.sightings[ i ];
			if ( httpTraffic.filteredElements[ sighting.elementDomId ] ?
					!USAPROXYREPORT.hideFilteredItems : USAPROXYREPORT.hideFilteredItems )
			{
				filteredSightings.push( sighting );
			}
		}
		return filteredSightings;
	};
	
	/*
	 * Shows a http headers dialog
	 */
	var showHTTPHeaders = function( httpTrafficId )
	{
		var httpTraffic = session.httptraffics[ httpTrafficId ];
		
		var requestHTML = createHTMLTableFromObject( httpTraffic.requestHeaders );
		var responseHTML = createHTMLTableFromObject( httpTraffic.responseHeaders );
		
		$( '<div id="httpHeadersDialog" title="HTTP Headers">' + 
				'<div><h3 class="title">Request</h3>' + requestHTML + '</div>' + 
				'<div><h3 class="title">Response</h3>' + responseHTML + '</div>' + 
			'</div>' )
		.css({
			'font-size': '80%'
		}).dialog({
			buttons: {
				"OK": function() { $(this).dialog("close"); } 
			},
			width: Math.floor( 0.8 * $( window ).width() )
		});
	};
	
	// Entry point. Run when document fully loaded.
	$( document ).ready( function() {
		
		$( "button" ).button();
	
		// Bind the filters button click
		$( '#filters' ).click( openFiltersWindow );
		
		// Bind the reset button click
		$( '#reset' ).click( function()
		{
			// Force reinit of filters and the plot itself
			initHTTPTrafficFilters( session.httptraffics[ USAPROXYREPORT.visiblePlot ], true );
			$( document ).trigger( 'reinit-plot', USAPROXYREPORT.visiblePlot );
		} );
		
		// Bind the HTTP headers button click
		$( '#headers' ).click( function()
		{
			showHTTPHeaders( USAPROXYREPORT.visiblePlot );
		} );
		
		$( document ).bind( 'reinit-plot', function( event, httpTrafficId )
		{
			// Destroy previous contents
			$( session.httptraffics[httpTrafficId].container ).empty().unbind();
			
			// Init again
			$( document ).trigger( 'init-plot', [ httpTrafficId, session.httptraffics[httpTrafficId].container ] );
		} );
		
		$( document ).bind( 'init-plot', function( event, httpTrafficId, placeholder )
		{
			var sightings = getFilteredElementSightings( httpTrafficId );
			
			$.plot(placeholder, session.httptraffics[httpTrafficId].viewportMovement.concat(sightings), {
				xaxis : {
					mode : 'time'
                },
	            yaxis : {
	            	position: 'right',
	            	labelWidth: 40,
	                tickFormatter : function(v) {
	                	return v + " %";
	                },
	                transform : function(v) {
	                	return -v;
	                },
	                inverseTransform : function(v) {
	                	return -v;
	                }
	            },
	            grid : {
	                        hoverable : true,
	                        clickable : true
	            },
	            zoom: {
	                        interactive: true
	            },
	            pan: {
	                        interactive: true
	            }
	        });
			
			session.httptraffics[httpTrafficId].container = placeholder;
			
			$( placeholder ).bind("plothover", function(event, pos, item) {
                $("#x").text(pos.x.toFixed(2));
                $("#y").text(pos.y.toFixed(2));

                if(item) {
                    $("#tooltip").remove();
                    var x = item.datapoint[0].toFixed(2), y = item.datapoint[1].toFixed(2);

                    showTooltip(item.pageX, item.pageY, item.series.elementDomId + ": " + 
                    	new Date(parseInt(x)).toUTCString() + ", " + y + " %");
                } else {
                    $("#tooltip").remove();
                }
            }).bind( 'plotclick', (function( x )
            {
            	return function( event, pos, item )
            	{
            		if ( item )
            		{
            			showElementDetails( x, item.series );
            		}
            	};
            })( httpTrafficId ) );
	    } );
		

	} );
})($);