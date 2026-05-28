package fp.practices.ocularis_mobile.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fp.practices.ocularis_mobile.data.model.DashboardVisualContent
import fp.practices.ocularis_mobile.data.repository.DashboardVisualRepository
import kotlinx.coroutines.launch

class DashboardVisualViewModel(
    private val repository: DashboardVisualRepository = DashboardVisualRepository()
) : ViewModel() {
    private val _visualContent = MutableLiveData<DashboardVisualContent?>(null)
    val visualContent: LiveData<DashboardVisualContent?> = _visualContent

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    init {
        loadVisualContent()
    }

    fun loadVisualContent() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            runCatching { repository.getVisualContent() }
                .onSuccess { _visualContent.value = it }
                .onFailure { _error.value = it.message ?: "Error al cargar las secciones" }
            _isLoading.value = false
        }
    }
}

