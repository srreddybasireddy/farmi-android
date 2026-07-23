package com.example.farmi.ui.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.farmi.data.remote.LocationSyncService
import com.example.farmi.domain.entities.AssetItem
import com.example.farmi.domain.entities.ChatMessage
import com.example.farmi.domain.entities.ChatSession
import com.example.farmi.domain.entities.MessageSender
import com.example.farmi.theme.CardBackground
import com.example.farmi.theme.CardBorder
import com.example.farmi.theme.DarkBackground
import com.example.farmi.theme.IndigoPrimary
import com.example.farmi.theme.TextGreenAccent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToCropDetail: (String) -> Unit,
    onNavigateToCattleDetail: (String) -> Unit,
    onNavigateToGenericAssetDetail: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    var showingAddAssetDialog by remember { mutableStateOf(false) }

    // Geolocation sync workflow on start
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        LocationSyncService.getInstance(context).startSyncFlowIfNeeded()
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        } else {
            LocationSyncService.getInstance(context).startSyncFlowIfNeeded()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            SidebarDrawerContent(
                selectedCategory = state.selectedCategory,
                previousSessions = state.previousSessions,
                onCategorySelected = { catName ->
                    viewModel.selectCategory(catName)
                    coroutineScope.launch { drawerState.close() }
                },
                onSessionSelected = { session ->
                    viewModel.loadSession(session)
                    coroutineScope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = DarkBackground,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = if (state.selectedCategory == "Crops") "Crops and Farming Assistant" else "${state.selectedCategory} Assistant",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 18.sp
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DarkBackground,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.Gray,
                        actionIconContentColor = Color.Gray
                    ),
                    navigationIcon = {
                        IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open Navigation Menu"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.clearChatMessages() }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Clear Chat log"
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(DarkBackground)
            ) {
                // 1. Horizontal Scroll of Category Assets
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (state.selectedCategory == "General") "Farming Categories" else "My ${state.selectedCategory}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    if (state.isLoading) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.Green, modifier = Modifier.size(24.dp))
                        }
                    } else {
                        val activeAssets = remember(state.selectedCategory, state.crops, state.cattleList, state.gardenList, state.poultryList, state.birdsBeesList, state.fishShrimpList) {
                            viewModel.activeCategoryAssets()
                        }

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth().height(84.dp)
                        ) {
                            items(activeAssets) { asset ->
                                Box(
                                    modifier = Modifier.clickable {
                                        if (state.selectedCategory == "General") {
                                            viewModel.selectCategory(asset.title)
                                        } else {
                                            when (state.selectedCategory) {
                                                "Crops" -> onNavigateToCropDetail(asset.title)
                                                "Cattle" -> onNavigateToCattleDetail(asset.title.substringAfter("#"))
                                                else -> onNavigateToGenericAssetDetail(asset.title, state.selectedCategory)
                                            }
                                        }
                                    }
                                ) {
                                    HorizontalAssetCard(asset = asset)
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(
                    color = CardBorder,
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                // 2. Chat Area
                val listState = rememberLazyListState()
                LaunchedEffect(state.chatMessages.size, state.isAssistantTyping) {
                    if (state.chatMessages.isNotEmpty()) {
                        listState.animateScrollToItem(
                            if (state.isAssistantTyping) state.chatMessages.size else state.chatMessages.size - 1
                        )
                    }
                }

                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    val showWelcome = !state.chatMessages.any { it.sender == MessageSender.USER }
                    if (showWelcome) {
                        val greetingText = state.chatMessages.firstOrNull { it.sender == MessageSender.ASSISTANT }?.content 
                            ?: "How can I help you today?"
                        WelcomeChatSuggestions(
                            greeting = greetingText,
                            suggestions = viewModel.activeCategorySuggestions(),
                            onSuggestionClicked = { suggestion ->
                                if (state.selectedCategory == "General") {
                                    val target = suggestion
                                        .replace("🌾 Chat about ", "")
                                        .replace("🐄 Chat about ", "")
                                        .replace("🏡 Chat about ", "")
                                        .replace("🥚 Chat about ", "")
                                    viewModel.selectCategory(target)
                                } else {
                                    viewModel.sendMessage(suggestion)
                                }
                            }
                        )
                    } else {
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.chatMessages) { message ->
                                MessageBubbleView(
                                    message = message,
                                    onRate = { rating -> viewModel.rateMessage(message.id, rating) }
                                )
                            }

                            if (state.isAssistantTyping) {
                                item {
                                    TypingIndicatorBubble()
                                }
                            }
                        }
                    }
                }

                // 3. Input Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isGeneral = state.selectedCategory == "General"
                    IconButton(
                        onClick = { showingAddAssetDialog = false; showingAddAssetDialog = true },
                        enabled = !isGeneral,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(CardBackground)
                            .border(1.dp, CardBorder, CircleShape)
                            .alpha(if (isGeneral) 0.3f else 1.0f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Asset",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Input Capsule
                    OutlinedTextField(
                        value = state.chatInputText,
                        onValueChange = { viewModel.updateChatInputText(it) },
                        placeholder = {
                            Text(
                                if (state.selectedCategory == "General") "Ask Farmi AI..." else "Ask ${state.selectedCategory} AI...",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            if (state.chatInputText.trim().isNotEmpty()) {
                                viewModel.sendMessage(state.chatInputText)
                                keyboardController?.hide()
                            }
                        }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = CardBackground,
                            unfocusedContainerColor = CardBackground,
                            focusedBorderColor = CardBorder,
                            unfocusedBorderColor = CardBorder
                        ),
                        shape = RoundedCornerShape(24.dp),
                        trailingIcon = {
                            Row(
                                modifier = Modifier.padding(end = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Speech input",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                                IconButton(
                                    onClick = {
                                        if (state.chatInputText.trim().isNotEmpty()) {
                                            viewModel.sendMessage(state.chatInputText)
                                            keyboardController?.hide()
                                        }
                                    },
                                    enabled = state.chatInputText.trim().isNotEmpty(),
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Send",
                                        tint = if (state.chatInputText.trim().isNotEmpty()) Color(0xFF0F734D) else Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        }
    }

    if (showingAddAssetDialog) {
        AddAssetDialog(
            initialCategory = state.selectedCategory,
            onDismiss = { showingAddAssetDialog = false },
            onConfirm = { cat, title, sub, status ->
                viewModel.addAsset(cat, title, sub, status)
                showingAddAssetDialog = false
            }
        )
    }
}

// MARK: - Sidebar Drawer Overlay Panel View
@Composable
fun SidebarDrawerContent(
    selectedCategory: String,
    previousSessions: List<ChatSession>,
    onCategorySelected: (String) -> Unit,
    onSessionSelected: (ChatSession) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf(
        Pair("General", "🏠"),
        Pair("Crops", "🌾"),
        Pair("Cattle", "🐄"),
        Pair("Garden", "🏡"),
        Pair("Poultry & Eggs", "🥚"),
        Pair("Birds & Bees", "🐝"),
        Pair("Fish & Shrimp", "🦐")
    )

    ModalDrawerSheet(
        drawerContainerColor = Color(0xFF06090E),
        modifier = modifier.width(280.dp).fillMaxHeight()
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0F734D).copy(alpha = 0.15f))
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFF0F734D),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = "Farmi Pro",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Agronomist Assistant",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }

            HorizontalDivider(color = CardBorder, thickness = 1.dp)

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) {
                // Section 1: Top Assets
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "TOP ASSETS",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        categories.forEach { (catName, icon) ->
                            val isSelected = selectedCategory == catName
                            val bg = if (isSelected) Color(0xFF0F734D).copy(alpha = 0.15f) else Color.Transparent
                            val border = if (isSelected) BorderModifier(Color(0xFF0F734D)) else Modifier

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(bg)
                                    .then(border)
                                    .clickable { onCategorySelected(catName) }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(text = icon, fontSize = 14.sp)
                                    Text(
                                        text = catName,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }

                // Section 2: Earlier Conversations
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "EARLIER CHATS",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        previousSessions.forEach { session ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.02f))
                                    .clickable { onSessionSelected(session) }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "💬", fontSize = 12.sp)
                                Text(
                                    text = session.title,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BorderModifier(color: Color): Modifier {
    return Modifier.border(1.dp, color, RoundedCornerShape(10.dp))
}

// MARK: - Horizontal Asset Card
@Composable
fun HorizontalAssetCard(
    asset: AssetItem,
    modifier: Modifier = Modifier
) {
    val statusColor = when (asset.statusColorName.lowercase()) {
        "green" -> Color(0xFF10B981)
        "blue" -> Color(0xFF3B82F6)
        "orange" -> Color(0xFFF59E0B)
        "purple" -> Color(0xFF8B5CF6)
        "red" -> Color(0xFFEF4444)
        else -> Color(0xFF9CA3AF)
    }

    Column(
        modifier = modifier
            .width(135.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(CardBackground)
            .border(width = 1.dp, color = CardBorder, shape = RoundedCornerShape(14.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (asset.iconName == "pawprint.fill") "🐄" else "🌾",
                fontSize = 12.sp
            )
            Text(
                text = asset.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1
            )
        }

        Text(
            text = asset.subtitle,
            fontSize = 10.sp,
            color = Color.Gray,
            maxLines = 1
        )

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(statusColor.copy(alpha = 0.15f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = asset.status,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = statusColor
            )
        }
    }
}

// MARK: - Welcome & Suggestions
@Composable
fun WelcomeChatSuggestions(
    greeting: String,
    suggestions: List<String>,
    onSuggestionClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFF0F734D).copy(alpha = 0.15f))
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color(0xFF0F734D),
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = greeting,
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.85f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            suggestions.forEach { suggestion ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(CardBackground)
                        .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                        .clickable { onSuggestionClicked(suggestion) }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = suggestion,
                        fontSize = 13.sp,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// MARK: - Message Bubble
@Composable
fun MessageBubbleView(
    message: ChatMessage,
    onRate: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isUser = message.sender == MessageSender.USER
    val bubbleColor = if (isUser) Color(0xFF0F734D).copy(alpha = 0.85f) else CardBackground
    val borderStroke = if (isUser) Modifier else Modifier.border(1.dp, CardBorder, RoundedCornerShape(18.dp))

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0F734D).copy(alpha = 0.15f))
                    .padding(end = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFF0F734D),
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
        }

        Column(
            modifier = Modifier
                .width(280.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(bubbleColor)
                .then(borderStroke)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = message.content,
                color = Color.White,
                fontSize = 14.sp
            )

            if (!isUser && message.qaId != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onRate(1) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (message.likeStatus == 1) Icons.Default.ThumbUp else Icons.Outlined.ThumbUp,
                            contentDescription = "Like",
                            tint = if (message.likeStatus == 1) Color(0xFF0F734D) else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = { onRate(-1) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (message.likeStatus == -1) Icons.Default.ThumbDown else Icons.Outlined.ThumbDown,
                            contentDescription = "Dislike",
                            tint = if (message.likeStatus == -1) Color.Red else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// MARK: - Typing Indicator
@Composable
fun TypingIndicatorBubble(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    val dot1Scale by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0xFF0F734D).copy(alpha = 0.15f))
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color(0xFF0F734D),
                modifier = Modifier.size(14.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(CardBackground)
                .border(1.dp, CardBorder, RoundedCornerShape(18.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color.Gray.copy(alpha = 0.7f * dot1Scale))
                )
            }
        }
    }
}
