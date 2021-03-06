package net.bytebuddy.asm;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.MethodList;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.test.utility.MockitoRule;
import net.bytebuddy.test.utility.ObjectPropertyAssertion;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class AsmVisitorWrapperForDeclaredMethodsTest {

    private static final int MODIFIERS = 42, FLAGS = 42;

    private static final String FOO = "foo", BAR = "bar", QUX = "qux", BAZ = "baz";

    @Rule
    public TestRule mockitoRule = new MockitoRule(this);

    @Mock
    private ElementMatcher<? super MethodDescription.InDefinedShape> matcher;

    @Mock
    private AsmVisitorWrapper.ForDeclaredMethods.MethodVisitorWrapper methodVisitorWrapper;

    @Mock
    private TypeDescription instrumentedType;

    @Mock
    private MethodDescription.InDefinedShape foo, bar;

    @Mock
    private ClassVisitor classVisitor;

    @Mock
    private MethodVisitor methodVisitor, wrappedVisitor;

    @Before
    public void setUp() throws Exception {
        when(instrumentedType.getDeclaredMethods()).thenReturn(new MethodList.Explicit<MethodDescription.InDefinedShape>(foo, bar));
        when(foo.getInternalName()).thenReturn(FOO);
        when(foo.getDescriptor()).thenReturn(QUX);
        when(bar.getInternalName()).thenReturn(BAR);
        when(bar.getDescriptor()).thenReturn(BAZ);
        when(classVisitor.visitMethod(eq(MODIFIERS), any(String.class), any(String.class), eq(BAZ), eq(new String[]{QUX + BAZ}))).thenReturn(methodVisitor);
        when(methodVisitorWrapper.wrap(instrumentedType, foo, methodVisitor)).thenReturn(wrappedVisitor);
        when(matcher.matches(foo)).thenReturn(true);
    }

    @Test
    public void testMatched() throws Exception {
        assertThat(new AsmVisitorWrapper.ForDeclaredMethods()
                .method(matcher, methodVisitorWrapper)
                .wrap(instrumentedType, classVisitor)
                .visitMethod(MODIFIERS, FOO, QUX, BAZ, new String[]{QUX + BAZ}), is(wrappedVisitor));
        verify(matcher).matches(foo);
        verifyNoMoreInteractions(matcher);
        verify(methodVisitorWrapper).wrap(instrumentedType, foo, methodVisitor);
        verifyNoMoreInteractions(methodVisitorWrapper);
    }

    @Test
    public void testNotMatched() throws Exception {
        assertThat(new AsmVisitorWrapper.ForDeclaredMethods()
                .method(matcher, methodVisitorWrapper)
                .wrap(instrumentedType, classVisitor)
                .visitMethod(MODIFIERS, BAR, BAZ, BAZ, new String[]{QUX + BAZ}), is(methodVisitor));
        verify(matcher).matches(bar);
        verifyNoMoreInteractions(matcher);
        verifyZeroInteractions(methodVisitorWrapper);
    }

    @Test
    public void testUnknown() throws Exception {
        assertThat(new AsmVisitorWrapper.ForDeclaredMethods()
                .method(matcher, methodVisitorWrapper)
                .wrap(instrumentedType, classVisitor)
                .visitMethod(MODIFIERS, FOO + BAR, QUX, BAZ, new String[]{QUX + BAZ}), is(methodVisitor));
        verifyZeroInteractions(matcher);
        verifyZeroInteractions(methodVisitorWrapper);
    }

    @Test
    public void testWriterFlags() throws Exception {
        assertThat(new AsmVisitorWrapper.ForDeclaredMethods().writerFlags(FLAGS).mergeWriter(0), is(FLAGS));
    }

    @Test
    public void testReaderFlags() throws Exception {
        assertThat(new AsmVisitorWrapper.ForDeclaredMethods().readerFlags(FLAGS).mergeReader(0), is(FLAGS));
    }

    @Test
    public void testObjectProperties() throws Exception {
        ObjectPropertyAssertion.of(AsmVisitorWrapper.ForDeclaredMethods.class).apply();
        ObjectPropertyAssertion.of(AsmVisitorWrapper.ForDeclaredMethods.Entry.class).apply();
        ObjectPropertyAssertion.of(AsmVisitorWrapper.ForDeclaredMethods.DispatchingVisitor.class).refine(new ObjectPropertyAssertion.Refinement<TypeDescription>() {
            @Override
            public void apply(TypeDescription mock) {
                when(mock.getDeclaredMethods()).thenReturn(new MethodList.Explicit<MethodDescription.InDefinedShape>(Mockito.mock(MethodDescription.InDefinedShape.class)));
            }
        }).apply();
    }
}
