package visualization;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

public class AnimationController {
    private Timer timer;
    private int currentStep;
    private int maxSteps;
    private boolean isRunning;
    private int delay = 50;

    public AnimationController() {
        this.timer = new Timer(delay, null);
        this.isRunning = false;
    }

    public void startAnimation(int maxSteps, Consumer<Integer> stepCallback) {
        stopAnimation();

        this.currentStep = 0;
        this.maxSteps = maxSteps;
        this.isRunning = true;

        timer = new Timer(delay, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentStep >= maxSteps) {
                    stopAnimation();
                    return;
                }

                stepCallback.accept(currentStep);
                currentStep++;
            }
        });

        timer.start();
    }

    public void stopAnimation() {
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }
        isRunning = false;
    }

    public void setDelay(int delay) {
        this.delay = delay;
        if (timer != null && timer.isRunning()) {
            timer.setDelay(delay);
        }
    }

    public boolean isRunning() { return isRunning; }
    public int getCurrentStep() { return currentStep; }
}