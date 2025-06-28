package com.vtu.translate.data.util

import com.vtu.translate.data.model.Event
import com.vtu.translate.data.repository.LogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.IOException
import retrofit2.HttpException

/**
 * Lớp xử lý lỗi tập trung cho toàn bộ ứng dụng.
 * Cung cấp các phương thức để xử lý và hiển thị lỗi một cách nhất quán.
 */
class ErrorHandler(
    private val logRepository: LogRepository
) {
    private val _errorEvent = MutableStateFlow<Event<String>?>(null)
    val errorEvent: StateFlow<Event<String>?> = _errorEvent
    
    /**
     * Xử lý lỗi và tạo thông báo lỗi phù hợp
     *
     * @param error Lỗi cần xử lý
     * @param logError Có ghi log lỗi hay không
     * @return Thông báo lỗi đã được xử lý
     */
    fun handleError(error: Throwable, logError: Boolean = true): String {
        val errorMessage = when (error) {
            is HttpException -> handleHttpException(error)
            is IOException -> "Lỗi kết nối mạng. Vui lòng kiểm tra kết nối của bạn."
            else -> error.message ?: "Đã xảy ra lỗi không xác định."
        }
        
        if (logError) {
            logRepository.logError("Lỗi: $errorMessage")
        }
        
        _errorEvent.value = Event(errorMessage)
        return errorMessage
    }
    
    /**
     * Xử lý các lỗi HTTP cụ thể
     *
     * @param error Lỗi HTTP cần xử lý
     * @return Thông báo lỗi HTTP đã được xử lý
     */
    private fun handleHttpException(error: HttpException): String {
        return when (error.code()) {
            429 -> "Đạt giới hạn tốc độ API. Vui lòng thử lại sau."
            401 -> "API key không hợp lệ. Vui lòng kiểm tra lại."
            403 -> "Không có quyền truy cập API. Vui lòng kiểm tra API key."
            404 -> "Không tìm thấy tài nguyên yêu cầu."
            500, 502, 503, 504 -> "Lỗi máy chủ. Vui lòng thử lại sau."
            else -> "Lỗi HTTP: ${error.code()}"
        }
    }
    
    /**
     * Xóa sự kiện lỗi hiện tại sau khi đã được xử lý
     */
    fun clearError() {
        _errorEvent.value = null
    }
}