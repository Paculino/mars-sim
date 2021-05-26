/**
 * Mars Simulation Project
 * TendGreenhouseMeta.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.job.JobUtil;
import org.mars_sim.msp.core.person.ai.task.TendGreenhouse;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskTrait;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.ai.job.Gardenbot;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * Meta task for the Tend Greenhouse task.
 */
public class TendGreenhouseMeta extends MetaTask {

    private static final double VALUE = 4D;
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.tendGreenhouse"); //$NON-NLS-1$

    public TendGreenhouseMeta() {
		super(NAME, WorkerType.BOTH, TaskScope.WORK_HOUR);
		addFavorite(FavoriteType.TENDING_PLANTS);
		addTrait(TaskTrait.ARTISITC);

	}

    @Override
    public Task constructInstance(Person person) {
        return new TendGreenhouse(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;

        if (person.isInSettlement()) {
        	
            // Probability affected by the person's stress and fatigue.
            PhysicalCondition condition = person.getPhysicalCondition();
            double fatigue = condition.getFatigue();
            double stress = condition.getStress();
            double hunger = condition.getHunger();
            
            if (fatigue > 1000 || stress > 80 || hunger > 500)
            	return 0;
            
            try {
                // See if there is an available greenhouse.
                Building farmingBuilding = TendGreenhouse.getAvailableGreenhouse(person);
                if (farmingBuilding != null) {

                    int needyCropsNum = person.getSettlement().getCropsNeedingTending();
                    result = needyCropsNum * VALUE;

                    if (result <= 0) result = 0;
                    
                    // Crowding modifier.
                    result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, farmingBuilding);
                    result *= TaskProbabilityUtil.getRelationshipModifier(person, farmingBuilding);

                    // Settlement factors
            		result *= (person.getSettlement().getGoodsManager().getCropFarmFactor()
            				+ .5 * person.getAssociatedSettlement().getGoodsManager().getTourismFactor());
            		
                    result = applyPersonModifier(result, person);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            	//logger.log(Level.SEVERE, person + " cannot calculate probability : " + e.getMessage());
            }

        }

        return result;
    }

	@Override
	public Task constructInstance(Robot robot) {
        return new TendGreenhouse(robot);
	}

	@Override
	public double getProbability(Robot robot) {

        double result = 0D;

        if (robot.getBotMind().getRobotJob() instanceof Gardenbot && robot.isInSettlement()) {

            try {
                // See if there is an available greenhouse.
                Building farmingBuilding = TendGreenhouse.getAvailableGreenhouse(robot);
                if (farmingBuilding != null) {
 
                    int needyCropsNum = robot.getSettlement().getCropsNeedingTending();

                    result += needyCropsNum * 50D;
    	            // Effort-driven task modifier.
    	            result *= robot.getPerformanceRating();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                //logger.log(Level.SEVERE, robot + " cannot calculate probability : " + e.getMessage());
            }


        }

        return result;
	}
}
