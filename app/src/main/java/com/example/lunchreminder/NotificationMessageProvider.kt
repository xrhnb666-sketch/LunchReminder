package com.example.lunchreminder

import kotlin.random.Random

data class NotificationContent(
    val title: String,
    val message: String,
)

object NotificationMessageProvider {
    fun contentFor(
        config: ReminderConfig,
        mealType: MealType,
        random: Random = Random.Default,
    ): NotificationContent {
        val titles = messagesFor(config, mealType)
        return NotificationContent(
            title = titles[random.nextInt(titles.size)],
            message = bodyFor(mealType),
        )
    }

    fun messagesFor(config: ReminderConfig, mealType: MealType): List<String> {
        val rawMessages = when (mealType) {
            MealType.BREAKFAST -> config.breakfastMessages
            MealType.LUNCH -> config.lunchMessages
            MealType.DINNER -> config.dinnerMessages
        }

        return parseMessages(rawMessages, defaultTitleFor(mealType))
    }

    private fun parseMessages(rawMessages: String, fallback: String): List<String> {
        return rawMessages
            .lineSequence()
            .map { message -> message.trim() }
            .filter { message -> message.isNotEmpty() }
            .distinct()
            .toList()
            .ifEmpty { listOf(fallback) }
    }

    private fun defaultTitleFor(mealType: MealType): String {
        return when (mealType) {
            MealType.BREAKFAST -> "🍳 早餐时间到了"
            MealType.LUNCH -> "🍱 午饭时间到了"
            MealType.DINNER -> "🍜 晚饭时间到了"
        }
    }

    private fun bodyFor(mealType: MealType): String {
        return when (mealType) {
            MealType.BREAKFAST -> "记得吃早餐，开启活力一天"
            MealType.LUNCH -> "记得按时吃饭"
            MealType.DINNER -> "辛苦一天，记得好好吃饭"
        }
    }
}
