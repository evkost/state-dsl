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

import com.evkost.statedsl.core.builder.DslBuilder
import com.evkost.statedsl.extension.flow.FlowCreate
import com.evkost.statedsl.extension.flow.FlowUpdate
import com.evkost.statedsl.extension.flow.InlineUpdate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

fun main() {
    val m = MutableStateFlow(
        NotifyUiState(
            infoUiState = InfoUiState(
                title = "SDfs"
            ),
            scheduleNotifyUiState = ScheduleNotifyUiState(
                infoUiState = InfoUiState(
                    title = "SDfsdf"
                )
            )
        )
    )
    val n = notifyUiStateFlow {
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
    m.updateState {
        scheduleNotifyUiState {
            infoUiState {
                title += "sddsfsdf"
            }
        }
    }

    m.updateErrorMessage {
        ""
    }

    NotifyUiState {
        errorMessage = "dafsdf"
    }








    var p = NotifyUiState(
        infoUiState = InfoUiState(
            title = "SDfs"
        ),
        scheduleNotifyUiState = ScheduleNotifyUiState(
            infoUiState = InfoUiState(
                title = "SDfsdf"
            )
        )
    )




    p = p.copy(
        scheduleNotifyUiState = p.scheduleNotifyUiState.copy(
            infoUiState = p.scheduleNotifyUiState.infoUiState.copy(
                title = p.scheduleNotifyUiState.infoUiState.title + "dshfudf"
            )
        )
    )



    p = NotifyUiState {
        scheduleNotifyUiState {
            infoUiState {
                title += "SDfsdfsdf"
            }
        }
    }



}

@DslBuilder
@FlowCreate
@FlowUpdate
data class NotifyUiState(
    val scheduleNotifyUiState: ScheduleNotifyUiState = ScheduleNotifyUiState(true),
    val infoUiState: InfoUiState = InfoUiState(),
    @InlineUpdate val errorMessage: String? = null,
)

@DslBuilder
@FlowUpdate
data class ScheduleNotifyUiState(
    val isNotifying: Boolean = false,
    val isLoading: Boolean? = false,
    @InlineUpdate val infoUiState: InfoUiState = InfoUiState(),
    val infoExpanded: Boolean = false,
    val infoItems: List<InfoUiState> = emptyList()
)

@DslBuilder
data class InfoUiState(
    val title: String = "",
    val text: String = ""
)