package io.mo.viaport.ui.home

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.transform.RoundedCornersTransformation
import io.nekohasekai.sfa.R
import io.nekohasekai.sfa.bg.BoxService
import io.nekohasekai.sfa.constant.Status
import io.nekohasekai.sfa.database.ProfileManager
import io.nekohasekai.sfa.database.TypedProfile
import io.mo.viaport.helper.ioScope
import io.mo.viaport.ui.LocalActivity
import io.mo.viaport.ui.LocalAppViewModel
import io.mo.viaport.ui.LocalPrompter
import io.mo.viaport.ui.LocalViewHostState
import io.mo.viaport.ui.Routor
import io.nekohasekai.sfa.ui.profile.EditProfileActivity
import io.nekohasekai.sfa.ui.profile.NewProfileActivity
import io.mo.viaport.ui.theme.RoundedCornerShape8
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.DateFormat

fun Routor.homeId() = "home"
fun NavGraphBuilder.homeScreen(homeViewModel: HomeViewModel) { //
    // XLog.d("userUiState 变化结束 Failure")
    composable(Routor.homeId()) {
        // val viewModel = viewModel { HomeViewModel()}
        HomeScreen(homeViewModel)
    }
}
fun NavController.toHomeScreen(){
    navigate(Routor.homeId())
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(homeViewModel: HomeViewModel) {
    val appViewModel = LocalAppViewModel.current
    val activity = LocalActivity.current
    val initialStartup by homeViewModel.initialStartup.collectAsStateWithLifecycle()

    val profiles by homeViewModel.profiles.collectAsStateWithLifecycle()

    val viewHostState = LocalViewHostState.current
    val prompter = LocalPrompter.current

    var showAddProfileDialog by remember { mutableStateOf(false) }

    if (profiles.isEmpty()){
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
            TextButton(onClick = {
                showAddProfileDialog = true
            }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(id = R.string.profile_add_file))
                Text(text = stringResource(id = R.string.profile_add_file))
            }
        }
    }else{
        val lastSelectedIndex by homeViewModel.lastSelectedIndex.collectAsStateWithLifecycle()
        val trafficAvailable by homeViewModel.trafficAvailable.collectAsStateWithLifecycle()

        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp), contentPadding = PaddingValues(5.dp)) {
            item {
                AnimatedVisibility(
                    visible = initialStartup,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement= Arrangement.SpaceBetween) {
                        ElevatedCard(Modifier.weight(0.5f), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                            Text(text = stringResource(id = R.string.status_status), modifier = Modifier.padding(horizontal = 5.dp, vertical = 4.dp), fontWeight = FontWeight.Bold)
                            val memory by homeViewModel.memory.collectAsStateWithLifecycle()
                            Row(Modifier.padding(horizontal = 5.dp, vertical = 4.dp)) {
                                Text(text = stringResource(id = R.string.status_memory))
                                Spacer(Modifier.weight(0.5f))
                                Text(text = memory ?: stringResource(id = R.string.loading))
                            }
                            val goroutines by homeViewModel.goroutines.collectAsStateWithLifecycle()
                            Row(Modifier.padding(horizontal = 5.dp, vertical = 4.dp)) {
                                Text(text = stringResource(id = R.string.status_goroutines))
                                Spacer(Modifier.weight(0.5f))
                                Text(text = goroutines ?: stringResource(id = R.string.loading))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        ElevatedCard(Modifier.weight(0.5f), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                            Text(text = stringResource(id = R.string.status_connections), modifier = Modifier.padding(horizontal = 5.dp, vertical = 4.dp), fontWeight = FontWeight.Bold)
                            val connectionsIn by homeViewModel.connectionsIn.collectAsStateWithLifecycle()
                            Row(Modifier.padding(horizontal = 5.dp, vertical = 4.dp)) {
                                Text(text = stringResource(id = R.string.status_connections_inbound))
                                Spacer(Modifier.weight(0.5f))
                                Text(text = connectionsIn ?: stringResource(id = R.string.loading))
                            }
                            val connectionsOut by homeViewModel.connectionsOut.collectAsStateWithLifecycle()
                            Row(Modifier.padding(horizontal = 5.dp, vertical = 4.dp)) {
                                Text(text = stringResource(id = R.string.status_connections_outbound))
                                Spacer(Modifier.weight(0.5f))
                                Text(text = connectionsOut ?: stringResource(id = R.string.loading))
                            }
                        }
                    }
                }
            }
            item {
                if (initialStartup){
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
            item {
                AnimatedVisibility(
                    visible = (initialStartup) , //&& trafficAvailable,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement= Arrangement.SpaceBetween) {
                        ElevatedCard(Modifier.weight(0.5f), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                            Text(text = stringResource(id = R.string.status_traffic), modifier = Modifier.padding(horizontal = 5.dp, vertical = 4.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            val uplink by homeViewModel.uplink.collectAsStateWithLifecycle()
                            Row(Modifier.padding(horizontal = 5.dp, vertical = 4.dp)) {
                                Text(text = stringResource(id = R.string.status_uplink))
                                Spacer(Modifier.weight(0.5f))
                                Text(text = uplink ?: stringResource(id = R.string.loading))
                            }
                            val downlink by homeViewModel.downlink.collectAsStateWithLifecycle()
                            Row(Modifier.padding(horizontal = 5.dp, vertical = 4.dp)) {
                                Text(text = stringResource(id = R.string.status_downlink))
                                Spacer(Modifier.weight(0.5f))
                                Text(text = downlink ?: stringResource(id = R.string.loading))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        ElevatedCard(Modifier.weight(0.5f), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                            Text(text = stringResource(id = R.string.status_traffic_total), modifier = Modifier.padding(horizontal = 5.dp, vertical = 4.dp), fontWeight = FontWeight.Bold)
                            Row(Modifier.padding(horizontal = 5.dp, vertical = 4.dp)) {
                                val uplink by homeViewModel.uplinkTotal.collectAsStateWithLifecycle()
                                Text(text = stringResource(id = R.string.status_uplink))
                                Spacer(Modifier.weight(0.5f))
                                Text(text = uplink ?: stringResource(id = R.string.loading))
                            }
                            Row(Modifier.padding(horizontal = 5.dp, vertical = 4.dp)) {
                                val downlink by homeViewModel.downlinkTotal.collectAsStateWithLifecycle()
                                Text(text = stringResource(id = R.string.status_downlink))
                                Spacer(Modifier.weight(0.5f))
                                Text(text = downlink ?: stringResource(id = R.string.loading))
                            }
                        }
                    }
                }
            }
            item {
                val serviceStatus by homeViewModel.serviceStatus.collectAsStateWithLifecycle()
                val systemProxyStatus by homeViewModel.systemProxyStatus.collectAsStateWithLifecycle()
                AnimatedVisibility(
                    visible = serviceStatus != Status.Stopped && systemProxyStatus?.available == true,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Card {
                        ListItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape8)
                            ,
                            headlineContent = {
                                Text(text = stringResource(id = R.string.http_proxy), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            },
                            trailingContent = {
                                var isEnabled by remember { mutableStateOf(true) }
                                Switch(
                                    enabled = isEnabled,
                                    checked = systemProxyStatus?.enabled ?: false,
                                    onCheckedChange = {
                                        isEnabled = false
                                        runCatching {
                                            ioScope.launch {
                                                homeViewModel.changeSystemProxyStatus(it)
                                                isEnabled = true
                                            }
                                        }.onFailure {
                                            prompter.toast(it.message ?: it.stackTraceToString())
                                        }
                                    }
                                )
                            }
                        )
                    }
                }
            }

            item {
                if (profiles.isNotEmpty()){
                    ListItem(
                        headlineContent = {
                            Text(text = stringResource(id = R.string.profile))
                        },
                        trailingContent = {
                            TextButton(onClick = {
                                showAddProfileDialog = true
                            }) {
                                Text(text = stringResource(id = R.string.add))
                                Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(id = R.string.profile_add_file))
                            }
                        }
                    )
                }
            }
            items(profiles.size){ index ->
                val profile = profiles[index]
                var listEnabled by remember { mutableStateOf(true) }
                Card {
                    ListItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape8)
                            .clickable {
                                ioScope.launch {
                                    if (!listEnabled || homeViewModel.lastSelectedIndex.value == index) {
                                        return@launch
                                    }
                                    listEnabled = false
                                    homeViewModel.selectProfile(index, profile,
                                        onRestart = {
                                            activity.reconnect()
                                            BoxService.stop()
                                            delay(1000L)
                                            activity.startService()
                                        },
                                        onThrow = {
                                            listEnabled = true
                                            prompter.toast("异常：" + (it.message ?: it.toString()))
                                        }
                                    )
                                    listEnabled = true
                                }
                            }
                        ,
                        colors = if (index == lastSelectedIndex) ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant) else ListItemDefaults.colors(),
                        headlineContent = {
                            Text(text = profile.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        },
                        supportingContent = if (profile.typed.type != TypedProfile.Type.Remote) null else {
                            {
                                Text(stringResource(R.string.profile_item_last_updated, DateFormat.getDateTimeInstance().format(profile.typed.lastUpdated)))
                            }
                        },
                        trailingContent = {
                            var showMoreMenu by remember{ mutableStateOf(false) }
                            IconButton(onClick = { showMoreMenu = true }) {
                                Icon(imageVector = Icons.Default.MoreHoriz, contentDescription = stringResource(R.string.read_more))
                                DropdownMenu(expanded = showMoreMenu, onDismissRequest = { showMoreMenu = false }) {
                                    DropdownMenuItem(
                                        onClick = {
                                            showMoreMenu = false
                                            val intent = Intent(activity, EditProfileActivity::class.java)
                                            intent.putExtra("profile_id", profile.id)
                                            activity.startActivity(intent)
                                        },
                                        text = {Text(stringResource(R.string.title_edit_configuration))}
                                    )
                                    DropdownMenuItem(
                                        onClick = {
                                            showMoreMenu = false
                                            val uri = homeViewModel.shareProfileToUri(activity, profile)
                                            activity.startActivity(
                                                Intent.createChooser(
                                                    Intent(Intent.ACTION_SEND).setType("application/octet-stream")
                                                        .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                        .putExtra(Intent.EXTRA_STREAM, uri),
                                                    activity.getString(com.google.android.material.R.string.abc_shareactionprovider_share_with)
                                                )
                                            )
                                        },
                                        text = {Text(stringResource(R.string.menu_share))}
                                    )

                                    if (profile.typed.type == TypedProfile.Type.Remote){
                                        DropdownMenuItem(
                                            onClick = {
                                                showMoreMenu = false
                                                ioScope.launch {
                                                    val qrBitmap = homeViewModel.shareProfileToQR(profile)
                                                    val lazyRoundedCornersTransformation by lazy { RoundedCornersTransformation(5f, 5f, 5f, 5f)  }
                                                    viewHostState.show {
                                                        BasicAlertDialog(onDismissRequest = { viewHostState.close() }) {
                                                            AsyncImage(
                                                                model = ImageRequest.Builder(
                                                                    LocalContext.current)
                                                                    .data(qrBitmap)
                                                                    .transformations(lazyRoundedCornersTransformation)
                                                                    .allowRgb565(true)
                                                                    .crossfade(true)
                                                                    .build(),
                                                                modifier = Modifier
                                                                    .size(300.dp)
                                                                    // .align(Alignment.CenterVertically)
                                                                    .padding(2.dp),
                                                                contentDescription = stringResource(R.string.profile_add_scan_qr_code)
                                                            )
                                                        }
                                                    }
                                                }
                                            },
                                            text = {Text(stringResource(R.string.profile_share_url))}
                                        )
                                    }

                                    DropdownMenuItem(
                                        onClick = {
                                            ioScope.launch {
                                                runCatching {
                                                    ProfileManager.delete(profile)
                                                }
                                            }
                                        },
                                        text = {Text(stringResource(R.string.menu_delete))}
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    LaunchedEffect(showAddProfileDialog) {
        if (showAddProfileDialog){
            viewHostState.show {
                // val coroutineScope = rememberCoroutineScope()

                BasicAlertDialog(onDismissRequest = { showAddProfileDialog = false }) {
                    Column() {
                        ListItem(headlineContent = { TextButton(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                            showAddProfileDialog = false
                            // coroutineScope.launch {
                            //     activity.requestGetContent("*/*")?.let{
                            //         homeViewModel.importProfiles(activity.contentResolver, it)
                            //     }
                            // }
                            activity.importFromFile.launch("*/*")
                        }) { Text(text = stringResource(id = R.string.profile_add_import_file))} })

                        ListItem(headlineContent = { TextButton(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                            showAddProfileDialog = false
                            activity.scanQrCode.launch(null)
                        }) { Text(text = stringResource(id = R.string.profile_add_scan_qr_code))} })

                        ListItem(headlineContent = { TextButton(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                            showAddProfileDialog = false
                            activity.startActivity(Intent(activity, NewProfileActivity::class.java))
                        }) { Text(text = stringResource(id = R.string.profile_add_create_manually))} })
                    }
                }
            }
        }else{
            viewHostState.close()
        }
    }
    // if (showAddProfileDialog){
    // }
}