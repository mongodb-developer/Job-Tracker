@file:OptIn(ExperimentalMaterial3Api::class)

package com.mongodb.jobtracker.android.screen.home

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mongodb.jobtracker.android.R
import com.mongodb.jobtracker.android.screen.profile.ProfileScreen

class HomeScreen : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TopBar()
        }
    }


    @Preview
    @Composable
    fun TopBar() {
        val context = LocalContext.current

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = stringResource(id = R.string.app_name),
                            fontSize = 24.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    },
                    colors = TopAppBarDefaults.smallTopAppBarColors(
                        containerColor = Color(0xFF3700B3),
                        titleContentColor = Color.White
                    ),
                    actions = {
                        IconButton(onClick = {
                            startActivity(Intent(context, ProfileScreen::class.java))
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.AccountCircle,
                                contentDescription = "Localized description",
                                tint = Color.White
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { /* doSomething() */ }) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "Localized description",
                                tint = androidx.compose.ui.graphics.Color.White
                            )
                        }
                    },
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = {

                }) {
                    Icon(Icons.Filled.Add, "")
                }
            },
            floatingActionButtonPosition = FabPosition.End
        ) {
            Container(it.calculateTopPadding())
        }

    }

    @Preview
    @Composable
    fun ContainerPreview() {
        Container(10.dp)
    }

    @Composable
    fun Container(topPadding: Dp) {

        val selectedTab = remember { mutableStateOf(0) }
        val homeVM = viewModel<HomeViewModel>()

        val unassignedJobs = homeVM.unassignedJobs.observeAsState(emptyList())
        val doneJobs = homeVM.doneJobs.observeAsState(emptyList())
        val assignedJobs = homeVM.assignedJobs.observeAsState(emptyList())

        val selectedJobs = when (selectedTab.value) {
            0 -> assignedJobs
            1 -> unassignedJobs
            else -> doneJobs
        }


        Column(modifier = Modifier.padding(top = topPadding)) {

            TabRow(
                selectedTabIndex = selectedTab.value,
                tabs = {
                    Tab(
                        selected = false,
                        onClick = { selectedTab.value = 0 },
                        text = { Text(text = "Assigned") })


                    Tab(
                        selected = false,
                        onClick = { selectedTab.value = 1 },
                        text = { Text(text = "Unassigned") })

                    Tab(
                        selected = false,
                        onClick = { selectedTab.value = 2 },
                        text = { Text(text = "Done") })
                }
            )

            LazyColumn() {
                items(count = selectedJobs.value.size) { itemPosition ->

                }
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun ListRow() {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("1")
                Text(text = "22-10-2022")
            }

            Text("This is job is about nothing, so enjoy", maxLines = 2)

        }
    }
}