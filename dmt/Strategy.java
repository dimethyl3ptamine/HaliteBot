package dmt;

import hlt.GameMap;
import hlt.Move;

import java.util.ArrayList;

public interface Strategy {

    void calculateMovements(GameMap gameMap, ArrayList<Move> moveList) throws StrategyException;

    String getStrategyName();
}
