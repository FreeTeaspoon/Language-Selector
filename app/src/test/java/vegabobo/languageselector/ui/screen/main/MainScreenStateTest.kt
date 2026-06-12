package vegabobo.languageselector.ui.screen.main

import org.junit.Assert.assertEquals
import org.junit.Test

class MainScreenStateTest {
    @Test
    fun openRequested_movesCollapsedSearchToExpanding() {
        assertEquals(
            SearchPhase.Expanding,
            AppSearchState().openRequested().phase
        )
    }

    @Test
    fun animationFinished_movesExpandingToExpanded() {
        assertEquals(
            SearchPhase.Expanded,
            AppSearchState(phase = SearchPhase.Expanding).animationFinished().phase
        )
    }

    @Test
    fun backClearsQueryBeforeCollapsing() {
        val next = AppSearchState(
            phase = SearchPhase.Expanded,
            query = "settings",
            resultState = SearchResultState.Results
        ).closeRequested()

        assertEquals(SearchPhase.Expanded, next.phase)
        assertEquals("", next.query)
        assertEquals(SearchResultState.Default, next.resultState)
    }

    @Test
    fun backCollapsesExpandedSearchWhenQueryIsEmpty() {
        assertEquals(
            SearchPhase.Collapsing,
            AppSearchState(phase = SearchPhase.Expanded).closeRequested().phase
        )
    }

    @Test
    fun cancelClearsAndCollapsesSearch() {
        val next = AppSearchState(
            phase = SearchPhase.Expanded,
            query = "browser",
            resultState = SearchResultState.Results
        ).cancelRequested()

        assertEquals(SearchPhase.Collapsing, next.phase)
        assertEquals("", next.query)
        assertEquals(SearchResultState.Default, next.resultState)
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
