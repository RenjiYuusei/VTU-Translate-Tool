package com.vtu.translate.data.model

/**
 * Sealed class để quản lý trạng thái UI một cách toàn diện.
 * Giúp xử lý các trạng thái Loading, Success và Error một cách nhất quán.
 *
 * @param T Kiểu dữ liệu của trạng thái thành công
 */
sealed class UiState<out T> {
    /**
     * Trạng thái đang tải dữ liệu
     */
    object Loading : UiState<Nothing>()
    
    /**
     * Trạng thái tải dữ liệu thành công
     *
     * @param data Dữ liệu đã tải thành công
     */
    data class Success<T>(val data: T) : UiState<T>()
    
    /**
     * Trạng thái xảy ra lỗi
     *
     * @param message Thông báo lỗi
     * @param exception Exception gây ra lỗi (nếu có)
     */
    data class Error(val message: String, val exception: Throwable? = null) : UiState<Nothing>()
}