package dmt;

import hlt.Position;

class Line {
    private Position start;
    private Position end;

    Line(Position start, Position end) {
        this.start = start;
        this.end = end;
    }

    Position getStart() {
        return start;
    }

    Position getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return "Line[" + start + "," + end + "]";
    }
}
