/**
 * 
 */
package com.starredsolutions.assemblandroid.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * @author Juan M. Hidalgo <juan@starredsolutions.com.ar>
 *
 */
public class AssemblaContract {
	
	interface SpacesColumns{
		String SPACE_ID = "space_id";
		String NAME = "name";
		String DESCRIPTION = "description";
		String CREATED_AT = "created_at";
	}
	
	interface TicketsColumns{
		String TICKET_ID = "ticket_id";
		String NUMBER = "number";
		String PRIORITY = "priority";
		String STATUS = "status";
		String ASSIGNED_TO_ID = "assigned_to_id";
		String SPACE_ID = "space_id";
		String SUMMARY = "summary";
		String DESCRIPTION = "description";
		String WORKING_HOURS = "working_hours";
	}
	
	public static final String CONTENT_AUTHORITY = "com.starredsolutions.assembla";
	private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
	
	public static final String PATH_SPACES = "spaces";
	public static final String PATH_TICKETS = "tickets";
	
	
	public static class Spaces implements SpacesColumns,BaseColumns{
		public static final Uri CONTENT_URI =BASE_CONTENT_URI.buildUpon().appendPath(PATH_SPACES).build();
		public static final String[] PROJECTION = new String[]{_ID,NAME,SPACE_ID,DESCRIPTION,CREATED_AT};
		public static final String CONTENT_TYPE ="vnd.android.cursor.dir/vnd.assembla.space";
        public static final String CONTENT_ITEM_TYPE ="vnd.android.cursor.item/vnd.assembla.space";
        
        /** Build {@link Uri} for requested {@link #ARTICLE_ID}. */
        public static Uri buildSpaceUri(String spaceId){
        	return CONTENT_URI.buildUpon().appendPath(spaceId).build();
        }
	}
	
	public static class Tickets implements TicketsColumns,BaseColumns{
		public static final Uri CONTENT_URI =BASE_CONTENT_URI.buildUpon().appendPath(PATH_TICKETS).build();
		public static final String[] PROJECTION = new String[]{_ID,TICKET_ID,NUMBER,SPACE_ID,PRIORITY,STATUS,ASSIGNED_TO_ID,SUMMARY,DESCRIPTION,WORKING_HOURS};
		public static final String CONTENT_TYPE ="vnd.android.cursor.dir/vnd.assembla.ticket";
		public static final String CONTENT_ITEM_TYPE ="vnd.android.cursor.item/vnd.assembla.ticket";
		
		/** Build {@link Uri} for requested {@link #ARTICLE_ID}. */
		public static Uri buildTicketUri(String ticketId){
			return CONTENT_URI.buildUpon().appendPath(ticketId).build();
		}
		
		public static Uri buildTicketBySpaceUri(long spaceId){
			return CONTENT_URI.buildUpon().appendPath("space").appendPath(String.valueOf(spaceId)).build();
		}
	}
}
