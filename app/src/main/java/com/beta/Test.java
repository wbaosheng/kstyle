package com.beta;

import android.util.Log;

public class Test {
    private static Elements elements = new Elements();

    public static void main() {
        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    for (String s : elements.getLists()) {
                        Log.i("block...", Thread.currentThread().getName() + " s: " + s);
                    }
                }
            }
        }, "thread-1");
        thread1.start();
        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                elements.lists = new String[] {
                        "modify1",
                        "modify2",
                        "modify3",
                        "modify4"
                };
            }
        }, "thread-2");
        thread2.start();
    }

    static class Elements {
        String[] lists = new String[100000];

        Elements() {
            int i = 0;
            while (i < 100000) {
                lists[i] = "list " + i;
                i++;
            }
        }

        public String[] getLists() {
            return lists;
        }
    }
}
