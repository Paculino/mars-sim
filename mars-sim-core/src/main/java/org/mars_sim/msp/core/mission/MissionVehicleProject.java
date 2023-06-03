/**
 * Mars Simulation Project
 * MissionVehicleProject.java
 * @date 2023-06-03
 * @author Barry Evans
 */
package org.mars_sim.msp.core.mission;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.MissionStatus;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.person.ai.mission.NavPoint;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.task.LoadingController;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * This represents a MissionProject that specialises in Vehcile based Mission.
 */
public class MissionVehicleProject extends MissionProject
    implements VehicleMission {
    
    private static final MissionStatus NO_AVAILABLE_VEHICLES = new MissionStatus("Mission.status.noVehicle");

    private Vehicle vehicle;
    private double proposedDistance;
    private List<NavPoint> route;

    public MissionVehicleProject(String name, MissionType type, int priority, int maxMembers, Person leader) {
        super(name, type, priority, maxMembers, leader);

        Vehicle best = findBestVehicle(leader.getAssociatedSettlement());
        if (best == null) {
			abortMission(NO_AVAILABLE_VEHICLES);
		}
        else {
            setVehicle(best);
        }
    }

    /**
     * Set the Vehicle being used
     * @param best
     */
    private void setVehicle(Vehicle best) {
        vehicle = best;
        vehicle.setMission(this);
        vehicle.setReservedForMission(true);
    }

    /**
	 * Find the best suitable vehicle for the mission if possible.
	 *
	 * @return The selected bets vehicle; null if none found
	 */
	private final Vehicle findBestVehicle(Settlement base) {
		Collection<Vehicle> vList = base.getParkedVehicles();
		List<Vehicle> bestVehicles = new ArrayList<>();
        int bestScore = 0;

		for (Vehicle v : vList) {
			int vehicleScore = scoreVehicle(v);
            if (bestScore == vehicleScore) {
                // Just as good
                bestVehicles.add(v);
            }
            else if (bestScore < vehicleScore) {
                // New bets so reset
                bestVehicles = new ArrayList<>();
                bestVehicles.add(v);
                bestScore = vehicleScore;
            }
		}

		// Randomly select from the best vehicles.
		if (!bestVehicles.isEmpty()) {
			int bestVehicleIndex = RandomUtil.getRandomInt(bestVehicles.size() - 1);
			return bestVehicles.get(bestVehicleIndex);
		}
        return null;
	}

    /**
     * Score the vehcle suitability for this Mission
     * @param v
     * @return Return -1 if not suitable at all
     */
    private int scoreVehicle(Vehicle v) {
        return 1;
    }

    @Override
    public Vehicle getVehicle() {
        return vehicle;
    }

    @Override
    public double getDistanceProposed() {
        return proposedDistance;
    }

    @Override
    public double getTotalDistanceTravelled() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTotalDistanceTravelled'");
    }

    @Override
    public double getTotalDistanceRemaining() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTotalDistanceRemaining'");
    }

    @Override
    public double getDistanceCurrentLegRemaining() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getDistanceCurrentLegRemaining'");
    }

    @Override
    public LoadingController getLoadingPlan() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getLoadingPlan'");
    }

    @Override
    public MarsClock getLegETA() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getLegETA'");
    }

    @Override
    public boolean isTravelling() {
        return (getControl().getStep() instanceof MissionTravelStep);
    }

    @Override
    public NavPoint getCurrentDestination() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCurrentDestination'");
    }

    @Override
    public int getNumberOfNavpoints() {
        return route.size();
    }

    @Override
    public NavPoint getNavpoint(int i) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getNavpoint'");
    }

    @Override
    public int getNextNavpointIndex() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getNextNavpointIndex'");
    }

    @Override
    public void getHelp(MissionStatus status) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getHelp'");
    }

    @Override
    public boolean isVehicleUnloadableHere(Settlement settlement) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isVehicleUnloadableHere'");
    }
    
    /**
     * Extract the Navpoints and calculate the proposed
     */
    @Override
    protected void setSteps(List<MissionStep> plan) {
        route = plan.stream()
                        .filter(sc -> sc instanceof MissionTravelStep)
                        .map (sc -> ((MissionTravelStep) sc).getDestination())
                        .toList();
        proposedDistance = route.stream()
                            .mapToDouble(NavPoint::getDistance)
                            .sum();
        super.setSteps(plan);
    }
    
}
