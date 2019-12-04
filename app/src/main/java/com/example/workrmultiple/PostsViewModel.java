package com.example.workrmultiple;


import androidx.lifecycle.ViewModel;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkManager;

import com.example.workrmultiple.workers.FirstWorker;
import com.example.workrmultiple.workers.SecondWorker;
import com.example.workrmultiple.workers.ThirdWorker;

public class PostsViewModel extends ViewModel {

    private WorkManager mWorkManager;

    public void callAllApis() {
        mWorkManager = WorkManager.getInstance();
        OneTimeWorkRequest oneTimeWorkRequest =
                new OneTimeWorkRequest.Builder(FirstWorker.class)
                        .addTag("FirstWorker")
                        .build();

        WorkContinuation continuation = mWorkManager.beginWith(oneTimeWorkRequest);
        continuation = continuation.then(OneTimeWorkRequest.from(SecondWorker.class));
        continuation = continuation.then(OneTimeWorkRequest.from(ThirdWorker.class));
        continuation.enqueue();
    }

}
