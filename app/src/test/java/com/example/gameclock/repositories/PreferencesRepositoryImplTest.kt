package com.example.gameclock.repositories

import android.content.Context
import android.content.SharedPreferences
import com.example.gameclock.models.AppTheme
import com.example.gameclock.models.TimeControl
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PreferencesRepositoryImplTest {
    
    private lateinit var context: Context
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var repository: PreferencesRepositoryImpl
    
    private val testTheme = AppTheme(
        id = "test",
        name = "Test Theme",
        player1Color = 0xFF123456,
        player2Color = 0xFF654321,
        isDark = false
    )
    
    private val testTimeControl = TimeControl(
        id = "test",
        name = "Test Control",
        timeInSeconds = 300,
        incrementInSeconds = 5
    )
    
    @Before
    fun setup() {
        context = mockk()
        sharedPreferences = mockk()
        editor = mockk(relaxed = true)
        
        every { context.getSharedPreferences("game_clock_preferences", Context.MODE_PRIVATE) } returns sharedPreferences
        every { sharedPreferences.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor
        every { editor.remove(any()) } returns editor
        every { editor.apply() } returns Unit
        
        repository = PreferencesRepositoryImpl(context)
    }
    
    @Test
    fun `getSelectedTheme returns default theme when no data stored`() = runTest {
        every { sharedPreferences.getString("selected_theme", null) } returns null
        
        val result = repository.getSelectedTheme()
        
        assertEquals(AppTheme.DEFAULT, result)
    }
    
    @Test
    fun `saveTheme stores theme in SharedPreferences`() = runTest {
        val jsonSlot = slot<String>()
        
        repository.saveTheme(testTheme)
        
        verify { editor.putString("selected_theme", capture(jsonSlot)) }
        assertTrue(jsonSlot.captured.contains("Test Theme"))
    }
    
    @Test
    fun `getSelectedTheme returns stored theme`() = runTest {
        val themeJson = """{"id":"test","name":"Test Theme","player1Color":1193046,"player2Color":6636321,"isDark":false}"""
        every { sharedPreferences.getString("selected_theme", null) } returns themeJson
        
        val result = repository.getSelectedTheme()
        
        assertEquals("test", result.id)
        assertEquals("Test Theme", result.name)
        assertEquals(0xFF123456, result.player1Color)
        assertEquals(0xFF654321, result.player2Color)
        assertFalse(result.isDark)
    }
    
    @Test
    fun `getSelectedTheme handles corrupted data gracefully`() = runTest {
        every { sharedPreferences.getString("selected_theme", null) } returns "invalid json"
        
        val result = repository.getSelectedTheme()
        
        assertEquals(AppTheme.DEFAULT, result)
        verify { editor.remove("selected_theme") }
    }
    
    @Test
    fun `getLastUsedTimeControl returns null when no data stored`() = runTest {
        every { sharedPreferences.getString("last_used_time_control", null) } returns null
        
        val result = repository.getLastUsedTimeControl()
        
        assertNull(result)
    }
    
    @Test
    fun `saveLastUsedTimeControl stores time control in SharedPreferences`() = runTest {
        val jsonSlot = slot<String>()
        
        repository.saveLastUsedTimeControl(testTimeControl)
        
        verify { editor.putString("last_used_time_control", capture(jsonSlot)) }
        assertTrue(jsonSlot.captured.contains("Test Control"))
    }
    
    @Test
    fun `getLastUsedTimeControl returns stored time control`() = runTest {
        val timeControlJson = """{"id":"test","name":"Test Control","timeInSeconds":300,"incrementInSeconds":5}"""
        every { sharedPreferences.getString("last_used_time_control", null) } returns timeControlJson
        
        val result = repository.getLastUsedTimeControl()
        
        assertNotNull(result)
        assertEquals("test", result!!.id)
        assertEquals("Test Control", result.name)
        assertEquals(300L, result.timeInSeconds)
        assertEquals(5L, result.incrementInSeconds)
    }
    
    @Test
    fun `getLastUsedTimeControl handles corrupted data gracefully`() = runTest {
        every { sharedPreferences.getString("last_used_time_control", null) } returns "invalid json"
        
        val result = repository.getLastUsedTimeControl()
        
        assertNull(result)
        verify { editor.remove("last_used_time_control") }
    }
}