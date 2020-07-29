package com.zenaton.jobManager.storages

import com.zenaton.jobManager.common.avro.AvroConverter
import com.zenaton.jobManager.common.data.JobId
import com.zenaton.jobManager.avroInterfaces.AvroStorage
import com.zenaton.jobManager.common.states.JobEngineState

class JobEngineStateStorage(val avroStorage: AvroStorage) {

    fun getState(jobId: JobId): JobEngineState? {
        return avroStorage.getJobEngineState(jobId.id)?.let { AvroConverter.fromStorage(it) }
    }

    fun updateState(jobId: JobId, newState: JobEngineState, oldState: JobEngineState?) {
        avroStorage.updateJobEngineState(
            jobId.id,
            AvroConverter.toStorage(newState),
            oldState?.let { AvroConverter.toStorage(it) }
        )
    }

    fun deleteState(jobId: JobId) {
        avroStorage.deleteJobEngineState(jobId.id)
    }
}
