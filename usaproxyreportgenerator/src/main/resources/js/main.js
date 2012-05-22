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
			contents = $( '<p />' )
				.append( $( '<a href="javascript:;">Click here to attempt to load the image</a>' )
						.click( function()
						{
							var image = '<img src="' + unescape(details.img) + '" />';
							contents
								.empty()
								.append( $(image) );
							details.img = false;
							details.content = image;
						} ) );
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
	
	/**
	 * A function to check whether the mouse cursor is hovering over a DOM 
	 * element. The plothover event only knows whether the cursor is hovering
	 * near an edge point. Returns a tuple [element,dataIndex] for the series
	 * and datapoint near which the cursor is hovering or null if none.
	 */
	var isHoveringOverAnElement = function( filteredSightings, xPos, yPos )
	{
		/**
		 * A function for checking whether two segments intersect.
		 * A,B are the lines, 1,2 are the end points.
		 * Algorithm stolen from: 
		 * University of Illinois at Urbana-Champaign • College of Engineering
		 * lecture material at:
		 * http://compgeom.cs.uiuc.edu/~jeffe/teaching/373/notes/x06-sweepline.pdf
		 */
		var intersect = function( A1X, A1Y, A2X, A2Y, B1X, B1Y, B2X, B2Y )
		{
			/**
			 * Check whether the point triplet (A,B,C) is in 
			 * counter-clockwise order. That is, the angle between (A->B) and
			 * (A->C) is positive when A is considered to be the origin.
			 * Stolen from wikipedia (http://en.wikipedia.org/wiki/Graham_scan).
			 * "ccw is a determinant that gives the signed area of the triangle
			 * formed by A, B and C"
			 */
			var ccw = function( AX, AY, BX, BY, CX, CY )
			{
				return ((BX - AX)*(CY - AY) - (BY - AY)*(CX - AX)) > 0;
			}

			/*
			 * "A1 and A2 are on opposite sides of line B1->B2 if and only if exactly one 
			 * of the two triples (A1,B1,B2) and (A2,B1,B2) is in counterclockwise order"
			 */
			return (ccw( A1X, A1Y, B1X, B1Y, B2X, B2Y ) != ccw( A2X, A2Y, B1X, B1Y, B2X, B2Y )) &&
				(ccw( A1X, A1Y, A2X, A2Y, B1X, B1Y ) != ccw( A1X, A1Y, A2X, A2Y, B2X, B2Y ));
		}
		
		// store the found element in this
		var found = null;
		
		// Assume that top and bottom data points are in order.
		// i.e. every other item is a top data set and every other a 
		// bottom data set.
		// Will fail miserably if that is not the case.
		for ( var i = 0; i < filteredSightings.length; i += 2 )
		{
			var top = filteredSightings[ i ];
			var bottom = filteredSightings[ i + 1 ];
			var topDataset = top.data;
			var bottomDataset = bottom.data;
			
			// Create quadrilateral areas using four data points, and
			// check the coordinates against each one.
			// Null data points will be skipped over.
			for ( var j = 0; j < topDataset.length; j += 2 )
			{
				// Idx 0 = X axis, idx 1 = Y axis
				var topLeftPoint = topDataset[ j ];
				if ( topLeftPoint[ 1 ] == null )
				{
					// Null data point = break in element visibility.
					// Subtracting one will result in the loop starting over
					// from the next element.
					--j;
					continue;
				}
				
				// Establish area boundaries
				var points = {
						'top' : {
							'left' : topLeftPoint,
							'right' : topDataset[ j + 1 ]
						},
						'bottom' : {
							'left' : bottomDataset[ j ],
							'right' : bottomDataset[ j + 1 ]
						}
				};
				
				var absoluteEastBoundary = points.top.right[0] > points.bottom.right[0] ? 
						points.top.right[0] : points.bottom.right[0];
				
				// Use ray casting algorithm to find whether the point resides
				// inside the area.
				// In this implementation, we cast the ray from the point 
				// straight to the right and stop at the absolute east boundary.
				// Edges are checked counter-clockwise starting from the east edge.
				
				var intersections = 0;
						
				// East edge
				if ( intersect( points.top.right[0], points.top.right[1], 
						points.bottom.right[0], points.bottom.right[1],
						xPos, yPos, absoluteEastBoundary, yPos ) ) intersections++;
				
				// North edge
				if ( intersect( points.top.left[0], points.top.left[1],
						points.top.right[0], points.top.right[1], 
						xPos, yPos, absoluteEastBoundary, yPos ) ) intersections++;
				
				// West edge
				if ( intersect( points.bottom.left[0], points.bottom.left[1],
						points.top.left[0], points.top.left[1],
						xPos, yPos, absoluteEastBoundary, yPos ) ) intersections++;
				
				// South edge
				if ( intersect( points.bottom.left[0], points.bottom.left[1],
						points.bottom.right[0], points.bottom.right[1],
						xPos, yPos, absoluteEastBoundary, yPos ) ) intersections++;
				
				if ( intersections === 1 )
				{
					found = [ top, j ];
					break;
				}
			}
			
			if ( found !== null ) break;
		}
		
		return found;
	}
	
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
					session.httptraffics[httpTrafficId].viewportMovement.concat(sightings), 
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
			
			$( placeholder ).bind("plothover", (function( httpTrafficId, plotObj )
			{
				return function(event, pos, item) {
	
					var hoveringOver = isHoveringOverAnElement( sightings, pos.x, pos.y );
					
	                if( hoveringOver !== null || item !== null ) {
	                    $("#tooltip").remove();
	                    
	                    var series, dataIdx;
	                    if ( item )
	                    {
	                    	series = item.series;
	                    	dataIdx = item.dataIndex;
	                    }
	                    else
	                    {
	                    	series = hoveringOver[ 0 ];
		                    dataIdx = hoveringOver[ 1 ];
	                    }
	                    
	                    var details = session.httptraffics[ httpTrafficId ].domElements.details[ series.elementDomId ];
	                    
	                    showTooltip(pos.pageX, pos.pageY, 
	                    	new Date(parseInt(pos.x.toFixed(2))).toUTCString() + ", " + pos.y.toFixed(2) + " % <br />" +
	                    	series.elementDomId + ", " + 
	                    	(details.nodeName ? details.nodeName : '&lt;unknown&gt;') + ': <br />' + 
	                    	(details.nodeName !== 'IMG' ? unescape( details.content ).substr(0,30) : details.content ) );
	                } else {
	                    $("#tooltip").remove();
	                }
				}
            })( httpTrafficId, plotObj )).bind( 'plotclick', (function( httpTrafficId, plotObj )
            {
            	return function( event, pos, item )
            	{
            		var hoveringOver = isHoveringOverAnElement( sightings, pos.x, pos.y );
            		if ( hoveringOver !== null || item !== null )
            		{
            			showElementDetails( httpTrafficId, item ? item.series : hoveringOver[ 0 ], plotObj );
            		}
            	};
            })( httpTrafficId, plotObj ) );
	    } );
		

	} );
})($);