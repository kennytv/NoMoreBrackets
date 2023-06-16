package eu.kennytv;

public final class Progress {

    private int removed;

    public int removed() {
        return removed;
    }

    public void addRemoved(final int removed) {
        this.removed += removed;
    }
}
