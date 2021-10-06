package com.docwei.rxjava2demo;

import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private Disposable mDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btn = findViewById(R.id.btn);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Observer<String> myObserver2 = new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.e("test", "用户的onSubscribe2");
                        mDisposable = d;
                    }

                    @Override
                    public void onNext(String value) {
                        Log.e("test", "用户的onNext2-->onNext--|" + value + "|----------" + Thread.currentThread().getName());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("test", "用户的onError2", e);
                        Snackbar.make(view, "通知错误终止" + e.getMessage(), BaseTransientBottomBar.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onComplete() {
                        Log.e("test", "用户的onComplete2");
                        Snackbar.make(view, "通知完成", BaseTransientBottomBar.LENGTH_SHORT).show();
                    }
                };


                Observer<String> myObserver = new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mDisposable = d;
                        Log.e("test", "用户的onSubscribe");
                    }

                    @Override
                    public void onNext(String value) {
                        Log.e("test", "用户的onNext-->onNext--|" + value + "|----------" + Thread.currentThread().getName());
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                    }
                };

                Observable<String> observable = Observable.create(new ObservableOnSubscribe<String>() {
                    @Override
                    public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                        emitter.onNext("a");
                        emitter.onComplete();
                    }
                })
                        .subscribeOn(Schedulers.computation())
                        .subscribeOn2(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .map(new Function<String, String>() {
                            @Override
                            public String apply(@NonNull String s) throws Throwable {
                                return s + "bc";
                            }
                        })
                        .observeOn2(AndroidSchedulers.mainThread());
                observable.subscribe(myObserver);
                observable.subscribe(myObserver2);


            }
        });


    }
}