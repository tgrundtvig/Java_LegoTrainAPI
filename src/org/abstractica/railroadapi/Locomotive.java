package org.abstractica.railroadapi;

public interface Locomotive
{
    enum Direction {FORWARD, BACKWARD};
    String getName();
    boolean identify(int numberOfBlinks) throws InterruptedException;
    boolean move(Direction dir, int maxSpeed, int magnets) throws InterruptedException;
    boolean moveAndWait(Direction dir, int maxSpeed, int magnets) throws InterruptedException;
    boolean emergencyBrake() throws InterruptedException;
    int distanceToGoal() throws InterruptedException;
    void waitWhileDistanceToGoalGreaterThan(int distance) throws InterruptedException;
    void waitWhileDistanceToGoal(DistanceToGoalWaiter waiter) throws InterruptedException;

    interface DistanceToGoalWaiter
    {
        public boolean keepWaiting(int distanceToGoal);
    }
}
