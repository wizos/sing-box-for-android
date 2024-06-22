package io.mo.viaport.ui.log

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import io.mo.viaport.ui.LocalActivity
import io.mo.viaport.ui.Routor
import io.mo.viaport.ui.home.HomeViewModel
import io.mo.viaport.ui.toast
import io.nekohasekai.sfa.R
import kotlinx.coroutines.launch

fun Routor.logsId() = "logs"
fun NavGraphBuilder.logsScreen(homeViewModel: HomeViewModel) { //
    // XLog.d("userUiState 变化结束 Failure")
    composable(Routor.logsId()) {
        LogsScreen(homeViewModel)
    }
}
fun NavController.toLogsScreen(){
    navigate(Routor.logsId())
}
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LogsScreen(homeViewModel: HomeViewModel) {
    // val serviceStatus by homeViewModel.serviceStatus.collectAsStateWithLifecycle()
    // if (profiles.isEmpty()){
    //     Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
    //         TextButton(onClick = {
    //             showAddProfileDialog = true
    //         }) {
    //             Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(id = R.string.profile_add_file))
    //             Text(text = stringResource(id = R.string.profile_add_file))
    //         }
    //     }
    // }else{
    //
    // }
    // Box(Modifier.fillMaxSize(), Alignment.Center) {
    //     Image(
    //         painter = painterResource(id = R.drawable.ic_launcher_foreground),
    //         contentDescription = stringResource(id = R.string.app_name),
    //     )
    // }

    val activity = LocalActivity.current
    val lazyListState = rememberLazyListState()

    val logs by homeViewModel.logList.collectAsStateWithLifecycle()
    val paused by homeViewModel.paused.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        if (logs.size > lazyListState.layoutInfo.totalItemsCount) return@LaunchedEffect
        lazyListState.scrollToItem((logs.size - 1).coerceAtLeast(0))
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp)
            .combinedClickable(
                onClick = {
                    if (paused) {
                        activity.toast(R.string.resumed)
                    } else {
                        activity.toast(R.string.paused)
                    }
                    homeViewModel.receiveLog(!paused)
                },
                onDoubleClick = {
                    coroutineScope.launch {
                        lazyListState.scrollToItem(0)
                    }
                }
            ),
        state = lazyListState
    ) {
        items(logs.size){index ->
            SelectionContainer {
                Text(text = logs[index], modifier = Modifier.padding(horizontal = 2.dp, vertical = 5.dp))
            }
        }
    }

}