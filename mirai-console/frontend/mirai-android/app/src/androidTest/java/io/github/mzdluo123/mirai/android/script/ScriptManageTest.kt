package io.github.mzdluo123.mirai.android.script


import androidx.navigation.findNavController
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import io.github.mzdluo123.mirai.android.R
import io.github.mzdluo123.mirai.android.TestWithIdleResources
import io.github.mzdluo123.mirai.android.ToastMatcher
import io.github.mzdluo123.mirai.android.activity.MainActivity
import io.github.mzdluo123.mirai.android.childAtPosition
import org.hamcrest.Matchers.allOf
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters


@ExperimentalStdlibApi
@LargeTest
@FixMethodOrder(MethodSorters.JVM)
@RunWith(AndroidJUnit4::class)
class ScriptManageTest : TestWithIdleResources() {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun scriptCenterInstall() {
        mActivityTestRule.runOnUiThread {
            mActivityTestRule.activity.findNavController(R.id.nav_host_fragment)
                .navigate(R.id.nav_scripts_center)
        }
        val cardView = onView(
            allOf(
                withId(R.id.cv_item),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.rcl_scripts),
                        0
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        cardView.perform(click())

        onView(withText("OK")).perform(click())
        onView(withText("导入成功！")).inRoot(ToastMatcher()).check(matches(isDisplayed()))

    }

    @Test
    fun scriptDelete() {
        mActivityTestRule.runOnUiThread {
            mActivityTestRule.activity.findNavController(R.id.nav_host_fragment)
                .navigate(R.id.nav_scripts)
        }
        val cardView2 = onView(
            allOf(
                withId(R.id.cv_item),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.script_recycler),
                        0
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        cardView2.perform(click())

        val appCompatImageButton3 = onView(
            allOf(
                withId(R.id.btn_delete), withContentDescription("delete"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.custom),
                        0
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        appCompatImageButton3.perform(click())

        onView(withText("OK")).perform(click())
        onView(withText("当前无脚本")).check(matches(isDisplayed()))
    }
}
