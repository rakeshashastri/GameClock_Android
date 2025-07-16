package com.example.gameclock.ui

import com.example.gameclock.models.AppTheme
import com.example.gameclock.repositories.PreferencesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ThemeViewModelTest {

    private lateinit var preferencesRepository: PreferencesRepository
    private lateinit var viewModel: ThemeViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        preferencesRepository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads current theme from repository`() = runTest {
        // Given
        val expectedTheme = AppTheme.MODERN
        coEvery { preferencesRepository.getSelectedTheme() } returns expectedTheme

        // When
        viewModel = ThemeViewModel(preferencesRepository)
        advanceUntilIdle()

        // Then
        assertEquals(expectedTheme, viewModel.currentTheme.first())
        coVerify { preferencesRepository.getSelectedTheme() }
    }

    @Test
    fun `init uses default theme when repository throws exception`() = runTest {
        // Given
        coEvery { preferencesRepository.getSelectedTheme() } throws RuntimeException("Test error")

        // When
        viewModel = ThemeViewModel(preferencesRepository)
        advanceUntilIdle()

        // Then
        assertEquals(AppTheme.DEFAULT, viewModel.currentTheme.first())
    }

    @Test
    fun `selectTheme saves theme to repository and updates current theme`() = runTest {
        // Given
        coEvery { preferencesRepository.getSelectedTheme() } returns AppTheme.DEFAULT
        coEvery { preferencesRepository.saveTheme(any()) } returns Unit
        viewModel = ThemeViewModel(preferencesRepository)
        advanceUntilIdle()

        val newTheme = AppTheme.OCEAN

        // When
        viewModel.selectTheme(newTheme)
        advanceUntilIdle()

        // Then
        assertEquals(newTheme, viewModel.currentTheme.first())
        coVerify { preferencesRepository.saveTheme(newTheme) }
    }

    @Test
    fun `selectTheme keeps current theme when repository save fails`() = runTest {
        // Given
        val initialTheme = AppTheme.FOREST
        coEvery { preferencesRepository.getSelectedTheme() } returns initialTheme
        coEvery { preferencesRepository.saveTheme(any()) } throws RuntimeException("Save failed")
        viewModel = ThemeViewModel(preferencesRepository)
        advanceUntilIdle()

        val newTheme = AppTheme.OCEAN

        // When
        viewModel.selectTheme(newTheme)
        advanceUntilIdle()

        // Then
        assertEquals(initialTheme, viewModel.currentTheme.first())
        coVerify { preferencesRepository.saveTheme(newTheme) }
    }

    @Test
    fun `availableThemes returns all predefined themes`() = runTest {
        // Given
        coEvery { preferencesRepository.getSelectedTheme() } returns AppTheme.DEFAULT
        viewModel = ThemeViewModel(preferencesRepository)

        // When
        val availableThemes = viewModel.availableThemes

        // Then
        assertEquals(AppTheme.ALL_THEMES, availableThemes)
        assertEquals(4, availableThemes.size)
        assertEquals(AppTheme.DEFAULT, availableThemes[0])
        assertEquals(AppTheme.MODERN, availableThemes[1])
        assertEquals(AppTheme.FOREST, availableThemes[2])
        assertEquals(AppTheme.OCEAN, availableThemes[3])
    }

    @Test
    fun `currentTheme StateFlow emits theme changes`() = runTest {
        // Given
        coEvery { preferencesRepository.getSelectedTheme() } returns AppTheme.DEFAULT
        coEvery { preferencesRepository.saveTheme(any()) } returns Unit
        viewModel = ThemeViewModel(preferencesRepository)
        advanceUntilIdle()

        // When
        val initialTheme = viewModel.currentTheme.first()
        viewModel.selectTheme(AppTheme.MODERN)
        advanceUntilIdle()
        val updatedTheme = viewModel.currentTheme.first()

        // Then
        assertEquals(AppTheme.DEFAULT, initialTheme)
        assertEquals(AppTheme.MODERN, updatedTheme)
    }
}