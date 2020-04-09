package fr.mattmouss.gates.tscapability;

public interface ITSStorage {
    int getId();
    void changeId();
    void setId(int newId);
    boolean getAnimationInWork();
    void startAnimation();
    void endAnimation();
}
