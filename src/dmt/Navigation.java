package dmt;

import hlt.*;

// Copied from hlt.Navigation and enhanced
class Navigation {

    static ThrustMove navigateShipToDock(GameMap gameMap, Ship ship, Entity dockTarget, int maxThrust) {
        return navigateShipTowardsTarget(gameMap, ship, ship.getClosestPoint(dockTarget), maxThrust,
                true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI / 180.0);
    }

    static ThrustMove navigateShipTowardsTarget(GameMap gameMap, Ship ship, Position targetPos, int maxThrust,
            boolean avoidObstacles, int maxCorrections, double angularStepRad) {
        if (maxCorrections <= 0) {
            return null;
        }

        final double distance = ship.getDistanceTo(targetPos);
        final double angleRad = ship.orientTowardsInRad(targetPos);

        if (avoidObstacles && !gameMap.objectsBetween(ship, targetPos).isEmpty()) {
            Position newTarget = getNewTarget(ship, angleRad, angularStepRad, distance);
            return navigateShipTowardsTarget(gameMap, ship, newTarget, maxThrust, true, (maxCorrections - 1), angularStepRad);
        }

        int thrust = distance < maxThrust ? (int) distance : maxThrust;

        Position nextStepPosition = getNewTarget(ship, angleRad, 0, thrust);

        if (avoidObstacles && Utils.intersectShipWithMyOtherShips(ship, nextStepPosition)) {
            Position newTarget = getNewTarget(ship, angleRad, angularStepRad, distance);
            return navigateShipTowardsTarget(gameMap, ship, newTarget, maxThrust, true, (maxCorrections - 1), angularStepRad);
        }

        Utils.saveShipToNavigationMap(ship, nextStepPosition);
        int angleDeg = Util.angleRadToDegClipped(angleRad);

        return new ThrustMove(ship, angleDeg, thrust);
    }

    private static Position getNewTarget(Ship ship, double angleRad, double angularStepRad, double distance) {
        double dx = Math.cos(angleRad + angularStepRad) * distance;
        double dy = Math.sin(angleRad + angularStepRad) * distance;
        return new Position(ship.getXPos() + dx, ship.getYPos() + dy);
    }

}
