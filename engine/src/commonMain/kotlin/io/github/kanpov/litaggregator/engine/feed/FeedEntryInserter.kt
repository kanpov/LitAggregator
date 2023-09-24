package io.github.kanpov.litaggregator.engine.feed

class FeedEntryInserter(val feed: Feed, val exitOnHit: Boolean) {
    inline fun <reified E : FeedEntry> insert(entry: E): Boolean {
        var comparisonResult: FeedEntryComparisonResult? = null
        var oldEntry: E? = null
        var wasHit = true

        // first scan
        feed.withPool<E> { pool ->
            for (otherEntry in pool) {
                val currentResult = FeedEntry.compare(entry, otherEntry)

                if (currentResult == FeedEntryComparisonResult.Duplicates) continue

                comparisonResult = currentResult
                oldEntry = otherEntry
                return@withPool
            }
        }

        println(comparisonResult)

        // apply necessary changes
        feed.withPool<E> { pool ->
            if (pool.contains(entry)) {
                return true
            }

            pool += when (comparisonResult) {
                null -> { // new
                    wasHit = false
                    entry
                }
                FeedEntryComparisonResult.Different -> {
                    wasHit = false
                    entry
                }
                FeedEntryComparisonResult.OldIsOutdated -> {
                    pool.remove(oldEntry)
                    entry
                }
                FeedEntryComparisonResult.NeedMerge -> {
                    entry.metadata.merge(oldEntry!!.metadata)
                    pool.remove(oldEntry)
                    entry
                }
                else -> throw IllegalArgumentException() // can never happen
            }
        }

        return if (exitOnHit) wasHit else false
    }
}