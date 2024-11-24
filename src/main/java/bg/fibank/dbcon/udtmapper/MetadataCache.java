package bg.fibank.dbcon.udtmapper;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MetadataCache {

    private final UdtMetadataLoader metadataLoader;
    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public MetadataCache(UdtMetadataLoader metadataLoader) {
        this.metadataLoader = metadataLoader;
    }

    /**
     * Retrieves metadata from the cache or loads it from the database if not present or expired.
     *
     * @param udtName The fully qualified name of the UDT.
     * @return A list of AttributeMetadata for the given UDT.
     */
    public List<AttributeMetadata> getMetadata(String udtName) {
        return cache.compute(udtName, (key, oldEntry) -> {
            if (oldEntry == null) {
                return loadMetadata(udtName);
            }
            return oldEntry;
        }).getMetadata();
    }

    /**
     * Force refreshes the metadata for a specific UDT by reloading it from the database.
     *
     * @param udtName The fully qualified name of the UDT.
     */
    public void forceRefresh(String udtName) {
        cache.put(udtName, loadMetadata(udtName));
    }

    private CacheEntry loadMetadata(String udtName) {
        List<AttributeMetadata> metadata = metadataLoader.fetchUdtMetadata(udtName);
        return new CacheEntry(metadata, System.currentTimeMillis());
    }

    /**
     * Cache entry that includes metadata and a timestamp for expiry validation.
     */
    private static class CacheEntry {
        private final List<AttributeMetadata> metadata;
        private final long timestamp;

        public CacheEntry(List<AttributeMetadata> metadata, long timestamp) {
            this.metadata = metadata;
            this.timestamp = timestamp;
        }

        public List<AttributeMetadata> getMetadata() {
            return metadata;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}
