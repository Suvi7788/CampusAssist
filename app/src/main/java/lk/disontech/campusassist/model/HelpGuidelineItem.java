package lk.disontech.campusassist.model;

public class HelpGuidelineItem {

    private final String title;
    private final String shortDescription;
    private final String fullDescription;
    private final String accentColorHex;
    private final int iconResId;

    public HelpGuidelineItem(String title,
                             String shortDescription,
                             String fullDescription,
                             String accentColorHex,
                             int iconResId) {
        this.title = title;
        this.shortDescription = shortDescription;
        this.fullDescription = fullDescription;
        this.accentColorHex = accentColorHex;
        this.iconResId = iconResId;
    }

    public String getTitle() {
        return title;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getFullDescription() {
        return fullDescription;
    }

    public String getAccentColorHex() {
        return accentColorHex;
    }

    public int getIconResId() {
        return iconResId;
    }
}

