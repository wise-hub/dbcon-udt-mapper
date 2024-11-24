package bg.fibank.dbcon.udtmapper;

import org.springframework.stereotype.Component;

import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Struct;
import java.util.*;

@Component
public class UdtMapper {

    private final MetadataCache metadataCache;

    public UdtMapper(MetadataCache metadataCache) {
        this.metadataCache = metadataCache;
    }

    /**
     * Fetches and maps a UDT (STRUCT) to a Map<String, Object>.
     * Handles retries if the datatype changes (e.g., invalid datatype errors).
     *
     * @param udtName      The fully qualified UDT type name.
     * @param callableStmt A CallableStatement to execute the procedure that returns the UDT.
     * @return A Map<String, Object> containing the mapped UDT attributes and values.
     */
    public Map<String, Object> fetchAndMapUdt(String udtName, CallableStatement callableStmt) {
        try {
            return executeAndMapUdt(udtName, callableStmt);
        } catch (Exception e) {
            if (isInvalidDatatypeError(e)) {
                System.err.println("Detected invalid datatype for UDT: " + udtName + ". Reloading metadata...");
                metadataCache.forceRefresh(udtName); // Refresh metadata on invalid datatype error
                try {
                    return executeAndMapUdt(udtName, callableStmt); // Retry after refresh
                } catch (Exception retryException) {
                    throw new RuntimeException("Failed to fetch and map UDT after retries: " + udtName, retryException);
                }
            } else {
                throw new RuntimeException("Error while fetching or mapping UDT: " + udtName, e);
            }
        }
    }

    private Map<String, Object> executeAndMapUdt(String udtName, CallableStatement callableStmt) throws SQLException {
        callableStmt.execute();
        Struct udtStruct = (Struct) callableStmt.getObject(1);
        return mapUdtToMap(udtStruct);
    }

    public Map<String, Object> mapUdtToMap(Struct udtStruct) throws SQLException {
        if (udtStruct == null) {
            return Collections.emptyMap();
        }

        String udtName = udtStruct.getSQLTypeName();
        Object[] attributes = udtStruct.getAttributes();
        List<AttributeMetadata> metadata = metadataCache.getMetadata(udtName);

        if (metadata.size() != attributes.length) {
            throw new IllegalStateException("Mismatch between metadata and attributes for UDT: " + udtName);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < attributes.length; i++) {
            AttributeMetadata meta = metadata.get(i);
            Object value = attributes[i];

            if (value instanceof Struct structValue) {
                value = mapUdtToMap(structValue); // Recursively map nested UDTs
            } else if (value instanceof Array arrayValue) {
                value = processArray(arrayValue); // Handle arrays
            }
            result.put(meta.name().toLowerCase(), value);
        }
        return result;
    }

    private List<Object> processArray(Array array) throws SQLException {
        if (array == null) {
            return null;
        }

        Object[] arrayElements = (Object[]) array.getArray();
        List<Object> result = new ArrayList<>(arrayElements.length);
        for (Object element : arrayElements) {
            if (element instanceof Struct structElement) {
                result.add(mapUdtToMap(structElement));
            } else {
                result.add(element);
            }
        }
        return result;
    }

    private boolean isInvalidDatatypeError(Exception e) {
        Throwable cause = e;
        while (cause != null) {
            if (cause.getMessage() != null && cause.getMessage().contains("invalid datatype")) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
