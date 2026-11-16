package com.srap.wash.logic.repository

import com.srap.wash.logic.model.base.BaseListResponse
import com.srap.wash.logic.state.Code
import com.srap.wash.logic.state.LoadingState

// Some list endpoints use null for a successful response with no history.
internal fun <T> BaseListResponse<T>.toLoadingState(
    allowNullData: Boolean = false
): LoadingState<List<T>> = when {
    code != Code.SUCCESS -> LoadingState.Error(msg)
    data != null -> LoadingState.Success(data.orEmpty())
    allowNullData -> LoadingState.Success(emptyList())
    else -> LoadingState.Error("异常")
}
