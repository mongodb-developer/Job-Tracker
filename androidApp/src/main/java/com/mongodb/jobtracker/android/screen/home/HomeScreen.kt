@file:OptIn(ExperimentalMaterial3Api::class)

package com.mongodb.jobtracker.android.screen.home

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.Settings
import android.text.format.DateUtils
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mongodb.jobtracker.Job
import com.mongodb.jobtracker.Status
import com.mongodb.jobtracker.android.R
import com.mongodb.jobtracker.android.screen.profile.ProfileScreen
import com.mongodb.jobtracker.displayDate
import io.realm.kotlin.types.ObjectId
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
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
        val homeVM = viewModel<HomeViewModel>()
        val onNewJob = homeVM.newJobAlert.observeAsState(false)

        Scaffold(topBar = {
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
                            tint = Color.White
                        )
                    }
                },
            )
        }) {
            Container(topPadding = it.calculateTopPadding(), homeVM = homeVM)
            if (onNewJob.value) {
                val player: MediaPlayer =
                    MediaPlayer.create(this, Settings.System.DEFAULT_NOTIFICATION_URI)
                player.start()

                Toast.makeText(LocalContext.current, "New Job is available", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    @Composable
    fun Container(topPadding: Dp, homeVM: HomeViewModel) {

        val selectedTab = remember { mutableStateOf(0) }

        val unassignedJobs = homeVM.unassignedJobs.observeAsState(emptyList())
        val doneJobs = homeVM.doneJobs.observeAsState(emptyList())
        val assignedJobs = homeVM.assignedJobs.observeAsState(emptyList())
        val locationList = homeVM.getLocations.observeAsState(emptyList())

        val currentJobList = when (selectedTab.value) {
            0 -> unassignedJobs
            1 -> assignedJobs
            else -> doneJobs
        }

        val onJobStatusChange = { jobId: ObjectId, status: Status ->
            homeVM.updateJobStatus(jobId, status)
        }

        val dropDownExpanded = remember { mutableStateOf(false) }
        val selectionLocation = remember {
            mutableStateOf("")
        }
        val localSearchText = homeVM.searchKeyword.collectAsState("")


        Column(modifier = Modifier.padding(top = topPadding)) {

            ExposedDropdownMenuBox(
                expanded = dropDownExpanded.value,
                onExpandedChange = { dropDownExpanded.value = !dropDownExpanded.value },
                modifier = Modifier.fillMaxWidth()
            ) {

                OutlinedTextField(
                    value = selectionLocation.value,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(text = "Selection Location") },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropDownExpanded.value)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .padding(8.dp)
                )

                ExposedDropdownMenu(
                    expanded = dropDownExpanded.value,
                    onDismissRequest = { dropDownExpanded.value = false }) {
                    locationList.value.forEachIndexed { index, location ->
                        DropdownMenuItem(
                            text = { Text(text = location.name) },
                            onClick = {
                                dropDownExpanded.value = false
                                selectionLocation.value = location.name
                                if (index == 0) {
                                    homeVM.onLocationUpdate(null)
                                } else {
                                    homeVM.onLocationUpdate(location)
                                }
                            })
                    }


                }
            }

            TabRow(
                selectedTabIndex = selectedTab.value,
                tabs = {
                    Tab(
                        selected = true,
                        onClick = { selectedTab.value = 0 },
                        text = { Text(text = "Unassigned") })

                    Tab(
                        selected = true,
                        onClick = { selectedTab.value = 1 },
                        text = { Text(text = "Assigned") })

                    Tab(
                        selected = true,
                        onClick = { selectedTab.value = 2 },
                        text = { Text(text = "Done") })
                }
            )

            LazyColumn {

                item {
                    OutlinedTextField(
                        value = localSearchText.value,
                        onValueChange = {
                            homeVM.onSearchUpdate(it)
                        },
                        placeholder = {
                            Text(text = "Enter your search keyword")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                }

                items(count = currentJobList.value.size) { position ->
                    ListRow(currentJobList.value[position], onJobStatusChange)
                }
            }
        }
    }

    @Composable
    fun ListRow(job: Job, onJobStatusChange: (id: ObjectId, status: Status) -> Unit) {
        val maxLines = remember { mutableStateOf(2) }
        val elipseLabel = if (maxLines.value == Int.MAX_VALUE) "Less" else "More"


        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = (job._id.toString()).takeLast(5))

                val displayDate = DateUtils.getRelativeTimeSpanString(
                    job.displayDate(),
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE
                )
                Text(text = displayDate.toString())
            }


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    job.desc,
                    maxLines = maxLines.value,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(0.8f, true),
                )

                Text(text = elipseLabel,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable {
                            maxLines.value =
                                if (maxLines.value == Int.MAX_VALUE) 2 else Int.MAX_VALUE
                        }
                        .fillMaxHeight()
                        .align(Alignment.Bottom))
            }

            if (maxLines.value == Int.MAX_VALUE) {
                when (job.status) {
                    Status.UNASSIGNED.name -> {
                        Button(
                            onClick = {
                                onJobStatusChange(job._id, Status.ACCEPTED)
                                maxLines.value = 2
                            },
                            content = {
                                Text(text = "Assign")
                            }
                        )
                    }

                    Status.ACCEPTED.name -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(onClick = { onJobStatusChange(job._id, Status.DONE) }) {
                                Text(text = "Done")
                            }

                            Button(onClick = { onJobStatusChange(job._id, Status.UNASSIGNED) }) {
                                Text(text = "Cancel")
                            }
                        }
                    }

                    else -> {}
                }
            }
        }
    }
}