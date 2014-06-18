package com.github.gwtd3.api.layout;


public class Partition extends HierarchicalLayout {

	protected Partition() {
	}

	public final native Partition value( String function )/*-{
		// all right it's not the best
    	eval("var func = " + function + ";");
		return this.value( func );
	}-*/;
	
}
