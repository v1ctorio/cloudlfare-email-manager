package com.nosesisaid.cfer.management

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nosesisaid.cfer.R
import com.nosesisaid.cfer.management.api.CloudflareError
import com.nosesisaid.cfer.management.api.CloudflareMessage
import com.nosesisaid.cfer.management.api.createEmail
import com.nosesisaid.cfer.management.api.deleteEmail
import com.nosesisaid.cfer.management.api.email
import com.nosesisaid.cfer.management.api.emailListResponse
import com.nosesisaid.cfer.management.api.fetchEmails
import com.nosesisaid.cfer.management.api.resultInfo
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageEmailsScreen(navController: NavController) {
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }


    val exampleResponse = emailListResponse(
        result = listOf(
            email(
                created = "EMPTY",
                email = "EMPTY",
                id = "EMPTY",
                modified = "EMPTY",
                tag = "EMPTY",
                verified = "EMPTY"
            )
        ),
        success = true,
        result_info = resultInfo(
            count = 1,
            page = 1,
            per_page = 10,
            total_count = 2
        ),
        errors = emptyList<CloudflareError>(),
        messages = emptyList<CloudflareMessage>()
    )
    var isLoading by remember { mutableStateOf(true) }
    var emails by remember { mutableStateOf(emptyList<email>()) }
    val context = LocalContext.current

    var newTargetEmail by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        fetchEmails(1, context) { response ->
            if (response != null) {
                emails = response.result
            } else {
                emails = exampleResponse.result
            }
            isLoading = false
        }
    }

    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false,
    )

    val openDialogIdOfThing = remember { mutableStateOf<email?>(null) }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(
                    WindowInsets.ime.asPaddingValues()
                ),
            )
        },

        topBar = {
            TopAppBar(
                title = { Text(text = "Emails") },
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    IconButton(onClick = { uriHandler.openUri("https://github.com/v1ctorio/cloudlfare-email-manager") }) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_code_24),
                            contentDescription = "source code"
                        )

                    }
                }
            )
        },
        bottomBar = {
            CFERNavigationBar(
                isEmailsSelected = true,
                nav = navController
            )
        },
        floatingActionButton = {

            FloatingActionButton(onClick = { showBottomSheet = true }) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Add email")

            }


        }
    ) { cpadding ->
        Column(
            modifier = Modifier
                .padding(cpadding)
                .padding(12.dp)
                .fillMaxWidth(),


            )
        {

            Card(
                modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                )
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Email", modifier = Modifier
                            .padding(16.dp)
                            .width(160.dp),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "State", modifier = Modifier
                            .padding(16.dp)
                            .width(50.dp)
                    )
                    Text(
                        "Delete", modifier = Modifier
                            .padding(16.dp)
                            .width(90.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxSize()) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(alignment = Alignment.Center))
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        content
                        = {
                            items(emails) { email ->
                                ElevatedCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            email.email,
                                            Modifier
                                                .padding(16.dp)
                                                .width(160.dp),
                                            maxLines = 1
                                        )

                                        if (email.verified != null) {
                                            ClickableText(
                                                text = AnnotatedString("Verified"),
                                                modifier = Modifier
                                                    .padding(16.dp)
                                                    .width(80.dp),
                                                maxLines = 1,
                                                onClick = {
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar(
                                                            message = email.verified,
                                                            duration = SnackbarDuration.Short
                                                        )
                                                    }

                                                }
                                            )

                                        } else {
                                            ClickableText(text = AnnotatedString("Pending"),
                                                modifier = Modifier
                                                    .padding(16.dp)
                                                    .width(80.dp),
                                                maxLines = 1,
                                                onClick = {
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar(
                                                            message = "Pending",
                                                            duration = SnackbarDuration.Short
                                                        )
                                                    }
                                                }
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                openDialogIdOfThing.value = email
                                            }, modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(end = 16.dp)
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.baseline_delete_forever_24),
                                                contentDescription = "Delete"
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    )

                }
            }

            when {
                openDialogIdOfThing.value != null -> {
                    WarningElementDeletion(
                        onDismissRequest = { openDialogIdOfThing.value = null },
                        onConfirmation = { id ->

                            deleteEmail(id, context) { r ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = r,
                                        duration = SnackbarDuration.Short
                                    )
                                }
                                openDialogIdOfThing.value = null
                            }
                        },
                        target = openDialogIdOfThing.value!!.email,
                        target_id = openDialogIdOfThing.value!!.id,
                        isEmail = true
                    )
                }
            }


            if (showBottomSheet) {
                ModalBottomSheet(
                    modifier = Modifier.fillMaxHeight(),
                    sheetState = sheetState,
                    onDismissRequest = { showBottomSheet = false }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 32.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.Top

                    ) {

                        Text(
                            "Add a new Target Email Address to your account.",

                            style = MaterialTheme.typography.titleLarge
                        )
                        OutlinedTextField(
                            value = newTargetEmail,
                            onValueChange = {
                                newTargetEmail = it
                            },
                            label = { Text("Email address") },

                            modifier = Modifier
                                .fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            createEmail(newTargetEmail, context) { r ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = r,
                                        duration = SnackbarDuration.Long
                                    )
                                }
                                showBottomSheet = false
                            }

                        }, modifier = Modifier.fillMaxWidth()) {
                            Text("Add Target Email")
                        }
                    }

                }
            }

        }
    }
}

