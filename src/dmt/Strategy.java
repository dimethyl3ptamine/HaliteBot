package dmt;

import hlt.Move;

import java.util.ArrayList;

public interface Strategy {

    void calculateMovements(ArrayList<Move> moveList) throws StrategyException;

    String getStrategyName();

    default GameState getState() {
        return StrategyHelper.HELPER.getCurrentState();
    }
}
