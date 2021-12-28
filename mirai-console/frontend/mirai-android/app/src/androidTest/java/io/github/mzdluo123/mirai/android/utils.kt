package io.github.mzdluo123.mirai.android;

import android.os.IBinder
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.test.espresso.Root
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher


internal fun childAtPosition(
    parentMatcher: Matcher<View>, position: Int
): Matcher<View> {

    return object : TypeSafeMatcher<View>() {
        override fun describeTo(description: Description) {
            description.appendText("Child at position $position in parent ")
            parentMatcher.describeTo(description)
        }

        public override fun matchesSafely(view: View): Boolean {
            val parent = view.parent
            return parent is ViewGroup && parentMatcher.matches(parent)
                    && view == parent.getChildAt(position)
        }
    }
}


class ToastMatcher : TypeSafeMatcher<Root>() {
    override fun describeTo(description: Description) {
        description.appendText("is toast")
    }

    public override fun matchesSafely(root: Root): Boolean {
        val type: Int = root.getWindowLayoutParams().get().type
        if (type == WindowManager.LayoutParams.TYPE_TOAST) {
            val windowToken: IBinder = root.getDecorView().getWindowToken()
            val appToken: IBinder = root.getDecorView().getApplicationWindowToken()
            if (windowToken === appToken) {
                //means this window isn't contained by any other windows.
                return true
            }
        }
        return false
    }
}
