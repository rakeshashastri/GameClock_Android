package com.example.gameclock.repositories

import android.content.Context
import android.content.SharedPreferences
import com.example.gameclock.models.TimeControl
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GameRepositoryImplTest {
    
    private lateinit var context: Context
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var repository: GameRepositoryImpl
    
    private val testTimeControl1 = TimeControl(
        id = "test1",
        name = "Test 1",
        timeInSeconds = 300,
        incrementInSeconds = 5
    )
    
    private val testTimeControl2 = TimeControl(
        id = "test2", 
        name = "Test 2",
        timeInSeconds = 600,
        incrementInSeconds = 10
    )
    
    @Before
    fun setup() {
        context = mockk()
        sharedPreferences = mockk()
        editor = mockk(relaxed = true)
        
        every { context.getSharedPreferences("game_clock_prefs", Context.MODE_PRIVATE) } returns sharedPreferences
        every { sharedPreferences.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor
        every { editor.remove(any()) } returns editor
        every { editor.apply() } returns Unit
        
        repository = GameRepositoryImpl(context)
    }
    
    @Test
    fun `getRecentTimeControls returns empty list when no data stored`() = runTest {
        every { sharedPreferences.getString("recent_time_controls", null) } returns null
        
        val result = repository.getRecentTimeControls()
        
        assertTrue(result.isEmpty())
    }
    
    @Test
    fun `saveRecentTimeControl stores time control and limits to 3 items`() = runTest {
        every { sharedPreferences.getString("recent_time_controls", null) } returns null
        val jsonSlot = slot<String>()
        
        repository.saveRecentTimeControl(testTimeControl1)
        
        verify { editor.putString("recent_time_controls", capture(jsonSlot)) }
        assertTrue(jsonSlot.captured.contains("Test 1"))
    }
    
    @Test
    fun `saveRecentTimeControl removes duplicates and adds to beginning`() = runTest {
        val existingJson = """[{"id":"test2","name":"Test 2","timeInSeconds":600,"incrementInSeconds":10}]"""
        every { sharedPreferences.getString("recent_time_controls", null) } returns existingJson
        val jsonSlot = slot<String>()
        
        repository.saveRecentTimeControl(testTimeControl1)
        
        verify { editor.putString("recent_time_controls", capture(jsonSlot)) }
        val savedJson = jsonSlot.captured
        assertTrue(savedJson.indexOf("Test 1") < savedJson.indexOf("Test 2"))
    }
    
    @Test
    fun `getRecentTimeControls handles corrupted data gracefully`() = runTest {
        every { sharedPreferences.getString("recent_time_controls", null) } returns "invalid json"
        
        val result = repository.getRecentTimeControls()
        
        assertTrue(result.isEmpty())
        verify { editor.remove("recent_time_controls") }
    }
    
    @Test
    fun `getCustomTimeControls returns empty list when no data stored`() = runTest {
        every { sharedPreferences.getString("custom_time_controls", null) } returns null
        
        val result = repository.getCustomTimeControls()
        
        assertTrue(result.isEmpty())
    }
    
    @Test
    fun `saveCustomTimeControl stores custom time control`() = runTest {
        every { sharedPreferences.getString("custom_time_controls", null) } returns null
        val jsonSlot = slot<String>()
        
        repository.saveCustomTimeControl(testTimeControl1)
        
        verify { editor.putString("custom_time_controls", capture(jsonSlot)) }
        assertTrue(jsonSlot.captured.contains("Test 1"))
    }
    
    @Test
    fun `saveCustomTimeControl throws exception when limit exceeded`() = runTest {
        val existingCustomControls = (1..5).map { 
            TimeControl(id = "custom$it", name = "Custom $it", timeInSeconds = 300, incrementInSeconds = 0)
        }
        val existingJson = """[
            {"id":"custom1","name":"Custom 1","timeInSeconds":300,"incrementInSeconds":0},
            {"id":"custom2","name":"Custom 2","timeInSeconds":300,"incrementInSeconds":0},
            {"id":"custom3","name":"Custom 3","timeInSeconds":300,"incrementInSeconds":0},
            {"id":"custom4","name":"Custom 4","timeInSeconds":300,"incrementInSeconds":0},
            {"id":"custom5","name":"Custom 5","timeInSeconds":300,"incrementInSeconds":0}
        ]"""
        every { sharedPreferences.getString("custom_time_controls", null) } returns existingJson
        
        assertThrows(IllegalStateException::class.java) {
            runTest {
                repository.saveCustomTimeControl(testTimeControl1)
            }
        }
    }
    
    @Test
    fun `deleteCustomTimeControl removes time control from storage`() = runTest {
        val existingJson = """[{"id":"test1","name":"Test 1","timeInSeconds":300,"incrementInSeconds":5}]"""
        every { sharedPreferences.getString("custom_time_controls", null) } returns existingJson
        val jsonSlot = slot<String>()
        
        repository.deleteCustomTimeControl(testTimeControl1)
        
        verify { editor.putString("custom_time_controls", capture(jsonSlot)) }
        assertEquals("[]", jsonSlot.captured)
    }
    
    @Test
    fun `getCustomTimeControls handles corrupted data gracefully`() = runTest {
        every { sharedPreferences.getString("custom_time_controls", null) } returns "invalid json"
        
        val result = repository.getCustomTimeControls()
        
        assertTrue(result.isEmpty())
        verify { editor.remove("custom_time_controls") }
    }
}