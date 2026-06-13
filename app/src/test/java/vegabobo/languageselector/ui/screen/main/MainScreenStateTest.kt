package vegabobo.languageselector.ui.screen.main

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class MainScreenStateTest {
    @Test
    fun openRequested_movesCollapsedSearchToExpanding() {
        val next = AppSearchStatus(offsetY = 32.dp).openRequested()

        assertEquals(SearchPhase.Expanding, next.current)
        assertEquals(32.dp, next.offsetY)
    }

    @Test
    fun animationFinished_movesExpandingToExpanded() {
        assertEquals(
            SearchPhase.Expanded,
            AppSearchStatus(current = SearchPhase.Expanding).animationFinished().current
        )
    }

    @Test
    fun animationFinished_movesCollapsingToCollapsedAndPreservesOffset() {
        val next = AppSearchStatus(
            current = SearchPhase.Collapsing,
            offsetY = 24.dp,
            searchText = "settings",
            resultStatus = SearchResultState.Results
        ).animationFinished()

        assertEquals(SearchPhase.Collapsed, next.current)
        assertEquals(24.dp, next.offsetY)
        assertEquals("", next.searchText)
        assertEquals(SearchResultState.Default, next.resultStatus)
    }

    @Test
    fun measuredOffset_updatesOnlyWhenCollapsed() {
        val collapsed = AppSearchStatus().withMeasuredOffset(18.dp)
        val expanded = AppSearchStatus(
            current = SearchPhase.Expanded,
            offsetY = 18.dp
        ).withMeasuredOffset(42.dp)

        assertEquals(18.dp, collapsed.offsetY)
        assertEquals(18.dp, expanded.offsetY)
    }

    @Test
    fun backClearsQueryBeforeCollapsing() {
        val next = AppSearchStatus(
            current = SearchPhase.Expanded,
            searchText = "settings",
            resultStatus = SearchResultState.Results
        ).closeRequested()

        assertEquals(SearchPhase.Expanded, next.current)
        assertEquals("", next.searchText)
        assertEquals(SearchResultState.Default, next.resultStatus)
    }

    @Test
    fun backCollapsesExpandedSearchWhenQueryIsEmpty() {
        assertEquals(
            SearchPhase.Collapsing,
            AppSearchStatus(current = SearchPhase.Expanded).closeRequested().current
        )
    }

    @Test
    fun cancelClearsAndCollapsesSearch() {
        val next = AppSearchStatus(
            current = SearchPhase.Expanded,
            searchText = "browser",
            resultStatus = SearchResultState.Results
        ).cancelRequested()

        assertEquals(SearchPhase.Collapsing, next.current)
        assertEquals("", next.searchText)
        assertEquals(SearchResultState.Default, next.resultStatus)
    }

    @Test
    fun searchResultStateUsesDefaultForBlankQuery() {
        assertEquals(SearchResultState.Default, searchResultStateFor("", hasResults = false))
    }

    @Test
    fun searchResultStateDistinguishesEmptyAndResults() {
        assertEquals(SearchResultState.Empty, searchResultStateFor("abc", hasResults = false))
        assertEquals(SearchResultState.Results, searchResultStateFor("abc", hasResults = true))
    }
}
