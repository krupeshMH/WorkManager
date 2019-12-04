package com.example.workrmultiple.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.workrmultiple.models.Post;
import com.example.workrmultiple.network.ApiClient;
import com.example.workrmultiple.network.MainApi;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class FirstWorker extends Worker {
    private MainApi service;

    public FirstWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        service = ApiClient.getClient().create(MainApi.class);
    }

    @NonNull
    @Override
    public Result doWork() {
        Flowable<List<Post>> call = service.getPostsFromUser(1);
        Disposable disposable = call.subscribe(new Consumer<List<Post>>() {
            @Override
            public void accept(List<Post> posts) throws Exception {
                if (posts.size() > 0) {

                }
            }
        });

        disposable.dispose();

        return Result.success();
    }
}
