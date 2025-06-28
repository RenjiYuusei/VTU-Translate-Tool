package com.vtutranslate.models

enum class ModelType {
    GEMINI, DEEPSEEK
}

enum class TranslationModel(val modelId: String, val displayName: String, val type: ModelType) {
    GEMMA_27B("google/gemma-3-27b-it:free", "Gemma 3 27B IT", ModelType.GEMINI),
    GEMINI_2_FLASH("google/gemini-2.0-flash-exp:free", "Gemini 2.0 Flash", ModelType.GEMINI),
    DEEPSEEK_R1("deepseek/deepseek-r1-0528:free", "DeepSeek R1 0528", ModelType.DEEPSEEK),
    DEEPSEEK_V3("deepseek/deepseek-chat-v3-0324:free", "DeepSeek V3 0324", ModelType.DEEPSEEK)
} 