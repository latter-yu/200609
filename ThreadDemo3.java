public class ThreadDemo3 {
    // 线程等待(线程之间是并发执行的关系)
    // join 方法会阻塞所在的线程, 让其他线程先执行, 以此控制线程结束的先后顺序.

    public static void main1(String[] args) throws InterruptedException {
        Thread t1 = new Thread() {
            @Override
            public void run() {
            for (int i = 0; i<10; i++) {
                System.out.println("xz");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            }
        };

        Thread t2 = new Thread() {
            @Override
            public void run() {
            for (int i = 0; i<10; i++) {
                System.out.println("xfx");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            }
        };
        /*t1.start();
        t1.join();
        // 相当于线程串行:
        // t1 线程结束后 t2 线程再执行
        t2.start();
        t2.join();*/

        t1.start();// 真正创建了 PCB
        t2.start();
        t1.join();
        t2.join();
        // t1 t2 线程同时执行
        // join 的作用是 保证主线程是最后结束的(阻塞主线程)
    }

    public static void main2(String[] args) {
        Thread thread = Thread.currentThread();
        // Thread.currentThread(); 返回当前线程对象的引用
        // 哪个线程调用, 获取到的引用就指向哪个线程(类似 this.)
        System.out.println(thread.getName());
    }

    public static void main3(String[] args) {
        for (Thread.State state : Thread.State.values()) {
            System.out.println(state);
            // NEW: 任务布置了, 但是还没开始执行
            //RUNNABLE: 就绪状态
            //BLOCKED: 等待锁(阻塞状态)
            //WAITING: wait 状态(阻塞状态)
            //TIMED_WAITING: sleep 状态(阻塞状态)
            //TERMINATED: 内核中线程结束了, 但是代码中 Thread 对象还存在

            // 线程状态的转换用 t.getState() 观察
        }
    }
}

class ThreadSafe {
    static class Counter {
        public int count = 0;

        public void increase() {
            count++;
            // 自增操作步骤:
            // 1. 把内存中的数据读取到 CPU 中(load)
            // 2. CPU 中把数据加一(increase)
            // 3. 把计算结束的数据传回内存中(save)
        }

        synchronized public void increase1() {
            // synchronized: 监视器锁
            // 进入方法之前会先尝试自动加锁(加锁不成功会自动阻塞,直到成功加锁)
            // 方法执行完毕之后会自动解锁
            // 也可以指定某个对象来加锁
            // 例如 boolean 类:
            // 未加锁状态 就是 false  加锁状态 就是 true

            // synchonized 用法:
            // 1. 加到普通方法前: 表示锁 this
            // 2. 加到静态方法前: 表示锁当前类的 类对象(同一个类只有一个类对象)
            //    lock.getclass(): 获取 lock 的类对象
            //    ps: 相同类(int, char等)的两个对象对应一个类对象, 一定会发生阻塞
            //    两个对象类型不同, 则对应到两个类对象, 互不干扰
            // 3. 加到某个代码块之前: 显式指定给某个对象加锁
            count++;
        }
    }
    public static void main(String[] args) throws InterruptedException {
        // 线程安全: 多线程并发执行时, 没有产生逻辑错误
        // 线程不安全: 多线程并发执行时, 产生逻辑错误

        // 不安全理由:
        // 1. 线程是抢占式执行的
        // 2. 自增操作不是原子的(每次 ++ 都能拆分成三个步骤), 执行任何一步时, 都可能被调度器调度走
        // 3. 多个线程尝试修改同一个变量
        // 4. 内存可见性导致的线程安全问题
        // 5. 指令重排序 (编译器在编译代码时, 会针对指令进行优化, 调整指令的运行顺序, 提升程序运行效率)

        // 解决线程不安全: 给自增操作加 锁, 使其变为原子操作
        // 锁的关键字: synchronized (英文原意: 同步)
        // 锁的特点: 互斥
        // 同一时刻只有一个线程能获取到锁, 其他尝试获取锁的线程会发生阻塞等待,
        // 一直到刚才获取了锁的线程释放锁, 剩下的线程才能重新竞争
        // 加锁(获取锁) lock   解锁(释放锁)unlock

        Counter counter = new Counter();
        Counter counter1 = new Counter();

        Thread t1 = new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < 100000; i++) {
                    counter.increase();
                }
            }
        };
        Thread t2 = new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < 100000; i++) {
                    counter.increase();
                }
            }
        };
        t1.start();
        t2.start();

        t1.join();
        t2.join();

        Thread t3 = new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < 100000; i++) {
                    counter1.increase1();
                }
            }
        };
        t3.start();
        Thread t4 = new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < 100000; i++) {
                    counter1.increase1();
                }
            }
        };
        t4.start();

        t3.join();
        t4.join();

        System.out.println(counter.count);
        System.out.println(counter1.count);
    }
}