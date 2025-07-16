# Implementation Plan

- [x] 1. Set up core data models and enums
  - Create GameState and Player enums with proper serialization
  - Implement TimeControl data class with preset constants and validation
  - Create AppTheme data class with predefined theme constants
  - _Requirements: 1.1, 2.1, 4.1, 6.1, 10.1_

- [x] 2. Implement GameUiState and state management foundation
  - Create GameUiState data class with all required properties
  - Write unit tests for state transitions and validation
  - _Requirements: 1.1, 1.2, 5.1, 8.3_

- [x] 3. Create repository interfaces and implementations
  - [x] 3.1 Implement GameRepository interface and SharedPreferences-based implementation
    - Write GameRepository interface with time control management methods
    - Create GameRepositoryImpl with SharedPreferences storage
    - Implement JSON serialization for TimeControl objects
    - Write unit tests for repository operations
    - _Requirements: 2.5, 3.1, 3.4, 9.1, 9.3_

  - [x] 3.2 Implement PreferencesRepository for theme and settings persistence
    - Write PreferencesRepository interface for theme and settings
    - Create PreferencesRepositoryImpl with SharedPreferences storage
    - Implement theme persistence and retrieval methods
    - Write unit tests for preferences operations
    - _Requirements: 4.4, 3.4_

- [x] 4. Implement GameViewModel with timer logic
  - [x] 4.1 Create GameViewModel with basic state management
    - Implement GameViewModel with StateFlow for UI state
    - Add methods for startGame, pauseGame, resetGame, switchPlayer
    - Implement timer logic using coroutines and System.currentTimeMillis()
    - Write unit tests for game state transitions
    - _Requirements: 1.2, 1.3, 1.4, 1.5, 1.6, 8.2, 8.3, 8.4_

  - [x] 4.2 Add time control management to GameViewModel
    - Implement setTimeControl and setDifferentTimeControls methods
    - Add recent time controls tracking functionality
    - Implement custom time controls management
    - Write unit tests for time control operations
    - _Requirements: 2.2, 2.6, 3.1, 3.2, 9.1, 9.2_

  - [x] 4.3 Implement increment functionality in timer logic
    - Add increment time application when switching players
    - Ensure increment is not applied on game reset
    - Write unit tests for increment behavior
    - _Requirements: 6.1, 6.2, 6.5_

- [x] 5. Create ThemeViewModel for theme management
  - Implement ThemeViewModel with theme selection logic
  - Add StateFlow for current theme and available themes list
  - Implement theme persistence through PreferencesRepository
  - Write unit tests for theme management
  - _Requirements: 4.1, 4.2, 4.4, 4.5_

- [x] 6. Implement core UI composables
  - [x] 6.1 Create PlayerTimerArea composable
    - Implement PlayerTimerArea with timer display and touch handling
    - Add visual state changes for active/inactive/paused states
    - Implement size animations based on game state
    - Add haptic feedback for touch interactions
    - _Requirements: 1.1, 1.3, 1.6, 5.1, 5.2, 5.3, 7.2, 7.3_

  - [x] 6.2 Create ControlButtonsOverlay composable
    - Implement floating action buttons for game controls
    - Add conditional button display based on game state
    - Implement smooth transitions between button states
    - Add proper touch target sizing for accessibility
    - _Requirements: 1.2, 1.5, 7.4, 8.1, 10.3_

- [x] 7. Implement time control selection UI
  - [x] 7.1 Create TimeControlBottomSheet composable
    - Implement bottom sheet with preset categories (Bullet, Blitz, Rapid)
    - Add recent time controls section at the top
    - Create custom time controls section with delete functionality
    - Write Compose UI tests for time control selection
    - _Requirements: 2.1, 3.3, 9.4, 9.5_

  - [x] 7.2 Create CustomTimeControlDialog composable
    - Implement dialog for creating custom time controls
    - Add input validation for minutes (1-60) and increment (0-60)
    - Implement save functionality with preset limit enforcement
    - Write Compose UI tests for custom time control creation
    - _Requirements: 2.3, 2.4, 6.4, 9.2_

- [x] 8. Create settings and theme selection UI
  - Implement SettingsBottomSheet with theme selection
  - Add theme preview functionality
  - Implement immediate theme application
  - Write Compose UI tests for settings interactions
  - _Requirements: 4.1, 4.2, 4.5, 10.2, 10.4_

- [x] 9. Implement main game screen composition
  - [x] 9.1 Create GameScreen composable with layout structure
    - Implement main screen layout with two PlayerTimerArea components
    - Add ControlButtonsOverlay in center position
    - Implement portrait orientation layout
    - Add proper spacing and Material Design styling
    - _Requirements: 1.1, 7.1, 10.1, 10.5_

  - [x] 9.2 Integrate bottom sheets and dialogs with GameScreen
    - Connect TimeControlBottomSheet to game screen
    - Integrate CustomTimeControlDialog with time control flow
    - Connect SettingsBottomSheet to game screen
    - Implement proper modal presentation patterns
    - _Requirements: 2.1, 2.3, 4.1, 10.4_

- [x] 10. Implement MainActivity and app-level setup
  - [x] 10.1 Set up MainActivity with Hilt dependency injection
    - Configure MainActivity with proper Android lifecycle handling
    - Set up Hilt dependency injection for ViewModels
    - Implement GameClockTheme wrapper for Material Design
    - Add proper configuration change handling
    - _Requirements: 7.1, 10.1_

  - [x] 10.2 Create app-level theme system
    - Implement GameClockTheme composable with Material 3 theming
    - Add dynamic color application based on selected theme
    - Implement proper color contrast and accessibility support
    - Write tests for theme application
    - _Requirements: 4.2, 4.3, 10.1, 10.5_

- [ ] 11. Add winner display and game over functionality
  - Implement winner display UI when timer reaches zero
  - Add game over state handling with disabled interactions
  - Implement proper visual feedback for game completion
  - Write tests for game over scenarios
  - _Requirements: 1.4, 5.4, 8.4_

- [ ] 12. Implement responsive design and accessibility
  - Add responsive text scaling for different screen sizes
  - Implement proper content descriptions for accessibility
  - Add TalkBack support and keyboard navigation
  - Test with accessibility services enabled
  - _Requirements: 7.3, 7.4, 7.5_

- [ ] 13. Add error handling and edge cases
  - Implement error recovery for data persistence failures
  - Add timer drift correction mechanisms
  - Handle app backgrounding and lifecycle edge cases
  - Write tests for error scenarios and recovery
  - _Requirements: All requirements - error handling_

- [ ] 14. Create comprehensive test suite
  - [ ] 14.1 Write unit tests for ViewModels and repositories
    - Test all GameViewModel methods and state transitions
    - Test ThemeViewModel theme management
    - Test repository implementations with mock data
    - Achieve high test coverage for business logic
    - _Requirements: All requirements - testing_

  - [ ] 14.2 Write Compose UI tests for user interactions
    - Test complete game flow from start to finish
    - Test time control selection and application
    - Test theme changes and persistence
    - Test custom time control creation and management
    - _Requirements: All requirements - UI testing_

- [x] 15. Final integration and polish
  - Integrate all components into complete working app
  - Test complete user workflows end-to-end
  - Add final polish for animations and transitions
  - Verify all requirements are met through manual testing
  - _Requirements: All requirements - integration_