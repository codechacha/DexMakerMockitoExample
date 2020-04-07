package com.codechacha.dexmakermockito;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoSession;

import static android.provider.Settings.Global.DEVICE_NAME;
import static com.android.dx.mockito.inline.extended.ExtendedMockito.doReturn;
import static com.android.dx.mockito.inline.extended.ExtendedMockito.mockitoSession;
import static com.android.dx.mockito.inline.extended.ExtendedMockito.spyOn;
import static com.android.dx.mockito.inline.extended.ExtendedMockito.staticMockMarker;
import static com.android.dx.mockito.inline.extended.ExtendedMockito.verify;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;


@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    private static class SuperClass {
        final String returnA() {
            return "superA";
        }

        static String returnB() {
            return "superB";
        }

        static String returnC() {
            return "superC";
        }
    }

    private static final class SubClass extends SuperClass {
        static String recorded = null;

        static String returnC() {
            return "subC";
        }

        static final String record(String toRecord) {
            recorded = toRecord;
            return "record";
        }
    }

    @Test
    public void mockingExample() {
        assertEquals("superB", SuperClass.returnB());

        MockitoSession session = mockitoSession().mockStatic(SuperClass.class).startMocking();
        try {
            assertNull(SuperClass.returnB());

            when(SubClass.returnB()).thenReturn("fakeB");
            assertEquals("fakeB", SuperClass.returnB());
        } finally {
            session.finishMocking();
        }
    }

    @Test
    public void spyStatic() throws Exception {
        ContentResolver resolver = InstrumentationRegistry.getTargetContext().getContentResolver();
        String deviceName = Settings.Global.getString(resolver, DEVICE_NAME);

        MockitoSession session = mockitoSession().spyStatic(Settings.Global.class).startMocking();
        try {
            // Cannot call when(Settings.getString(any(ContentResolver.class), eq("...")))
            // as any(ContentResolver.class) returns null which makes getString fail. Hence need to
            // use less lambda API
            doReturn("23").when(() -> Settings.Global.getString(any
                    (ContentResolver.class), eq("twenty three")));

            doReturn(42).when(() -> Settings.Global.getInt(any
                    (ContentResolver.class), eq("fourty two")));

            // Make sure behavior is changed
            assertEquals("23", Settings.Global.getString(resolver, "twenty three"));
            assertEquals(42, Settings.Global.getInt(resolver, "fourty two"));

            // Make sure non-mocked methods work as before
            assertEquals(deviceName, Settings.Global.getString(resolver, DEVICE_NAME));
        } finally {
            session.finishMocking();
        }
    }

    @Test
    public void resetMock() throws Exception {
        MockitoSession session = mockitoSession().mockStatic(SuperClass.class).startMocking();
        try {
            assertNull(SuperClass.returnB());

            when(SuperClass.returnB()).thenReturn("fakeB");
            assertEquals("fakeB", SuperClass.returnB());

            reset(staticMockMarker(SuperClass.class));
            assertNull(SuperClass.returnB());
        } finally {
            session.finishMocking();
        }
    }

    @Test
    public void mockOverriddenStaticMethod() throws Exception {
        MockitoSession session = mockitoSession().mockStatic(SubClass.class).startMocking();
        try {
            // By default all static methods of the mocked class should return the default answers
            assertNull(SubClass.returnB());
            assertNull(SubClass.returnC());

            // Super class is not mocked
            assertEquals("superB", SuperClass.returnB());
            assertEquals("superC", SuperClass.returnC());

            when(SubClass.returnB()).thenReturn("fakeB");
            when(SubClass.returnC()).thenReturn("fakeC");

            // Make sure behavior is changed
            assertEquals("fakeB", SubClass.returnB());
            assertEquals("fakeC", SubClass.returnC());

            // Super class should not be affected
            assertEquals("superB", SuperClass.returnB());
            assertEquals("superC", SuperClass.returnC());
        } finally {
            session.finishMocking();
        }

        // Mocking should be stopped
        assertEquals("superB", SubClass.returnB());
        assertEquals("subC", SubClass.returnC());
    }

    @Test
    public void mockSuperMethod() throws Exception {
        MockitoSession session = mockitoSession().mockStatic(SuperClass.class).startMocking();
        try {
            // By default all static methods of the mocked class should return the default answers
            assertNull(SuperClass.returnB());
            assertNull(SuperClass.returnC());

            // Sub class should not be affected
            assertEquals("superB", SubClass.returnB());
            assertEquals("subC", SubClass.returnC());

            when(SuperClass.returnB()).thenReturn("fakeB");
            when(SuperClass.returnC()).thenReturn("fakeC");

            // Make sure behavior is changed
            assertEquals("fakeB", SuperClass.returnB());
            assertEquals("fakeC", SuperClass.returnC());

            // Sub class should not be affected
            assertEquals("superB", SubClass.returnB());
            assertEquals("subC", SubClass.returnC());
        } finally {
            session.finishMocking();
        }

        // Mocking should be stopped
        assertEquals("superB", SuperClass.returnB());
        assertEquals("superC", SuperClass.returnC());
    }

    class FinalTrojan {
        final String finalOpen() { return "horse"; }
    }

    @Test
    public void testStubbingFinalMethod() {
        FinalTrojan mockF = mock(FinalTrojan.class);
        when(mockF.finalOpen()).thenReturn("soldiers");

        assertEquals("soldiers", mockF.finalOpen());
    }

    class T {
        String echo(String in) {
            return in;
        }
    }

    @Test
    public void testStubbing() {
        T mockT = mock(T.class);
        // mock objects return a default value when not stubbed
        assertNull(mockT.echo("Marco"));

        when(mockT.echo("Marco")).thenReturn("Polo");
        assertEquals("Polo", mockT.echo("Marco"));
    }

    @Test
    public void testStubbingOnSpy() {
        T spyT = spy(new T());
        assertEquals("Marco", spyT.echo("Marco"));

        when(spyT.echo("Marco")).thenReturn("Polo");
        assertEquals("Polo", spyT.echo("Marco"));
    }

    class F {
        final String finalEcho(String in) {
            return in;
        }
    }


    static class S {
        static String staticEcho(String in) {
            return in;
        }
    }

    @Test
    public void testStubbingStaticMethod() {
        MockitoSession session = mockitoSession().mockStatic(S.class).startMocking();
        try {
            when(S.staticEcho("Marco")).thenReturn("Polo");
            assertEquals("Polo", S.staticEcho("Marco"));
        } finally {
            session.finishMocking();
        }

        // Once the session is finished, all stubbings are reset
        assertEquals("Marco", S.staticEcho("Marco"));
    }

    @Test
    public void testSpyOn() {
        T originalT = new T();
        spyOn(originalT); // Returns void

        when(originalT.echo("Marco")).thenReturn("Polo");
        assertEquals("Polo", originalT.echo("Marco"));
    }


    static class UtilClass {
        public static String staticMethod(String str) {
            return str;
        }

        public static void staticVoidMethod(String str) {
            staticMethod(str);
        }
    }

    @Test
    public void testVerifyStaticMethod() {
        MockitoSession session = mockitoSession().spyStatic(UtilClass.class).startMocking();
        try {
            UtilClass.staticVoidMethod("string");
            UtilClass.staticMethod("string");
            verify(() -> UtilClass.staticMethod("string"), atLeastOnce());
            verify(() -> UtilClass.staticMethod("string"), times(2));
            verify(() -> UtilClass.staticVoidMethod("string"), atLeastOnce());
            verify(() -> UtilClass.staticVoidMethod("string"), atMost(2));
        } finally {
            session.finishMocking();
        }
    }

}
