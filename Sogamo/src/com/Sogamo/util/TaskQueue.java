package com.Sogamo.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;

public class TaskQueue {
	private ExecutorService executorService = null;
	
	public TaskQueue(Context context){
		executorService=Executors.newFixedThreadPool(1);
	}
	
	
	public void add(Runnable newtask) {
		executorService.submit(newtask);
	}

	public ExecutorService getExecutorService() {
		return executorService;
	}

	public void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
	}
}
