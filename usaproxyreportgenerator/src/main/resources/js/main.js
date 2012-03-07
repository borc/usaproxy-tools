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
	
    // Function for opening a dialog window with details of a DOM element
	var showElementDetails = function( httpTrafficId, domPath )
	{
		var details = session.httptraffics[ httpTrafficId ].domElements.details[ domPath ];
		
		var detailsTable = {
			'Node name': details.nodeName,
			'DOM path': details.path,
			'Appearances': details.appearances,
			'Disappearances': details.disappearances
		};
		
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
		
		var tableHTML = createHTMLTableFromObject( detailsTable );
		
		$( '<div title="Element details">' + tableHTML + 
			'<h3 class="title">Contents</h3><p>' + 
			unescape(details.content) + '</p></div>' )
		.css({
			'font-size': '80%'
		}).dialog({
			buttons: {
				"OK": function() { $(this).dialog("close"); } 
			},
			width: Math.floor( 0.8 * $( window ).width() )
		});
	};
	
	/**
	 * Filters handling
	 */
	var openFiltersWindow = function()
	{
		var httpTraffic = session.httptraffics[ USAPROXYREPORT.visiblePlot ];
		
		// Initialize filtered elements array if not already inited
		httpTraffic.filteredElements = httpTraffic.filteredElements ? httpTraffic.filteredElements : []; 
		
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
				processedElementTypes[ elementDetails.nodeName ] = true;
				
				var typeListItem = $( '<li class="filter-list-item" />' );
				
				var typeid = 'type-filter-checkbox-' + USAPROXYREPORT.visiblePlot + '-' + i;
				$( '<input type="checkbox" id="' + typeid +	'" />' )
					.click( (function( type )
					{
						return function()
						{
							listElement.find('input').trigger( 'toggle-all', [this.checked, type] )
						};
					})( elementDetails.nodeName ) )
					.appendTo( typeListItem );
				$( '<label for="' + typeid + '">' + elementDetails.nodeName + 
						'</label>' ).appendTo( typeListItem );
				
				typeListItem.appendTo( elementTypeList );
			}
			
			var id = 'filter-checkbox-' + USAPROXYREPORT.visiblePlot + '-' + i;
			
			var listItem = $( '<li class="filter-list-item" />' );
			
			$( '<input type="checkbox" id="' + id +	'" />' )
				.click( (function( i )
				{
					return function() 
					{
						httpTraffic.filteredElements[ i ] = this.checked;
					};
				})( i ) )
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
					if ( httpTraffic.filteredElements[ i ] ) this.checked = true;
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
	
	// Entry point. Run when document fully loaded.
	$( document ).ready( function() {
		
		$( "button" ).button();
	
		// Bind the filters button click
		$( '#filters' ).click( openFiltersWindow );
		
		// Bind the reset button click
		$( '#reset' ).click( function()
		{
			session.httptraffics[ USAPROXYREPORT.visiblePlot ].filteredElements = [];
			$( document ).trigger( 'reinit-plot', USAPROXYREPORT.visiblePlot );
		} );
		
		$( document ).bind( 'reinit-plot', function( event, httpTrafficId )
		{
			// Destroy previous contents
			$( session.httptraffics[httpTrafficId].container ).empty();
			
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
            			showElementDetails( x, item.series.elementDomId );
            		}
            	};
            })( httpTrafficId ) );
	    } );
		

	} );
})($);