package com.starredsolutions.assemblandroid.asyncTask;

import java.util.ArrayList;

public class ParsedArrayList<E> extends ArrayList<E>
{
	private static final long serialVersionUID = 7578010024942855484L;
    
    protected int _skippedItems = 0;
	
	public void setSkippedItems(int skippedItems) { _skippedItems = skippedItems; }
	public int getSkippedItems() { return _skippedItems; }
	
	public ParsedArrayList()
	{
		super();
	}
}