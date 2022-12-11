/*
 * Copyright 2022 Murat Kostoev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.evkost.example

import com.evkost.core.annotation.InnerState
import com.evkost.core.annotation.StateDsl
import com.evkost.core.annotation.Updatable

fun main() {
    val m by notifyUiStateFlow()
    m.updateState {
        scheduleNotifyUiState6 {
            isNotifying = true
            isLoading = false
            infoExpanded = true
        }

        scheduleNotifyUiState7 {
            isNotifying = true
            isLoading = false
            infoExpanded = true
        }

        errorMessage = "SDfsdfsdf"
    }

    m.updateScheduleNotifyUiState {
        isNotifying =false
    }
}

@StateDsl
data class NotifyUiState(
    @Updatable val scheduleNotifyUiState: ScheduleNotifyUiState = ScheduleNotifyUiState(true),
    val scheduleNotifyUiState1: ScheduleNotifyUiState = ScheduleNotifyUiState(true),
    val scheduleNotifyUiState2: ScheduleNotifyUiState = ScheduleNotifyUiState(true),
    val scheduleNotifyUiState3: ScheduleNotifyUiState = ScheduleNotifyUiState(true),
    val scheduleNotifyUiState4: ScheduleNotifyUiState = ScheduleNotifyUiState(true),
    val scheduleNotifyUiState5: ScheduleNotifyUiState = ScheduleNotifyUiState(true),
    val scheduleNotifyUiState6: ScheduleNotifyUiState = ScheduleNotifyUiState(true),
    val scheduleNotifyUiState7: ScheduleNotifyUiState = ScheduleNotifyUiState(true),
    val scheduleNotifyUiState8: ScheduleNotifyUiState = ScheduleNotifyUiState(true),
    @Updatable val infoUiState: InfoUiState = InfoUiState(),
    val errorMessage: String? = null,
)

@InnerState
data class ScheduleNotifyUiState(
    val isNotifying: Boolean,
    val isLoading: Boolean? = false,
    val infoExpanded: Boolean = false,
    val infoItems: List<InfoUiState> = emptyList()
)

@InnerState
data class InfoUiState(
    val title: String = "",
    val text: String = ""
)