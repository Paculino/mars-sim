/*
 * Mars Simulation Project
 * Shift.java
 * @date 2022-11-20
 * @author Barry Evans
 */
package org.mars_sim.msp.core.structure;

import java.io.Serializable;

/**
 * This is an activeShift that has a numebr of Slots forworks allocated.
 */
public class Shift implements Serializable {

    private int start;
    private int end;
    private String name;
    private boolean onDuty= false;
    private int targetPercentage;
    private int members = 0;

    /**
     * Create an active Shift defined by a shared specification
     * @param spec The Specification of the Shift
     * @param offset MSols offset 
     */
    Shift(ShiftSpec spec, int offset) {
        // The Shift Spec assumes standard timezie; but an offset has to be applied to
        // keep the Shift aligned with Sunrise/Sunset
        this.start = (spec.getStart() + offset)%1000;
        this.end = (spec.getEnd() + offset)%1000;
        this.name = spec.getName();
        this.targetPercentage = spec.getPopPercentage();
    }

    public String getName() {
        return name;
    }

    public boolean isOnDuty() {
        return onDuty;
    }

    /**
     * Check the on duty stats of this Shift
     * @param mSol The time in the day to check for.
     * @return True if the onduty flag has changed status
     */
    boolean checkShift(int mSol) {
        boolean oldOnDuty = onDuty;

        if (start < end) {
            // Start and end on same Sol
            onDuty = (start <= mSol) && (mSol <  end);
        }
        else {
            // Ends on the following Sol so 2 seperate segments
            onDuty = (start <= mSol) || (mSol <  end);
        }
        return (oldOnDuty != onDuty);
    }

    /**
     * What is the target percentage of the population that should use this Shift
     * @return
     */
    public int getPopPercentage() {
        return targetPercentage;
    }

    public int getSlotNumber() {
        return members;
    }
    
    /**
     * Increase how many shots have been allocationed to the Shift
     */
    void joinShift() {
        members++;
    }

    /**
     * Someone has left the Shift
     */
    public void leaveShift() {
        members--;
    }

    /**
     * When does the shift start
     */
    public int getStart() {
        return start;
    }

    /**
     * When does the shift end
     */
    public int getEnd() {
        return end;
    }
}