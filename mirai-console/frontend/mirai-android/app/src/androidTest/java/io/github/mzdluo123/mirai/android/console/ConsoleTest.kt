package io.github.mzdluo123.mirai.android.console


import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import io.github.mzdluo123.mirai.android.R
import io.github.mzdluo123.mirai.android.TestWithIdleResources
import io.github.mzdluo123.mirai.android.activity.MainActivity
import io.github.mzdluo123.mirai.android.childAtPosition
import org.hamcrest.CoreMatchers
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf
import org.hamcrest.core.IsInstanceOf
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

@LargeTest
@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.JVM)
class ConsoleTest : TestWithIdleResources() {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)


    @Test
    fun fastLoginAndFastRestartTest() {
        val overflowMenuButton = onView(
            allOf(
                childAtPosition(
                    childAtPosition(
                        withId(R.id.toolbar),
                        2
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        overflowMenuButton.perform(click())

        val appCompatTextView = onView(
            allOf(
                withId(R.id.title), withText("设置自动登录"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.content),
                        0
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        appCompatTextView.perform(click())

        val appCompatEditText = onView(
            allOf(
                withId(R.id.qq_input),
                childAtPosition(
                    childAtPosition(
                        withId(android.R.id.custom),
                        0
                    ),
                    2
                ),
                isDisplayed()
            )
        )
        appCompatEditText.perform(replaceText("1"), closeSoftKeyboard())

        val appCompatEditText2 = onView(
            allOf(
                withId(R.id.password_input),
                childAtPosition(
                    childAtPosition(
                        withId(android.R.id.custom),
                        0
                    ),
                    3
                ),
                isDisplayed()
            )
        )
        appCompatEditText2.perform(replaceText("2"), closeSoftKeyboard())

        val appCompatButton = onView(
            allOf(
                withId(android.R.id.button1), withText("设置自动登录")

            )
        )
        appCompatButton.perform(scrollTo(), click())

        val appCompatImageButton7 = onView(
            allOf(
                childAtPosition(
                    allOf(
                        withId(R.id.toolbar),
                        childAtPosition(
                            withClassName(Matchers.`is`("com.google.android.material.appbar.AppBarLayout")),
                            0
                        )
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        appCompatImageButton7.perform(click())

        onView(withText("快速重启")).perform(click())

        onView(withId(R.id.log_text)).check(ViewAssertions.matches(isDisplayed()))
        Thread.sleep(2000)  //花两秒钟给控制台加载log

        onView(withId(R.id.log_text)).check(
            ViewAssertions.matches(
                withText(
                    CoreMatchers.containsString(
                        "自动登录"
                    )
                )
            )
        )

    }


    @Test
    fun commandInputTest() {
        val appCompatEditText = onView(
            allOf(
                withId(R.id.command_input),
                isDisplayed()
            )
        )
        appCompatEditText.perform(replaceText("help"), closeSoftKeyboard())

        val appCompatImageButton = onView(withId(R.id.commandSend_btn))
        appCompatImageButton.perform(click())

        val textView = onView(
            allOf(
                withId(R.id.log_text),
                childAtPosition(
                    allOf(
                        withId(R.id.main_scroll),
                        childAtPosition(
                            IsInstanceOf.instanceOf(android.view.ViewGroup::class.java),
                            0
                        )
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        textView.check(
            ViewAssertions.matches(
                withText(
                    CoreMatchers.containsString(
                        "android"
                    )
                )
            )
        )
    }


}
