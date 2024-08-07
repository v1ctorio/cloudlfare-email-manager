package com.nosesisaid.cfer.management


import android.widget.Spinner
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.Typography
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.navigation.NavController
import com.nosesisaid.cfer.management.api.addRoute
import com.nosesisaid.cfer.management.api.createEmail
import com.nosesisaid.cfer.management.api.fetchEmails
import com.nosesisaid.cfer.management.api.fetchRoutes
import com.nosesisaid.cfer.management.api.route
import com.nosesisaid.cfer.management.api.updateCatchAllRule
import com.nosesisaid.cfer.ui.theme.Typography
import kotlinx.coroutines.launch


enum class ActionType {
    Forward, Drop, Worker
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageRoutesScreen(navController: NavController) {

    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()


    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false,
    )

    var selectedNewRouteAction by remember { mutableStateOf(ActionType.Forward) }
    var actionTypes = ActionType.entries.toTypedArray()

    var emailsList = mutableListOf<String>("")
    var isNewEmailListDropDownExpanded by remember { mutableStateOf(false) }
    var selectedTargetEmailNewRoute by remember { mutableStateOf(emailsList[0]) }



    var catchAllRouteEmail by remember { mutableStateOf("") }
    var isCatchAllDropdownExpanded by remember { mutableStateOf(false) }

    var caroutes_is_checked by remember { mutableStateOf(false) }

    var forwardRules by remember  { mutableStateOf(emptyList<route>())}
    var dropRules by remember  { mutableStateOf(emptyList<route>())}
LaunchedEffect(Unit) {
    fetchRoutes(1,context) { r ->
        println(r?.result)
        r?.result?.forEach(){e->
            if (e.matchers[0].type == "all") {
                catchAllRouteEmail = e.actions[0].value[0]
                caroutes_is_checked = e.enabled
            }
            else if (e.actions[0].type == "forward") {
                forwardRules.plus(e)
            } else if (e.actions[0].type == "drop") {
                dropRules.plus(e)
            }
            println(forwardRules)
            println(dropRules)
            isLoading = false
        }
    }
    fetchEmails(1,context) { r ->
        r?.result?.forEach { e ->
            if (e.verified != null) {
                emailsList.remove("")
                emailsList.add(e.email)
            }
        }

    }
}


    val snackbarHostState = remember { SnackbarHostState() }

    var newRuleAlias by remember { mutableStateOf("") }
    Scaffold (
        snackbarHost = { SnackbarHost(hostState = snackbarHostState,
            modifier = Modifier.padding(
                WindowInsets.ime.asPaddingValues()),) },
        topBar = { TopAppBar(
            title = { Text(text = "Routes") },
            colors = topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.primary
            ))},
        bottomBar = { CFERNavigationBar(
            isEmailsSelected = false,
            nav = navController
        ) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showBottomSheet = true }) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Add email")
            }

        },
        content = { cpadding ->
            Column(
                modifier = Modifier
                    .padding(cpadding)
                    .padding(12.dp)
                    .fillMaxWidth(),


                )
            {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp), colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    )) {
                    Row (
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Text(text = "Catch-all route", modifier = Modifier.padding(20.dp), style = Typography.titleMedium)
                        Spacer(modifier = Modifier.weight(1f))
                        Switch(checked = caroutes_is_checked,
                            onCheckedChange = {
                                caroutes_is_checked = it
                                updateCatchAllRule(catchAllRouteEmail,it, context) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
                                    }
                                }
                            },
                            modifier = Modifier
                                .scale(0.7f)
                                .padding(8.dp))
                    }
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp), colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    )) {
                    ExposedDropdownMenuBox(
                        expanded = isCatchAllDropdownExpanded,
                        onExpandedChange = { isCatchAllDropdownExpanded = !isCatchAllDropdownExpanded },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        OutlinedTextField(
                            readOnly = true,
                            value = catchAllRouteEmail,
                            onValueChange = {},
                            label = { Text(text = "Target") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCatchAllDropdownExpanded)
                            },
                            colors = OutlinedTextFieldDefaults.colors(),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = isCatchAllDropdownExpanded, onDismissRequest = { isCatchAllDropdownExpanded = false }) {
                            emailsList.forEach { option: String ->
                                DropdownMenuItem(
                                    text = { Text(text = option) },
                                    onClick = {
                                        isCatchAllDropdownExpanded = false
                                        catchAllRouteEmail = option
                                            updateCatchAllRule(option,caroutes_is_checked, context) {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("Catch-all route updated", duration = SnackbarDuration.Short)
                                                }
                                            }

                                    }
                                )
                            }
                        }

                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                )) {

                    Row(
                    ) {
                        Text("Forward adresses",modifier= Modifier
                            .padding(16.dp),
                            textAlign = TextAlign.Center,
                            style = Typography.titleMedium
                        )

                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.fillMaxWidth())
                } else {
                    LazyColumn(
                        content = {
                            items(forwardRules) { r ->
                                ElevatedCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically

                                    ) {

                                        Text(r.name,
                                            Modifier
                                                .padding(16.dp)
                                                .width(160.dp), maxLines = 1)
                                        Text(text = "to",
                                            Modifier
                                                .padding(16.dp)
                                                .width(60.dp)
                                        )
                                        Text(r.actions[0].value[0],modifier= Modifier
                                            .padding(16.dp)
                                            .width(160.dp)
                                        )
                                    }
                                }

                            }
                        }
                    )
                }

                if (showBottomSheet) {
                    ModalBottomSheet(sheetState = sheetState, modifier = Modifier.fillMaxHeight(), onDismissRequest = { showBottomSheet = false }) {
                        Column(
                            modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
                        ) {
                            Text("Create a new route rule", style = MaterialTheme.typography.titleLarge)
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(value = newRuleAlias, onValueChange = { newRuleAlias = it }, label = { Text("Alias") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            ExposedDropdownMenuBox(
                                expanded = isNewEmailListDropDownExpanded,
                                onExpandedChange = { isNewEmailListDropDownExpanded = !isNewEmailListDropDownExpanded },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    enabled = selectedNewRouteAction != ActionType.Drop,
                                    readOnly = true,
                                    value = selectedTargetEmailNewRoute,
                                    onValueChange = {},
                                    label = { Text(text = "Target") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = isNewEmailListDropDownExpanded)
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(),
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                )
                                ExposedDropdownMenu(expanded = isNewEmailListDropDownExpanded, onDismissRequest = { isNewEmailListDropDownExpanded = false }) {
                                    emailsList.forEach { option: String ->
                                        DropdownMenuItem(
                                            text = { Text(text = option) },
                                            onClick = {
                                                isNewEmailListDropDownExpanded = false
                                                selectedTargetEmailNewRoute = option
                                            }
                                        )
                                    }
                                }

                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            actionTypes.forEach { option ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                )
                                {
                                    RadioButton(
                                        selected = selectedNewRouteAction == option,

                                        onClick = {
                                            selectedNewRouteAction = option
                                        }
                                    )
                                    Text(option.name)
                                }
                            }
                            Button(onClick = {

                                addRoute(selectedNewRouteAction,newRuleAlias,selectedTargetEmailNewRoute,context) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
                                    }
                                    showBottomSheet = false
                                }
                            }, modifier = Modifier.fillMaxWidth()) {
                                Text("Add Target Email")
                            }
                            Spacer(modifier =Modifier.height(16.dp))

                        }
                    }
                }
            }
        }
    )
}