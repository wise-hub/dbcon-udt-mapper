package bg.fibank.dbcon.udtmapper;

public record AttributeMetadata(String name, String typeName, boolean isCollection) {

    public boolean isNestedUdt() {
        return !isCollection && !typeName.equalsIgnoreCase("NUMBER")
                && !typeName.equalsIgnoreCase("VARCHAR2")
                && !typeName.equalsIgnoreCase("DATE");
    }
}
