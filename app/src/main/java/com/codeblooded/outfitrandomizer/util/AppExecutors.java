package com.codeblooded.outfitrandomizer.util;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AppExecutors {
    private static final Executor IO = Executors.newSingleThreadExecutor();
    public static  Executor io() {return IO;}
}
