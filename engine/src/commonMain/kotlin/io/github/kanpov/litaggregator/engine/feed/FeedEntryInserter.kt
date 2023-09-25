package io.github.kanpov.litaggregator.engine.feed

class FeedEntryInserter(val feed: Feed, val exitOnHit: Boolean) {
    inline fun <reified E : FeedEntry> insert(entry: E): Boolean {
        // Check if there are any entries with the same source fingerprint as the given entry.
        // Because of the way this algorithm works, it is guaranteed that only 0 or 1 matches will ever
        // be present in the pool
        var matchingEntry: E? = null
        feed.withPool<E> { pool ->
            matchingEntry = pool.firstOrNull { it.sourceFingerprint == entry.sourceFingerprint }
        }

        // If the source fingerprint is unique (new), simply insert the entry into the pool
        if (matchingEntry == null) {
            feed.withPool<E> { pool ->
                pool.add(entry)
            }
            return false
        }

        // If the metadata fingerprints of the matching and new entry are different, a merge is needed
        if (matchingEntry!!.metadataFingerprint != entry.metadataFingerprint) {
            matchingEntry!!.metadata.merge(entry.metadata)
            return false
        }

        // If the content fingerprints of the matching and new entry are different, the matching entry needs to be
        // swapped out for the new entry
        if (matchingEntry!!.contentFingerprint != entry.contentFingerprint) {
            feed.withPool<E> { pool ->
                pool.remove(matchingEntry)
                pool.add(entry)
            }
            return false
        }

        // If all fingerprints are equal, both entries can only be equal, so the matching entry should be kept
        return exitOnHit
    }
}