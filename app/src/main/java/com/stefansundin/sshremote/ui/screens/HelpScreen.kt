/*
 * SSH Remote
 * Copyright (C) 2026  Stefan Sundin
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.stefansundin.sshremote.ui.screens

import android.content.res.Configuration
import android.view.SoundEffectConstants
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.stefansundin.sshremote.R
import com.stefansundin.sshremote.ui.components.MarkdownText
import com.stefansundin.sshremote.ui.theme.SSHRemoteTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    onNavigateUp: () -> Unit,
) {
    val view = LocalView.current
    val resources = LocalResources.current
    val searchFocusRequester = remember { FocusRequester() }
    var isSearchEnabled by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var searchNextRequest by rememberSaveable { mutableIntStateOf(0) }
    var searchCurrentPosition by rememberSaveable { mutableIntStateOf(0) }
    var searchTotalMatches by rememberSaveable { mutableIntStateOf(0) }
    val helpText = remember(resources) {
        resources.openRawResource(R.raw.help)
            .bufferedReader()
            .use { it.readText() }
    }

    LaunchedEffect(isSearchEnabled) {
        if (isSearchEnabled) {
            searchFocusRequester.requestFocus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.help)) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            onNavigateUp()
                        },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            if (isSearchEnabled) {
                                isSearchEnabled = false
                                searchQuery = ""
                                searchCurrentPosition = 0
                                searchTotalMatches = 0
                            } else {
                                isSearchEnabled = true
                            }
                        },
                    ) {
                        Icon(
                            imageVector = if (isSearchEnabled) Icons.Filled.Close else Icons.Filled.Search,
                            contentDescription = if (isSearchEnabled) {
                                stringResource(R.string.close)
                            } else {
                                stringResource(R.string.search)
                            },
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
        ) {
            if (isSearchEnabled) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text(stringResource(R.string.search)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(searchFocusRequester),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            if (searchTotalMatches > 0) {
                                searchNextRequest++
                            }
                        },
                    ),
                    trailingIcon = {
                        IconButton(
                            onClick = { searchNextRequest++ },
                            enabled = searchTotalMatches > 0,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowDown,
                                contentDescription = stringResource(R.string.search_next_match),
                            )
                        }
                    },
                    supportingText = {
                        if (searchQuery.isNotBlank()) {
                            if (searchTotalMatches == 0) {
                                Text(text = stringResource(R.string.search_no_matches))
                            } else {
                                Text(
                                    text = stringResource(
                                        R.string.search_match_position,
                                        searchCurrentPosition,
                                        searchTotalMatches,
                                    ),
                                )
                            }
                        }
                    },
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            MarkdownText(
                markdown = helpText,
                searchQuery = if (isSearchEnabled) searchQuery else "",
                searchNextRequest = if (isSearchEnabled) searchNextRequest else 0,
                onSearchPositionChanged = { current, total ->
                    searchTotalMatches = total
                    searchCurrentPosition = if (total > 0) {
                        current.coerceIn(1, total)
                    } else {
                        0
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, fontScale = 2.0f)
@Composable
private fun HelpScreenPreview() {
    SSHRemoteTheme {
        HelpScreen(onNavigateUp = {})
    }
}
