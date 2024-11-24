package com.example.localgigs.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localgigs.repository.MessageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel(private val messageRepository: MessageRepository) : ViewModel() {

    // StateFlow for messages
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> get() = _messages

    // StateFlow for loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    // StateFlow for error message
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> get() = _error

    // Fetch messages for a conversation
    fun getMessages(conversationId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                messageRepository.getMessages(conversationId) { fetchedMessages ->
                    _messages.value = fetchedMessages // Update the messages
                }
            } catch (e: Exception) {
                _error.value = "Failed to load messages: ${e.message}" // Update error state
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Send a new message and refresh the conversation
    fun sendMessage(conversationId: String, messageText: String, senderId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                messageRepository.sendMessage(conversationId, messageText, senderId)
                getMessages(conversationId) // Refresh messages after sending a new one
            } catch (e: Exception) {
                _error.value = "Failed to send message: ${e.message}" // Update error state
            } finally {
                _isLoading.value = false
            }
        }
    }
}
