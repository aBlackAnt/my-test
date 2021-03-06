[并发总结](https://github.com/CL0610/Java-concurrency)

[面试题](https://segmentfault.com/a/1190000013813740)

[15个顶级Java多线程面试题及回答](http://ifeve.com/15-java-faq/)

# 一、基础知识

**线程生命周期**：

![](https://user-gold-cdn.xitu.io/2018/4/30/163159bceb956cb4?imageView2/0/w/1280/h/960/format/webp/ignore-error/1)

**yield**：

​	public static native void yield();这是一个静态方法，一旦执行，它会是当前线程让出CPU，但是，需要注意的是，让出的CPU并不是代表当前线程不再运行了，如果在下一次竞争中，又获得了CPU时间片当前线程依然会继续运行。另外，让出的时间片只会分配**给当前线程相同优先级**的线程。

​	sleep()和yield()方法，同样都是当前线程会交出处理器资源，而它们不同的是，sleep()交出来的时间片其他线程都可以去竞争，也就是说都有机会获得当前线程让出的时间片。而yield()方法只允许与当前线程具有相同优先级的线程能够获得释放出来的CPU时间片。

> 详见：[线程的状态转换以及基本操作](https://juejin.im/post/5ae6cf7a518825670960fcc2)

**销毁线程**：

```java
public class ThreadSafe extends Thread {
    public void run() { 
        while (!isInterrupted()){ //非阻塞过程中通过判断中断标志来退出
            try{
                Thread.sleep(5*1000);//阻塞过程捕获中断异常来退出
            }catch(InterruptedException e){
                e.printStackTrace();
                break;//捕获到异常之后，执行break跳出循环。
            }
        }
    } 
}
```

> 详见：[线程销毁三种方式](https://blog.csdn.net/xu__cg/article/details/52831127)

# 二、并发理论

> 详见：[Java内存模型以及happens-before规则](https://juejin.im/post/5ae6d309518825673123fd0e)

## 2.1 JMM

![](https://user-gold-cdn.xitu.io/2018/4/30/16315b2410a9e3eb?imageView2/0/w/1280/h/960/format/webp/ignore-error/1)

## 2.2 重排序

![](https://user-gold-cdn.xitu.io/2018/4/30/16315b2b7b2a63e9?imageView2/0/w/1280/h/960/format/webp/ignore-error/1)

## 2.3 happens-before

- 程序顺序规则：一个线程中的每个操作，happens-before于该线程中的任意后续操作。

- 监视器锁规则：对一个锁的解锁，happens-before于随后对这个锁的加锁。

- volatile变量规则：对一个volatile域的写，happens-before于任意后续对这个volatile域的读。

- 传递性：如果A happens-before B，且B happens-before C，那么A happens-before C。

- start()规则：如果线程A执行操作ThreadB.start()（启动线程B），那么A线程的ThreadB.start()操作happens-before于线程B中的任意操作。

- join()规则：如果线程A执行操作ThreadB.join()并成功返回，那么线程B中的任意操作happens-before于线程A从ThreadB.join()操作成功返回。

- 程序中断规则：对线程interrupted()方法的调用先行于被中断线程的代码检测到中断时间的发生。

- 对象finalize规则：一个对象的初始化完成（构造函数执行结束）先行于发生它的finalize()方法的开始。

# 三、并发关键字

## 3.1 synchronized

[synchronized简介与优化](https://juejin.im/post/5ae6dc04f265da0ba351d3ff#heading-14)

### 3.1.1 实现

![](https://user-gold-cdn.xitu.io/2018/4/30/16315cc79aaac173?imageView2/0/w/1280/h/960/format/webp/ignore-error/1)

### 3.1.2 对象锁（monitor）机制

```java
public class SynchronizedDemo {
    public static void main(String[] args) {
        synchronized (SynchronizedDemo.class) {
        }
        method();
    }

    private static void method() {
    }
}

```

``javac``后使用``javap -v SynchronizedDemo.class``，查看字节码文件。

![](https://user-gold-cdn.xitu.io/2018/4/30/16315cce259af0d2?imageView2/0/w/1280/h/960/format/webp/ignore-error/1)

执行同步代码块后首先要先执行**monitorenter**指令，退出的时候**monitorexit**指令。执行静态同步方法的时候就只有一条monitorexit指令，并没有monitorenter获取锁的指令。这就是**锁的重入性**，即在同一锁程中，线程不需要再次获取同一把锁。Synchronized先天具有重入性。**每个对象拥有一个计数器，当线程获取该对象锁后，计数器就会加一，释放锁后就会将计数器减一**。

### 3.1.3 优化

[优化](https://www.cnblogs.com/paddix/p/5405678.html)

问题：synchronized在同一时刻只有一个线程能够获得对象的监视器（monitor），从而进入到同步代码块或者同步方法之中，即表现为**互斥性（排它性）**。这种方式肯定效率低下，每次只能通过一个线程。

方案：使用轻量级锁和偏向锁对synchronized进行优化，减少获得锁和释放锁所带来的性能消耗

| 锁       | 优点                                                         | 缺点                                             | 适用场景                             |
| -------- | ------------------------------------------------------------ | ------------------------------------------------ | ------------------------------------ |
| 偏向锁   | 加锁和解锁不需要额外的消耗，和执行非同步方法比仅存在纳秒级的差距。 | 如果线程间存在锁竞争，会带来额外的锁撤销的消耗。 | 适用于只有一个线程访问同步块场景。   |
| 轻量级锁 | 竞争的线程不会阻塞，提高了程序的响应速度。                   | 如果始终得不到锁竞争的线程使用自旋会消耗CPU。    | 追求响应时间。同步块执行速度非常快。 |
| 重量级锁 | 线程竞争不使用自旋，不会消耗CPU。                            | 线程阻塞，响应时间缓慢。                         | 追求吞吐量。同步块执行速度较长。     |

> 锁的状态总共有四种：无锁状态、偏向锁、轻量级锁和重量级锁。随着锁的竞争，锁可以从偏向锁升级到轻量级锁，再升级的重量级锁（但是锁的升级是单向的，也就是说只能从低到高升级，不会出现锁的降级）。JDK 1.6中默认是开启偏向锁和轻量级锁的，我们也可以通过-XX:-UseBiasedLocking来禁用偏向锁

## 3.2 volatile

[彻底理解volatile](https://juejin.im/post/5ae9b41b518825670b33e6c4#heading-3)

### 3.2.1 实现原理

在生成汇编代码时会在volatile修饰的共享变量进行写操作的时候会多出**Lock前缀的指令**，作用如下：

1. 将当前处理器缓存行的数据写回系统内存；
2. 这个写回内存的操作会使得其他CPU里缓存了该内存地址的数据无效

### 3.2.2 volatile的内存语义实现

为了性能优化，JMM在不改变正确语义的前提下，会允许编译器和处理器对指令序列进行重排序，那如果想阻止重排序要怎么办了？答案是可以添加**内存屏障**。

![](https://user-gold-cdn.xitu.io/2018/5/2/16320e796e1471c0?imageView2/0/w/1280/h/960/format/webp/ignore-error/1)

**StoreStore屏障**：禁止上面的普通写和下面的volatile写重排序；

**StoreLoad屏障**：防止上面的volatile写与下面可能有的volatile读/写重排序

**LoadLoad屏障**：禁止下面所有的普通读操作和上面的volatile读重排序

**LoadStore屏障**：禁止下面所有的普通写操作和上面的volatile读重排序

![](https://user-gold-cdn.xitu.io/2018/5/2/16320e796e03b351?imageView2/0/w/1280/h/960/format/webp/ignore-error/1)

![](https://user-gold-cdn.xitu.io/2018/5/2/16320e799b76d34c?imageView2/0/w/1280/h/960/format/webp/ignore-error/1)

### 3.2.3 为什么不能保证原子性

详见：[为什么volatile不能保证原子性](https://www.cnblogs.com/rainwang/p/4398488.html)

**volatile可以保证可见性和有序性，但不能保证原子性。**

原因：

例如你让一个volatile的integer自增（i++），其实要分成3步：1）读取volatile变量值到local； 2）增加变量的值；3）把local的值写回，让其它的线程可见。这3步的jvm指令为：

```
mov        0xc(%r10),%r8d ; Load
inc        %r8d           ; Increment
mov    %r8d,``0xc``(%r10) ; Store
lock addl $``0x0``,(%rsp) ; StoreLoad Barrier
```

从Load到store到内存屏障，一共4步，其中最后一步jvm让这个最新的变量的值在所有线程可见，也就是最后一步让所有的CPU内核都获得了最新的值，但**中间的几步（从Load到Store）**是不安全的，中间如果其他的CPU修改了值将会丢失。所以为了保证原子性，需要对**Increment**，加锁(synchronized或lock)或使用原子类**AtomicXXX**。

## 3.3 final

[你以为你真的了解final吗](https://juejin.im/post/5ae9b82c6fb9a07ac3634941)

[final和线程安全](http://www.cnblogs.com/mianlaoshu/articles/3648403.html)

看了还是不了解~~，只是对成员变量（static修饰的类变量、实例变量）、局部变量，final的基础使用加深了印象。

硬着头皮总结几点：

- 当构造函数结束时，final类型的值是被保证其他线程访问该对象时，它们的值是可见的，若没有用final修饰，则会进行重排序。
- 在构造函数，不能让这个被构造的对象被其他线程可见，也就是说该对象引用不能在构造函数中“逸出”。

>Q：如何不使用任何锁，任何线程安全的容器怎么实现线程安全
>
>A：使用volatile+cas+队列 或其他？（思路：AQS？参考线程安全容器底层实现？）

# 四、Lock体系

从整体上来看concurrent包的整体实现图如下图所示：

![](https://user-gold-cdn.xitu.io/2018/5/3/163260cff7cb847c?imageView2/0/w/1280/h/960/format/webp/ignore-error/1)

## 4.1 AQS

[深入理解AbstractQueuedSynchronizer(AQS)](https://juejin.im/post/5aeb07ab6fb9a07ac36350c8#heading-3)

使用的数据结构是：具有**头结点的双向链表**

AQS的核心包括了这些方面:

**同步队列，独占式锁的获取和释放，共享锁的获取和释放以及可中断锁，超时等待锁获取这些特性的实现**。

### 4.1.1 同步队列

1. 节点的数据结构，即AQS的静态内部类Node,节点的等待状态等信息；
2. 同步队列是一个双向队列，AQS通过持有头尾指针管理同步队列；

节点的入队出队对应着锁的获取和释放两个操作：获取锁失败进行入队操作，获取锁成功进行出队操作

### 4.1.2 独占锁

#### 4.1.2.1 独占锁获取（acquire方法）

1. 在当前线程是第一个加入同步队列时，调用compareAndSetHead(new Node())方法，完成链式队列的头结点的初始化；
2. 自旋不断尝试CAS尾插入节点直至成功为止。
3. 如果先驱节点是头结点的并且成功获得同步状态的时候（if (p == head && tryAcquire(arg))），当前节点所指向的线程能够获取锁，否则线程进入等待状态等待获取独占式锁

![](https://user-gold-cdn.xitu.io/2018/5/3/163261637c891cc2?imageView2/0/w/1280/h/960/format/webp/ignore-error/1)

#### 4.1.2.2 独占锁释放（release()方法）

​	首先获取头节点的后继节点，当后继节点的时候会调用LookSupport.unpark()方法，该方法会唤醒该节点的后继节点所包装的线程。因此，**每一次锁释放后就会唤醒队列中该节点的后继节点所引用的线程，从而进一步可以佐证获得锁的过程是一个FIFO（先进先出）的过程**

#### 4.1.2.3 可中断式获取锁（acquireInterruptibly方法）， 超时等待式获取锁（tryAcquireNanos()方法）

略

### 4.1.3 共享锁

#### 4.1.3.1 共享锁获取

略

#### 4.1.3.2 共享锁释放

略

#### 4.1.3.3 可中断（acquireSharedInterruptibly()方法），超时等待（tryAcquireSharedNanos()方法）

略

## 4.2 ReentrantLock

[彻底理解ReentrantLock](https://juejin.im/post/5aeb0a8b518825673a2066f0)

​	ReentrantLock重入锁，是实现Lock接口的一个类，也是在实际编程中使用频率很高的一个锁，**支持重入性，表示能够对共享资源能够重复加锁，即当前线程获取该锁再次获取不会被阻塞**。在java关键字synchronized隐式支持重入性，synchronized通过获取自增，释放自减的方式实现重入。与此同时，ReentrantLock还支持**公平锁和非公平锁**两种方式。

1. 重入性的实现原理；

   ```java
   final boolean nonfairTryAcquire(int acquires) {
       final Thread current = Thread.currentThread();
       int c = getState();
       //1. 如果该锁未被任何线程占有，该锁能被当前线程获取
   	if (c == 0) {
           if (compareAndSetState(0, acquires)) {
               setExclusiveOwnerThread(current);
               return true;
           }
       }
   	//2.若被占有，检查占有线程是否是当前线程
       else if (current == getExclusiveOwnerThread()) {
   		// 3. 再次获取，计数加一
           int nextc = c + acquires;
           if (nextc < 0) // overflow
               throw new Error("Maximum lock count exceeded");
           setState(nextc);
           return true;
       }
       return false;
   }
   
   ```

2.  公平锁和非公平锁。

   **公平锁每次都是从同步队列中的第一个节点获取到锁，而非公平性锁则不一定，有可能刚释放锁的线程能再次获取到锁**。

   > ​	a.公平锁每次获取到锁为同步队列中的第一个节点，**保证请求资源时间上的绝对顺序**，而非公平锁有可能刚释放锁的线程下次继续获取该锁，则有可能导致其他线程永远无法获取到锁，**造成“饥饿”现象**。
   >
   > ​	b.公平锁为了保证时间上的绝对顺序，需要频繁的上下文切换，而非公平锁会降低一定的上下文切换，降低性能开销。因此，ReentrantLock默认选择的是非公平锁，则是为了减少一部分上下文切换，**保证了系统更大的吞吐量**。

## 4.3 ReentrantReadWriteLock

[深入理解读写锁ReentrantReadWriteLock](https://juejin.im/post/5aeb0e016fb9a07ab7740d90)

1. 读写锁是怎样实现分别记录读写状态的？

   ![](https://user-gold-cdn.xitu.io/2018/5/3/163262ec97ebeac9?imageView2/0/w/1280/h/960/format/webp/ignore-error/1)

2.  写锁是怎样获取和释放的？

   独占锁

3. 读锁是怎样获取和释放的？

   共享锁

> 支持锁降级，**遵循按照获取写锁，获取读锁再释放写锁的次序，写锁能够降级成为读锁**，不支持锁升级

## 4.4 Condition

AQS中的ConditionObject：底层数据结构是没有头结点的单向链表

### 4.4.1 等待队列

等待队列的示意图如下：

![](https://user-gold-cdn.xitu.io/2018/5/6/16334382e58c4e34?imageView2/0/w/1280/h/960/format/webp/ignore-error/1)



​	可以多次调用lock.newCondition()方法创建多个condition对象，也就是一个lock可以持有多个等待队列。而在之前利用Object的方式实际上是指在**对象Object对象监视器上只能拥有一个同步队列和一个等待队列，而并发包中的Lock拥有一个同步队列和多个等待队列**。示意图如下：

![](https://user-gold-cdn.xitu.io/2018/5/6/16334382e65f9685?imageView2/0/w/1280/h/960/format/webp/ignore-error/1)

### 4.4.2 await实现原理

```java
public final void await() throws InterruptedException {
    if (Thread.interrupted())
        throw new InterruptedException();
	// 1. 将当前线程包装成Node，尾插入到等待队列中
    Node node = addConditionWaiter();
	// 2. 释放当前线程所占用的lock，在释放的过程中会唤醒同步队列中的下一个节点
    int savedState = fullyRelease(node);
    int interruptMode = 0;
    while (!isOnSyncQueue(node)) {
		// 3. 当前线程进入到等待状态
        LockSupport.park(this);
        if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
            break;
    }
	// 4. 自旋等待获取到同步状态（即获取到lock）
    if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
        interruptMode = REINTERRUPT;
    if (node.nextWaiter != null) // clean up if cancelled
        unlinkCancelledWaiters();
	// 5. 处理被中断的情况
    if (interruptMode != 0)
        reportInterruptAfterWait(interruptMode);
}

```

![](https://user-gold-cdn.xitu.io/2018/5/6/16334382e74cead3?imageView2/0/w/1280/h/960/format/webp/ignore-error/1)

### 4.4.3 signal/signalAll实现原理

#### 4.4.3.1 signal

```java
public final void signal() {
    //1. 先检测当前线程是否已经获取lock
    if (!isHeldExclusively())
        throw new IllegalMonitorStateException();
    //2. 获取等待队列中第一个节点，之后的操作都是针对这个节点
	Node first = firstWaiter;
    if (first != null)
        doSignal(first);
}

```

signal方法首先会检测当前线程是否已经获取lock，如果没有获取lock会直接抛出异常，如果获取的话再得到等待队列的头指针引用的节点，之后的操作的doSignal方法也是基于该节点。下面我们来看看doSignal方法做了些什么事情，doSignal方法源码为：

```java
private void doSignal(Node first) {
    do {
        if ( (firstWaiter = first.nextWaiter) == null)
            lastWaiter = null;
		//1. 将头结点从等待队列中移除
        first.nextWaiter = null;
		//2. while中transferForSignal方法对头结点做真正的处理
    } while (!transferForSignal(first) &&
             (first = firstWaiter) != null);
}

```

```java
final boolean transferForSignal(Node node) {
    /*
     * If cannot change waitStatus, the node has been cancelled.
     */
	//1. 更新状态为0
    if (!compareAndSetWaitStatus(node, Node.CONDITION, 0))
        return false;

    /*
     * Splice onto queue and try to set waitStatus of predecessor to
     * indicate that thread is (probably) waiting. If cancelled or
     * attempt to set waitStatus fails, wake up to resync (in which
     * case the waitStatus can be transiently and harmlessly wrong).
     */
	//2.将该节点移入到同步队列中去
    Node p = enq(node);
    int ws = p.waitStatus;
    if (ws > 0 || !compareAndSetWaitStatus(p, ws, Node.SIGNAL))
        LockSupport.unpark(node.thread);
    return true;
}

```

​	**调用condition的signal的前提条件是当前线程已经获取了lock，该方法会使得等待队列中的头节点即等待时间最长的那个节点移入到同步队列，而移入到同步队列后才有机会使得等待线程被唤醒，即从await方法中的LockSupport.park(this)方法中返回，从而才有机会使得调用await方法的线程成功退出**。signal执行示意图如下图：

![](https://user-gold-cdn.xitu.io/2018/5/6/16334382e7650d62?imageView2/0/w/1280/h/960/format/webp/ignore-error/1)

#### 4.4.3.2 signalAll

```java
private void doSignalAll(Node first) {
    lastWaiter = firstWaiter = null;
    do {
        Node next = first.nextWaiter;
        first.nextWaiter = null;
        transferForSignal(first);
        first = next;
    } while (first != null);
}

```

#### 4.4.3.3 await与signal/signalAll的结合思考

![](https://user-gold-cdn.xitu.io/2018/5/6/16334382e7911395?imageView2/0/w/1280/h/960/format/webp/ignore-error/1)

## 4.5 LockSupport

[LockSupport工具](https://juejin.im/post/5aeed27f51882567336aa0fa)

# 五、并发容器

## 5.1 ConcurrentHashmap

[并发容器之ConcurrentHashMap(JDK 1.8版本)](https://juejin.im/post/5aeeaba8f265da0b9d781d16#heading-7)

[深入分析ConcurrentHashMap1.8的扩容实现](https://www.jianshu.com/p/f6730d5784ad)

导致扩容两种原因：

1. 新增节点之后，所在链表的元素个数达到了阈值 **8**，则会调用`treeifyBin`方法把链表转换成红黑树，不过在结构转换之前，会对数组长度进行判断：如果数组长度n小于阈值`MIN_TREEIFY_CAPACITY`，默认是64，则会调用`tryPresize`方法把数组长度扩大到原来的两倍，并触发`transfer`方法，重新调整节点的位置。
2. 新增节点之后，会调用`addCount`方法记录元素个数，并检查是否需要进行扩容，当数组元素个数达到阈值时，会触发`transfer`方法，重新调整节点的位置。

put时如果正在扩容，则该线程阻塞帮助扩容；put后可能进行扩容。



**五种构造器：**

```java
// 1. 构造一个空的map，即table数组还未初始化，初始化放在第一次插入数据时，默认大小为16
ConcurrentHashMap()
// 2. 给定map的大小
ConcurrentHashMap(int initialCapacity) 
// 3. 给定一个map
ConcurrentHashMap(Map<? extends K, ? extends V> m)
// 4. 给定map的大小以及加载因子
ConcurrentHashMap(int initialCapacity, float loadFactor)
// 5. 给定map大小，加载因子以及并发度（预计同时操作数据的线程）
ConcurrentHashMap(int initialCapacity,float loadFactor, int concurrencyLevel)

```

## 5.2 CopyOnWriteArrayList

[并发容器之CopyOnWriteArrayList](https://juejin.im/post/5aeeb55f5188256715478c21)

```java
/** The array, accessed only via getArray/setArray. */
private transient volatile Object[] array;
```

该数组引用是被volatile修饰，注意这里**仅仅是修饰的是数组引用**



**get方法：**

```java
public E get(int index) {
    return get(getArray(), index);
}
/**
 * Gets the array.  Non-private so as to also be accessible
 * from CopyOnWriteArraySet class.
 */
final Object[] getArray() {
    return array;
}
private E get(Object[] a, int index) {
    return (E) a[index];
}

```

读线程只是会读取数据容器中的数据，并不会进行修改，所以不用加锁。



**add方法：**

```java
public boolean add(E e) {
    final ReentrantLock lock = this.lock;
	//1. 使用Lock,保证写线程在同一时刻只有一个
    lock.lock();
    try {
		//2. 获取旧数组引用
        Object[] elements = getArray();
        int len = elements.length;
		//3. 创建新的数组，并将旧数组的数据复制到新数组中
        Object[] newElements = Arrays.copyOf(elements, len + 1);
		//4. 往新数组中添加新的数据	        
		newElements[len] = e;
		//5. 将旧数组引用指向新的数组
        setArray(newElements);
        return true;
    } finally {
        lock.unlock();
    }
}
```

1. 采用ReentrantLock，保证同一时刻只有一个写线程正在进行数组的复制，否则的话内存中会有多份被复制的数据；

2. 前面说过数组引用是volatile修饰的，因此将旧的数组引用指向新的数组，根据volatile的happens-before规则，写线程对数组引用的修改对读线程是可见的。

3. 由于在写数据的时候，是在新的数组中插入数据的，从而保证读写实在两个不同的数据容器中进行操作。



**COW  vs 读写锁**

相同点：1. 两者都是通过读写分离的思想实现；2.读线程间是互不阻塞的

不同点：**对读线程而言，为了实现数据实时性，在写锁被获取后，读线程会等待或者当读锁被获取后，写线程会等待，从而解决“脏读”等问题。也就是说如果使用读写锁依然会出现读线程阻塞等待的情况。而COW则完全放开了牺牲数据实时性而保证数据最终一致性，即读线程对数据的更新是延时感知的，因此读线程不会存在等待的情况**。



**COW的缺点**

CopyOnWrite容器有很多优点，但是同时也存在两个问题，即内存占用问题和数据一致性问题。所以在开发的时候需要注意一下。

1. **内存占用问题**：因为CopyOnWrite的写时复制机制，所以在进行写操作的时候，内存里会同时驻扎两个对	象的内存，旧的对象和新写入的对象（注意:在复制的时候只是复制容器里的引用，只是在写的时候会创建新对	象添加到新容器里，而旧容器的对象还在使用，所以有两份对象内存）。如果这些对象占用的内存比较大，比	如说200M左右，那么再写入100M数据进去，内存就会占用300M，那么这个时候很有可能造成频繁的minor GC和major GC。
2. **数据一致性问题**：CopyOnWrite容器只能保证数据的最终一致性，不能保证数据的实时一致性。所以如果你希望写入的的数据，马上能读到，请不要使用CopyOnWrite容器。

## 5.3 ConcurrentLinkedQueue

[并发容器之ConcurrentLinkedQueue](https://juejin.im/post/5aeeae756fb9a07ab11112af#heading-3)

## 5.4 ThreadLocal

[并发容器之ThreadLocal](https://juejin.im/post/5aeeb22e6fb9a07aa213404a)

[一篇文章，从源码深入详解ThreadLocal内存泄漏问题](https://www.jianshu.com/p/dde92ec37bd1)

![](https://user-gold-cdn.xitu.io/2018/5/6/16334681776bb805?imageView2/0/w/1280/h/960/format/webp/ignore-error/1)

- 弱引用：ThreadLocal使用弱引用包装，用于gc，若使用强引用，通过可达性分析，通过ThreadLocalMap的value值可以找到key值ThreadLocal，即使key为null，回收不了，造成内存溢出
- hash：**ThreadLocalMap 中使用开放地址法来处理散列冲突**，而 HashMap 中使用的分离链表法。之所以采用不同的方式主要是因为：在 ThreadLocalMap 中的散列值分散的十分均匀，很少会出现冲突。并且 ThreadLocalMap 经常需要清除无用的对象，使用纯数组更加方便。hash值实际上总是用一个AtomicInteger加上0x61c88647来实现的。
- 清除脏Entry：value=null，entry==null，伴随threadLocal的set、get、remove
- 防止内存泄漏：使用完后调用remove方法释放

## 5.5 BlockingQueue

[并发容器之BlockingQueue](https://juejin.im/post/5aeebd02518825672f19c546)

[并发容器之ArrayBlockingQueue和LinkedBlockingQueue实现原理详解](https://juejin.im/post/5aeebdb26fb9a07aa83ea17e)

# 六、线程池（Executor体系）

## 6.1 ThreadPoolExecutor

![](https://user-gold-cdn.xitu.io/2018/5/6/163349e503061169?imageView2/0/w/1280/h/960/format/webp/ignore-error/1)

### 6.1.1 线程池可配置参数

1. corePoolSize：表示核心线程池的大小。当提交一个任务时，如果当前核心线程池的线程个数没有达到corePoolSize，则会创建新的线程来执行所提交的任务，**即使当前核心线程池有空闲的线程**。如果当前核心线程池的线程个数已经达到了corePoolSize，则不再重新创建线程。如果调用了`prestartCoreThread()`或者 `prestartAllCoreThreads()`，线程池创建的时候所有的核心线程都会被创建并且启动。

2. maximumPoolSize：表示线程池能创建线程的最大个数。如果当阻塞队列已满时，并且当前线程池线程个数没有超过maximumPoolSize的话，就会创建新的线程来执行任务。

3. keepAliveTime：空闲线程存活时间。如果当前线程池的线程个数已经超过了corePoolSize，并且线程空闲时间超过了keepAliveTime的话，就会将这些空闲线程销毁，这样可以尽可能降低系统资源消耗。

4. unit：时间单位。为keepAliveTime指定时间单位。

5. workQueue：阻塞队列。用于保存任务的阻塞队列，关于阻塞队列[可以看这篇文章](https://juejin.im/post/5aeebd02518825672f19c546)。可以使用**ArrayBlockingQueue, LinkedBlockingQueue, SynchronousQueue, PriorityBlockingQueue**。

6. threadFactory：创建线程的工程类。可以通过指定线程工厂为每个创建出来的线程设置更有意义的名字，如果出现并发问题，也方便查找问题原因。

7. handler：饱和策略。当线程池的阻塞队列已满和指定的线程都已经开启，说明当前线程池已经处于饱和状态了，那么就需要采用一种策略来处理这种情况。采用的策略有这几种： 

   - AbortPolicy： 直接拒绝所提交的任务，并抛出**RejectedExecutionException**异常；

   - CallerRunsPolicy：只用调用者所在的线程来执行任务；

   - DiscardPolicy：不处理直接丢弃掉任务；

   - DiscardOldestPolicy：丢弃掉阻塞队列中存放时间最久的任务，执行当前任务

### 6.1.2 线程池的关闭

关闭线程池，可以通过`shutdown`和`shutdownNow`这两个方法。它们的原理都是遍历线程池中所有的线程，然后依次中断线程。`shutdown`和`shutdownNow`还是有不一样的地方：

1. `shutdownNow`首先将线程池的状态设置为**STOP**,然后尝试**停止所有的正在执行和未执行任务**的线程，并返回等待执行任务的列表；
2. `shutdown`只是将线程池的状态设置为**SHUTDOWN**状态，然后中断所有没有正在执行任务的线程

可以看出shutdown方法会将正在执行的任务继续执行完，而shutdownNow会直接中断正在执行的任务。调用了这两个方法的任意一个，`isShutdown`方法都会返回true，当所有的线程都关闭成功，才表示线程池成功关闭，这时调用`isTerminated`方法才会返回true。

### 6.1.3 如何合理配置线程池参数

要想合理的配置线程池，就必须首先分析任务特性，可以从以下几个角度来进行分析：

1. 任务的性质：CPU密集型任务，IO密集型任务和混合型任务。
2. 任务的优先级：高，中和低。
3. 任务的执行时间：长，中和短。
4. 任务的依赖性：是否依赖其他系统资源，如数据库连接。

任务性质不同的任务可以用不同规模的线程池分开处理。CPU密集型任务配置尽可能少的线程数量，如配置**Ncpu+1**个线程的线程池。IO密集型任务则由于需要等待IO操作，线程并不是一直在执行任务，则配置尽可能多的线程，如**2xNcpu**。混合型的任务，如果可以拆分，则将其拆分成一个CPU密集型任务和一个IO密集型任务，只要这两个任务执行的时间相差不是太大，那么分解后执行的吞吐率要高于串行执行的吞吐率，如果这两个任务执行时间相差太大，则没必要进行分解。我们可以通过`Runtime.getRuntime().availableProcessors()`方法获得当前设备的CPU个数。

优先级不同的任务可以使用优先级队列PriorityBlockingQueue来处理。它可以让优先级高的任务先得到执行，需要注意的是如果一直有优先级高的任务提交到队列里，那么优先级低的任务可能永远不能执行。

执行时间不同的任务可以交给不同规模的线程池来处理，或者也可以使用优先级队列，让执行时间短的任务先执行。

依赖数据库连接池的任务，因为线程提交SQL后需要等待数据库返回结果，如果等待的时间越长CPU空闲时间就越长，那么线程数应该设置越大，这样才能更好的利用CPU。

并且，阻塞队列**最好是使用有界队列**，如果采用无界队列的话，一旦任务积压在阻塞队列中的话就会占用过多的内存资源，甚至会使得系统崩溃。

## 6.2 ScheduledThreadPoolExecutor

[线程池之ScheduledThreadPoolExecutor](https://juejin.im/post/5aeec106518825670a10328a)

## 6.3 FutureTask

[FutureTask基本操作总结](https://juejin.im/post/5aeec249f265da0b886d5101#heading-0)

# 七、atomic

[Java中atomic包中的原子操作类总结](https://juejin.im/post/5aeec351518825670a103292#heading-0)

1. 原子更新基本类型
2. 原子更新数组类型
3. 原子更新引用类型
4. 原子更新字段类型

# 八、并发工具

## 8.1 CountDownLatch，CyclicBarrier

[大白话说java并发工具类-CountDownLatch，CyclicBarrier](https://juejin.im/post/5aeec3ebf265da0ba76fa327)

CountDownLatch与CyclicBarrier都是用于控制并发的工具类，都可以理解成维护的就是一个计数器，但是这两者还是各有不同侧重点的：

1. CountDownLatch一般用于某个线程A等待若干个其他线程执行完任务之后，它才执行；而CyclicBarrier一般用于一组线程互相等待至某个状态，然后这一组线程再同时执行；CountDownLatch强调一个线程等多个线程完成某件事情。CyclicBarrier是多个线程互等，等大家都完成，再携手共进。
2. 调用CountDownLatch的countDown方法后，当前线程并不会阻塞，会继续往下执行；而调用CyclicBarrier的await方法，会阻塞当前线程，直到CyclicBarrier指定的线程全部都到达了指定点的时候，才能继续往下执行；
3. CountDownLatch方法比较少，操作比较简单，而CyclicBarrier提供的方法更多，比如能够通过getNumberWaiting()，isBroken()这些方法获取当前多个线程的状态，**并且CyclicBarrier的构造方法可以传入barrierAction**，指定当所有线程都到达时执行的业务功能；
4. CountDownLatch是不能复用的，而CyclicBarrier是可以复用的。

## 8.2 Semaphore，Exchanger

[大白话说java并发工具类-Semaphore，Exchanger](https://juejin.im/post/5aeec49b518825673614d183#heading-0)

# 九、生产者--消费者

[一篇文章，让你彻底弄懂生产者--消费者问题](https://juejin.im/post/5aeec675f265da0b7c072c56)

1. wait/notify的消息通知机制实现生产者-消费者
2. Lock中Condition的await/signalAll实现生产者-消费者
3. BlockingQueue实现生产者-消费者

# 十、锁相关

## 10.1 synchronized和lock

　Lock和synchronized的选择

　　总结来说，Lock和synchronized有以下几点不同：

　　1. Lock是一个接口，而synchronized是Java中的关键字，synchronized是内置的语言实现；

　　2. synchronized在发生异常时，会自动释放线程占有的锁，因此不会导致死锁现象发生；而Lock在发生异常时，如果没有主动通过unLock()去释放锁，则很可能造成死锁现象，因此使用Lock时需要在finally块中释放锁；

　　3. Lock可以让等待锁的线程响应中断，而synchronized却不行，使用synchronized时，等待的线程会一直等待下去，不能够响应中断；

　　4. 通过Lock可以知道有没有成功获取锁，而synchronized却无法办到。

　　5. Lock可以提高多个线程进行读操作的效率。

　　在性能上来说，如果竞争资源不激烈，两者的性能是差不多的，而当竞争资源非常激烈时（即有大量线程同时竞争），此时Lock的性能要远远优于synchronized。所以说，在具体使用时要根据适当情况选择。

## 10.2 分布式锁

[分布式锁](https://www.cnblogs.com/seesun2012/p/9214653.html)

[redis分布式锁的正确方式](https://www.cnblogs.com/linjiqin/p/8003838.html)

[zk分布式锁](https://www.cnblogs.com/linjiqin/p/6057290.html)

[zk分布式锁原理](https://www.cnblogs.com/linjiqin/p/6052031.html)

