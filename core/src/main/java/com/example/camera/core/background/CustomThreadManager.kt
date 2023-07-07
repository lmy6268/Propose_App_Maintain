package com.example.camera.core.background

import android.os.Handler
import android.os.HandlerThread


//제공할 서비스의 개수에 맞추어 스레드를 생성할 예정
//모든 서비스가 thread-safe 하게 동작하기 위해
//각각의 이름이 달린 thread를 가지고 작업하도록 한다.
class CustomThreadManager() {
    private val manageMap by lazy {
        HashMap<String, Any>()
    }
    private val threads by lazy {
        ArrayList<HandlerThread>()
    }

    companion object {
        private var ourInstance: CustomThreadManager? = null
        val instance: CustomThreadManager
            get() {
                if (ourInstance == null) ourInstance = CustomThreadManager()
                return ourInstance!!
            }

    }

    fun addHandler(name: String) {
        val handlerThread = HandlerThread(name).apply { start() }
        val handle = Handler(handlerThread.looper)
        threads.add(handlerThread)
        manageMap[name] = handle
    }

    fun getHandler(name: String) = manageMap[name]

    fun destroy() {
        threads.let {
            for (thread in it)
                thread.quitSafely() //스레드 모두 종료
            it.clear()
        }
        manageMap.clear()
    }


}