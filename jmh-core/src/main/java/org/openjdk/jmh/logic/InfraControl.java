/*
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package org.openjdk.jmh.logic;

import org.openjdk.jmh.runner.ActualParams;
import org.openjdk.jmh.runner.parameters.TimeValue;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The InfraControl logic class.
 * This is the rendezvous class for benchmark handler and JMH.
 *
 * @author staffan.friberg@oracle.com, anders.astrand@oracle.com, aleksey.shipilev@oracle.com
 */
public class InfraControl extends InfraControlL4 {

    private static final Unsafe U;

    static {
        try {
            Field unsafe = Unsafe.class.getDeclaredField("theUnsafe");
            unsafe.setAccessible(true);
            U = (Unsafe) unsafe.get(null);
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }

        consistencyCheck();
    }

    static void consistencyCheck() {
        // checking the fields are not reordered
        check("isDone");
    }

    static void check(String fieldName) {
        final long requiredGap = 128;
        long markerBegin = getOffset("markerBegin");
        long markerEnd = getOffset("markerEnd");
        long off = getOffset(fieldName);
        if (markerEnd - off < requiredGap || off - markerBegin < requiredGap) {
            throw new IllegalStateException("Consistency check failed for " + fieldName + ", off = " + off + ", markerBegin = " + markerBegin + ", markerEnd = " + markerEnd);
        }
    }

    static long getOffset(String fieldName) {
        try {
            Field f = InfraControl.class.getField(fieldName);
            return U.objectFieldOffset(f);
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException(e);
        }
    }

    public InfraControl(int threads, boolean syncIterations, TimeValue loopTime, CountDownLatch preSetup, CountDownLatch preTearDown, boolean lastIteration, TimeUnit timeUnit, int batchSize, ActualParams params) {
        super(threads, syncIterations, loopTime, preSetup, preTearDown, lastIteration, timeUnit, batchSize, params);
    }

    /**
     * @return requested loop duration in milliseconds.
     */
    public long getDuration() {
        return getDuration(TimeUnit.MILLISECONDS);
    }

    /**
     * @param unit timeunit to use
     * @return requested loop duration in the requested unit.
     */
    public long getDuration(TimeUnit unit) {
        return unit.convert(duration, TimeUnit.NANOSECONDS);
    }

    public void preSetup() {
        try {
            preSetup.countDown();
            preSetup.await();
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    public void preTearDown() throws InterruptedException {
        preTearDown.countDown();
        preTearDown.await();
    }

    public void preSetupForce() {
        preSetup.countDown();
    }

    public void preTearDownForce() {
        preTearDown.countDown();
    }

    public boolean isLastIteration() {
        return lastIteration;
    }
}

abstract class InfraControlL0 {
    public int markerBegin;
}

abstract class InfraControlL1 extends InfraControlL0 {
    private boolean p001, p002, p003, p004, p005, p006, p007, p008;
    private boolean p011, p012, p013, p014, p015, p016, p017, p018;
    private boolean p021, p022, p023, p024, p025, p026, p027, p028;
    private boolean p031, p032, p033, p034, p035, p036, p037, p038;
    private boolean p041, p042, p043, p044, p045, p046, p047, p048;
    private boolean p051, p052, p053, p054, p055, p056, p057, p058;
    private boolean p061, p062, p063, p064, p065, p066, p067, p068;
    private boolean p071, p072, p073, p074, p075, p076, p077, p078;
    private boolean p101, p102, p103, p104, p105, p106, p107, p108;
    private boolean p111, p112, p113, p114, p115, p116, p117, p118;
    private boolean p121, p122, p123, p124, p125, p126, p127, p128;
    private boolean p131, p132, p133, p134, p135, p136, p137, p138;
    private boolean p141, p142, p143, p144, p145, p146, p147, p148;
    private boolean p151, p152, p153, p154, p155, p156, p157, p158;
    private boolean p161, p162, p163, p164, p165, p166, p167, p168;
    private boolean p171, p172, p173, p174, p175, p176, p177, p178;
}

/**
 * @see BlackHole for rationale
 */
abstract class InfraControlL2 extends InfraControlL1 {
    /* Flag for if we are done or not.
     * This is specifically the public field, so to spare one virtual call.
     */
    public volatile boolean isDone;

    public volatile boolean volatileSpoiler;

    /** How long we should loop */
    public final long duration;

    public final CountDownLatch preSetup;
    public final CountDownLatch preTearDown;
    public final boolean lastIteration;
    public final TimeUnit timeUnit;
    public final int threads;
    public final boolean syncIterations;

    public final AtomicInteger warmupVisited, warmdownVisited;
    public volatile boolean warmupShouldWait, warmdownShouldWait;

    public final int batchSize;
    private final ActualParams params;

    public InfraControlL2(int threads, boolean syncIterations, TimeValue loopTime, CountDownLatch preSetup, CountDownLatch preTearDown, boolean lastIteration, TimeUnit timeUnit, int batchSize, ActualParams params) {
        this.threads = threads;
        this.syncIterations = syncIterations;
        this.warmupVisited = new AtomicInteger();
        this.warmdownVisited = new AtomicInteger();

        warmupShouldWait = syncIterations;
        warmdownShouldWait = syncIterations;
        this.preSetup = preSetup;
        this.preTearDown = preTearDown;
        this.duration = loopTime.convertTo(TimeUnit.NANOSECONDS);
        this.lastIteration = lastIteration;
        this.timeUnit = timeUnit;
        this.params = params;
        this.batchSize = batchSize;
    }

    public void announceWarmupReady() {
        if (!syncIterations) return;
        int v = warmupVisited.incrementAndGet();
        if (v == threads) {
            warmupShouldWait = false;
        }

        if (v > threads) {
            throw new IllegalStateException("More threads than expected");
        }
    }

    public void announceWarmdownReady() {
        if (!syncIterations) return;
        int v = warmdownVisited.incrementAndGet();
        if (v == threads) {
            warmdownShouldWait = false;
        }

        if (v > threads) {
            throw new IllegalStateException("More threads than expected");
        }
    }

    public String getParam(String name) {
        if (!params.containsKey(name)) {
            throw new IllegalStateException("The value for the parameter \"" + name + "\" is not set.");
        }
        return params.get(name);
    }

}

abstract class InfraControlL3 extends InfraControlL2 {
    private boolean q001, q002, q003, q004, q005, q006, q007, q008;
    private boolean q011, q012, q013, q014, q015, q016, q017, q018;
    private boolean q021, q022, q023, q024, q025, q026, q027, q028;
    private boolean q031, q032, q033, q034, q035, q036, q037, q038;
    private boolean q041, q042, q043, q044, q045, q046, q047, q048;
    private boolean q051, q052, q053, q054, q055, q056, q057, q058;
    private boolean q061, q062, q063, q064, q065, q066, q067, q068;
    private boolean q071, q072, q073, q074, q075, q076, q077, q078;
    private boolean q101, q102, q103, q104, q105, q106, q107, q108;
    private boolean q111, q112, q113, q114, q115, q116, q117, q118;
    private boolean q121, q122, q123, q124, q125, q126, q127, q128;
    private boolean q131, q132, q133, q134, q135, q136, q137, q138;
    private boolean q141, q142, q143, q144, q145, q146, q147, q148;
    private boolean q151, q152, q153, q154, q155, q156, q157, q158;
    private boolean q161, q162, q163, q164, q165, q166, q167, q168;
    private boolean q171, q172, q173, q174, q175, q176, q177, q178;

    public InfraControlL3(int threads, boolean syncIterations, TimeValue loopTime, CountDownLatch preSetup, CountDownLatch preTearDown, boolean lastIteration, TimeUnit timeUnit, int batchSize, ActualParams params) {
        super(threads, syncIterations, loopTime, preSetup, preTearDown, lastIteration, timeUnit, batchSize, params);
    }
}

abstract class InfraControlL4 extends InfraControlL3 {
    public int markerEnd;

    public InfraControlL4(int threads, boolean syncIterations, TimeValue loopTime, CountDownLatch preSetup, CountDownLatch preTearDown, boolean lastIteration, TimeUnit timeUnit, int batchSize, ActualParams params) {
        super(threads, syncIterations, loopTime, preSetup, preTearDown, lastIteration, timeUnit, batchSize, params);
    }
}

