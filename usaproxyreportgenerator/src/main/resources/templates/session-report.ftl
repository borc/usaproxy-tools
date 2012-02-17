<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <title>Flot test</title>
        <link type="text/css" href="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.17/themes/base/jquery-ui.css" rel="Stylesheet" />
        <link type="text/css" href="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.17/themes/smoothness/jquery-ui.css" rel="Stylesheet" />
        <script src="http://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js" type="text/javascript"></script>
        <script src="js/flot-0.7/jquery.flot.min.js" type="text/javascript"></script>
        <script src="js/flot-0.7/jquery.flot.fillbetween.min.js" type="text/javascript"></script>
        <script src="js/flot-0.7/jquery.mousewheel.min.js" type="text/javascript"></script>
        <script src="js/flot-0.7/jquery.flot.navigate.js" type="text/javascript"></script>
        <script src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.16/jquery-ui.min.js" type="text/javascript"></script>
        <script src="js/utils.js" type="text/javascript"></script>
    </head>
    <body>
    	<h1>UsaProxy session report</h1>
    	<table>
    		<thead>
    			<tr>
    				<td colspan="2">Session details</td>
    			</tr>
    		</thead>
    		<tbody>
    			<tr>
    				<td>Started at</td>
    				<td>${session.timestamp?datetime}</td>
    			</tr>
    			<tr>
    				<td>Session ID</td>
    				<td>${session.id}</td>
    			</tr>
    			<tr>
    				<td>User's IP address</td>
    				<td>${session.ip}</td>
    			</tr>
    		</tbody>
    	</table>
        <script type="text/javascript">
            $(function() {

                var session = ${session.data};

                function showTooltip(x, y, contents) {
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
                }

                for ( var x in session.httptraffics )
                {

                    var toplevel = $('<div class="ui-widget" style="font-size: 80%;" />').appendTo("body");

                    var timestamp = new Date(session.httptraffics[x].timestamp);

                    var nvbar = $('<div class="ui-widget-header">' +
                        '<span style="display:inline; float:left;" class="ui-icon ui-icon-circlesmall-plus"></span><span>' +
                        new String( timestamp.getDate() ).padLeft( '0', 2 ) + '.' + 
                        new String( (timestamp.getMonth()+1) ).padLeft( '0', 2 ) + '.' + 
                        timestamp.getFullYear() + " " +
                        new String( timestamp.getHours() ).padLeft( '0', 2 ) + ':' + 
                        new String( timestamp.getMinutes() ).padLeft( '0', 2 ) + ':' + 
                        new String( timestamp.getSeconds() ).padLeft( '0', 2 ) + ' -> ' +
                        session.httptraffics[x].url + '</span></div>').appendTo(toplevel).click( function(e) {
                            $(this).next().toggle();
                            $(this).children( ':first-child' )
                                .toggleClass( 'ui-icon-circlesmall-plus' )
                                .toggleClass( 'ui-icon-circlesmall-minus' );
                        }).css({
                            cursor: 'pointer'
                        });

                    var placeholder = $('<div />').css({
                        display : 'none',
                        padding : '5px',
                        margin: '25px',
                        width: '80%',
                        height: '600px'
                    }).appendTo(toplevel);

					(function( x, placeholder ) {
						nvbar.one( 'click', function()
						{
			                $.plot(placeholder, session.httptraffics[x].viewportMovement.concat(session.httptraffics[x].domElements.sightings), {
			                    xaxis : {
			                        mode : 'time'
			                    },
			                    yaxis : {
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
			                    legend : {
			                        position : 'se'
			                    },
			                    zoom: {
			                        interactive: true
			                    },
			                    pan: {
			                        interactive: true
			                    }
			                });
			            } );
			        })( x, placeholder );
                
                placeholder.bind("plothover", function(event, pos, item) {
                    $("#x").text(pos.x.toFixed(2));
                    $("#y").text(pos.y.toFixed(2));

                    if(item) {
                        $("#tooltip").remove();
                        var x = item.datapoint[0].toFixed(2), y = item.datapoint[1].toFixed(2);

                        showTooltip(item.pageX, item.pageY, item.series.invisibleLabel + ": " + new Date(parseInt(x)) + ", " + y + " %");
                    } else {
                        $("#tooltip").remove();
                    }
                });
                
                }

                
            });

        </script>
    </body>
</html>