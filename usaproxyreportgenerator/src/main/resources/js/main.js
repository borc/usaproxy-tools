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
    
    /**
     * Function for moving the existing tooltip to another location.
     * Does nothing if the tooltip doesn't exist.
     */
    var moveTooltip = function( x, y ) {
    	$( '#tooltip' ).css( {
    		top: y + 5,
    		left: x + 5
    	});
    }
    
    // The UsaProxy session
    var session = USAPROXYREPORT.session;
	
    // Function for creating a HTML table from a JS object.
    // Keys in first column. Values in second column.
    var createHTMLTableFromObject = function( object )
	{
		var tableHTML = '<table>';
		for ( var detail in object )
		{
			tableHTML += '<tr><td class="objectMember">' + detail + '</td><td class="objectMemberValue">';
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
    
	/**
	 * Function that replaces the contents in element details with an img tag
	 * with the original image URL as source. Only works for images.
	 */
	var replaceContentsWithImage = function( details )
	{
		if ( !details.img ) return; // not an image
		
		var image = '<img src="' + unescape(details.img) + '" />';
		details.img = false;
		details.content = image;
	}
	
    // Function for opening a dialog window with details of a DOM element
	var showElementDetails = function( httpTrafficId, dataseries, plotObj )
	{
		var details = session.httptraffics[ httpTrafficId ].domElements.details[ dataseries.elementDomId ];
		
		var detailsTable = {
			'Element type': details.nodeName ? details.nodeName : '&lt;unknown&gt;',
			'DOM path': details.path
		};
		
		var tableHTML = createHTMLTableFromObject( detailsTable );
		
		var appearancesPlaceholder = $('<div />').css( {
			width: '80%',
			height: '50px',
			margin: '1em',
			padding: 0
		} );
		
		var contentsId = 'contents-id-' + details.path;
		var contents = '';
		if ( details.img )
		{
			if ( $( '#autoload' ).is( ':checked' ) )
			{
				replaceContentsWithImage( details );
				contents = $( '<p>' + unescape(details.content) + '</p>' );
			}
			else
			{
				contents = $( '<p />' )
					.append( $( '<a href="javascript:;">Click here to attempt to load the image</a>' )
							.click( function()
							{
								replaceContentsWithImage( details );
								contents
									.empty()
									.append( $( details.content ) );
							} ) );
			}
		}
		else
		{
			contents = $( '<p>' + unescape(details.content) + '</p>' );
		}
		
		$( '<div title="Element details" />' )  
		.css({
			'font-size': '80%'
		})
		.append( $( tableHTML ) )
		.append( $( '<h3 class="title">Appearances</h3>' ) )
		.append( appearancesPlaceholder )
		.append( $( '<h3 class="title">Contents</h3>' ) )
		.append( contents )
		.dialog({
			buttons: {
				"OK": function() { $(this).dialog("close"); } 
			},
			width: Math.floor( 0.8 * $( window ).width() )
		});
		
		$.plot( appearancesPlaceholder, [ {data: dataseries.data} ], {
				xaxis : {
					mode: 'time',
					min: plotObj.getAxes().xaxis.datamin,
					max: plotObj.getAxes().xaxis.datamax
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
								$( this ).parent().removeClass( 'filter-list-alert' ).attr( 'title', '' );
							};
						})( elementDetails.nodeName ) )
						.each( function()
						{
							if ( httpTraffic.filteredTypes[ elementDetails.nodeName ] ) this.checked = true;
						} )
						.appendTo( typeListItem );
				$( '<label for="' + typeid + '">' + 
						(elementDetails.nodeName ? elementDetails.nodeName : '&lt;unknown&gt;') + 
						'</label>' ).appendTo( typeListItem );
				
				typeListItem.appendTo( elementTypeList );
			}
			
			var halfCheckTypeCheckbox = function( pJQInputElement )
			{
				pJQInputElement
					.attr( 'checked', true )
					.parent()
						.addClass( 'filter-list-alert' )
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
			
			$( '<label for="' + id + '">' + 
					( elementDetails.nodeName ? elementDetails.nodeName : '&lt;unknown&gt;' ) + 
					': ' + elementDetails.path + 
					( elementDetails.content ? " (" + unescape(elementDetails.content.substr(0,30)) + ")" : '&lt;empty&gt;' ) + 
					'</label>' ).appendTo( listItem );
			
			listItem.appendTo( listElement );
		}
		
		// Create the show/hide selection
		var showOnlyFilteredItems = session.httptraffics[ USAPROXYREPORT.visiblePlot ].showOnlyFilteredItems;
		$( '<div />' )
			.append( $( '<input type="radio" name="showhide" id="radioHide" ' + 
					( !showOnlyFilteredItems ? 'checked="checked"' : '' ) + ' />' )
					.click( function()
					{
						session.httptraffics[ USAPROXYREPORT.visiblePlot ].showOnlyFilteredItems = false;
					} ) )
			.append( '<label for="radioHide">Hide</label>')
			.append( $( '<input type="radio" name="showhide" id="radioShowOnly"' +
					( showOnlyFilteredItems ? 'checked="checked"' : '' ) + ' />' )
					.click( function()
					{
						session.httptraffics[ USAPROXYREPORT.visiblePlot ].showOnlyFilteredItems = true;
					} ) )
			.append( '<label for="radioShowOnly">Show</label>')
			.appendTo( dialogElement )
			.buttonset();
		
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
					httpTraffic.showOnlyFilteredItems : !httpTraffic.showOnlyFilteredItems )
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
		
		var requestHTML = $( createHTMLTableFromObject( httpTraffic.requestHeaders ) ).addClass('headers');
		var responseHTML = $( createHTMLTableFromObject( httpTraffic.responseHeaders ) ).addClass('headers');
		
		$( '<div id="httpHeadersDialog" title="HTTP Headers" />' )
			.append( $('<div />')
					.append( '<h3 class="title">Request</h3>' )
					.append( requestHTML ) ) 
			.append( $('<div />')
					.append( '<h3 class="title">Response</h3>' )
					.append( responseHTML ) )
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
		
		// Style with jQuery
		$( "button" ).button();
		$( "#autoload" )
			.button( { icons: { primary: 'ui-icon-radio-off' } } )
			.click( function() {
				$( this ).button( 'option', 'icons', { 
					primary: (this.checked ? 'ui-icon-radio-on' : 'ui-icon-radio-off') } );
			});
	
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
		
		// Reinit event handler. Reinit event destroys the plot and creates it again.
		$( document ).bind( 'reinit-plot', function( event, httpTrafficId )
		{
			// Destroy previous contents
			$( session.httptraffics[httpTrafficId].container ).empty().unbind();
			
			// Init again
			$( document ).trigger( 'init-plot', [ httpTrafficId, session.httptraffics[httpTrafficId].container ] );
		} );
		
		// Init-plot event handler. Plot init event sets up the plot data,
		// creates the plot itself and binds the plot's hover and click events.
		$( document ).bind( 'init-plot', function( event, httpTrafficId, placeholder )
		{
			var sightings = getFilteredElementSightings( httpTrafficId );
			
			var plotObj = $.plot( placeholder, 
					session.httptraffics[httpTrafficId].viewportMovement.concat( sightings ), 
			{
				xaxis : {
					mode : 'time'
                },
	            yaxis : {
	            	position: 'right',
	            	labelWidth: 40,
	            	min: 0,
	            	max: 100,
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
	                        clickable : true,
	                        hoverableFill: true,
	                        clickableFill: true
	            },
	            zoom: {
	                        interactive: true
	            },
	            pan: {
	                        interactive: true
	            }
	        });
			
			session.httptraffics[httpTrafficId].container = placeholder;
			
			// Which element is shown in the tooltip currently?
			var elementInTooltip = null;
			
			/*
			 * Updates or creates the element tooltip.
			 * Returns true if tooltip was updated, false if a new one was created.
			 */
			var updateTooltip = function( elementDomId, mouseX, mouseY, elementDetails, plotX, plotY )
			{
				var updated = false;
				if ( elementInTooltip == elementDomId )
                {
                	moveTooltip( mouseX, mouseY );
                	updated = true;
                }
                else
                {
                	$("#tooltip").remove();
                	
                	elementInTooltip = elementDomId;
                	var details = elementDetails;
                
                    if ( $( '#autoload' ).is( ':checked' ) ) replaceContentsWithImage( details );
                    
                    showTooltip(mouseX, mouseY, 
                    	new Date(parseInt(plotX.toFixed(2))).toUTCString() + ", " + plotY.toFixed(2) + " % <br />" +
                    	elementDomId + ", " + 
                    	(details.nodeName ? details.nodeName : '&lt;unknown&gt;') + ': <br />' + 
                    	(details.nodeName.toUpperCase() !== 'IMG' ? unescape( details.content ).substr(0,30) : details.content ) );
                }
				return updated;
			}
			
			/*
			 * Removes the tooltip entirely.
			 */
			var removeTooltip = function()
			{
				$("#tooltip").remove();
                elementInTooltip = null;
			}
			
			/*
			 * Gets the first non-viewport area from the areas-array.
			 * If no other area is found, the viewport area is returned.
			 * The array must be in the same format as the areas array parameter
			 * of the fillareahover and fillareaclick event handlers.
			 */
			var getFirstArea = function( areas )
			{
            	var area = null;
            	for ( var i in areas )
            	{
            		area = areas[i][0].elementDomId === null ?
            				null : areas[i];
            		if ( area === null ) continue; // viewport area, skip to next
            		break;
            	}
            	return area;
			}
			
			$( placeholder ).bind("plothover", (function( httpTrafficId, plotObj )
			{
				return function(event, pos, item) {
	
					// null element dom id means that the series is a viewport movement series
					// and will be ignored.
	                if( (item !== null && item.series.elementDomId !== null) ) {
	                    var itemdata = [ {
							'series' : item.series,
							'dataIndices' : [ item.dataIndex ],
							'seriesIndex' : null
                    	} ];
	                    
	                    var details = session.httptraffics[ httpTrafficId ]
                			.domElements.details[ itemdata[ 0 ].series.elementDomId ];
	                    updateTooltip( itemdata[ 0 ].series.elementDomId, pos.pageX, pos.pageY, details, pos.x, pos.y );
	                }
				}
            })( httpTrafficId, plotObj )).bind( 'plotclick', (function( httpTrafficId, plotObj )
            {
            	return function( event, pos, item )
            	{
            		if ( item !== null && item.series.elementDomId !== null )
            		{
            			showElementDetails( httpTrafficId, item.series, plotObj );
            		}
            	};
            })( httpTrafficId, plotObj ) ).bind( 'fillareahover', (function( plotObj )
            {
            	return function( event, pos, areas )
            	{
	            	// Find the element DOM id
	            	var area = getFirstArea( areas );
	            	var domId = area === null ? null : area[0].elementDomId;
	            	if ( domId === null )
	            	{
	            		// Hovering over a viewport-only area. Skip entirely.
	            		removeTooltip();
	            		// Unhighlight previous highlights
	            		plotObj.unhighlight();
	            		return; 
	            	}
	            	
	            	// Update tooltip with element details
	            	var details = session.httptraffics[ httpTrafficId ].domElements.details[ domId ];
	            	if ( !updateTooltip( domId, pos.pageX, pos.pageY, details, pos.x, pos.y ) )
	            	{
	            		// A new tooltip was created. Unhighlight everything and
	            		// highlight the new item.
	            		plotObj.unhighlight();
	            		
		            	for ( var i in area[0].data )
		            	{
		            		var point = area[0].data[ i ];
		            		if ( point[1] !== null )
		            		{
		            			plotObj.highlight( area[0], area[0].data[ i ] );
		            			
		            			// The second series MUST have an equal amount of data points.
		            			// Therefore its safe to highlight the points this way.
		            			plotObj.highlight( area[1], area[1].data[ i ] );
		            		}
		            	}
	            	}
            	};
            })( plotObj ) ).bind( 'fillareaclick', (function( plotObj ) 
            {
            	return function( event, pos, areas )
	            {
	            	var area = getFirstArea( areas );
	            	if ( area !== null && area[0].elementDomId !== null )
	        		{
	        			showElementDetails( httpTrafficId, area[0], plotObj );
	        		}
	            };
            })( plotObj ) );
	    } );
		

	} );
})($);