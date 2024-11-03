package com.snehasishroy.queue;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// Got one WA -- still not 100% correct
// DRAFT State
class BoundedBlockingQueue {

    Node dummyHead, dummyTail;
    // dummyHead -> message1 ==> message2 ---> message3 ===> dummyTail
    final int capacity;
    AtomicInteger size;
    // Separate locks for head and tail operations
    private final Lock headLock;
    private final Lock tailLock;
    private final Condition notEmpty;
    private final Condition notFull;

    public BoundedBlockingQueue(int capacity) {
        dummyHead = new Node(-1);
        dummyTail = new Node(-1);
        dummyHead.next = dummyTail;
        dummyTail.prev = dummyHead;
        size = new AtomicInteger(0);
        this.capacity = capacity;
        this.headLock = new ReentrantLock();
        this.tailLock = new ReentrantLock();
        this.notEmpty = headLock.newCondition();
        this.notFull = tailLock.newCondition();
    }

    public void enqueue(int element) throws InterruptedException {
        tailLock.lock();
        try {
            while (size.get() == capacity) {
                notFull.await();
            }
            Node node = new Node(element);
            Node prev = dummyTail.prev;
            // Insert new node at tail
            prev.next = node;
            node.prev = prev;
            node.next = dummyTail;
            dummyTail.prev = node;

            size.incrementAndGet();
            System.out.println("Put " + element);
        } finally {
            System.out.println("Released tail lock by enqueue" + Thread.currentThread().getName());
            tailLock.unlock();
            signalNotEmpty();
        }
    }

    public int dequeue() throws InterruptedException {
        headLock.lock();
        try {
            System.out.println("Took head lock " + Thread.currentThread().getName());
            while (size.get() == 0) {
                System.out.println("released head lock");
                notEmpty.await();
            }
            System.out.println("size not null reacquired head lock " + Thread.currentThread().getName());
            // Remove first node
            Node first = dummyHead.next;
            Node next = first.next;
            dummyHead.next = next;
            next.prev = dummyHead;

            size.decrementAndGet();

            System.out.println(first.message);
            return first.message;
        } finally {
            headLock.unlock();
            signalNotFull();
        }
    }

    public int size() {
        return size.get();
    }

    private void signalNotEmpty() {
        System.out.println("trying to get head lock " + Thread.currentThread().getName());
        headLock.lock();
        System.out.println("got head lock" + Thread.currentThread().getName());
        try {
            notEmpty.signalAll();
        } finally {
            headLock.unlock();
        }
    }

    private void signalNotFull() {
        System.out.println("trying to get tail lock " + Thread.currentThread().getName());
        tailLock.lock();
        System.out.println("Got tail lock " + Thread.currentThread().getName());
        try {
            notFull.signalAll();
        } finally {
            tailLock.unlock();
        }
    }
}