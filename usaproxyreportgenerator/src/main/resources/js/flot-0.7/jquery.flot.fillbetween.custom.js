/*
Flot plugin for computing bottoms for filled line and bar charts.

The case: you've got two series that you want to fill the area
between. In Flot terms, you need to use one as the fill bottom of the
other. You can specify the bottom of each data point as the third
coordinate manually, or you can use this plugin to compute it for you.

In order to name the other series, you need to give it an id, like this

  var dataset = [
       { data: [ ... ], id: "foo" } ,         // use default bottom
       { data: [ ... ], fillBetween: "foo" }, // use first dataset as bottom
       ];

  $.plot($("#placeholder"), dataset, { lines: { show: true, fill: true }});

As a convenience, if the id given is a number that doesn't appear as
an id in the series, it is interpreted as the index in the array
instead (so fillBetween: 0 can also mean the first series).
  
Internally, the plugin modifies the datapoints in each series. For
line series, extra data points might be inserted through
interpolation. Note that at points where the bottom line is not
defined (due to a null point or start/end of line), the current line
will show a gap too. The algorithm comes from the jquery.flot.stack.js
plugin, possibly some code could be shared.
*/

(function ($) {
    var options = {
        series: { fillBetween: null } // or number
    };
    
    function init(plot) {
	
		/**
		 * Creates a read-only point object with only getter methods.
		 */
		var immutablePoint = function( x, y )
		{
			var point = {
				x: x,
				y: y
			};
			
			return {
				getX: function() { return point.x; },
				getY: function() { return point.y; }
			};
		};
	
		function createAreaPolygonFromDatapoints( datapoints, s1, s2 )
		{
			/**
			 * Creates a read-only polygon object with only getter methods.
			 */
			var immutableAreaPolygon = function( outlinePoints, series )
			{
				var areaPolygon = {
					outline: outlinePoints,
					series: series
				};
				
				return {
					getOutline: function() {
						return areaPolygon.outline;
					},
					getSeries: function() {
						return areaPolygon.series;
					}
				};
			};
			
			var outline = []; // Top data points in normal order
			var bottomOutline = []; // Gather bottom data points for later reversing
			var prevNull = false; // Previous datapoint was null?
			
			for ( var i = 0; i < datapoints.points.length; i += datapoints.pointsize )
			{
				var x = datapoints.points[ i ];
				var topy = datapoints.points[ i + 1 ];
				var bottomy = datapoints.points[ i + datapoints.pointsize - 1 ];
				
				if ( x === null )
				{
					outline.push( immutablePoint( 
						bottomOutline[ bottomOutline.length - 1 ].getX(), 
						bottomOutline[ bottomOutline.length - 1 ].getY() ) );
					prevNull = true;
				}
				else
				{
					if ( prevNull )
					{
						// Previous datapoint was null. Produce an additional
						// segment for the top outline in order to have the polygon
						// run at zero width through the gap area.
						outline.push( immutablePoint( x, bottomy ) );
					}
				
					outline.push( immutablePoint( x, topy ) );
					bottomOutline.push( immutablePoint( x, bottomy ) );
					prevNull = false;
				}
						
			}

			bottomOutline.reverse();
			return immutableAreaPolygon( outline.concat( bottomOutline ), [ s1, s2 ] );
		}
	
		// All the areas represented as polygons
		var areaPolygons = [];
	
        function findBottomSeries(s, allseries) {
            var i;
            for (i = 0; i < allseries.length; ++i) {
                if (allseries[i].id == s.fillBetween)
                    return allseries[i];
            }

            if (typeof s.fillBetween == "number") {
                i = s.fillBetween;
            
                if (i < 0 || i >= allseries.length)
                    return null;

                return allseries[i];
            }
            
            return null;
        }
        
        function computeFillBottoms(plot, s, datapoints) {
            if (s.fillBetween == null)
                return;

            var other = findBottomSeries(s, plot.getData());
            if (!other)
                return;

            var ps = datapoints.pointsize,
                points = datapoints.points,
                otherps = other.datapoints.pointsize,
                otherpoints = other.datapoints.points,
                newpoints = [],
                px, py, intery, qx, qy, bottom,
                withlines = s.lines.show,
                withbottom = ps > 2 && datapoints.format[2].y,
                withsteps = withlines && s.lines.steps,
                fromgap = true,
                i = 0, j = 0, l;

            while (true) {
                if (i >= points.length)
                    break;

                l = newpoints.length;

                if (points[i] == null) {
                    // copy gaps
                    for (m = 0; m < ps; ++m)
                        newpoints.push(points[i + m]);
                    i += ps;
                }
                else if (j >= otherpoints.length) {
                    // for lines, we can't use the rest of the points
                    if (!withlines) {
                        for (m = 0; m < ps; ++m)
                            newpoints.push(points[i + m]);
                    }
                    i += ps;
                }
                else if (otherpoints[j] == null) {
                    // oops, got a gap
                    for (m = 0; m < ps; ++m)
                        newpoints.push(null);
                    fromgap = true;
                    j += otherps;
                }
                else {
                    // cases where we actually got two points
                    px = points[i];
                    py = points[i + 1];
                    qx = otherpoints[j];
                    qy = otherpoints[j + 1];
                    bottom = 0;

                    if (px == qx) {
                        for (m = 0; m < ps; ++m)
                            newpoints.push(points[i + m]);

                        //newpoints[l + 1] += qy;
                        bottom = qy;
                        
                        i += ps;
                        j += otherps;
                    }
                    else if (px > qx) {
                        // we got past point below, might need to
                        // insert interpolated extra point
                        if (withlines && i > 0 && points[i - ps] != null) {
                            intery = py + (points[i - ps + 1] - py) * (qx - px) / (points[i - ps] - px);
                            newpoints.push(qx);
                            newpoints.push(intery)
                            for (m = 2; m < ps; ++m)
                                newpoints.push(points[i + m]);
                            bottom = qy; 
                        }

                        j += otherps;
                    }
                    else { // px < qx
                        if (fromgap && withlines) {
                            // if we come from a gap, we just skip this point
                            i += ps;
                            continue;
                        }
                            
                        for (m = 0; m < ps; ++m)
                            newpoints.push(points[i + m]);
                        
                        // we might be able to interpolate a point below,
                        // this can give us a better y
                        if (withlines && j > 0 && otherpoints[j - otherps] != null)
                            bottom = qy + (otherpoints[j - otherps + 1] - qy) * (px - qx) / (otherpoints[j - otherps] - qx);

                        //newpoints[l + 1] += bottom;
                        
                        i += ps;
                    }

                    fromgap = false;
                    
                    if (l != newpoints.length && withbottom)
                        newpoints[l + 2] = bottom;
                }

                // maintain the line steps invariant
                if (withsteps && l != newpoints.length && l > 0
                    && newpoints[l] != null
                    && newpoints[l] != newpoints[l - ps]
                    && newpoints[l + 1] != newpoints[l - ps + 1]) {
                    for (m = 0; m < ps; ++m)
                        newpoints[l + ps + m] = newpoints[l + m];
                    newpoints[l + 1] = newpoints[l - ps + 1];
                }
            }

            datapoints.points = newpoints;
			
			// Create area polygons here. Depend on hoverable/clickable option.
			if ( plot.getOptions().grid.hoverableFill || plot.getOptions().grid.clickableFill )
			{
				areaPolygons.push( createAreaPolygonFromDatapoints( datapoints, s, other ) );
			}
        }
        
		/**
		 * Returns the filled areas over which the mouse is currently
		 * hovering. Empty array will be returned if the mouse isn't hovering
		 * over any filled area.
		 * Parameters:
		 *  plot - the plot object
		 *  mouseX - mouse location on X-axis (plot coordinates)
		 *  mouseY - mouse location on Y-axis (plot coordinates)
		 * Return format:
		 * [ 
		 *   [ seriesA, seriesB ], 
		 *   [ ..., ... ], 
		 *   ... 
		 * ]
		 */
		function isHoveringOverFilledArea( mouseX, mouseY )
		{
			var immutableSegment = function( pointA, pointB )
			{
				var segment = {
					a: pointA,
					b: pointB
				};
				
				return {
					getA: function() { return segment.a; },
					getB: function() { return segment.b; }
				};
			};
		
			// Check whether two segments intersect
			var intersect = function( segmentA, segmentB )
			{
				var ccw = function( pointA, pointB, pointC )
				{
					return ( (pointB.getX() - pointA.getX())*(pointC.getY() - pointA.getY()) - 
						(pointB.getY() - pointA.getY())*(pointC.getX() - pointA.getX())) > 0;
				}
				var pointAA = segmentA.getA();
				var pointAB = segmentA.getB();
				var pointBA = segmentB.getA();
				var pointBB = segmentB.getB();
				return (ccw( pointAA, pointBA, pointBB ) != ccw( pointAB, pointBA, pointBB )) &&
					(ccw( pointAA, pointAB, pointBA ) != ccw( pointAA, pointAB, pointBB ));
			}
		
			var hoverPoint = immutablePoint( mouseX, mouseY );
			var hoverAreas = [];
		
			for ( var i in areaPolygons )
			{
				var polygon = areaPolygons[ i ];
				var points = polygon.getOutline();
				var series = polygon.getSeries();
				
				var farEastEdge = series[ 0 ].xaxis.datamax > series[ 1 ].xaxis.datamax ?
					series[ 0 ].xaxis.datamax + 1 : series[ 1 ].xaxis.datamax + 1;
				var rayCastSegment = immutableSegment( hoverPoint, immutablePoint( farEastEdge, mouseY ) );
				
				var intersections = 0;
				for ( var j = 0; j < points.length; ++j )
				{
					var firstPoint = points[ j ];
					var secondPoint = j < (points.length - 1) ? secondPoint = points[ j + 1 ] : secondPoint = points[ 0 ];
					if ( intersect( rayCastSegment, immutableSegment( firstPoint, secondPoint ) ) ) intersections++;
				}
				
				if ( intersections % 2 == 1 )
				{
					hoverAreas.push( series );
				}
			}
			
			return hoverAreas;
		}
		
		/**
		 * Set up area hover and area click event triggering.
		 */
		var checkForFillAreaHover = function( plot, eventHolder )
		{
			// Require the 'hoverableFill' option
			if ( plot.getOptions().grid.hoverableFill )
			{
				// Bind to the existing plothover event. This way the 'hoverable'
				// attribute is taken into account (and we get pre-calculated 
				// mouse cursor positions easily).
				$( plot.getPlaceholder() ).bind( 'plothover', function( event, pos, item )
				{
					var areas = isHoveringOverFilledArea( pos.x, pos.y );
					if ( areas.length > 0 )
					{
						$( plot.getPlaceholder() ).trigger( 'fillareahover', [ pos, areas ] );
					}
				} );
			}
			
			// Require the 'clickableFill' option
			if ( plot.getOptions().grid.clickableFill )
			{
				// Bind to the plotclick event. This way the 'clickable' attribute
				// is taken into account.
				$( plot.getPlaceholder() ).bind( 'plotclick', function( event, pos, item )
				{
					var areas = isHoveringOverFilledArea( pos.x, pos.y );
					if ( areas.length > 0 )
					{
						$( plot.getPlaceholder() ).trigger( 'fillareaclick', [ pos, areas ] );
					}
				} );
			}
		}
		
        plot.hooks.processDatapoints.push(computeFillBottoms);
		plot.hooks.bindEvents.push( checkForFillAreaHover );
    }
    
    $.plot.plugins.push({
        init: init,
        options: options,
        name: 'fillbetween',
        version: '1.0'
    });
})(jQuery);
