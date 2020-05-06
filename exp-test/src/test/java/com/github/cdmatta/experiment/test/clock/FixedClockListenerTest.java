package com.github.cdmatta.experiment.test.clock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestContext;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FixedClockListenerTest {

    private static final ZoneId DEFAULT_TIMEZONE = TimeZone.getDefault().toZoneId();
    private static final String INSTANT_STRING = "2020-01-01T10:00:00Z";
    private static final Instant DEFAULT_INSTANT;

    static {
        try {
            DEFAULT_INSTANT = Instant.parse(FixedClock.class.getDeclaredMethod("value").getDefaultValue().toString());
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Should never happen", e);
        }
    }

    @Mock(lenient = true)
    private ApplicationContext applicationContext;
    @Mock(lenient = true)
    private TestContext testContext;
    @Mock
    private Clock mockedClock;

    private FixedClockListener listener = new FixedClockListener();

    @BeforeEach
    public void beforeEach() {
        when(applicationContext.getBean(Clock.class)).thenReturn(mockedClock);
    }


    static class WithoutFixedClockAnnotation {
        public void aMethod() {
        }
    }

    @Test
    public void shouldDoNothing_WithoutClassAnnotation_OnBeforeTestClass() throws Exception {
        WithoutFixedClockAnnotation instance = new WithoutFixedClockAnnotation();
        listener.beforeTestClass(mockTestClassContext(instance));
        assertThat(mockedClock.instant()).isNull();
        assertThat(mockedClock.getZone()).isNull();
    }

    @Test
    public void shouldDoNothing_WithoutClassAnnotation_OnAfterTestClass() throws Exception {
        WithoutFixedClockAnnotation instance = new WithoutFixedClockAnnotation();
        when(mockedClock.getZone()).thenReturn(DEFAULT_TIMEZONE);
        listener.afterTestClass(mockTestClassContext(instance));
        assertThat(mockedClock.getZone()).isEqualTo(DEFAULT_TIMEZONE);
    }

    @Test
    public void shouldDoNothing_WithoutClassAnnotation_OnBeforeTestMethod() throws Exception {
        WithoutFixedClockAnnotation instance = new WithoutFixedClockAnnotation();
        listener.beforeTestMethod(mockTestMethodContext(instance, "aMethod"));
        assertThat(mockedClock.instant()).isNull();
        assertThat(mockedClock.getZone()).isNull();
    }

    @Test
    public void shouldDoNothing_WithoutClassAnnotation_OnAfterTestMethod() throws Exception {
        WithoutFixedClockAnnotation instance = new WithoutFixedClockAnnotation();
        listener.afterTestMethod(mockTestMethodContext(instance, "aMethod"));
        assertThat(mockedClock.instant()).isNull();
        assertThat(mockedClock.getZone()).isNull();
    }


    static class FixedClockOnlyMethodAnnotated {
        @FixedClock
        public void aMethod() {
        }
    }

    @Test
    public void shouldThrowException_WhenOnlyMethodIsAnnotated_OnBeforeTestMethod() throws Exception {
        FixedClockOnlyMethodAnnotated instance = new FixedClockOnlyMethodAnnotated();
        assertThatThrownBy(() -> listener.beforeTestMethod(mockTestMethodContext(instance, "aMethod")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void shouldThrowException_WhenOnlyMethodIsAnnotated_OnAfterTestMethod() throws Exception {
        FixedClockOnlyMethodAnnotated instance = new FixedClockOnlyMethodAnnotated();
        assertThatThrownBy(() -> listener.afterTestMethod(mockTestMethodContext(instance, "aMethod")))
                .isInstanceOf(IllegalStateException.class);
    }


    @FixedClock
    static class WithFixedClockClassAnnotation {
        public void aMethod() {
        }
    }

    @Test
    public void shouldSetUpMockedClock_OnBeforeTestClass() throws Exception {
        WithFixedClockClassAnnotation instance = new WithFixedClockClassAnnotation();
        listener.beforeTestClass(mockTestClassContext(instance));
        assertThat(mockedClock.instant()).isEqualTo(DEFAULT_INSTANT);
        assertThat(mockedClock.getZone()).isEqualTo(DEFAULT_TIMEZONE);
    }

    @Test
    public void shouldResetMock_OnAfterTestClass() throws Exception {
        WithFixedClockClassAnnotation instance = new WithFixedClockClassAnnotation();
        when(mockedClock.getZone()).thenReturn(DEFAULT_TIMEZONE);
        listener.afterTestClass(mockTestClassContext(instance));
        assertThat(mockedClock.getZone()).isNull();
    }


    @FixedClock
    static class WithFixedClockClassAndMethodAnnotation {
        @FixedClock(INSTANT_STRING)
        public void aMethod() {
        }
    }

    @Test
    public void shouldSetUpMockedClock_WithAnnotatedMethodValue_OnBeforeTestMethod() throws Exception {
        WithFixedClockClassAndMethodAnnotation instance = new WithFixedClockClassAndMethodAnnotation();
        listener.beforeTestMethod(mockTestMethodContext(instance, "aMethod"));
        assertThat(mockedClock.instant()).isEqualTo(Instant.parse(INSTANT_STRING));
        assertThat(mockedClock.getZone()).isEqualTo(DEFAULT_TIMEZONE);
    }


    @Test
    public void shouldSetUpMock_WithAnnotatedClassValue_OnAfterTestMethod() throws Exception {
        WithFixedClockClassAndMethodAnnotation instance = new WithFixedClockClassAndMethodAnnotation();
        listener.afterTestMethod(mockTestMethodContext(instance, "aMethod"));
        assertThat(mockedClock.instant()).isEqualTo(DEFAULT_INSTANT);
        assertThat(mockedClock.getZone()).isEqualTo(DEFAULT_TIMEZONE);
    }


    private TestContext mockTestClassContext(Object instance) {
        when(testContext.getTestInstance()).thenReturn(instance);
        doReturn(instance.getClass()).when(testContext).getTestClass();
        when(testContext.getApplicationContext()).thenReturn(this.applicationContext);
        return testContext;
    }

    private TestContext mockTestMethodContext(Object instance, String methodName) throws Exception {
        TestContext testContext = mockTestClassContext(instance);
        when(testContext.getTestMethod()).thenReturn(instance.getClass().getDeclaredMethod(methodName));
        return testContext;
    }
}
