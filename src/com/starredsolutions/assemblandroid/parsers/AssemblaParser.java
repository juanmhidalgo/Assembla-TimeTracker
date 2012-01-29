package com.starredsolutions.assemblandroid.parsers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.util.Xml;

import com.starredsolutions.assemblandroid.models.Space;
import com.starredsolutions.assemblandroid.models.Task;
import com.starredsolutions.assemblandroid.models.Ticket;

public class AssemblaParser{
	

	/**
	 * Parse Space List
	 * @param xmlContent
	 * @return
	 */
	public static List<Space> parseSpaceList(String xmlContent) {
		final Space currentSpace = new Space();
		final List<Space> spaces = new ArrayList<Space>();
		RootElement root = new RootElement("spaces");
		Element space = root.getChild("space");
		
		space.setEndElementListener(new EndElementListener() {
			public void end() {
				spaces.add(currentSpace.clone());
			}
		});
		
		space.getChild("id").setEndTextElementListener(new EndTextElementListener() {
			public void end(String body) {
				currentSpace.setId(body);
			}
		});
		
		space.getChild("description").setEndTextElementListener(new EndTextElementListener() {
			public void end(String body) {
				currentSpace.setDescription(body);
			}
		});
		
		space.getChild("name").setEndTextElementListener(new EndTextElementListener() {
			public void end(String body) {
				currentSpace.setName(body);
			}
		});
		
		try {
			Xml.parse(xmlContent, root.getContentHandler());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return spaces;
	}
	
	/**
	 * 
	 * @param xmlContent
	 * @return
	 */
	public static Space parseSpace(String xmlContent) {
		final Space currentSpace = new Space();
		RootElement root = new RootElement("space");
		
		root.getChild("id").setEndTextElementListener(new EndTextElementListener() {
			public void end(String body) {
				currentSpace.setId(body);
			}
		});
		
		root.getChild("description").setEndTextElementListener(new EndTextElementListener() {
			public void end(String body) {
				currentSpace.setDescription(body);
			}
		});
		
		root.getChild("name").setEndTextElementListener(new EndTextElementListener() {
			public void end(String body) {
				currentSpace.setName(body);
			}
		});
		
		try {
			Xml.parse(xmlContent, root.getContentHandler());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return currentSpace;
	}

	/**
	 * 
	 * @param xmlContent
	 * @return
	 */
	public static List<Ticket> parseTicketList(String xmlContent,final boolean includeOthers,final String myUserId) {
		final Ticket currentTicket = new Ticket();
		final List<Ticket> tickets = new ArrayList<Ticket>();
		RootElement root = new RootElement("tickets");
		Element ticket = root.getChild("ticket");
		
		ticket.setEndElementListener(new EndElementListener() {
			public void end() {
				if (includeOthers || myUserId.equals(currentTicket.getAssignedToId())) {
					tickets.add(currentTicket.clone());
				}
			}
		});
		
		ticket.getChild("space-id").setEndTextElementListener(new EndTextElementListener() {
			public void end(String body) {
				currentTicket.setSpaceId(body);
			}
		});
		
		ticket.getChild("assigned-to-id").setEndTextElementListener(new EndTextElementListener() {
			public void end(String body) {
				currentTicket.setAssignedToId(body);
			}
		});
		
		ticket.getChild("id").setEndTextElementListener(new EndTextElementListener() {
			public void end(String body) {
				currentTicket.setId(Integer.valueOf(body));
			}
		});
		
		ticket.getChild("number").setEndTextElementListener(new EndTextElementListener() {
			public void end(String body) {
				currentTicket.setNumber(Integer.valueOf(body));
			}
		});
		
		ticket.getChild("priority").setEndTextElementListener(new EndTextElementListener() {
			public void end(String body) {
				currentTicket.setPriority(Integer.valueOf(body));
			}
		});
		
		ticket.getChild("status").setEndTextElementListener(new EndTextElementListener() {
			public void end(String body) {
				currentTicket.setStatus(Integer.valueOf(body));
			}
		});
		
		ticket.getChild("status-name").setEndTextElementListener(new EndTextElementListener() {
			public void end(String body) {
				currentTicket.setStatusName(body);
			}
		});
		
		ticket.getChild("description").setEndTextElementListener(new EndTextElementListener() {
			public void end(String body) {
				currentTicket.setDescription(body);
			}
		});
		
		ticket.getChild("summary").setEndTextElementListener(new EndTextElementListener() {
			public void end(String body) {
				currentTicket.setSummary(body);
			}
		});
		
		ticket.getChild("working-hours").setEndTextElementListener(new EndTextElementListener() {
			public void end(String body) {
				currentTicket.setWorkingHours(Float.valueOf(body));
			}
		});
		
		try {
			Xml.parse(xmlContent, root.getContentHandler());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		return tickets;
	}
	
	/**
	 * 
	 * @param xmlContent
	 * @return
	 */
	public static List<Task> parseTaskList(String xmlContent) {
		final Task currentTask = new Task();
		final List<Task> tasks = new ArrayList<Task>();
		RootElement root = new RootElement("tasks");
		Element task = root.getChild("task");
		
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		
		task.setEndElementListener(new EndElementListener() {
			public void end() {
				tasks.add(currentTask.clone());
			}
		});
		
		task.getChild("id").setEndTextElementListener(new EndTextElementListener() {
			public void end(String body) {
				currentTask.setId(Integer.valueOf(body));
			}
		});
		
		task.getChild("hours").setEndTextElementListener(new EndTextElementListener() {
			public void end(String body) {
				currentTask.setHours(Float.valueOf(body));
			}
		});
		
		task.getChild("description").setEndTextElementListener(new EndTextElementListener() {
			public void end(String body) {
				currentTask.setDescription(body);
			}
		});
		
		task.getChild("ticket-id").setEndTextElementListener(new EndTextElementListener() {
			public void end(String body) {
				currentTask.setTicketId(Integer.valueOf(body));
			}
		});
		
		task.getChild("ticket-number").setEndTextElementListener(new EndTextElementListener() {
			public void end(String body) {
				currentTask.setTicketNumber(Integer.valueOf(body));
			}
		});
		
		task.getChild("user-id").setEndTextElementListener(new EndTextElementListener() {
			public void end(String body) {
				currentTask.setUserId(body);
			}
		});
		
		task.getChild("space-id").setEndTextElementListener(new EndTextElementListener() {
			public void end(String body) {
				currentTask.setSpaceId(body);
			}
		});
		
		task.getChild("created-at").setEndTextElementListener(new EndTextElementListener() {
			public void end(String body) {
				try {
					currentTask.setBeginAt(sdf.parse(body));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		task.getChild("end-at").setEndTextElementListener(new EndTextElementListener() {
			public void end(String body) {
				try {
					currentTask.setEndAt(sdf.parse(body));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		
		
		try {
			Xml.parse(xmlContent, root.getContentHandler());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		return tasks;
	}
	
	/**
	 * 
	 * @param xmlContent
	 * @return
	 */
	public static Task parseTask(String xmlContent) {
		final Task currentTask = new Task();
		RootElement root = new RootElement("task");
		
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		
		
		root.getChild("id").setEndTextElementListener(new EndTextElementListener() {
			public void end(String body) {
				currentTask.setId(Integer.valueOf(body));
			}
		});
		
		root.getChild("hours").setEndTextElementListener(new EndTextElementListener() {
			public void end(String body) {
				currentTask.setHours(Float.valueOf(body));
			}
		});
		
		root.getChild("description").setEndTextElementListener(new EndTextElementListener() {
			public void end(String body) {
				currentTask.setDescription(body);
			}
		});
		
		root.getChild("ticket-id").setEndTextElementListener(new EndTextElementListener() {
			public void end(String body) {
				currentTask.setTicketId(Integer.valueOf(body));
			}
		});
		
		root.getChild("ticket-number").setEndTextElementListener(new EndTextElementListener() {
			public void end(String body) {
				currentTask.setTicketNumber(Integer.valueOf(body));
			}
		});
		
		root.getChild("user-id").setEndTextElementListener(new EndTextElementListener() {
			public void end(String body) {
				currentTask.setUserId(body);
			}
		});
		
		root.getChild("space-id").setEndTextElementListener(new EndTextElementListener() {
			public void end(String body) {
				currentTask.setSpaceId(body);
			}
		});
		
		root.getChild("created-at").setEndTextElementListener(new EndTextElementListener() {
			public void end(String body) {
				try {
					currentTask.setBeginAt(sdf.parse(body));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		root.getChild("end-at").setEndTextElementListener(new EndTextElementListener() {
			public void end(String body) {
				try {
					currentTask.setEndAt(sdf.parse(body));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		
		
		try {
			Xml.parse(xmlContent, root.getContentHandler());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		return currentTask;
	}
	

}
