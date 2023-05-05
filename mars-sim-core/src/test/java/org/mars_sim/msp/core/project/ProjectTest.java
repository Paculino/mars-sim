package org.mars_sim.msp.core.project;

import static org.junit.Assert.assertThrows;

import org.mars_sim.msp.core.person.ai.task.util.Worker;

import junit.framework.TestCase;


public class ProjectTest extends TestCase {
    static final class TestStep extends ProjectStep {

        int expectedCount = 0;
        int startCount = 0;
        int endCount = 0;

        TestStep(Stage stage, int expectedCount) {
            super(stage);
            this.expectedCount = expectedCount;
        }
        
        @Override
        void execute(Worker worker) {
            expectedCount--;

            if (expectedCount <= 0) {
                complete(worker);
            }
        }

        @Override
        void start(Worker worker) {
            startCount++;
            super.start(worker);
        }

        @Override
        void complete(Worker worker) {
            endCount++;
            super.complete(worker);
        }
    }

    public void testExecuteOneStep() {
        Project p = new Project("Test");

        assertEquals("Waiting with no steps", Stage.WAITING, p.getStage());

        TestStep step1 = new TestStep(Stage.ACTIVE, 2);
        p.addStep(step1);
        assertEquals("Waiting before execution", Stage.WAITING, p.getStage());

        // Execute once
        Worker worker = null;
        p.execute(worker);
        assertEquals("Stage is Active", Stage.ACTIVE, p.getStage());

        // Xecute second time
        p.execute(worker);
        assertEquals("Stage is Done", Stage.DONE, p.getStage());
        assertEquals("Step started once", 1, step1.startCount);
        assertEquals("Step ended once", 1, step1.endCount);
        assertEquals("Step fully expected", 0, step1.expectedCount);

    }

    public void testExecuteTwoStep() {
        Project p = new Project("Test");

        assertEquals("Waiting with no steps", Stage.WAITING, p.getStage());

        TestStep step1 = new TestStep(Stage.ACTIVE, 2);
        TestStep step2 = new TestStep(Stage.CLOSEDOWN, 3);

        p.addStep(step1);
        p.addStep(step2);

        assertEquals("Waiting before execution", Stage.WAITING, p.getStage());

        // Execute once
        Worker worker = null;
        p.execute(worker);
        assertEquals("Stage is Active", Stage.ACTIVE, p.getStage());

        // Xecute second time
        p.execute(worker);
        assertEquals("Step1 stage is Done", Stage.CLOSEDOWN, p.getStage());
        assertEquals("Step1 started once", 1, step1.startCount);
        assertEquals("Step1 ended once", 1, step1.endCount);
        assertEquals("Step1 fully expected", 0, step1.expectedCount);

        // Step 2, execute 1
        p.execute(worker);
        assertEquals("Stage is Active", Stage.CLOSEDOWN, p.getStage());

        // Step 2, execute 2
        p.execute(worker);
        assertEquals("Stage is Active", Stage.CLOSEDOWN, p.getStage());

        // Xecute second time
        p.execute(worker);
        assertEquals("Step2 stage is Done", Stage.DONE, p.getStage());
        assertEquals("Step2 started once", 1, step1.startCount);
        assertEquals("Step2 ended once", 1, step1.endCount);
        assertEquals("Step2 fully expected", 0, step1.expectedCount);
    }

    public void testRemoveStep() {
        Project p = new Project("Test");

        TestStep step1 = new TestStep(Stage.PREPARATION, 2);
        TestStep step2 = new TestStep(Stage.ACTIVE, 1);
        TestStep step3 = new TestStep(Stage.CLOSEDOWN, 1);


        p.addStep(step1);
        p.addStep(step2);

        // Execute once
        Worker worker = null;
        p.execute(worker);
        assertEquals("Stage is Active", Stage.PREPARATION, p.getStage());

        // Swap last step
        p.removeStep(step2);
        p.addStep(step3);

        // Xecute end of first step, stage is new last step
        p.execute(worker);
        assertEquals("Process stage is Closedown", Stage.CLOSEDOWN, p.getStage());

        // Last step executed
        p.execute(worker);
        assertEquals("Remove step not started", 0, step2.startCount);
        assertEquals("Removed step not stopped", 0, step2.endCount);
        assertEquals("Remove step not executed", 1, step2.expectedCount);

        // Xecute second time
        assertEquals("Step3 stage is Done", Stage.DONE, p.getStage());
        assertEquals("Step3 started once", 1, step3.startCount);
        assertEquals("Step3 ended once", 1, step3.endCount);
        assertEquals("Step3 fully expected", 0, step3.expectedCount);
    }



    public void testCreateBadStep() {

        // Add a step with Active stage
        assertThrows("Create illegal WAITING step", IllegalArgumentException.class, () ->  {
                        new TestStep(Stage.WAITING, 0);
                    });
        
        assertThrows("Create illegal DONE step", IllegalArgumentException.class, () ->  {
                        new TestStep(Stage.DONE, 0);
                    });
    }

    public void testAddingBadStage() {
        Project p = new Project("Bad");
        p.addStep(new TestStep(Stage.CLOSEDOWN, 0));

        // Add a step with Active stage
        assertThrows("Add illegal ACTIVE step", IllegalArgumentException.class, () ->  {
                        p.addStep(new TestStep(Stage.ACTIVE, 0));
                    });
        
        assertThrows("Add illegal PREPARATION step", IllegalArgumentException.class, () ->  {
                        p.addStep(new TestStep(Stage.PREPARATION, 0));
                    });
    }
}
