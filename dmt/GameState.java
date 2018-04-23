package dmt;

import hlt.GameMap;
import hlt.Planet;
import hlt.Ship;

import java.util.ArrayList;
import java.util.Collection;

class GameState {

    private int turn;
    private int myId;
    private int numberOfPlayers;
    private GameMap gameMap;
    private Collection<Ship> allShips;
    private Collection<Ship> allMyShips;
    private Collection<Ship> myUndockedShips;
    private Collection<Ship> enemiesShips;
    private Collection<Planet> allPlanets;
    private Collection<Planet> allMyPlanets;
    private Collection<Planet> freePlanets;
    private Collection<Planet> enemiesPlanets;

    GameState(GameMap map, int turn) {
        this.gameMap = map;
        this.turn = turn;
        myId = gameMap.getMyPlayerId();
        numberOfPlayers = gameMap.getAllPlayers().size();

        allMyShips = gameMap.getMyPlayer().getShips().values();
        allShips = gameMap.getAllShips();
        initShips();

        allPlanets = gameMap.getAllPlanets().values();
        initPlanets();
    }

    GameMap getMap() {
        return gameMap;
    }

    int getTurn() {
        return turn;
    }

    int getMyId() {
        return myId;
    }

    int getNumberOfPlayers() {
        return numberOfPlayers;
    }

    Collection<Ship> getAllShips() {
        return allShips;
    }

    Collection<Ship> getAllMyShips() {
        return allMyShips;
    }

    Collection<Ship> getEnemiesShips() {
        return enemiesShips;
    }

    Collection<Ship> getMyUndockedShips() {
        return myUndockedShips;
    }

    Collection<Planet> getAllMyPlanets() {
        return allMyPlanets;
    }

    Collection<Planet> getAllPlanets() {
        return allPlanets;
    }

    Collection<Planet> getEnemiesPlanets() {
        return enemiesPlanets;
    }

    Collection<Planet> getFreePlanets() {
        return freePlanets;
    }

    private void initShips() {
        myUndockedShips = new ArrayList<>();
        enemiesShips = new ArrayList<>();

        for (Ship ship : allShips) {
            if (ship.getOwner() != myId) {
                enemiesShips.add(ship);
            } else {
                if (ship.getDockingStatus() == Ship.DockingStatus.Undocked) {
                    myUndockedShips.add(ship);
                }
            }
        }
    }

    private void initPlanets() {
        allMyPlanets = new ArrayList<>();
        freePlanets = new ArrayList<>();
        enemiesPlanets = new ArrayList<>();

        for (Planet planet : allPlanets) {
            if (!planet.isOwned()) {
                freePlanets.add(planet);
            } else {
                if (planet.getOwner() != myId) {
                    enemiesPlanets.add(planet);
                } else {
                    allMyPlanets.add(planet);
                }
            }
        }
    }

}
