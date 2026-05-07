package bely.pinguiprofessions.models;

public enum Profession {
    NONE("Ninguna"),
    BLACKSMITH("Herrero"),
    DOCTOR("Doctor"),
    ALCHEMIST("Alquimista"),
    BARKEEP("Tabernero"),
    KNIGHT("Caballero"),
    MERCHANT("Comerciante"),
    THIEF("Ladrón"),
    INVESTIGATOR("Comisario");

    private final String displayName;

    Profession(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
