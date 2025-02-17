/**
 * Mars Simulation Project
 * CookMealMeta.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.task.CookMeal;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskTrait;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RobotType;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.cooking.Cooking;

/**
 * Meta task for the CookMeal task.
 */
public class CookMealMeta extends MetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.cookMeal"); //$NON-NLS-1$
    
    public CookMealMeta() {
		super(NAME, WorkerType.BOTH, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.COOKING);
		setTrait(TaskTrait.ARTISTIC);
		setPreferredJob(JobType.CHEF);
	}

    @Override
    public Task constructInstance(Person person) {
        return new CookMeal(person);
    }

    @Override
    public double getProbability(Person person) {
    	if (person.isOutside())
    		return 0;
    		
        double result = 0D;

        if (person.isInSettlement() && CookMeal.isLocalMealTime(person.getCoordinates(), 20)) {

            // Probability affected by the person's stress and fatigue.
            if (!person.getPhysicalCondition().isFitByLevel(1000, 70, 1000)) {
            	return 0;
            }
            
            // See if there is an available kitchen.
            Building kitchenBuilding = CookMeal.getAvailableKitchen(person);

            if (kitchenBuilding != null) {
                Cooking kitchen = kitchenBuilding.getCooking();

                // Check if enough meals have been cooked at kitchen for this meal time.
                boolean enoughMeals = kitchen.getCookNoMore();

                if (enoughMeals) 
                	return 0;

                if (kitchen.canCookMeal()) {

                    result = 50D;
                    
                	if (CookMeal.isLocalMealTime(person.getCoordinates(), 20)) {
                		result *= 2.5D;
                	}
                	else
                		result *= .25D;   
                	
                    // Crowding modifier.
                    result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, kitchenBuilding);
                    result *= TaskProbabilityUtil.getRelationshipModifier(person, kitchenBuilding);
                    
                    // Apply the standard Person modifiers
                    result = applyPersonModifier(result, person);
                }
            }
        }

        return result;
    }

	@Override
	public Task constructInstance(Robot robot) {
        return new CookMeal(robot);
	}

	@Override
	public double getProbability(Robot robot) {

        double result = 0D;


        if (CookMeal.isMealTime(robot, 20)
            && robot.getRobotType() == RobotType.CHEFBOT) {
            // See if there is an available kitchen.
            Building kitchenBuilding = CookMeal.getAvailableKitchen(robot);

            if (kitchenBuilding != null) {

                Cooking kitchen = kitchenBuilding.getCooking();

                // Check if enough meals have been cooked at kitchen for this meal time.
                boolean enoughMeals = kitchen.getCookNoMore();

                if (enoughMeals) return 0;

                if (kitchen.canCookMeal()) {
                    result = 300D;
                    // Crowding modifier.
                    result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(robot, kitchenBuilding);
                    // Effort-driven task modifier.
                    result *= robot.getPerformanceRating();
                }
            }
        }

        //System.out.println("cook meal : " + result);
        return result;
	}
}
