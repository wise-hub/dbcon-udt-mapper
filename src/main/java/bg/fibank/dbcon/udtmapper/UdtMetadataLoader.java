package bg.fibank.dbcon.udtmapper;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

@Component
public class UdtMetadataLoader {

    private final JdbcTemplate jdbcTemplate;

    public UdtMetadataLoader(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Fetches metadata for a UDT type from Oracle's ALL_TYPE_ATTRS and ALL_COLL_TYPES views.
     *
     * @param udtName The fully qualified name of the UDT.
     * @return A list of AttributeMetadata describing each attribute of the UDT.
     */
    public List<AttributeMetadata> fetchUdtMetadata(String udtName) {
        String query = """
            SELECT 
                ATTR_NAME,
                ATTR_NO - 1 AS ATTR_INDEX,
                ATTR_TYPE_NAME,
                ATTR_TYPE_OWNER,
                (SELECT COUNT(*) 
                 FROM ALL_COLL_TYPES 
                 WHERE TYPE_NAME = ATTR_TYPE_NAME 
                   AND OWNER = ATTR_TYPE_OWNER) AS IS_COLLECTION
            FROM ALL_TYPE_ATTRS
            WHERE TYPE_NAME = ?
              AND OWNER = ?
            ORDER BY ATTR_NO
        """;

        return jdbcTemplate.execute((Connection conn) -> {
            try (var pstmt = conn.prepareStatement(query)) {
                String[] parts = udtName.split("\\.");
                String typeName = parts[parts.length - 1];
                String owner = (parts.length > 1) ? parts[0] : "CURRENT_SCHEMA";

                pstmt.setString(1, typeName.toUpperCase());
                pstmt.setString(2, owner.toUpperCase());

                try (var rs = pstmt.executeQuery()) {
                    List<AttributeMetadata> metadataList = new ArrayList<>();
                    while (rs.next()) {
                        metadataList.add(new AttributeMetadata(
                                rs.getString("ATTR_NAME"),
                                rs.getString("ATTR_TYPE_NAME"),
                                rs.getInt("IS_COLLECTION") > 0
                        ));
                    }
                    return metadataList;
                }
            }
        });
    }
}
