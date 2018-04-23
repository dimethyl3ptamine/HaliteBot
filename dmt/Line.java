package dmt;

import hlt.Position;

class Line {
    private Position pos1;
    private Position pos2;

    Line(Position pos1, Position pos2) {
        this.pos1 = pos1;
        this.pos2 = pos2;
    }

    Position getPos1() {
        return pos1;
    }

    Position getPos2() {
        return pos2;
    }
}
