# HaliteBot

Source code for my [Halite II](https://halite.io) bot that I created for *internal* hackathon. Managed to get internal rating ~40.

All "magic" happens in **dmt** package. Classes in **hlt** package were not modified. 

##### Possible enhancements

* *FleeStrategy* : need to defend your current position and attack upcoming coward enemies
* *MoveAndKillStrategy* : if there are cowards in the corner, send some hunters to them
* *SplitToNearestPlanetsStrategy* : better to be in charge of the biggest planets only and send ships to kill enemies
* Add more strategies like defending planet
* Better to split ships, e.g. send X hunters and keep Y defenders
* Fix *Navigation* and get better friendly collision check
  * *GameMap.objectsBetween()* : maybe try to not consider allShips
  * *Utils.intersectShipWithMyOtherShips()* : better to refactor to limit lines intersection checking by 2 * thrust_max
* Analyze previous states and decide the behavior based on it

*Thanks a ton to [Two Sigma](https://www.twosigma.com/) for awesome Halite game! I had my fun!*