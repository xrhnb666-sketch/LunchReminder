package com.example.lunchreminder

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class NotificationMessageProviderTest {
    @Test
    fun messagesFor_usesDefaultMealMessageWhenCustomMessageIsBlank() {
        val config = ReminderConfig(lunchMessages = " \n ")

        val messages = NotificationMessageProvider.messagesFor(config, MealType.LUNCH)

        assertEquals(listOf("🍱 午饭时间到了"), messages)
    }

    @Test
    fun messagesFor_parsesMultipleLinesAndRemovesDuplicates() {
        val config = ReminderConfig(
            lunchMessages = "午饭时间到了\n别忘记吃饭\n午饭时间到了\n今天也要照顾好自己",
        )

        val messages = NotificationMessageProvider.messagesFor(config, MealType.LUNCH)

        assertEquals(
            listOf("午饭时间到了", "别忘记吃饭", "今天也要照顾好自己"),
            messages,
        )
    }

    @Test
    fun contentFor_selectsOneCustomMessageAndMealBody() {
        val config = ReminderConfig(
            dinnerMessages = "晚饭时间到了\n辛苦啦，先吃饭",
        )

        val content = NotificationMessageProvider.contentFor(
            config = config,
            mealType = MealType.DINNER,
            random = Random(0),
        )

        assertTrue(content.title in listOf("晚饭时间到了", "辛苦啦，先吃饭"))
        assertEquals("辛苦一天，记得好好吃饭", content.message)
    }
}
