package com.example.farmi.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmi.domain.entities.AssetItem
import com.example.farmi.domain.entities.Cattle
import com.example.farmi.domain.entities.ChatMessage
import com.example.farmi.domain.entities.ChatSession
import com.example.farmi.domain.entities.Crop
import com.example.farmi.domain.entities.CropStatus
import com.example.farmi.domain.entities.MessageSender
import com.example.farmi.domain.usecases.GetCropsUseCase
import com.example.farmi.domain.interfaces.ChatRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

data class HomeUiState(
    val crops: List<Crop> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    
    // Chat System State
    val chatMessages: List<ChatMessage> = emptyList(),
    val isAssistantTyping: Boolean = false,
    val chatInputText: String = "",
    
    // Category Management
    val selectedCategory: String = "General",
    val cattleList: List<Cattle> = emptyList(),
    val gardenList: List<AssetItem> = emptyList(),
    val poultryList: List<AssetItem> = emptyList(),
    val birdsBeesList: List<AssetItem> = emptyList(),
    val fishShrimpList: List<AssetItem> = emptyList(),
    
    // Chat History State
    val previousSessions: List<ChatSession> = emptyList()
)

class HomeViewModel(
    private val getCropsUseCase: GetCropsUseCase,
    private val chatRepository: ChatRepository,
    private val deviceUuid: String
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Cache of chat history for each category
    private val categoryChats = mutableMapOf<String, List<ChatMessage>>()

    init {
        setupMockData()
        loadCrops()
    }

    private fun setupMockData() {
        val generalGreeting = ChatMessage(
            content = "Welcome to Farmi farming app, how can I help you today with your farming operations?",
            sender = MessageSender.ASSISTANT
        )
        categoryChats["General"] = listOf(generalGreeting)

        val cropsGreeting = ChatMessage(
            content = "Hello! I am your Crops and Farming assistant. Ask me questions about crops, yields, watering recommendations, or health checks.",
            sender = MessageSender.ASSISTANT
        )
        categoryChats["Crops"] = listOf(cropsGreeting)

        val cattleList = listOf(
            Cattle(tagNumber = "1024", breed = "Angus Bull", ageMonths = 22, status = "Healthy"),
            Cattle(tagNumber = "2048", breed = "Hereford Heifer", ageMonths = 18, status = "Under Observation", healthAlerts = "Due for BVD Booster"),
            Cattle(tagNumber = "0512", breed = "Holstein Cow", ageMonths = 30, status = "Milking")
        )

        val gardenList = listOf(
            AssetItem(title = "Heirloom Tomatoes", subtitle = "Organic Roma", status = "Ripening", statusColorName = "orange", iconName = "sun.max.fill"),
            AssetItem(title = "Sweet Basil", subtitle = "Greenhouse Hydro", status = "Harvest Ready", statusColorName = "green", iconName = "leaf.fill"),
            AssetItem(title = "Butterhead Lettuce", subtitle = "Raised bed Seedlings", status = "Sprouting", statusColorName = "blue", iconName = "sparkles")
        )

        val poultryList = listOf(
            AssetItem(title = "Laying Hens", subtitle = "Coop Alpha (45 Birds)", status = "Producing", statusColorName = "green", iconName = "circle.grid.2x2.fill"),
            AssetItem(title = "Brown Eggs Inventory", subtitle = "Storage Room A (12 Dozen)", status = "In Stock", statusColorName = "blue", iconName = "shippingbox.fill"),
            AssetItem(title = "Broiler Chickens", subtitle = "Coop Beta (50 Birds)", status = "Growing", statusColorName = "green", iconName = "squareshape.dashed.squareshape")
        )

        val birdsBeesList = listOf(
            AssetItem(title = "Honeybee Hives", subtitle = "South Orchard (3 Hives)", status = "Active Honey", statusColorName = "green", iconName = "ant.fill"),
            AssetItem(title = "Wildflower Nest", subtitle = "North Pasture Border", status = "Established", statusColorName = "blue", iconName = "house.fill"),
            AssetItem(title = "Barn Swallow Box", subtitle = "Main Barn Rafters", status = "Occupied", statusColorName = "green", iconName = "bird.fill")
        )

        val fishShrimpList = listOf(
            AssetItem(title = "Nile Tilapia Pond", subtitle = "Pond #1 (2,500 Fingerlings)", status = "Feeding Stage", statusColorName = "green", iconName = "fish.fill"),
            AssetItem(title = "White Shrimp Tank", subtitle = "Recirculating Tank #2", status = "Under Control", statusColorName = "blue", iconName = "water.waves")
        )

        val previousSessions = listOf(
            ChatSession(
                title = "🌾 Wheat Harvest Advice",
                messages = listOf(
                    ChatMessage(content = "What is the yield estimate for Red Wheat?", sender = MessageSender.USER, timestamp = System.currentTimeMillis() - 86400000L * 2),
                    ChatMessage(content = "The estimated harvest is in 60 days. Keep soil moisture at 42% for best tiller growth.", sender = MessageSender.ASSISTANT, timestamp = System.currentTimeMillis() - 86400000L * 2)
                )
            ),
            ChatSession(
                title = "🐛 Corn Pest Help",
                messages = listOf(
                    ChatMessage(content = "I found small holes in my corn leaves, what should I do?", sender = MessageSender.USER, timestamp = System.currentTimeMillis() - 86400000L * 1),
                    ChatMessage(content = "This sounds like early corn borer infestation. Inspect leaf undersides and apply organic neem oil spray.", sender = MessageSender.ASSISTANT, timestamp = System.currentTimeMillis() - 86400000L * 1)
                )
            )
        )

        _uiState.update {
            it.copy(
                chatMessages = listOf(generalGreeting),
                cattleList = cattleList,
                gardenList = gardenList,
                poultryList = poultryList,
                birdsBeesList = birdsBeesList,
                fishShrimpList = fishShrimpList,
                previousSessions = previousSessions
            )
        }
    }

    fun loadCrops() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val crops = getCropsUseCase.execute()
                _uiState.update { it.copy(crops = crops, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message ?: "Unknown error", isLoading = false) }
            }
        }
    }

    fun selectCategory(name: String) {
        val currentCategory = _uiState.value.selectedCategory
        
        // 1. Save current active chat list to category cache
        categoryChats[currentCategory] = _uiState.value.chatMessages
        
        // 2. Load or initialize target chat messages
        val targetMessages = categoryChats[name] ?: run {
            val assistantWording = if (name == "Crops") "Crops and Farming" else name
            val greetingText = if (name == "General") {
                "Welcome to Farmi farming app, how can I help you today with your farming operations?"
            } else {
                "Welcome to your $assistantWording assistant. How can I help you today with your ${name.lowercase()} operations?"
            }
            val greeting = ChatMessage(
                content = greetingText,
                sender = MessageSender.ASSISTANT
            )
            val newGreetingList = listOf(greeting)
            categoryChats[name] = newGreetingList
            newGreetingList
        }

        _uiState.update {
            it.copy(
                selectedCategory = name,
                chatMessages = targetMessages
            )
        }
    }

    fun loadSession(session: ChatSession) {
        _uiState.update {
            it.copy(chatMessages = session.messages)
        }
    }

    fun updateChatInputText(text: String) {
        _uiState.update { it.copy(chatInputText = text) }
    }

    fun clearChatMessages() {
        _uiState.update { it.copy(chatMessages = emptyList()) }
    }

    fun activeCategoryAssets(): List<AssetItem> {
        val state = _uiState.value
        return when (state.selectedCategory) {
            "General" -> listOf(
                AssetItem(title = "Crops", subtitle = "🌾 Dynamic Crops Assistant", status = "Crops Chat", statusColorName = "green", iconName = "leaf.fill"),
                AssetItem(title = "Cattle", subtitle = "🐄 Livestock Manager", status = "Cattle Chat", statusColorName = "orange", iconName = "pawprint.fill"),
                AssetItem(title = "Garden", subtitle = "🏡 Greenery & Plants", status = "Garden Chat", statusColorName = "blue", iconName = "sun.max.fill"),
                AssetItem(title = "Poultry & Eggs", subtitle = "🥚 Coop production stats", status = "Poultry Chat", statusColorName = "purple", iconName = "circle.grid.2x2.fill"),
                AssetItem(title = "Birds & Bees", subtitle = "🐝 Pollinator Hives", status = "Apiary Chat", statusColorName = "gray", iconName = "ant.fill"),
                AssetItem(title = "Fish & Shrimp", subtitle = "🦐 Pond & Marine life", status = "Aquacraft Chat", statusColorName = "blue", iconName = "fish.fill")
            )
            "Crops" -> state.crops.map { crop ->
                AssetItem(
                    id = crop.id,
                    title = crop.name,
                    subtitle = crop.variety,
                    status = crop.status.value,
                    statusColorName = cropColor(crop.status),
                    iconName = "leaf.fill"
                )
            }
            "Cattle" -> state.cattleList.map { cattle ->
                AssetItem(
                    id = cattle.id,
                    title = "Tag #${cattle.tagNumber}",
                    subtitle = cattle.breed,
                    status = cattle.status,
                    statusColorName = cattleColor(cattle.status),
                    iconName = "pawprint.fill"
                )
            }
            "Garden" -> state.gardenList
            "Poultry & Eggs" -> state.poultryList
            "Birds & Bees" -> state.birdsBeesList
            "Fish & Shrimp" -> state.fishShrimpList
            else -> emptyList()
        }
    }

    fun addAsset(category: String, title: String, subtitle: String, status: String) {
        viewModelScope.launch {
            when (category) {
                "Crops" -> {
                    val cropStatus = CropStatus.entries.firstOrNull { it.value.equals(status, ignoreCase = true) } ?: CropStatus.PLANTED
                    val newCrop = Crop(
                        name = title,
                        variety = if (subtitle.isEmpty()) "Standard" else subtitle,
                        plantedDate = Date(),
                        estimatedHarvestDate = Date(System.currentTimeMillis() + 86400000L * 90),
                        status = cropStatus
                    )
                    try {
                        getCropsUseCase.repository.addCrop(newCrop)
                        loadCrops() // Reload crops
                    } catch (e: Exception) {
                        Log.e("HomeViewModel", "Failed to add crop: ${e.message}")
                    }
                }
                "Cattle" -> {
                    val newCattle = Cattle(
                        tagNumber = title,
                        breed = if (subtitle.isEmpty()) "Standard" else subtitle,
                        ageMonths = 14,
                        status = status
                    )
                    _uiState.update { it.copy(cattleList = it.cattleList + newCattle) }
                }
                "Garden" -> {
                    val newItem = AssetItem(
                        title = title,
                        subtitle = if (subtitle.isEmpty()) "Raised Bed" else subtitle,
                        status = status,
                        statusColorName = mapStatusToColorName(status),
                        iconName = "sun.max.fill"
                    )
                    _uiState.update { it.copy(gardenList = it.gardenList + newItem) }
                }
                "Poultry & Eggs" -> {
                    val newItem = AssetItem(
                        title = title,
                        subtitle = if (subtitle.isEmpty()) "Coop Yard" else subtitle,
                        status = status,
                        statusColorName = mapStatusToColorName(status),
                        iconName = "circle.grid.2x2.fill"
                    )
                    _uiState.update { it.copy(poultryList = it.poultryList + newItem) }
                }
                "Birds & Bees" -> {
                    val newItem = AssetItem(
                        title = title,
                        subtitle = if (subtitle.isEmpty()) "Orchard Apiary" else subtitle,
                        status = status,
                        statusColorName = mapStatusToColorName(status),
                        iconName = "ant.fill"
                    )
                    _uiState.update { it.copy(birdsBeesList = it.birdsBeesList + newItem) }
                }
                "Fish & Shrimp" -> {
                    val newItem = AssetItem(
                        title = title,
                        subtitle = if (subtitle.isEmpty()) "Pond Aquacraft" else subtitle,
                        status = status,
                        statusColorName = mapStatusToColorName(status),
                        iconName = "fish.fill"
                    )
                    _uiState.update { it.copy(fishShrimpList = it.fishShrimpList + newItem) }
                }
            }
        }
    }

    private fun mapStatusToColorName(status: String): String {
        return when (status.lowercase()) {
            "planted", "sprouting", "in stock", "blue" -> "blue"
            "growing", "healthy", "milking", "producing", "green" -> "green"
            "harvesting", "under observation", "ripening", "orange" -> "orange"
            "harvested", "purple" -> "purple"
            "sick", "red" -> "red"
            else -> "gray"
        }
    }

    fun activeCategorySuggestions(): List<String> {
        return when (_uiState.value.selectedCategory) {
            "General" -> listOf(
                "🌾 Chat about Crops",
                "🐄 Chat about Cattle",
                "🏡 Chat about Garden",
                "🥚 Chat about Poultry & Eggs"
            )
            "Crops" -> listOf(
                "🌾 Check Red Wheat health",
                "🌽 Get Sweet Corn tips",
                "📅 Calculate harvest time",
                "🧪 Recommend fertilizer"
            )
            "Cattle" -> listOf(
                "🐄 Check milk yields",
                "💉 Vaccination schedules",
                "📊 Feed intake tracker",
                "🐂 Breeding cycle info"
            )
            "Garden" -> listOf(
                "🍅 Tomato ripening tips",
                "🌿 Hydroponic basil care",
                "🥬 Raised bed watering",
                "🌻 Pest control guide"
            )
            "Poultry & Eggs" -> listOf(
                "🥚 Check egg production",
                "🐔 Hen feeding guidelines",
                "🌡️ Brooder temperature",
                "🧼 Coop cleaning cycle"
            )
            "Birds & Bees" -> listOf(
                "🐝 Honeybee hive status",
                "🌸 Pollinator gardens",
                "📦 Hive winterization",
                "🐦 Wild bird feeders"
            )
            "Fish & Shrimp" -> listOf(
                "🐟 Tilapia pond feeding",
                "🦐 Shrimp tank salinity",
                "🌡️ Water temperature index",
                "🔄 Pond water aeration"
            )
            else -> emptyList()
        }
    }

    private fun cropColor(status: CropStatus): String {
        return when (status) {
            CropStatus.PLANTED -> "blue"
            CropStatus.GROWING -> "green"
            CropStatus.HARVESTING -> "orange"
            CropStatus.HARVESTED -> "purple"
        }
    }

    private fun cattleColor(status: String): String {
        return when (status.lowercase()) {
            "healthy", "milking" -> "green"
            "under observation", "dry" -> "orange"
            else -> "blue"
        }
    }

    fun sendMessage(content: String) {
        val cleanedContent = content.trim()
        if (cleanedContent.isEmpty()) return

        viewModelScope.launch {
            if (_uiState.value.selectedCategory == "General") {
                val matchedCategory = detectCategoryFromQuery(cleanedContent)
                if (matchedCategory != null) {
                    if (_uiState.value.chatInputText == content) {
                        _uiState.update { it.copy(chatInputText = "") }
                    }
                    selectCategory(matchedCategory)
                    sendMessage(cleanedContent)
                    return@launch
                }
            }

            // 1. Add user message
            val userMessage = ChatMessage(content = cleanedContent, sender = MessageSender.USER)
            _uiState.update { it.copy(chatMessages = it.chatMessages + userMessage, chatInputText = "") }

            // 2. Start typing animation
            _uiState.update { it.copy(isAssistantTyping = true) }

            // 3. Simulate latency
            delay(1200)

            // 4. Generate context-aware response from backend API
            val responseText = chatRepository.getAdvisoryResponse(
                deviceUuid = deviceUuid,
                category = _uiState.value.selectedCategory,
                query = cleanedContent
            )
            val assistantMessage = ChatMessage(content = responseText, sender = MessageSender.ASSISTANT)

            _uiState.update {
                it.copy(
                    chatMessages = it.chatMessages + assistantMessage,
                    isAssistantTyping = false
                )
            }

            // Save history state back to cache
            categoryChats[_uiState.value.selectedCategory] = _uiState.value.chatMessages
        }
    }

    private fun detectCategoryFromQuery(query: String): String? {
        val lower = query.lowercase()
        
        // 1. Crops matches
        if (lower.contains("crop") || lower.contains("wheat") || lower.contains("corn") || lower.contains("barley") || lower.contains("grain")) {
            return "Crops"
        }
        
        // 2. Cattle matches
        if (lower.contains("cattle") || lower.contains("cow") || lower.contains("goat") || lower.contains("bull") || lower.contains("heifer") || lower.contains("livestock") || lower.contains("calf") || lower.contains("steer")) {
            return "Cattle"
        }
        
        // 3. Garden matches
        if (lower.contains("garden") || lower.contains("tomato") || lower.contains("basil") || lower.contains("lettuce") || lower.contains("plant") || lower.contains("vegetable") || lower.contains("herb")) {
            return "Garden"
        }
        
        // 4. Poultry matches
        if (lower.contains("poultry") || lower.contains("egg") || lower.contains("hen") || lower.contains("chicken") || lower.contains("broiler") || lower.contains("coop") || lower.contains("duck")) {
            return "Poultry & Eggs"
        }
        
        // 5. Birds & Bees matches
        if (lower.contains("bee") || lower.contains("hive") || lower.contains("honey") || lower.contains("apiary") || lower.contains("bird") || lower.contains("swallow")) {
            return "Birds & Bees"
        }
        
        // 6. Fish & Shrimp matches
        if (lower.contains("fish") || lower.contains("shrimp") || lower.contains("pond") || lower.contains("tilapia") || lower.contains("seafood") || lower.contains("marine") || lower.contains("aquaculture")) {
            return "Fish & Shrimp"
        }
        
        return null
    }

    private fun generateCategorySpecificResponse(query: String): String {
        val lowercaseQuery = query.lowercase()
        return when (_uiState.value.selectedCategory) {
            "General" -> "🚜 **General Farm Advisory**: Let's focus your session. Choose a category from the top panels (Crops, Cattle, Garden, etc.) or select a topic to get started!"
            "Crops" -> {
                if (lowercaseQuery.contains("wheat") || lowercaseQuery.contains("health")) {
                    "🌾 **Red Wheat Health Status**: Growing nicely (Planted 30 days ago). Soil moisture is healthy (42%). Keep current hydration levels."
                } else if (lowercaseQuery.contains("corn") || lowercaseQuery.contains("tips")) {
                    "🌽 **Sweet Corn Watering**: Placed 10 days ago. The soil drainage is currently critical as moisture is 58% (Slightly High). Keep a close watch."
                } else {
                    "🌾 For crops, please monitor moisture levels. Ensure proper crop aeration and fertilizer applications according to crop growth phases."
                }
            }
            "Cattle" -> {
                if (lowercaseQuery.contains("milk") || lowercaseQuery.contains("yield")) {
                    "🐄 **Milk Yields**: Holstein Tag #0512 produces average 8.2 Gallons/day, showing stable performance metrics."
                } else if (lowercaseQuery.contains("vaccination") || lowercaseQuery.contains("schedule")) {
                    "💉 **Vaccination alert**: Tag #2048 (Hereford Heifer) has an upcoming BVD booster due next Friday."
                } else {
                    "🐄 Cattle metrics: Feed intake rates look optimal. Monitor tags closely for step-count deviations indicating high stress."
                }
            }
            "Garden" -> {
                if (lowercaseQuery.contains("tomato") || lowercaseQuery.contains("ripening")) {
                    "🍅 **Tomato Ripening**: Heirloom tomatoes in bed #3 require dry conditions. Reduce watering slightly to enhance sugar content."
                } else {
                    "🌿 Garden status: Greenhouse humidity index is at 65%. Recommended to maintain ventilation."
                }
            }
            "Poultry & Eggs" -> {
                if (lowercaseQuery.contains("egg") || lowercaseQuery.contains("production")) {
                    "🥚 **Egg Output**: Laying hens in Coop Alpha are producing an average of 38 eggs/day, reflecting high feed conversion efficiency."
                } else if (lowercaseQuery.contains("brooder") || lowercaseQuery.contains("temperature")) {
                    "🌡️ **Brooder Temperature**: Ensure chicks under 1 week are kept at 95°F. Decrease temp by 5°F weekly."
                } else {
                    "🐔 Poultry check: Ventilation in Coop Alpha is optimal. Keep calcium supplements mixed with the feed."
                }
            }
            "Birds & Bees" -> {
                if (lowercaseQuery.contains("hive") || lowercaseQuery.contains("honeybee")) {
                    "🐝 **Hive Status**: Hives in South Orchard show active queen behavior with steady honey flow index."
                } else {
                    "🌸 Pollination report: Orchard wild flower beds are attracting high levels of pollinating insects."
                }
            }
            "Fish & Shrimp" -> {
                if (lowercaseQuery.contains("pond") || lowercaseQuery.contains("tilapia")) {
                    "🐟 **Pond #1 Feeding**: Tilapia feeding cycle is currently active. Maintain water oxygen level above 5 ppm."
                } else {
                    "🦐 Aquaculture check: Tank salinity is stable at 28 ppt. Keep UV filters running."
                }
            }
            else -> "How can I help you today with your farming operations?"
        }
    }
}
