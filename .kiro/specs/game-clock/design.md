# Design Document

## Overview

The Android GameClock app will be built using modern Android development practices with Kotlin and Jetpack Compose. The architecture follows MVVM (Model-View-ViewModel) pattern with Repository pattern for data persistence. The app will use Material Design 3 components and theming system to provide a native Android experience while maintaining the core functionality of the iOS counterpart.

## Architecture

### High-Level Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Presentation  │    │    Domain       │    │      Data       │
│     Layer       │    │     Layer       │    │     Layer       │
├─────────────────┤    ├─────────────────┤    ├─────────────────┤
│ • Composables   │◄──►│ • ViewModels    │◄──►│ • Repositories  │
│ • UI State      │    │ • Use Cases     │    │ • Data Sources  │
│ • Navigation    │    │ • Models        │    │ • Preferences   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### Technology Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with Repository Pattern
- **Dependency Injection**: Hilt
- **Data Persistence**: SharedPreferences + DataStore
- **Serialization**: Kotlinx Serialization
- **Coroutines**: For asynchronous operations
- **Material Design**: Material 3 components and theming

## Components and Interfaces

### Core Models

#### GameState
```kotlin
enum class GameState {
    STOPPED,
    RUNNING,
    PAUSED,
    GAME_OVER
}

enum class Player {
    PLAYER_ONE,
    PLAYER_TWO
}
```

#### TimeControl
```kotlin
@Serializable
data class TimeControl(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val timeInSeconds: Long,
    val incrementInSeconds: Long
) {
    companion object {
        val BULLET_PRESETS = listOf(
            TimeControl(name = "1 min", timeInSeconds = 60, incrementInSeconds = 0),
            TimeControl(name = "1 min + 1 sec", timeInSeconds = 60, incrementInSeconds = 1),
            TimeControl(name = "2 min + 1 sec", timeInSeconds = 120, incrementInSeconds = 1)
        )
        
        val BLITZ_PRESETS = listOf(
            TimeControl(name = "3 min", timeInSeconds = 180, incrementInSeconds = 0),
            TimeControl(name = "3 min + 2 sec", timeInSeconds = 180, incrementInSeconds = 2),
            TimeControl(name = "5 min", timeInSeconds = 300, incrementInSeconds = 0),
            TimeControl(name = "5 min + 3 sec", timeInSeconds = 300, incrementInSeconds = 3)
        )
        
        val RAPID_PRESETS = listOf(
            TimeControl(name = "10 min", timeInSeconds = 600, incrementInSeconds = 0),
            TimeControl(name = "10 min + 5 sec", timeInSeconds = 600, incrementInSeconds = 5),
            TimeControl(name = "15 min + 10 sec", timeInSeconds = 900, incrementInSeconds = 10)
        )
    }
}
```

### ViewModels

#### GameViewModel
```kotlin
@HiltViewModel
class GameViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()
    
    private var gameTimer: Job? = null
    
    fun startGame()
    fun pauseGame()
    fun resetGame()
    fun switchPlayer()
    fun setTimeControl(timeControl: TimeControl)
    fun setDifferentTimeControls(player1: TimeControl, player2: TimeControl)
}
```

#### ThemeViewModel
```kotlin
@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {
    
    val currentTheme: StateFlow<AppTheme>
    val availableThemes: List<AppTheme>
    
    fun selectTheme(theme: AppTheme)
}
```

### UI State Models

#### GameUiState
```kotlin
data class GameUiState(
    val gameState: GameState = GameState.STOPPED,
    val activePlayer: Player? = null,
    val player1Time: Long = 300L, // 5 minutes default
    val player2Time: Long = 300L,
    val player1TimeControl: TimeControl = TimeControl.BLITZ_PRESETS[2],
    val player2TimeControl: TimeControl = TimeControl.BLITZ_PRESETS[2],
    val winner: Player? = null,
    val recentTimeControls: List<TimeControl> = emptyList(),
    val customTimeControls: List<TimeControl> = emptyList()
)
```

### Repository Interfaces

#### GameRepository
```kotlin
interface GameRepository {
    suspend fun saveRecentTimeControl(timeControl: TimeControl)
    suspend fun getRecentTimeControls(): List<TimeControl>
    suspend fun saveCustomTimeControl(timeControl: TimeControl)
    suspend fun getCustomTimeControls(): List<TimeControl>
    suspend fun deleteCustomTimeControl(timeControl: TimeControl)
}
```

#### PreferencesRepository
```kotlin
interface PreferencesRepository {
    suspend fun saveTheme(theme: AppTheme)
    suspend fun getSelectedTheme(): AppTheme
    suspend fun saveLastUsedTimeControl(timeControl: TimeControl)
    suspend fun getLastUsedTimeControl(): TimeControl?
}
```

## Data Models

### Theme System

#### AppTheme
```kotlin
@Serializable
data class AppTheme(
    val id: String,
    val name: String,
    val player1Color: Long, // Color as Long for serialization
    val player2Color: Long,
    val isDark: Boolean = false
) {
    companion object {
        val DEFAULT = AppTheme(
            id = "default",
            name = "Default",
            player1Color = 0xFF1A1A1A,
            player2Color = 0xFFE5E5E5
        )
        
        val MODERN = AppTheme(
            id = "modern",
            name = "Modern",
            player1Color = 0xFF003366,
            player2Color = 0xFFFFCC00
        )
        
        val FOREST = AppTheme(
            id = "forest",
            name = "Forest",
            player1Color = 0xFF1A4D33,
            player2Color = 0xFFE5CC99
        )
        
        val OCEAN = AppTheme(
            id = "ocean",
            name = "Ocean",
            player1Color = 0xFF006666,
            player2Color = 0xFFCCE5FF
        )
        
        val ALL_THEMES = listOf(DEFAULT, MODERN, FOREST, OCEAN)
    }
}
```

### Data Persistence

#### Local Storage Structure
```
SharedPreferences Keys:
- "selected_theme" -> AppTheme (JSON)
- "recent_time_controls" -> List<TimeControl> (JSON)
- "custom_time_controls" -> List<TimeControl> (JSON)
- "last_used_time_control" -> TimeControl (JSON)
```

## UI Components Architecture

### Screen Composition

#### MainActivity
```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GameClockTheme {
                GameClockApp()
            }
        }
    }
}
```

#### Main Composables Structure

```
GameClockApp
├── GameScreen (Main timer interface)
│   ├── PlayerTimerArea (Top - Player 2, rotated 180°)
│   ├── ControlButtonsOverlay (Center)
│   └── PlayerTimerArea (Bottom - Player 1)
├── TimeControlBottomSheet
│   ├── RecentTimeControlsSection
│   ├── CustomTimeControlsSection
│   └── PresetTimeControlsSection
├── CustomTimeControlDialog
└── SettingsBottomSheet
    └── ThemeSelectionSection
```

### Key Composables

#### PlayerTimerArea
```kotlin
@Composable
fun PlayerTimerArea(
    player: Player,
    timeInSeconds: Long,
    isActive: Boolean,
    isPaused: Boolean,
    theme: AppTheme,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Large timer display with background color based on theme and player
    // Handles touch interactions for switching turns
    // Animates size changes based on active state
}
```

#### ControlButtonsOverlay
```kotlin
@Composable
fun ControlButtonsOverlay(
    gameState: GameState,
    onPlayClick: () -> Unit,
    onPauseClick: () -> Unit,
    onResetClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onTimeControlClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Floating action buttons positioned at center
    // Shows different button combinations based on game state
    // Animated transitions between states
}
```

## Error Handling

### Error Types and Handling Strategy

#### Timer Precision Errors
- **Issue**: Android timer drift or background processing delays
- **Solution**: Use `System.currentTimeMillis()` for precise time tracking, store start time and calculate elapsed time rather than decrementing
- **Fallback**: Implement timer correction mechanism that adjusts for drift

#### Data Persistence Errors
- **Issue**: SharedPreferences corruption or serialization failures
- **Solution**: Implement try-catch blocks with fallback to default values
- **Recovery**: Clear corrupted data and reinitialize with defaults

#### Memory Management
- **Issue**: Timer continues running when app is backgrounded
- **Solution**: Implement proper lifecycle management with `onPause()`/`onResume()`
- **Cleanup**: Cancel coroutines and timers in `onCleared()`

#### State Consistency Errors
- **Issue**: UI state becomes inconsistent with game logic
- **Solution**: Use single source of truth pattern with StateFlow
- **Validation**: Implement state validation before UI updates

### Error Recovery Mechanisms

```kotlin
class GameViewModel {
    private fun handleTimerError(error: Throwable) {
        when (error) {
            is CancellationException -> {
                // Timer was cancelled, normal operation
            }
            else -> {
                // Log error and reset to safe state
                resetGame()
                // Show user-friendly error message
            }
        }
    }
}
```

## Testing Strategy

### Unit Testing

#### ViewModel Testing
- Test game state transitions
- Test timer logic with fake time sources
- Test time control management
- Test error handling scenarios

#### Repository Testing
- Test data persistence operations
- Test serialization/deserialization
- Test error recovery mechanisms

### Integration Testing

#### UI Testing with Compose
- Test user interactions (tap to switch, pause/resume)
- Test navigation between screens
- Test theme changes
- Test time control selection

#### End-to-End Testing
- Complete game flow testing
- Settings persistence testing
- Custom time control creation and usage

### Testing Tools
- **JUnit 5** for unit tests
- **Mockk** for mocking
- **Compose Testing** for UI tests
- **Turbine** for Flow testing
- **Robolectric** for Android unit tests

## Performance Considerations

### Timer Accuracy
- Use coroutines with `Dispatchers.Main.immediate` for UI updates
- Implement time drift correction
- Store absolute timestamps rather than relative counters

### Memory Optimization
- Use `StateFlow` instead of `LiveData` for better performance
- Implement proper lifecycle management
- Cancel background operations when not needed

### UI Performance
- Use `LazyColumn` for time control lists
- Implement proper recomposition scoping
- Use `remember` and `derivedStateOf` appropriately

### Battery Optimization
- Pause timers when app is backgrounded
- Use efficient animation APIs
- Minimize wake locks and background processing

## Accessibility

### Material Design Accessibility
- Implement proper content descriptions
- Ensure minimum touch target sizes (48dp)
- Support TalkBack screen reader
- Provide sufficient color contrast ratios

### Inclusive Design
- Support large text sizes
- Provide haptic feedback for interactions
- Ensure keyboard navigation support
- Test with accessibility services enabled

## Platform-Specific Considerations

### Android Lifecycle
- Handle configuration changes (rotation)
- Manage timer state during app backgrounding
- Implement proper memory cleanup

### Material Design Integration
- Use Material 3 color system
- Implement proper elevation and shadows
- Follow Material motion principles
- Use appropriate Material components (FAB, BottomSheet, etc.)

### Hardware Integration
- Implement haptic feedback for button presses
- Support different screen sizes and densities
- Handle edge-to-edge display on modern devices
- Optimize for foldable devices