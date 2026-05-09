package vegabobo.languageselector.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.InputField
import top.yukonga.miuix.kmp.basic.SearchBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.theme.MiuixTheme
import vegabobo.languageselector.ui.screen.main.AppInfo
import vegabobo.languageselector.ui.screen.main.AppLabels

@Composable
fun AppSearchBar(
    modifier: Modifier = Modifier,
    placeholder: String = "",
    query: String,
    onUpdatedValue: (String) -> Unit,
    apps: List<AppInfo> = emptyList(),
    history: List<AppInfo> = emptyList(),
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    selectedLabels: Set<AppLabels>,
    onSelectedLabelsChange: (AppLabels) -> Unit,
    onClickApp: (AppInfo) -> Unit,
    onClickClear: () -> Unit,
    actions: @Composable RowScope.() -> Unit,
) {
    Box(
        modifier = Modifier
            .semantics { isTraversalGroup = true }
            .then(modifier)
    ) {
        SearchBar(
            inputField = {
                InputField(
                    query = query,
                    onQueryChange = { onUpdatedValue(it) },
                    onSearch = { onExpandedChange(false) },
                    expanded = isExpanded,
                    onExpandedChange = { onExpandedChange(it) },
                    label = placeholder
                )
            },
            outsideEndAction = {
                if (isExpanded) {
                    TextButton(
                        text = "Cancel",
                        onClick = {
                            onExpandedChange(false)
                            onUpdatedValue("")
                        }
                    )
                }
            },
            expanded = isExpanded,
            onExpandedChange = { onExpandedChange(it) },
        ) {
            LazyColumn {
                if (query.isNotBlank()) {
                    item {
                        Row(
                            modifier = Modifier
                                .padding(
                                    start = 23.dp,
                                    top = 8.dp,
                                    bottom = 8.dp,
                                    end = 8.dp
                                )
                                .horizontalScroll(rememberScrollState())
                        ) {
                            FilterLabel(
                                title = "Show System",
                                onClick = {
                                    onSelectedLabelsChange(AppLabels.SYSTEM_APP)
                                },
                                isSelected = selectedLabels.contains(AppLabels.SYSTEM_APP)
                            )
                            Spacer(Modifier.padding(8.dp))
                            FilterLabel(
                                title = "Show Modified",
                                onClick = { onSelectedLabelsChange(AppLabels.MODIFIED) },
                                isSelected = selectedLabels.contains(AppLabels.MODIFIED)
                            )
                        }
                    }

                    items(
                        items = apps,
                        key = { it.pkg }
                    ) { app ->
                        AppListItem(
                            modifier = Modifier.padding(
                                start = 23.dp,
                                end = 23.dp,
                                top = 4.dp,
                                bottom = 4.dp
                            ),
                            app = app,
                            onClickApp = { onClickApp(app) }
                        )
                    }
                } else if (history.isNotEmpty()) {
                    item {
                        Row(
                            Modifier.padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "History".uppercase(),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MiuixTheme.colorScheme.onSurfaceSecondary,
                                modifier = Modifier
                                    .padding(start = 18.dp)
                                    .padding(bottom = 8.dp)
                                    .padding(top = 8.dp)
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            TextButton(text = "Clear", onClick = { onClickClear() })
                            Spacer(modifier = Modifier.padding(6.dp))
                        }
                    }
                    items(
                        items = history,
                        key = { it.pkg }
                    ) { app ->
                        AppListItem(
                            modifier = Modifier.padding(
                                start = 23.dp,
                                end = 23.dp,
                                top = 4.dp,
                                bottom = 4.dp
                            ),
                            app = app,
                            onClickApp = { onClickApp(app) }
                        )
                    }
                    item {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                } else {
                    item {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                                .alpha(0.4f),
                            text = "Type something to search",
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        if (!isExpanded) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = actions
            )
        }
    }

    if (query.isNotBlank())
        BackHandler {
            onUpdatedValue("")
        }
}
