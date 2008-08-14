/**
 * Mars Simulation Project
 * ConstructionConfig.java
 * @version 2.85 2008-08-10
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.structure.construction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.resource.ItemResource;
import org.mars_sim.msp.simulation.resource.Part;
import org.mars_sim.msp.simulation.vehicle.LightUtilityVehicle;
import org.mars_sim.msp.simulation.vehicle.Rover;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Parses construction configuration file.
 */
public class ConstructionConfig implements Serializable {

    // Element names
    private static final String NAME = "name";
    private static final String WORK_TIME = "work-time";
    private static final String SKILL_REQUIRED = "skill-required";
    private static final String PART = "part";
    private static final String NUMBER = "number";
    private static final String RESOURCE = "resource";
    private static final String AMOUNT = "amount";
    private static final String VEHICLE = "vehicle";
    private static final String TYPE = "type";
    private static final String ATTACHMENT_PART = "attachment-part";
    
    // Data members
    private Document constructionDoc;
    private List<ConstructionStageInfo> foundationStageInfoList;
    private List<ConstructionStageInfo> frameStageInfoList;
    private List<ConstructionStageInfo> buildingStageInfoList;
    
    /**
     * Constructor
     * @param buildingDoc DOM document with construction configuration
     */
    public ConstructionConfig(Document constructionDoc) {
        this.constructionDoc = constructionDoc; 
    }
    
    /**
     * Gets a list of foundation construction stage infos.
     * @return list of construction stage infos.
     * @throws Exception if error parsing list.
     */
    List<ConstructionStageInfo> getFoundationConstructionStageInfoList() throws Exception {
        
        if (foundationStageInfoList != null)
            createConstructionStageInfoList(ConstructionSite.FOUNDATION);
        
        return new ArrayList<ConstructionStageInfo>(foundationStageInfoList);
    }
    
    /**
     * Gets a list of frame construction stage infos.
     * @return list of construction stage infos.
     * @throws Exception if error parsing list.
     */
    List<ConstructionStageInfo> getFrameConstructionStageInfoList() throws Exception {
        
        if (frameStageInfoList != null)
            createConstructionStageInfoList(ConstructionSite.FRAME);
        
        return new ArrayList<ConstructionStageInfo>(frameStageInfoList);
    }
    
    /**
     * Gets a list of building construction stage infos.
     * @return list of construction stage infos.
     * @throws Exception if error parsing list.
     */
    List<ConstructionStageInfo> getBuildingConstructionStageInfoList() throws Exception {
        
        if (buildingStageInfoList != null)
            createConstructionStageInfoList(ConstructionSite.BUILDING);
        
        return new ArrayList<ConstructionStageInfo>(buildingStageInfoList);
    }
    
    /**
     * Creates a stage info list.
     * @param stageType the stage type.
     * @return list of construction stage infos.
     * @throws Exception if error parsing XML file.
     */
    private List<ConstructionStageInfo> createConstructionStageInfoList(String stageType) 
            throws Exception {
        
        List<ConstructionStageInfo> stageInfoList = null;
        if (ConstructionSite.FOUNDATION.equals(stageType)) {
            foundationStageInfoList = new ArrayList<ConstructionStageInfo>();
            stageInfoList = foundationStageInfoList;
        }
        else if (ConstructionSite.FRAME.equals(stageType)) {
            frameStageInfoList = new ArrayList<ConstructionStageInfo>();
            stageInfoList = frameStageInfoList;
        }
        else if (ConstructionSite.BUILDING.equals(stageType)) {
            buildingStageInfoList = new ArrayList<ConstructionStageInfo>();
            stageInfoList = buildingStageInfoList;
        }
        else throw new Exception("stageType: " + stageType + " not valid.");
            
        Element root = constructionDoc.getDocumentElement();
        Element stageInfoListElement = (Element) root.getElementsByTagName(stageType + "-list").item(0);
        NodeList stageInfoNodes = stageInfoListElement.getElementsByTagName(stageType);
        for (int x = 0; x < stageInfoNodes.getLength(); x++) {
            String name = "";
                
            try {
                Element stageInfoElement = (Element) stageInfoNodes.item(x);
                    
                // Get name.
                name = stageInfoElement.getAttribute(NAME);
                    
                double workTime = Double.parseDouble(stageInfoElement.getAttribute(WORK_TIME));
                    
                int skillRequired = Integer.parseInt(stageInfoElement.getAttribute(SKILL_REQUIRED));
                    
                String prerequisiteStage = null;
                String prerequisiteStageType = null;
                if (ConstructionSite.FRAME.equals(stageType)) 
                    prerequisiteStageType = ConstructionSite.FOUNDATION;
                else if (ConstructionSite.BUILDING.equals(stageType)) 
                    prerequisiteStageType = ConstructionSite.FRAME;
                if (prerequisiteStageType != null) 
                    prerequisiteStage = stageInfoElement.getAttribute(prerequisiteStageType);
                    
                NodeList partList = stageInfoElement.getElementsByTagName(PART);
                Map<Part, Integer> parts = new HashMap<Part, Integer>(partList.getLength());
                for (int y = 0; y < partList.getLength(); y++) {
                    Element partElement = (Element) partList.item(y);
                    String partName = partElement.getAttribute(NAME);
                    int partNum = Integer.parseInt(partElement.getAttribute(NUMBER));
                    Part part = (Part) ItemResource.findItemResource(partName);
                    parts.put(part, partNum);
                }
                    
                NodeList resourceList = stageInfoElement.getElementsByTagName(RESOURCE);
                Map<AmountResource, Double> resources = 
                    new HashMap<AmountResource, Double>(resourceList.getLength());
                for (int y = 0; y < resourceList.getLength(); y++) {
                    Element resourceElement = (Element) resourceList.item(y);
                    String resourceName = resourceElement.getAttribute(NAME);
                    double resourceAmount = Double.parseDouble(resourceElement.getAttribute(AMOUNT));
                    AmountResource resource = AmountResource.findAmountResource(resourceName);
                    resources.put(resource, resourceAmount);
                }
                    
                NodeList vehicleList = stageInfoElement.getElementsByTagName(VEHICLE);
                List<ConstructionVehicleType> vehicles = 
                    new ArrayList<ConstructionVehicleType>(vehicleList.getLength());
                for (int y = 0; y < vehicleList.getLength(); y++) {
                    Element vehicleElement = (Element) vehicleList.item(y);
                    String vehicleType = vehicleElement.getAttribute(TYPE);
                        
                    Class vehicleClass = null;
                    if (vehicleType.toLowerCase().indexOf("rover") > -1) vehicleClass = Rover.class;
                    else if (vehicleType.equalsIgnoreCase("light utility vehicle")) 
                        vehicleClass = LightUtilityVehicle.class;
                    else throw new Exception("Unknown vehicle type: " + vehicleType);
                        
                    NodeList attachmentPartList = vehicleElement.getElementsByTagName(ATTACHMENT_PART);
                    List<Part> attachmentParts = new ArrayList<Part>(attachmentPartList.getLength());
                    for (int z = 0; z < attachmentPartList.getLength(); z++) {
                        Element attachmentPartElement = (Element) attachmentPartList.item(z);
                        String partName = attachmentPartElement.getAttribute(NAME);
                        Part attachmentPart = (Part) ItemResource.findItemResource(partName);
                        attachmentParts.add(attachmentPart);
                    }
                        
                    vehicles.add(new ConstructionVehicleType(vehicleType, vehicleClass, attachmentParts));
                }
                    
                ConstructionStageInfo foundationInfo = new ConstructionStageInfo(name, 
                        stageType, workTime, skillRequired, prerequisiteStage, parts, 
                        resources, vehicles);
                stageInfoList.add(foundationInfo);
            }
            catch (Exception e) {
                throw new Exception("Error reading construction stage " + name + ": " + e.getMessage());
            }
        }
        
        return stageInfoList;
    }
}