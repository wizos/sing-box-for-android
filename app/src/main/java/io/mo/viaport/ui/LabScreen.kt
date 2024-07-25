package io.mo.viaport.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import cafe.adriel.bonsai.core.Bonsai
import cafe.adriel.bonsai.json.JsonBonsaiStyle
import cafe.adriel.bonsai.json.JsonTree
import kotlinx.serialization.json.Json

// @Composable
// fun JsonTreeScreen(){
//     TextButton(onClick = {
//         viewHostState.show {
//             AlertDialog(
//                 onDismissRequest = { viewHostState.close()},
//                 confirmButton = {},
//                 text = {
//                     val tree = JsonTree(
//                         // Sample JSON from https://gateway.marvel.com/v1/public/characters
//                         json = context.readTextFromManorOrAssets(input1.ifBlank { "PIKAQ_Q.json" })
//                     )
//                     Bonsai(
//                         tree = tree,
//                         // Custom style
//                         style = JsonBonsaiStyle(),
//                         onClick = { node ->
//                             tree.clearSelection()
//                             tree.toggleExpansion(node)
//                             prompter.toast("单击节点：" + node.name)
//                         },
//                         onDoubleClick = { node ->
//                             prompter.toast("双击节点：" + node.name)
//                         },
//                         onLongClick = { node ->
//                             prompter.toast("长按节点：" + node.name)
//                         }
//                     )
//                 }
//             )
//         }
//     }) {
//         Text(text = "Json 树")
//     }
// }