package com.leshen.letseatmobile

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.google.firebase.auth.FirebaseAuth
import com.leshen.letseatmobile.login.SignInActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.lang.Thread.sleep
import org.hamcrest.Matcher

class LoginActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    private lateinit var auth: FirebaseAuth

    @Before
    fun setup() {
        auth = FirebaseAuth.getInstance()
        auth.signOut()
    }

    @After
    fun cleanup() {
        auth.signOut()
    }

    @Test
    fun testLoginWithValidCredentials() {
        val validEmail = "test@example.com"
        val validPassword = "testPassword"
        ActivityScenario.launch(SignInActivity::class.java)

        onView(withId(R.id.etSinInEmail)).perform(ViewActions.typeText(validEmail))
        onView(withId(R.id.etSinInPassword)).perform(ViewActions.typeText(validPassword))

        onView(withId(R.id.btnSignIn)).perform(click())
        ActivityScenario.launch(MainActivity::class.java)
    }
    @Test
    fun testLoginWithInvalidCredentials() {
        val invalidEmail = "invalid@example.com"
        val invalidPassword = "invalidPassword"
        ActivityScenario.launch(SignInActivity::class.java)

        onView(withId(R.id.etSinInEmail)).perform(replaceText(invalidEmail))
        onView(withId(R.id.etSinInPassword)).perform(replaceText(invalidPassword))

        onView(withId(R.id.btnSignIn)).perform(click())

        sleep(1000)

        assertDisplayed(R.id.etSinInEmail)
    }

}


class AddToFavoriteTest {

    @Test
    fun testAddAndDeleteFromFavorites() {

        ActivityScenario.launch(MainActivity::class.java)
        Thread.sleep(5000)

        onView(withId(R.id.recyclerView))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<RestaurantListAdapter.ViewHolder>(
                    0,
                    CustomViewAction.clickChildViewWithId(R.id.listFavoriteButton)
                )
            )

        Thread.sleep(4000)

        onView(withId(R.id.favourites)).perform(click())

        Thread.sleep(4000)

        onView(withId(R.id.home)).perform(click())

        Thread.sleep(4000)

        onView(withId(R.id.recyclerView))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<RestaurantListAdapter.ViewHolder>(
                    0,
                    CustomViewAction.clickChildViewWithId(R.id.listFavoriteButton)
                )
            )

        Thread.sleep(4000)

        onView(withId(R.id.favourites)).perform(click())

        Thread.sleep(4000)
    }
}


object CustomViewAction {
    fun clickChildViewWithId(id: Int): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return ViewMatchers.isAssignableFrom(RecyclerView::class.java)
            }

            override fun getDescription(): String {
                return "Click on child view with specified id."
            }

            override fun perform(uiController: UiController?, view: View?) {
                val v = view?.findViewById<View>(id)
                v?.performClick()
            }
        }
    }
}