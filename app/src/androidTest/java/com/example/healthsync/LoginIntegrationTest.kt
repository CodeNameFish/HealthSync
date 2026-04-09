package com.example.healthsync

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.healthsync.ui.auth.LoginActivity
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginIntegrationTest {

    private lateinit var scenario: ActivityScenario<LoginActivity>

    @Before
    fun setUp() {
        scenario = ActivityScenario.launch(LoginActivity::class.java)
        Thread.sleep(3000) // Wait for activity to fully load
    }

    @Test
    fun typingInEmailFieldWorks() {
        onView(withId(R.id.etEmail))
            .perform(typeText("test@email.com"), closeSoftKeyboard())
        onView(withId(R.id.etEmail))
            .check(matches(withText("test@email.com")))
    }

    @Test
    fun typingInPasswordFieldWorks() {
        onView(withId(R.id.etPassword))
            .perform(typeText("password123"), closeSoftKeyboard())
        onView(withId(R.id.etPassword))
            .check(matches(withText("password123")))
    }

    @Test
    fun loginButtonIsDisplayed() {
        onView(withId(R.id.btnLogin))
            .check(matches(isDisplayed()))
    }

    @Test
    fun registerLinkIsDisplayed() {
        onView(withId(R.id.tvRegister))
            .check(matches(isDisplayed()))
    }
}