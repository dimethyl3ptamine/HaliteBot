import dmt.Strategy;
import dmt.StrategyException;
import dmt.StrategyHelper;
import dmt.Utils;
import hlt.*;

import java.util.*;

public class MyBot {

    private static final String BOT_NAME = "_Nag1bAt0r_";

    public static void main(final String[] args) {
        final Networking networking = new Networking();
        final GameMap gameMap = networking.initialize(BOT_NAME);
        final ArrayList<Move> moveList = new ArrayList<>();

        Log.log("**H***E***L***L***O**");
        Log.log("    " + BOT_NAME);
        Log.log("**T***H***E***R***E**");

        int turn = 0;
        StrategyHelper.INSTANCE.init(gameMap);

        while (true) {
            moveList.clear();
            networking.updateMap(gameMap);

            Strategy strategy = StrategyHelper.INSTANCE.getStrategy(turn++, gameMap);
            try {
                strategy.calculateMovements(moveList);
            } catch (StrategyException e) {
                Utils.logError(e.getMessage());
                StrategyHelper.INSTANCE.rollbackToDefaultStrategy(moveList);
            }

            Networking.sendMoves(moveList);
        }
    }

}
