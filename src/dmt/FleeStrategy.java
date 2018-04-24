package dmt;

import hlt.*;

import java.util.*;

class FleeStrategy implements Strategy {

    private static final String NAME = "Flee, coward, flee (and pray)!";

    private double width;
    private double height;
    private boolean isActivated = false;
    private Map<Integer, Position> shipsToBurrow = new HashMap<>(); // Ship.id -> Position

    void initStrategy() {
        if (!isActivated) {
            isActivated = true;

            GameMap gameMap = getState().getMap();
            width = gameMap.getWidth();
            height = gameMap.getHeight();

            for (Ship ship : getState().getAllMyShips()) {
                shipsToBurrow.put(ship.getId(), getDesertTarget(ship));
            }
        }
    }

    @Override
    public String getStrategyName() {
        return NAME;
    }

    @Override
    public void calculateMovements(ArrayList<Move> moveList) {
        int shift = 0;
        for (Ship ship : getState().getAllMyShips()) {
            Utils.log("Flee, coward: " + ship);

            if (ship.getDockingStatus() == Ship.DockingStatus.Docked) {
                moveList.add(new UndockMove(ship));
                shipsToBurrow.put(ship.getId(), getDesertTarget(ship));
                continue;
            }

            fleeAndPray(moveList, shift++, ship);
        }
    }

    boolean isActivated() {
        return isActivated;
    }

    private Position getDesertTarget(Ship ship) {
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

    private void fleeAndPray(ArrayList<Move> moveList, int shift, Ship ship) {
        Position cowardsBurrow = shipsToBurrow.get(ship.getId());

        if (cowardsBurrow == null) {
            cowardsBurrow = getDesertTarget(ship);
            shipsToBurrow.put(ship.getId(), cowardsBurrow);
        }

        double desX = getDesertionMetaPosition(shift, cowardsBurrow.getXPos());
        double desY = getDesertionMetaPosition(shift, cowardsBurrow.getYPos());

        ThrustMove newThrustMove = Utils.navigateShipToPosition(ship, new Position(desX, desY), true);

        // Pray!
        if (newThrustMove != null) {
            moveList.add(newThrustMove);
        }
    }

    private double getDesertionMetaPosition(int shift, double pos) {
        int factor = (pos == width || pos == height) ? -1 : 1;
        return pos + Constants.FORECAST_FUDGE_FACTOR * 2 * shift * factor;
    }

}
