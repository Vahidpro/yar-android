package io.github.vahidpro.yar

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.vahidpro.yar.hokm.domain.GameSettings
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HokmFlowTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun recordsPersianNumbersRestoresAnOpenHandAndResetsTheScore() {
        val app = compose.activity.application as YarApplication
        runBlocking { app.hokmRepository.newGame(GameSettings()) }

        compose.onNodeWithText("شروع بازی").performClick()
        waitForText("ثبت عددهای اعلامی")
        compose.onNodeWithText("ثبت عددهای اعلامی").performClick()

        compose.onNodeWithTag("first_bid").performTextInput("۷")
        compose.onNodeWithTag("second_bid").performTextInput("٥")
        compose.onNodeWithText("ثبت اعلام و شروع دست").assertIsNotEnabled()
        compose.onNodeWithTag("second_bid").performTextReplacement("۶")
        compose.onNodeWithText("ثبت اعلام و شروع دست").performScrollTo().performClick()
        waitForText("ثبت نتیجهٔ دست")

        compose.activityRule.scenario.recreate()
        waitForText("ثبت نتیجهٔ دست")
        compose.onNodeWithText("ثبت نتیجهٔ دست").performClick()
        compose.onNodeWithTag("first_taken").performTextInput("۸")
        compose.onNodeWithText("ذخیرهٔ نتیجه").performScrollTo().performClick()
        waitForText("۱ دست ثبت‌شده")
        assertScore("first_total", "۷")
        assertScore("second_total", "۰")

        compose.onNodeWithContentDescription("گزینه‌های بازی").performClick()
        compose.onNodeWithText("صفر کردن امتیازها").performClick()
        compose.onNodeWithText("صفر کردن", substring = false).performClick()
        waitForText("۰ دست ثبت‌شده")
        assertScore("first_total", "۰")
    }

    private fun waitForText(text: String) {
        compose.waitUntil(timeoutMillis = 8_000) {
            compose.onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun assertScore(tag: String, value: String) {
        compose.onNode(hasText(value) and hasAnyAncestor(hasTestTag(tag)), useUnmergedTree = true).assertExists()
    }
}
