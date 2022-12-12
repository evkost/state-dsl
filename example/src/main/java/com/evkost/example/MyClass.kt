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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

fun main() {
    val m = MutableStateFlow(NotifyUiState(
        infoUiState = InfoUiState(
            title = "SDfs"
        ),
        scheduleNotifyUiState = ScheduleNotifyUiState(
            infoUiState = InfoUiState(
                title = "SDfsdf"
            )
        )
    ))
    val n by notifyUiStateFlow {
        infoUiState {
            title = "Sdfsdf"
        }
        scheduleNotifyUiState {
            infoUiState {
                title = "Sdfsdfsd"
            }
        }
    }

    m.update {
        it.copy(
            scheduleNotifyUiState = it.scheduleNotifyUiState.copy(
                infoUiState = it.infoUiState.copy(
                    title = it.infoUiState.title + "dsfsgdsfg"
                )
            )
        )
    }
    m.updateScheduleNotifyUiState {
        infoUiState {
            title += "sdfs"
        }
    }

}

@StateDsl
data class NotifyUiState(
    @Updatable val scheduleNotifyUiState: ScheduleNotifyUiState = ScheduleNotifyUiState(true),
    val infoUiState: InfoUiState = InfoUiState(),
    val errorMessage: String? = null,
)

@InnerState
data class ScheduleNotifyUiState(
    val isNotifying: Boolean = false,
    val isLoading: Boolean? = false,
    val infoUiState: InfoUiState = InfoUiState(),
    val infoExpanded: Boolean = false,
    val infoItems: List<InfoUiState> = emptyList()
)

@InnerState
data class InfoUiState(
    val title: String = "",
    val text: String = ""
)