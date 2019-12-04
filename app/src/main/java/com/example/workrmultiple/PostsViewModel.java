package com.example.workrmultiple;


import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.LiveDataReactiveStreams;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkManager;

import com.example.workrmultiple.models.Post;
import com.example.workrmultiple.network.MainApi;
import com.example.workrmultiple.network.Resource;
import com.example.workrmultiple.workers.FirstWorker;
import com.example.workrmultiple.workers.SecondWorker;
import com.example.workrmultiple.workers.ThirdWorker;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class PostsViewModel extends ViewModel {

    private static final String TAG = "PostsViewModel";

    private MediatorLiveData<Resource<List<Post>>> posts;
    private Retrofit retrofit;
    private MainApi service;
    private WorkManager mWorkManager;

    public PostsViewModel() {
    }

    public void initRetrofit() {
        retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(MainApi.class);
        mWorkManager = WorkManager.getInstance();
    }

    public void callAllApis() {
        // Add WorkRequest to Cleanup temporary images
        OneTimeWorkRequest oneTimeWorkRequest =
                new OneTimeWorkRequest.Builder(FirstWorker.class)
                        .addTag("FirstWorker")
                        .build();

        WorkContinuation continuation = mWorkManager.beginWith(oneTimeWorkRequest);
        continuation = continuation.then(OneTimeWorkRequest.from(SecondWorker.class));
        continuation = continuation.then(OneTimeWorkRequest.from(ThirdWorker.class));
        continuation.enqueue();
        /*mWorkManager.enqueueUniqueWork(
                "FirstWorker",
                ExistingWorkPolicy.REPLACE,
                oneTimeWorkRequest
        );*/

    }


    public LiveData<Resource<List<Post>>> observePosts() {
        if (posts == null) {
            posts = new MediatorLiveData<>();
            posts.setValue(Resource.loading((List<Post>) null));

            final LiveData<Resource<List<Post>>> source = LiveDataReactiveStreams.fromPublisher(

                    service.getPostsFromUser(1)

                            .onErrorReturn(new Function<Throwable, List<Post>>() {
                                @Override
                                public List<Post> apply(Throwable throwable) throws Exception {
                                    Log.e(TAG, "apply: ", throwable);
                                    Post post = new Post();
                                    post.setId(-1);
                                    ArrayList<Post> posts = new ArrayList<>();
                                    posts.add(post);
                                    return posts;
                                }
                            })

                            .map(new Function<List<Post>, Resource<List<Post>>>() {
                                @Override
                                public Resource<List<Post>> apply(List<Post> posts) throws Exception {

                                    if (posts.size() > 0) {
                                        if (posts.get(0).getId() == -1) {
                                            return Resource.error("Something went wrong", null);
                                        }
                                    }

                                    return Resource.success(posts);
                                }
                            })

                            .subscribeOn(Schedulers.io())
            );

            posts.addSource(source, new Observer<Resource<List<Post>>>() {
                @Override
                public void onChanged(Resource<List<Post>> listResource) {
                    posts.setValue(listResource);
                    posts.removeSource(source);
                }
            });
        }
        return posts;
    }

    /*class FirstWorker extends Worker {

        public FirstWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
            super(appContext, workerParams);
        }

        @NonNull
        @Override
        public Result doWork() {
            Flowable<List<Post>> call = service.getPostsFromUser(1);
            call.blockingSubscribe(new Subscriber<List<Post>>() {
                @Override
                public void onSubscribe(Subscription s) {

                }

                @Override
                public void onNext(List<Post> posts) {
                    if (posts.size() > 0) {

                    }
                }

                @Override
                public void onError(Throwable t) {

                }

                @Override
                public void onComplete() {

                }
            });
            *//*Data outPut = new Data.Builder()
                    .putString(Constant.WORK_RESULT,Constant.WORK_SUCCESS)
                    .putString(Constant.WORK_RESPONSE, String.valueOf(response.code()))
                    .build();

            setOutputData(outPut);*//*
            return Result.success();

        }
    }*/

}
