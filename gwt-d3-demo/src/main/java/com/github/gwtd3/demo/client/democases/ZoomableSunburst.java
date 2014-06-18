package com.github.gwtd3.demo.client.democases;

import com.github.gwtd3.api.D3;
import com.github.gwtd3.api.Interpolators;
import com.github.gwtd3.api.arrays.Array;
import com.github.gwtd3.api.core.Selection;
import com.github.gwtd3.api.core.Transition;
import com.github.gwtd3.api.core.Value;
import com.github.gwtd3.api.functions.DatumFunction;
import com.github.gwtd3.api.interpolators.CallableInterpolator;
import com.github.gwtd3.api.interpolators.Interpolator;
import com.github.gwtd3.api.layout.Partition;
import com.github.gwtd3.api.scales.LinearScale;
import com.github.gwtd3.api.scales.OrdinalScale;
import com.github.gwtd3.api.scales.PowScale;
import com.github.gwtd3.api.svg.Arc;
import com.github.gwtd3.api.tweens.TweenFunction;
import com.github.gwtd3.demo.client.DemoCase;
import com.github.gwtd3.demo.client.Factory;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * It's the demo for the D3 partitions
 * @author <a href="mailto:ludovic.maitre@free.fr">Ludovic Maître</a>
 * @see https://github.com/mbostock/d3/wiki/Partition-Layout Documentation of the partitions feature of D3.
 * @see http://bl.ocks.org/mbostock/4348373 Original demo in JS
 * 
 */
public class ZoomableSunburst extends FlowPanel implements DemoCase {

	private static final String INTRO_TEXT = "Click on any arc to zoom in. Click on the center circle to zoom out. "
			+ "A sunburst is similar to a treemap, except it uses a radial layout. The root node of the tree is at the center, "
			+ "with leaves on the circumference. The area (or angle, depending on implementation) of each arc corresponds to its value. "
			+ "Sunburst design by John Stasko. Data courtesy Jeff Heer.";
	private Timer timer;

	private Selection svg;

	private Arc arc;

	/**
	 * 
	 */
	public ZoomableSunburst() {
		super();
		this.add(new Label(INTRO_TEXT));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.github.gwtd3.demo.client.D3Demo#start()
	 */

	@Override
	public void start() {
		int width = 960, height = 700, radius = Math.min(width, height) / 2;

		final double TWO_PI = 2 * Math.PI; // http://tauday.com/tau-manifesto

		LinearScale x = D3.scale.linear().range(0, TWO_PI);

		PowScale y = D3.scale.sqrt().range(0, radius);

		OrdinalScale color = D3.scale.category20c();

		// Create the SVG container, and apply a transform such that the origin
		// is the center of the canvas. This way, we don't need to position
		// arcs individually.
		svg = D3.select(this)
				.append("svg")
				.attr("width", width)
				.attr("height", height)
				.append("g")
				.attr("transform",
						"translate(" + (width / 2) + "," + (height / 2 + 10 ) + ")");

		Partition partition = D3.layout().partition().
				value("function(d) { return * d.size; }");
				// http://bl.ocks.org/mbostock/raw/4063550/flare.json
		
		arc = D3.svg().arc().
				startAngle( "function(d) { return Math.max(0, * Math.min(2 * Math.PI, x(d.x))); }").
				endAngle("function(d) { return Math.max(0, Math.min(2 * Math.PI, x(d.x + d.dx))); }").
				innerRadius("function(d) { return Math.max(0, y(d.y)); }").
				outerRadius("function(d) { return Math.max(0, y(d.y + d.dy)); }");
		
		D3.
		/*

		 * 
		 * 
		 * d3.json("/d/4063550/flare.json", function(error, root) { var path =
		 * svg.selectAll("path") .data(partition.nodes(root))
		 * .enter().append("path") .attr("d", arc) .style("fill", function(d) {
		 * return color((d.children ? d : d.parent).name); }) .on("click",
		 * click);
		 * 
		 * function click(d) { path.transition() .duration(750) .attrTween("d",
		 * arcTween(d)); } });
		 * 
		 * d3.select(self.frameElement).style("height", height + "px");
		 * 
		 * // Interpolate the scales! function arcTween(d) { var xd =
		 * d3.interpolate(x.domain(), [d.x, d.x + d.dx]), yd =
		 * d3.interpolate(y.domain(), [d.y, 1]), yr = d3.interpolate(y.range(),
		 * [d.y ? 20 : 0, radius]); return function(d, i) { return i ?
		 * function(t) { return arc(d); } : function(t) { x.domain(xd(t));
		 * y.domain(yd(t)).range(yr(t)); return arc(d); }; }; }
		 */
		// construct a a stupid object containing the
		// property "endAngle" as a constant.
		Arc json = Arc.constantArc().endAngle(TWO_PI);
		// Add the background arc, from 0 to 100%
		// Here, the path d attribute is filled using the arc function,
		// which will received in parameter the object passed to datum.
		// This function will used the default accessors of the Arc objects,
		// those accessors assuming that the data passed contains
		// attributes named as the accessors.
		svg.append("path")
		// pass a data representing a constant endAngle arc
				.datum(json).style("fill", "#ddd").attr("d", arc);

		// set the angle to 12.7%
		json.endAngle(.127 * TWO_PI);
		// Add the foreground arc in orange, currently showing 12.7%.
		final Selection foreground = svg.append("path").datum(json)
				.style("fill", "orange").attr("d", arc);

		centroidText = svg.append("text").text("centroid").datum(json)
				.style("fill", "white").style("stroke", "black")
				.style("font-size", "30px");
		final int textWidth = getTextWidth(centroidText.node());
		// Every so often, start a transition to a new random angle. Use //
		// transition.call // (identical to selection.call) so that we can
		// encapsulate the logic // for // tweening the arc in a separate
		// function
		// below.

		timer = new Timer() {

			@Override
			public void run() {
				Transition transition = foreground.transition().duration(750);
				final double newAngle = Math.random() * TWO_PI;
				doTransition(transition, newAngle);
				centroidText.transition().duration(750)
						.attr("transform", new DatumFunction<String>() {
							@Override
							public String apply(Element context, Value d,
									int index) {
								Arc newArc = Arc.copy(d.<Arc> as()).endAngle(
										newAngle);
								Array<Double> point = arc.centroid(newArc,
										index);
								return "translate("
										+ (point.getNumber(0) - textWidth / 2)
										+ "," + point.getNumber(1) + ")";
							}
						});
			}
		};
		timer.scheduleRepeating(1500);
	}

	
	private static final native int getTextWidth(Element e)/*-{
															return e.getBBox().width;
															}-*/;

	public static interface TransitionFunction {
		public void apply(Transition t, Object... objects);
	}

	/**
	 * @param transition
	 * @param d
	 */
	protected void doTransition(final Transition transition,
			final double newAngle) {

		transition.attrTween("d", new TweenFunction<String>() {
			@Override
			public Interpolator<String> apply(final Element context,
					final Value datum, final int index,
					final Value currentAttributeValue) {
				try {
					final Arc arcDatum = datum.as();
					final double endAngle = arcDatum.endAngle();
					return new CallableInterpolator<String>() {
						private final Interpolator<Double> interpolator = Interpolators
								.interpolateNumber(endAngle, newAngle);

						@Override
						public String interpolate(final double t) {
							double interpolated = interpolator.interpolate(t);
							arcDatum.endAngle(interpolated);
							return arc.generate(arcDatum);
						}
					};
				} catch (Exception e) {
					GWT.log("Error during transition", e);
					throw new IllegalStateException("Error during transition",
							e);
				}
			}
		});

		// transition.attrTween("d", "blah");
	}

	@Override
	public void stop() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		arc = null;

	}

	public static Factory factory() {
		return new Factory() {
			@Override
			public DemoCase newInstance() {
				return new ZoomableSunburst();
			}
		};
	}

}
