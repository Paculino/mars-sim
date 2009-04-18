/**
 * Mars Simulation Project
 * UIConfig.java
 * @version 2.85 2008-10-13
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard;

import java.awt.Dimension;
import java.awt.Point;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JInternalFrame;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;

import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.mars_sim.msp.simulation.Unit;
import org.mars_sim.msp.ui.standard.sound.AudioPlayer;
import org.mars_sim.msp.ui.standard.tool.ToolWindow;
import org.mars_sim.msp.ui.standard.unit_window.UnitWindow;





// import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

public class UIConfig {

    private static String CLASS_NAME = "org.mars_sim.msp.ui.standard.UIConfig";

    private static Logger logger = Logger.getLogger(CLASS_NAME);

    // Singleton instance.
    public static final UIConfig INSTANCE = new UIConfig();

    // Internal window types.
    public static final String TOOL = "tool";

    public static final String UNIT = "unit";

    // Config filename.
    private static final String DIRECTORY = "saved";

    private static final String FILE_NAME = "ui_settings.xml";
    
    private static final String FILE_NAME_DTD = "ui_settings.dtd";
    
 
    // UI config elements and attributes.
    private static final String UI = "ui";

    private static final String USE_DEFAULT = "use-default";

    private static final String LOOK_AND_FEEL = "look-and-feel";

    private static final String MAIN_WINDOW = "main-window";

    private static final String LOCATION_X = "location-x";

    private static final String LOCATION_Y = "location-y";

    private static final String WIDTH = "width";

    private static final String HEIGHT = "height";

    private static final String VOLUME = "volume";

    private static final String SOUND = "sound";

    private static final String MUTE = "mute";

    private static final String INTERNAL_WINDOWS = "internal-windows";

    private static final String WINDOW = "window";

    private static final String TYPE = "type";

    private static final String NAME = "name";

    private static final String DISPLAY = "display";

    private static final String Z_ORDER = "z-order";

    private Document configDoc;

    // Private singleton constructor.
    private UIConfig() {
    }

    /**
     * Loads and parses the XML save file.
     */
    public void parseFile() {
        File stream = null;
        try {
            String path = DIRECTORY + File.separator + FILE_NAME;
            stream = new File(path);
            
            SAXBuilder saxBuilder = new SAXBuilder(true);
            
            configDoc = saxBuilder.build(stream);
            
        } 
        catch (Exception e) {
            logger.log(Level.SEVERE, "parseFile()", e);
        } 
        
    }

    /**
     * Creates an XML document for the UI configuration and saves it to a file.
     * 
     * @param window the main window.
     */
    public void saveFile(MainWindow window) {
        OutputStream stream = null;
        try {
            Document outputDoc = new Document();
            DocType dtd = new DocType(UI,FILE_NAME_DTD);
            Element uiElement = new Element(UI);
            outputDoc.setDocType(dtd);
            outputDoc.addContent(uiElement);
            outputDoc.setRootElement(uiElement);

            uiElement.setAttribute(USE_DEFAULT, "false");

            if (UIManager.getLookAndFeel() instanceof MetalLookAndFeel)
                uiElement.setAttribute(LOOK_AND_FEEL, "default");
            else
                uiElement.setAttribute(LOOK_AND_FEEL, "native");

            Element mainWindowElement = new Element(MAIN_WINDOW);
            uiElement.addContent(mainWindowElement);

            mainWindowElement.setAttribute(LOCATION_X, Integer.toString(window.getX()));
            mainWindowElement.setAttribute(LOCATION_Y, Integer.toString(window.getY()));
            mainWindowElement.setAttribute(WIDTH, Integer.toString(window.getWidth()));
            mainWindowElement.setAttribute(HEIGHT, Integer.toString(window.getHeight()));

            Element volumeElement = new Element(VOLUME);
            uiElement.addContent(volumeElement);

            AudioPlayer player = window.getDesktop().getSoundPlayer();
            volumeElement.setAttribute(SOUND, Float.toString(player.getVolume()));
            volumeElement.setAttribute(MUTE, Boolean.toString(player.isMute()));

            Element internalWindowsElement = new Element(INTERNAL_WINDOWS);
            uiElement.addContent(internalWindowsElement);

            // Add all internal windows.
            MainDesktopPane desktop = window.getDesktop();
            JInternalFrame[] windows = desktop.getAllFrames();
            for (int x = 0; x < windows.length; x++) {
                Element windowElement = new Element(WINDOW);
                internalWindowsElement.addContent(windowElement);

                windowElement.setAttribute(Z_ORDER, Integer.toString(desktop.getComponentZOrder(windows[x])));
                windowElement.setAttribute(LOCATION_X, Integer.toString(windows[x].getX()));
                windowElement.setAttribute(LOCATION_Y, Integer.toString(windows[x].getY()));
                windowElement.setAttribute(WIDTH, Integer.toString(windows[x].getWidth()));
                windowElement.setAttribute(HEIGHT, Integer.toString(windows[x].getHeight()));
                windowElement.setAttribute(DISPLAY, Boolean.toString(!windows[x].isClosed()));

                if (windows[x] instanceof ToolWindow) {
                    windowElement.setAttribute(TYPE, TOOL);
                    windowElement.setAttribute(NAME, ((ToolWindow) windows[x]).getToolName());
                } 
                else if (windows[x] instanceof UnitWindow) {
                    windowElement.setAttribute(TYPE, UNIT);
                    windowElement.setAttribute(NAME, ((UnitWindow) windows[x]).getUnit().getName());
                }
            }

            // Check unit toolbar for unit buttons without open windows.
            Unit[] toolBarUnits = window.getUnitToolBar().getUnitsInToolBar();
            for (int x = 0; x < toolBarUnits.length; x++) {
                UnitWindow unitWindow = desktop.findUnitWindow(toolBarUnits[x]);

                if ((unitWindow == null) || unitWindow.isIcon()) {
                    Element windowElement = new Element(WINDOW);
                    internalWindowsElement.addContent(windowElement);

                    windowElement.setAttribute(TYPE, UNIT);
                    windowElement.setAttribute(NAME, toolBarUnits[x].getName());
                    windowElement.setAttribute(DISPLAY, "false");
                }
            }

            // Save to file.
            String path = DIRECTORY + File.separator + FILE_NAME;
            XMLOutputter fmt=new XMLOutputter();
            fmt.setFormat(Format.getPrettyFormat());
            stream = new BufferedOutputStream(new FileOutputStream(path));
            fmt.output(outputDoc, stream);
        } 
        catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage());
        } 
        finally {
            if (stream != null) {
                try {
                    stream.close();
                } 
                catch (Exception e) {};
            }
        }
    }

    /**
     * Checks if UI should use default configuration.
     * 
     * @return true if default.
     */
    public boolean useUIDefault() {
        try {
            Element root = configDoc.getRootElement();
            return Boolean.parseBoolean(root.getAttributeValue(USE_DEFAULT));
        } 
        catch (Exception e) {
            return true;
        }
    }

    /**
     * Checks if UI should use native or default look & feel.
     * 
     * @return true if native.
     */
    public boolean useNativeLookAndFeel() {
        try {
            Element root = configDoc.getRootElement();
            String lookAndFeel = root.getAttributeValue(LOOK_AND_FEEL);
            return (lookAndFeel.equals("native"));
        } 
        catch (Exception e) {
            return false;
        }
    }

    /**
     * Gets the screen location of the main window origin.
     * 
     * @return location.
     */
    public Point getMainWindowLocation() {
        try {
            Element root = configDoc.getRootElement();
            Element mainWindow = root.getChild(MAIN_WINDOW);
            int x = Integer.parseInt(mainWindow.getAttributeValue(LOCATION_X));
            int y = Integer.parseInt(mainWindow.getAttributeValue(LOCATION_Y));
            return new Point(x, y);
        } 
        catch (Exception e) {
            return new Point(0, 0);
        }
    }

    /**
     * Gets the size of the main window.
     * 
     * @return size.
     */
    public Dimension getMainWindowDimension() {
        try {
            Element root = configDoc.getRootElement();
            Element mainWindow = root.getChild(MAIN_WINDOW);
            int width = Integer.parseInt(mainWindow.getAttributeValue(WIDTH));
            int height = Integer.parseInt(mainWindow.getAttributeValue(HEIGHT));
            return new Dimension(width, height);
        } 
        catch (Exception e) {
            return new Dimension(300, 300);
        }
    }

    /**
     * Gets the sound volume level.
     * 
     * @return volume (0 (silent) to 1 (loud)).
     */
    public float getVolume() {
        try {
            Element root = configDoc.getRootElement();
            Element volume = root.getChild(VOLUME);
            return Float.parseFloat(volume.getAttributeValue(SOUND));
        } 
        catch (Exception e) {
            return 50F;
        }
    }

    /**
     * Checks if sound volume is set to mute.
     * 
     * @return true if mute.
     */
    public boolean isMute() {
        try {
            Element root = configDoc.getRootElement();
            Element volume = root.getChild(VOLUME);
            return Boolean.parseBoolean(volume.getAttributeValue(MUTE));
        } 
        catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if an internal window is displayed.
     * 
     * @param windowName the window name.
     * @return true if displayed.
     */
    public boolean isInternalWindowDisplayed(String windowName) {
        try {
            Element root = configDoc.getRootElement();
            Element internalWindows = root.getChild(INTERNAL_WINDOWS);
            List<Object> internalWindowNodes = internalWindows.getChildren();
            boolean result = false;
            for (Object element : internalWindowNodes) {
                if (element instanceof Element) {
                    Element internalWindow = (Element) element;
                    String name = internalWindow.getAttributeValue(NAME);
                    if (name.equals(windowName)){
                        result = Boolean.parseBoolean(internalWindow.getAttributeValue(DISPLAY));
                        break;
                    }
                }
            }
            return result;
        } 
        catch (Exception e) {
            return false;
        }
    }

    /**
     * Gets the origin location of an internal window on the desktop.
     * 
     * @param windowName the window name.
     * @return location.
     */
    public Point getInternalWindowLocation(String windowName) {
        try {
            Element root = configDoc.getRootElement();
            Element internalWindows = root.getChild(INTERNAL_WINDOWS);
            List<Object> internalWindowNodes = internalWindows.getChildren();
            Point result = new Point(0, 0);
            for (Object element : internalWindowNodes) {
                if (element instanceof Element) {
                    Element internalWindow = (Element) element;
                    String name = internalWindow.getAttributeValue(NAME);
                    if (name.equals(windowName)) {
                        int locationX = Integer.parseInt(internalWindow.getAttributeValue(LOCATION_X));
                        int locationY = Integer.parseInt(internalWindow.getAttributeValue(LOCATION_Y));
                        result.setLocation(locationX, locationY);
                    }
                }
            }
            return result;
        } 
        catch (Exception e) {
            return new Point(0, 0);
        }
    }

    /**
     * Gets the z order of an internal window on the desktop.
     * 
     * @param windowName the window name.
     * @return z order (lower number represents higher up)
     */
    public int getInternalWindowZOrder(String windowName) {
        try {
            Element root = configDoc.getRootElement();
            Element internalWindows = root.getChild(INTERNAL_WINDOWS);
            List<Object> internalWindowNodes = internalWindows.getChildren();
            int result = -1;
            for (Object element : internalWindowNodes) {
                if (element instanceof Element) {
                    Element internalWindow = (Element) element;
                    String name = internalWindow.getAttributeValue(NAME);
                    if (name.equals(windowName))
                        result = Integer.parseInt(internalWindow.getAttributeValue(Z_ORDER));
                }
            }
            return result;
        } 
        catch (Exception e) {
            return -1;
        }
    }

    /**
     * Gets the size of an internal window.
     * 
     * @param windowName the window name.
     * @return size.
     */
    public Dimension getInternalWindowDimension(String windowName) {
        try {
            Element root = configDoc.getRootElement();
            Element internalWindows = root.getChild(INTERNAL_WINDOWS);
            List<Object> internalWindowNodes = internalWindows.getChildren();
            Dimension result = new Dimension(0, 0);
            for (Object element : internalWindowNodes) {
                if (element instanceof Element) {
                    Element internalWindow = (Element) element;
                    String name = internalWindow.getAttributeValue(NAME);
                    if (name.equals(windowName)) {
                        int width = Integer.parseInt(internalWindow.getAttributeValue(WIDTH));
                        int height = Integer.parseInt(internalWindow.getAttributeValue(HEIGHT));
                        result = new Dimension(width, height);
                    }
                }
            }
            return result;
        } 
        catch (Exception e) {
            return new Dimension(0, 0);
        }
    }

    /**
     * Gets the internal window type.
     * 
     * @param windowName the window name.
     * @return "unit" or "tool".
     */
    public String getInternalWindowType(String windowName) {
        try {
            Element root = configDoc.getRootElement();
            Element internalWindows = root.getChild(INTERNAL_WINDOWS);
            List<Object> internalWindowNodes = internalWindows.getChildren();
            String result = "";
            for (Object element : internalWindowNodes) {
                if (element instanceof Element) {
                    Element internalWindow = (Element) element;
                    String name = internalWindow.getAttributeValue(NAME);
                    if (name.equals(windowName))
                        result = internalWindow.getAttributeValue(TYPE);
                }
            }
            return result;
        } 
        catch (Exception e) {
            return "";
        }
    }

    /**
     * Checks if internal window is configured.
     * 
     * @param windowName the window name.
     * @return true if configured.
     */
    public boolean isInternalWindowConfigured(String windowName) {
        try {
            Element root = configDoc.getRootElement();
            Element internalWindows = root.getChild(INTERNAL_WINDOWS);
            List<Object> internalWindowNodes = internalWindows.getChildren();
            boolean result = false;
            for (Object element : internalWindowNodes) {
                if (element instanceof Element) {
                    Element internalWindow = (Element) element;
                    String name = internalWindow.getAttributeValue(NAME);
                    if (name.equals(windowName))
                        result = true;
                }
            }
            return result;
        } 
        catch (Exception e) {
            return false;
        }
    }

    /**
     * Gets all of the internal window names.
     * 
     * @return list of window names.
     */
    public List<String> getInternalWindowNames() {
        List<String> result = new ArrayList<String>();
        try {
            Element root = configDoc.getRootElement();
            Element internalWindows = root.getChild(INTERNAL_WINDOWS);
            List<Object> internalWindowNodes = internalWindows.getChildren();
            for (Object element : internalWindowNodes) {
                if (element instanceof Element) {
                    Element internalWindow = (Element) element;
                    result.add(internalWindow.getAttributeValue(NAME));
                }
            }
            return result;
        } 
        catch (Exception e) {
            return result;
        }
    }
}