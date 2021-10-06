/**
 * Copyright (c) 2016-present, RxJava Contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package io.reactivex.internal.operators.observable;

import android.util.Log;

import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.disposables.DisposableHelper;

public final class ObservableSubscribeOn3<T> extends AbstractObservableWithUpstream<T, T> {
    final Scheduler scheduler;

    public ObservableSubscribeOn3(ObservableSource<T> source, Scheduler scheduler) {
        super(source);
        this.scheduler = scheduler;
    }

    @Override
    public void subscribeActual(final Observer<? super T> observer) {
        Log.e("test","ObservableSubscribeOn3-->subscribeActual-->"+Thread.currentThread().getName()+"--->"+observer.getClass().getSimpleName());
        //Observer也被封装了
        final SubscribeOnObserver3<T> parent = new SubscribeOnObserver3<T>(observer);
       //方便取消
        observer.onSubscribe(parent);

        parent.setDisposable(scheduler.scheduleDirect(new SubscribeTask(parent)));
    }

    static final class SubscribeOnObserver3<T> extends AtomicReference<Disposable> implements Observer<T>, Disposable {

        private static final long serialVersionUID = 8094547886072529208L;
        final Observer<? super T> downstream;

        final AtomicReference<Disposable> upstream;

        SubscribeOnObserver3(Observer<? super T> downstream) {
            this.downstream = downstream;
            //上游的Observer
            this.upstream = new AtomicReference<Disposable>();
        }

        @Override
        public void onSubscribe(Disposable d) {
            Log.e("test","this.upstream-->SubscribeOnObserver3"+upstream);
            //获取上游的Observer
            DisposableHelper.setOnce(this.upstream, d);
        }

        @Override
        public void onNext(T t) {
            Log.e("test","ObservableSubscribeOn3-->onNext--》"+t+"----------"+Thread.currentThread().getName());
            downstream.onNext(t);

//            try {
//                throw new Exception("捕捉onNext的触发");
//            } catch (Exception e) {
//               Log.e("test","test",e);
//            }
           }

        @Override
        public void onError(Throwable t) {
            downstream.onError(t);
        }

        @Override
        public void onComplete() {
            downstream.onComplete();
        }

        @Override
        public void dispose() {
            //上游的取消，本Observer也取消
            DisposableHelper.dispose(upstream);
            DisposableHelper.dispose(this);

        }

        @Override
        public boolean isDisposed() {
            return DisposableHelper.isDisposed(get());
        }


        void setDisposable(Disposable d) {
            DisposableHelper.setOnce(this, d);
        }
    }

    final class SubscribeTask implements Runnable {
        private final SubscribeOnObserver3<T> parent;

        SubscribeTask(SubscribeOnObserver3<T> parent) {
            this.parent = parent;
        }

        @Override
        public void run() {
            source.subscribe(parent);
        }
    }
}
