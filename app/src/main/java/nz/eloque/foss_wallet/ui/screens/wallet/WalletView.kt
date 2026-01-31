package nz.eloque.foss_wallet.ui.screens.wallet

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateSet
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.flow.map
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.LocalizedPassWithTags
import nz.eloque.foss_wallet.model.PassType
import nz.eloque.foss_wallet.model.SortOption
import nz.eloque.foss_wallet.model.SortOptionSaver
import nz.eloque.foss_wallet.model.Tag
import nz.eloque.foss_wallet.ui.card.ShortPassCard
import nz.eloque.foss_wallet.ui.components.GroupCard
import nz.eloque.foss_wallet.ui.components.SwipeToDismiss
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import java.time.Instant
import java.util.Comparator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletView(
    navController: NavController,
    passViewModel: PassViewModel,
    modifier: Modifier = Modifier,
    emptyIcon: ImageVector = Icons.Default.Wallet,
    archive: Boolean = false,
    listState: LazyListState = rememberLazyListState(),
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
    selectedPasses: SnapshotStateSet<LocalizedPassWithTags>,
) {
    val emptyState = rememberLazyListState()
    val passFlow = passViewModel.filteredPasses
    val passes: List<LocalizedPassWithTags> by remember(passFlow) { passFlow }.map { passes -> passes.filter { archive == it.pass.archived } }.collectAsState(listOf())

    val tagFlow = passViewModel.allTags
    val tags by tagFlow.collectAsState(setOf())

    val passTypesToShow = remember { PassType.all().toMutableStateList() }

    val sortOption = rememberSaveable(stateSaver = SortOptionSaver) { mutableStateOf(SortOption.TimeAdded) }

    val manualOrder by passViewModel.manualOrder.collectAsState()

    val tagToFilterFor = remember { mutableStateOf<Tag?>(null) }

    val manualOrdering = remember(passes, manualOrder) {
        applyManualOrder(passes, manualOrder)
    }

    val filteredPasses = manualOrdering.ordered
        .filter { localizedPass -> passTypesToShow.any { localizedPass.pass.type.isSameType(it) } }
        .filter { localizedPass -> tagToFilterFor.value == null || localizedPass.tags.contains(tagToFilterFor.value) }

    val orderedPasses = if (sortOption.value == SortOption.Manual) {
        filteredPasses
    } else {
        filteredPasses.sortedWith(sortOption.value.comparator)
    }

    if (sortOption.value == SortOption.Manual && manualOrdering.normalizedOrder != manualOrder) {
        LaunchedEffect(manualOrdering.normalizedOrder) {
            passViewModel.setManualOrder(manualOrdering.normalizedOrder)
        }
    }

    val groupedPasses = orderedPasses
        .groupBy { it.pass.groupId }.toList()

    if (groupedPasses.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                imageVector = emptyIcon,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.secondary),
                contentDescription = stringResource(R.string.wallet),
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth(0.5f),
                alpha = 0.25f
            )
        }
    }

    val baseListState = if (passes.isEmpty()) emptyState else listState

    val groups = groupedPasses.filter { it.first != null }
    val ungrouped = groupedPasses.filter { it.first == null }.flatMap { it.second }

    val reorderableState = if (sortOption.value == SortOption.Manual) {
        rememberReorderableLazyListState(
            onMove = { from, to ->
                val fromId = ungrouped.getOrNull(from.index)?.pass?.id ?: return@rememberReorderableLazyListState
                val toId = ungrouped.getOrNull(to.index)?.pass?.id ?: return@rememberReorderableLazyListState

                val updatedOrder = manualOrdering.normalizedOrder.toMutableList()
                val fromPosition = updatedOrder.indexOf(fromId)
                if (fromPosition == -1) return@rememberReorderableLazyListState

                updatedOrder.removeAt(fromPosition)
                val desiredIndex = updatedOrder.indexOf(toId).takeIf { it >= 0 } ?: updatedOrder.size
                val insertAt = if (from.index < to.index) desiredIndex + 1 else desiredIndex
                updatedOrder.add(insertAt.coerceIn(0, updatedOrder.size), fromId)
                passViewModel.setManualOrder(updatedOrder)
            },
            listState = baseListState
        )
    } else {
        null
    }

    LazyColumn(
        state = reorderableState?.listState ?: baseListState,
        verticalArrangement = Arrangement
            .spacedBy(8.dp),
        contentPadding = WindowInsets.navigationBars.asPaddingValues(),
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .let { columnModifier ->
                if (reorderableState != null) {
                    columnModifier.reorderable(reorderableState)
                } else columnModifier
            }
    ) {

        item {
            FilterBlock(
                passViewModel = passViewModel,
                sortOption = sortOption,
                passTypesToShow = passTypesToShow,
                tags = tags,
                tagToFilterFor = tagToFilterFor
            )
        }

        items(groups) { (groupId, passes) ->
            GroupCard(
                groupId = groupId!!,
                passes = passes,
                allTags = tags,
                onClick = {
                    navController.navigate("pass/${it.id}")
                },
                passViewModel = passViewModel,
                selectedPasses = selectedPasses
            )
        }
        if (reorderableState != null) {
            items(ungrouped, key = { it.pass.id }) { pass ->
                ReorderableItem(reorderableState, key = pass.pass.id) { _ ->
                    SwipeToDismiss(
                        leftSwipeIcon = Icons.Default.SelectAll,
                        allowRightSwipe = false,
                        onLeftSwipe = { if (selectedPasses.contains(pass)) selectedPasses.remove(pass) else selectedPasses.add(pass) },
                        onRightSwipe = { },
                        modifier = Modifier
                            .padding(2.dp)
                            .detectReorderAfterLongPress(reorderableState)
                    ) {
                        ShortPassCard(
                            pass = pass,
                            allTags = tags,
                            onClick = {
                                navController.navigate("pass/${pass.pass.id}")
                            },
                            selected = selectedPasses.contains(pass),
                            barcodePosition = passViewModel.barcodePosition(),
                            increaseBrightness = passViewModel.increasePassViewBrightness()
                        )
                    }
                }
            }
        } else {
            items(ungrouped) { pass ->
                SwipeToDismiss(
                    leftSwipeIcon = Icons.Default.SelectAll,
                    allowRightSwipe = false,
                    onLeftSwipe = { if (selectedPasses.contains(pass)) selectedPasses.remove(pass) else selectedPasses.add(pass) },
                    onRightSwipe = { },
                    modifier = Modifier.padding(2.dp)
                ) {
                    ShortPassCard(
                        pass = pass,
                        allTags = tags,
                        onClick = {
                            navController.navigate("pass/${pass.pass.id}")
                        },
                        selected = selectedPasses.contains(pass),
                        barcodePosition = passViewModel.barcodePosition(),
                        increaseBrightness = passViewModel.increasePassViewBrightness()
                    )
                }
            }
        }
        item {
            Spacer(modifier = Modifier.padding(4.dp))
        }
    }
}

private data class ManualOrderingResult(
    val ordered: List<LocalizedPassWithTags>,
    val normalizedOrder: List<String>
)

private fun applyManualOrder(
    passes: List<LocalizedPassWithTags>,
    storedOrder: List<String>,
): ManualOrderingResult {
    val passById = passes.associateBy { it.pass.id }
    val existing = storedOrder.distinct().mapNotNull { passById[it] }
    val missing = passes.filterNot { storedOrder.contains(it.pass.id) }
        .sortedWith(
            Comparator.comparing<LocalizedPassWithTags, Instant?>(
                { it.pass.addedAt },
                Comparator.reverseOrder()
            )
        )

    val orderedPasses = missing + existing
    val normalizedOrder = orderedPasses.map { it.pass.id }
    return ManualOrderingResult(orderedPasses, normalizedOrder)
}