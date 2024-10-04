package fr.moonshade.gates.costsstorage;

import java.util.HashMap;

public interface ICostStorage {
    HashMap<Integer,Integer> getCostMap();
    void addIdWithPrice(int id, int cost);
    void addIdWithoutPrice(int id);
    void removeId(int id);
    boolean containsId(int id);
    int getPrice(int id);
    void lowerPrice(int id);
    void raisePrice(int id);
    void changeCost(int id,int newCost);

}
