/**
 * Mars Simulation Project
 * ToolWindow.java
 * @version 2.70 2000-09-01
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard;  
 
import javax.swing.*;

/** The ToolWindow class is an abstract UI window for a tool.
 *  Particular tool windows should be derived from this.
 */
public abstract class ToolWindow extends JInternalFrame {

    protected String toolName;    // The name of the tool the window is for.
    protected boolean notOpened;  // True if window hasn't yet been opened.

    public ToolWindow(String toolName) {
		
	// use JInternalFrame constructor
	super(toolName,
	      true,     // resizable
	      true,     // closable
	      false,    // maximizable
	      false);   // iconifiable
		
	// Initialize tool name
	this.toolName = new String(toolName);
		
	// Set notOpened to true
	notOpened = true;
    }
	
    /** Returns tool name */
    public String getToolName() {
	return new String(toolName);
    }
	
    /** Returns true if tool window has not previously been opened. */
    public boolean hasNotBeenOpened() {
	return notOpened;
    }
	
    /** Sets notOpened to false. */
    public void setOpened() {
	notOpened = false;
    }
}
