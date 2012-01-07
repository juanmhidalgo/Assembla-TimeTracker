package com.starredsolutions.assemblandroid.models;

import java.util.Comparator;

public class TicketComparator implements Comparator<Ticket> {

	public int compare(Ticket a, Ticket b) {
		// 1) First, sort by priority
		if (a.priority() < b.priority())
			return -1;
		else if (a.priority() > b.priority())
			return 1;
		else {
		    // 2. Then, sort by name
		    return (a.name().compareToIgnoreCase(b.name()));
		    
//			// 3. Then, sort by ticket number
//			if (a.number() < b.number())
//				return -1;
//			else if (a.number() > b.number())
//				return 1;
//			else
//				return 0;
		}
	}

}
