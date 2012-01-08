package com.starredsolutions.assemblandroid;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import android.content.Context;
import android.content.ContextWrapper;


/**
 * Snapshot Manager Class
 * 
 * Snapshot Pattern:
 * 
 * - Target (the model you are serializing) :
 *       An Serializer object converts the state of instances of classes in this role to a byte 
 *       stream. An Deserializer object restores the state of instances of classes in this role 
 *       from a byte stream. The role of the Target object in these activities is purely passive. 
 *       The Serializer object or Deserializer object does all of the work.
 *  
 * - Serializer (ObjectOutputStream) :
 *       responsible for serializing the Target object. It copies the state information of the 
 *       Target object and all other objects it refers to that are part of the Target's state as a 
 *       byte stream to a file.
 * 
 * - OutputStream (FileOutputStream) :
 *       writes a stream of bytes to a file.
 * 
 * - Deserializer (ObjectInputStream) :
 *       responsible for reading a serialized byte stream and creating a copy of the Target object 
 *       and other objects that were serialized to create the byte stream.
 * 
 * - InputStream (FileInputStream) : 
 *       reads a stream of bytes from a file.
 * 
 *  - Serializable :
 *       Classes other than the Originator access the Memento Objects only through the 
 *       Serializable interface (Memento is a private inner-class of Originator).
 *       Related patterns :
 *          - Read-Only Interface,  
 *          - Marker Interface
 * 
 * @author david
 * @author Mike GRAND, from the book "Patterns in Java", WILEY
 */
public class SnapshotManager
{
    /*********************************************************************************************
     * VARIABLES, SIMPLE GETTERS & SETTERS
     *********************************************************************************************/
    
    /**
     * The object providing +openFileOutput+ and +openFileInput+ in Android
     */
    private ContextWrapper _context;
	
    
    
    /*********************************************************************************************
     * PUBLIC METHODS
     *********************************************************************************************/
    
    public SnapshotManager(ContextWrapper context) {
        _context = context;
    }
    
    
	/**
	 * @param target to serialize to the filesystem
	 * @param outputFile
	 * @throws FileNotFoundException
     * @throws IOException 
	 */
	public void create(TimeTrackerModel target, String outputFile)
				throws FileNotFoundException, IOException
	{
		FileOutputStream   fos = _context.openFileOutput(outputFile, Context.MODE_PRIVATE);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		
		oos.writeObject( target.createMemento() );
	}
	
	/**
	 * @param inputFile
	 * @return target restored from the file system
	 * @throws ClassNotFoundException 
	 * @throws FileNotFoundException
	 * @throws IOException 
	 */
	public TimeTrackerModel restore(String inputFile) 
	            throws FileNotFoundException, IOException, ClassNotFoundException
	{
	    FileInputStream   fis = _context.openFileInput(inputFile);
		ObjectInputStream ois = new ObjectInputStream(fis);
		
		TimeTrackerModel target = new TimeTrackerModel( (Serializable) ois.readObject() );
		
		return target;
		
		//return new TimeTrackerModel();  // temp
	}
}
