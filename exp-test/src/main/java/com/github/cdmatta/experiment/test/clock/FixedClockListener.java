package com.github.cdmatta.experiment.test.clock;

import com.github.cdmatta.experiment.test.utils.TestContextHelper;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

import java.time.Clock;
import java.time.Instant;
import java.util.TimeZone;

import static org.mockito.Mockito.*;

public class FixedClockListener extends AbstractTestExecutionListener {
    @Override
    public void beforeTestClass(TestContext testContext) {
        var contextHelper = new TestContextHelper(testContext);
        var classFixedClock = contextHelper.getTestClassAnnotation(FixedClock.class);
        if (classFixedClock == null) {
            return;
        }
        mockClock(contextHelper, classFixedClock);
    }

    @Override
    public void afterTestClass(TestContext testContext) {
        var contextHelper = new TestContextHelper(testContext);
        var annotation = contextHelper.getTestClassAnnotation(FixedClock.class);
        if (annotation == null) {
            return;
        }
        reset(contextHelper.getBean(Clock.class));
    }

    @Override
    public void beforeTestMethod(TestContext testContext) {
        var contextHelper = new TestContextHelper(testContext);
        var methodFixedClock = contextHelper.getTestMethodAnnotation(FixedClock.class);
        if (methodFixedClock == null) {
            return;
        }
        verifyClassAnnotation(contextHelper);
        mockClock(contextHelper, methodFixedClock);
    }

    @Override
    public void afterTestMethod(TestContext testContext) {
        var contextHelper = new TestContextHelper(testContext);
        var methodFixedClock = contextHelper.getTestMethodAnnotation(FixedClock.class);
        if (methodFixedClock == null) {
            return;
        }
        verifyClassAnnotation(contextHelper);

        var classFixedClock = contextHelper.getTestClassAnnotation(FixedClock.class);
        mockClock(contextHelper, classFixedClock);
    }

    private void verifyClassAnnotation(TestContextHelper helper) {
        var classAnnotation = helper.getTestClassAnnotation(FixedClock.class);
        if (classAnnotation == null) {
            throw new IllegalStateException("@FixedClock class level annotation is missing. Required to allow 'mock' clock bean injection.");
        }
    }

    private void mockClock(TestContextHelper helper, FixedClock fixedClock) {
        var instant = Instant.parse(fixedClock.value());
        Clock mockedClock = helper.getBean(Clock.class);
        when(mockedClock.instant()).thenReturn(instant);
        when(mockedClock.getZone()).thenReturn(TimeZone.getDefault().toZoneId());
    }
}
