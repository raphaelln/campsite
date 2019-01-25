package com.upgrade.campsite.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component
@Scope("singleton")
public final class ResourceLocker {

    private static final ResourceLocker INSTANCE = new ResourceLocker();

    private ReadWriteLock lock;

    private Lock writeLock;

    private ResourceLocker() {
        this.lock = new ReentrantReadWriteLock();
        this.writeLock = this.lock.writeLock();
    }

    public static ResourceLocker getInstance() {
        return INSTANCE;
    }

    public void lock() {
      writeLock.lock();
    }

    public void unlock() {
      writeLock.unlock();
    }
}