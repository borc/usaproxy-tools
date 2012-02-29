/**
 * Main JS application
*/

/**
 * Create a closure for the main app. Takes jQuery as argument.
 */
(function($)
{
	// Entry point. Run when document fully loaded.
	$( document ).ready( function() {
		
		var session = USAPROXYREPORT.session;
		
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
		
		$( document ).bind( 'init-plot', function( event, httpTrafficId, placeholder )
		{
			$.plot(placeholder, session.httptraffics[httpTrafficId].viewportMovement.concat(
					session.httptraffics[httpTrafficId].domElements.sightings), {
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