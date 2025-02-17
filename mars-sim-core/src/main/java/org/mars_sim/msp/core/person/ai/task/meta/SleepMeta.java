/*
 * Mars Simulation Project
 * SleepMeta.java
 * @date 2022-07-19
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.CircadianClock;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ShiftType;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.task.Sleep;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingCategory;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * Meta task for the Sleep task.
 */
public class SleepMeta extends MetaTask {

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(SleepMeta.class.getName());

	private static final double MAX = 1000;
	
    /** Task name */
    private static final String NAME = Msg.getString("Task.description.sleep"); //$NON-NLS-1$
		
    
    public SleepMeta() {
		super(NAME, WorkerType.BOTH, TaskScope.ANY_HOUR);
	}
    
    @Override
    public Task constructInstance(Person person) {
    	return new Sleep(person);
    }

    @Override
    public double getProbability(Person person) {
  		
        double result = 0;
	
        // No sleeping outside.
    	// Should allow a person to walk back in to find a place to sleep
    	if (person.isOutside())
    		return 0;
    	
    	if (person.isInSettlement()) {
	        Building b = person.getBuildingLocation();
	        if ((b != null) && (b.getCategory() == BuildingCategory.EVA_AIRLOCK)) {
	        	return 0;
	        }
    	}
    	
   		CircadianClock circadian = person.getCircadianClock();
   		PhysicalCondition pc = person.getPhysicalCondition();
 
       	boolean proceed = false;

    	// Note : each millisol generates 1 fatigue point
    	// 500 millisols is ~12 hours

       // boolean isOnCall = ts.getShiftType() == ShiftType.ON_CALL;
        
        double fatigue = pc.getFatigue();
    	double stress = pc.getStress();
    	double ghrelinS = circadian.getSurplusGhrelin();
    	double leptinS = circadian.getSurplusLeptin();
    	double hunger = pc.getHunger();
    	double energy = pc.getEnergy();
    	
    	// When we are sleep deprived. The 2 hormones (Leptin and Ghrelin) are "out of sync" and that is 
    	// when we eat more than we should without even realizing.
    	
    	// People who don't sleep enough end up with too much ghrelin in their system, so the body thinks 
    	// it's hungry and it needs more calories, and it stops burning those calories because it thinks 
    	// there's a shortage.
    	
    	// 1000 millisols is 24 hours, if a person hasn't slept for 24 hours,
        // he is supposed to want to sleep right away.
    	// Note:  sleep deprivation increases ghrelin levels, while at the same time 
    	// lowers leptin levels in the blood.
    	if (fatigue > 300 || stress > 50 || ghrelinS > 0 || leptinS == 0) {
    		
        	int rand = RandomUtil.getRandomInt(1);
        	// Take a break from sleep if it's too hungry and too low energy
            proceed = rand != 1 || (!(hunger > 667) && !(energy < 1000));
//    		logger.info(person + "  ghrelin: " + ghrelin + "  leptin:" + leptin);
    	}
    	
        if (proceed) {
			double ghrelin = person.getCircadianClock().getGhrelin();
			double leptin = person.getCircadianClock().getLeptin();
		    double sleepMillisols = person.getCircadianClock().getTodaySleepTime();
            double[] muscle = person.getPhysicalCondition().getMusculoskeletal();
            
        	// the desire to go to bed increase linearly after 6 hours of wake time
            result += Math.max((fatigue - 250), 0) * 10 + stress * 10 
            		+ (ghrelin - leptin)
            		// High hunger makes it harder to fall asleep
            		// Therefore, limit the hunger contribution to a max of 300
            		+ Math.min(hunger, 300)
            		// Note: muscle condition affects the desire to exercise
            		- muscle[2]/2.5 
            		- sleepMillisols / 2;
                   
            double pref = person.getPreference().getPreferenceScore(this);
            
         	result += result * pref/12D;                            	
        	
    	    if (result <= 0) {
    	    	// Reduce the probability if it's not the right time to sleep
//    	    	refreshSleepHabit(person); 
    	    	return 0;
    	    }
    	    	
            // Check if person is an astronomer.
            boolean isAstronomer = (person.getMind().getJob() == JobType.ASTRONOMER);

            // Dark outside modifier.
            boolean isDark = (surfaceFeatures.getSolarIrradiance(person.getCoordinates()) < 5);
            
            if (isDark && !isAstronomer) {
                // Non-astronomers more likely to sleep when it's dark out.
                result *= 2D;
            }
            else if (!isDark && isAstronomer) {
                // Astronomers more likely to sleep when it's not dark out.
                result *= 2D;
            }

            if (person.getShiftType() == ShiftType.ON_CALL)
            	result = result * 10;
            
            else if (person.getTaskSchedule().isShiftHour(marsClock.getMillisolInt())) {
         	   // Reduce the probability of sleep
               result = result / 10D;
            }

        	int maxNumSleep = 0;

        	if (person.getTaskSchedule().getShiftType() == ShiftType.ON_CALL)
            	maxNumSleep = 8;
            else
            	maxNumSleep = 4;

        	int sol = marsClock.getMissionSol();
        	
        	// Skip the first sol since the sleep time pattern has not been established
            if (sol != 1 && person.getCircadianClock().getNumSleep() <= maxNumSleep) {
            	result = modifiedBySleepHabit(person, result);
            }
            
    	    if (result < 0)
    	    	result = 0;
        }
        
        else {
        	// if process is false
        	
	        // Reduce the probability if it's not the right time to sleep
//	    	refreshSleepHabit(person);  
       }

        return result;
    }
    
    public double modifyProbability(double value, Person person, Building quarters) {
    	return value * TaskProbabilityUtil.getCrowdingProbabilityModifier(person, quarters) 
    			* TaskProbabilityUtil.getRelationshipModifier(person, quarters);
    }
    
    
	@Override
	public Task constructInstance(Robot robot) {
		return new Sleep(robot); 	  	
	}

	@Override
	public double getProbability(Robot robot) {

        double result = 1D;

        // No sleeping outside.
        if (robot.isOutside())
            return 0;

        else if (robot.isInVehicle())
        	// Future: will re-enable robot serving in a vehicle
            return result;
        
        // Crowding modifier.
        else if (robot.isInSettlement()) {
        	result += 1D;
     
        	double level = robot.getSystemCondition().getBatteryState();
        	
        	// Checks if the battery is low
        	if (robot.getSystemCondition().isLowPower()) {
        		result += (1.0 - level) * MAX;
        	}
        	else
        		result += (1.0 - level) * 5.0;
        		
            Building building = Sleep.getAvailableRoboticStationBuilding(robot);
            if (building != null) {
//            	logger.info(robot, building + " has empty slot.");
            }
            else
            	result = 0;
            
//    		logger.info(robot, "level: " + level + "  prob: " + result);
        }

        return result;
	}
	
	public double modifiedBySleepHabit(Person person, double result) {
        	// Checks the current time against the sleep habit heat map
	    	int bestSleepTime[] = person.getPreferredSleepHours();
	    	// is now falling two of the best sleep time ?
	    	for (int time : bestSleepTime) {
	        	int now = marsClock.getMillisolInt();
		    	int diff = time - now;
		    	if (diff < 30 || diff > -30) {
		    		result = result*5;
		    		return result;
		    	}
		    	else {
		    		// Reduce the probability by a factor of 10
		    		result = result/5;
		    		return result;
		    	}
	    	}
	    	return result;
	}
}
