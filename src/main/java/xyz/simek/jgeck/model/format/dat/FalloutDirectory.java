package xyz.simek.jgeck.model.format.dat;

public class FalloutDirectory implements FalloutDatItem {

    private String filename;

    public FalloutDirectory(String filename) {
        this.filename = filename;
    }

    @Override
    public String getFilename() {
        return this.filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Override
    public String toString() {
        return this.filename;
    }
}
