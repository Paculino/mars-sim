/*
 * Mars Simulation Project
 * WorkerActivityCommand.java
 * @date 2022-06-24
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.unit;

import java.util.List;
import java.util.Map;

import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.ai.task.utils.TaskManager;
import org.mars_sim.msp.core.person.ai.task.utils.TaskManager.OneActivity;
import org.mars_sim.msp.core.person.ai.task.utils.Worker;

/** 
 * 
 */
public class WorkerActivityCommand extends AbstractUnitCommand {
	
	public WorkerActivityCommand(String group) {
		super(group, "ac", "activities", "Activites done by the Worker");
	}

	/** 
	 * Output the current immediate location of the Unit
	 */
	@Override
	protected boolean execute(Conversation context, String input, Unit target) {

		TaskManager tManager = null;

		if (target instanceof Worker) {
			tManager = ((Worker)target).getTaskManager();
		}
		else {
			context.println("Sorry I am not a Worker.");
			return false;
		}
		
		// TODO allow optional inout to choose a day
		int sol = context.getSim().getMasterClock().getMarsClock().getMissionSol();
		Map<Integer, List<OneActivity>> tasks = tManager.getAllActivities();
		if (input != null) {
			sol = Integer.parseInt(input);
			
			if (!tasks.containsKey(sol)) {
				context.println("Sorry there is no activity data for mission sol " + sol);
				return false;
			}
		}
		
		List<OneActivity> activities = tasks.get(sol);
		StructuredResponse response = new StructuredResponse();
		response.appendLabelledDigit("Activities on Mission Sol", sol);
		response.appendTableHeading("When", 4,
									"Activity", -32,
									"Phase");

		for (OneActivity attr : activities) {
			response.appendTableRow(String.format("%3d", attr.getStartTime()),
									attr.getDescription(),
									attr.getPhase());
		}
		context.println(response.getOutput());
		
		return true;
	}
}
