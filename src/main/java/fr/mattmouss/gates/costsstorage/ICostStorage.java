package fr.mattmouss.gates.costsstorage;

import java.util.HashMap;

public interface ICostStorage {
    HashMap<Integer,Integer> getCostMap();
    void addIdWithCost(int id,int cost);
    void addIdWithoutCost(int id);
    void removeId(int id);
    boolean containsId(int id);
    void lowerPrice(int id);
    void raisePrice(int id);
    void changeCost(int id,int newCost);

}
