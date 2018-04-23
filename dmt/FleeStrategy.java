package dmt;

import hlt.*;

import java.util.*;

public class FleeStrategy implements Strategy {

    private static final String NAME = "Flee, coward, flee!";

    // Ship.id -> Position
    private Map<Integer, Position> shipsToBurrow = new HashMap<>();
    private boolean isActivated = false;
    private double width;
    private double height;

    void initStrategy(GameMap map) {
        if (!isActivated) {
            isActivated = true;
            width = map.getWidth();
            height = map.getHeight();

            for (Ship ship : Utils.getMyShips(map)) {
                shipsToBurrow.put(ship.getId(), getClosestDesertPoint(ship));
            }
        }
    }

    boolean isActivated() {
        return isActivated;
    }

    @Override
    public String getStrategyName() {
        return NAME;
    }

    @Override
    public void calculateMovements(GameMap gameMap, ArrayList<Move> moveList) {
        int shift = 0;
        Collection<Ship> allShips = gameMap.getMyPlayer().getShips().values();

        for (Ship ship : allShips) {
            Utils.log("Flee, coward: " + ship);

            if (ship.getDockingStatus() == Ship.DockingStatus.Docked) {
                moveList.add(new UndockMove(ship));
                continue;
            }

            shift++;
            Position cowardsBurrow = shipsToBurrow.get(ship.getId());
            double desertionMetaX = getDesertionMetaPosition(shift, cowardsBurrow.getXPos());
            double desertionMetaY = getDesertionMetaPosition(shift, cowardsBurrow.getYPos());

            ThrustMove newThrustMove = Navigation.navigateShipTowardsTarget(gameMap, ship,
                        new Position(desertionMetaX, desertionMetaY), Constants.MAX_SPEED,
                        true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI / 180.0);

            if (newThrustMove != null) {
                moveList.add(newThrustMove);
            }
        }
    }

    private double getDesertionMetaPosition(int shift, double pos) {
        int factor;

        if (pos == width || pos == height) {
            factor = -1;
        } else {
            factor = 1;
        }

        return pos + Constants.FORECAST_FUDGE_FACTOR * shift * factor;
    }

    private Position getClosestDesertPoint(Ship ship) {
        Position pos00 = new Position(0, 0);
        Position pos0H = new Position(0, height);
        Position posW0 = new Position(width, 0);
        Position posWH = new Position(width, height);

        Map<Double, Position> points = new TreeMap<>();
        points.put(ship.getDistanceTo(pos00), pos00);
        points.put(ship.getDistanceTo(pos0H), pos0H);
        points.put(ship.getDistanceTo(posW0), posW0);
        points.put(ship.getDistanceTo(posWH), posWH);

        return ((TreeMap<Double, Position>) points).firstEntry().getValue();
    }

}
