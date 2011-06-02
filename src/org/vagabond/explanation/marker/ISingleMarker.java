package org.vagabond.explanation.marker;

public interface ISingleMarker {

	public String getTid ();
	public String getRel ();
	public boolean isSubsumed (ISingleMarker other);
	public int getSize ();
	
}
