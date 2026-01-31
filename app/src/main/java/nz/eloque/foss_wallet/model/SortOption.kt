package nz.eloque.foss_wallet.model

import androidx.annotation.StringRes
import androidx.compose.runtime.saveable.Saver
import nz.eloque.foss_wallet.R
import java.time.Instant
import java.time.ZonedDateTime

const val TIME_ADDED = "TimeAdded"
const val RELEVANT_DATE_NEWEST = "RelevantDateNewest"
const val RELEVANT_DATE_OLDEST = "RelevantDateOldest"
const val NAME = "Name"
const val CREATED_AT = "CreatedAt"
const val MANUAL = "Manual"

private val timeAdded = Comparator.comparing<LocalizedPassWithTags, Instant?>(
    { it.pass.addedAt },
    Comparator.reverseOrder()
)

private val newestFirst = Comparator.comparing<LocalizedPassWithTags, ZonedDateTime?>(
    { it.pass.relevantDates.firstOrNull()?.startDate() },
    Comparator.nullsLast(Comparator.reverseOrder())
)

private val oldestFirst = Comparator.comparing<LocalizedPassWithTags, ZonedDateTime?>(
    { it.pass.relevantDates.firstOrNull()?.startDate() },
    Comparator.nullsLast(Comparator.naturalOrder())
)

private val nameAscending = Comparator.comparing<LocalizedPassWithTags, String>(
    { it.pass.description.lowercase() },
    Comparator.naturalOrder()
)

private val createdOldestFirst = Comparator.comparing<LocalizedPassWithTags, Instant?>(
    { it.pass.addedAt },
    Comparator.naturalOrder()
)

sealed class SortOption(val name: String, @param:StringRes val l18n: Int, val comparator: Comparator<LocalizedPassWithTags>) {
    object TimeAdded : SortOption(TIME_ADDED, R.string.date_added, timeAdded)
    object RelevantDateNewest : SortOption(RELEVANT_DATE_NEWEST, R.string.relevant_date_newest, newestFirst)
    object RelevantDateOldest : SortOption(RELEVANT_DATE_OLDEST, R.string.relevant_date_oldest, oldestFirst)
    object Name : SortOption(NAME, R.string.sort_name, nameAscending)
    object CreatedAt : SortOption(CREATED_AT, R.string.sort_created_at, createdOldestFirst)
    object Manual : SortOption(MANUAL, R.string.sort_manual, timeAdded)

    companion object {
        fun all(): List<SortOption> {
            return listOf(TimeAdded, RelevantDateNewest, RelevantDateOldest, Name, CreatedAt, Manual)
        }
    }
}

val SortOptionSaver: Saver<SortOption, String> = Saver(
    save = { it.name },
    restore = { SortOption.all().find { option -> option.name == it } }
)
