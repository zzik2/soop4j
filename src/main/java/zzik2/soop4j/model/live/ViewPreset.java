package zzik2.soop4j.model.live;

public class ViewPreset {

    private final String label;
    private final String labelResolution;
    private final String name;
    private final int bps;

    public ViewPreset(String label, String labelResolution, String name, int bps) {
        this.label = label;
        this.labelResolution = labelResolution;
        this.name = name;
        this.bps = bps;
    }

    public String getLabel() {
        return label;
    }

    public String getLabelResolution() {
        return labelResolution;
    }

    public String getName() {
        return name;
    }

    public int getBps() {
        return bps;
    }
}
