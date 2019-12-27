package com.tagtraum.perf.gcviewer.view;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.time.ZonedDateTime;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import com.tagtraum.perf.gcviewer.math.DoubleData;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.view.model.GCPreferences;

/**
 * Testcase to test usage of "datestamp" flag in {@link ModelChartImpl}.
 * 
 * @author <a href="maciej.kwiecien@gmail.com">xylu</a>
 * <p>created on: 21.02.2014</p>
 */
class ModelChartImplTest {

    private static Stream<Arguments> getTestCases() {
        return Stream.of(
                Arguments.arguments(new TestCase().withDateStamp(true).withShowDateStamp(true).withExpectedShowDateStamp(true)),
                Arguments.arguments(new TestCase().withDateStamp(true).withShowDateStamp(true).withExpectedShowDateStamp(true)),
                Arguments.arguments(new TestCase().withDateStamp(false).withShowDateStamp(true).withExpectedShowDateStamp(true)),
                Arguments.arguments(new TestCase().withDateStamp(false).withShowDateStamp(false).withExpectedShowDateStamp(false))
        );
    }

    @ParameterizedTest
    @MethodSource("getTestCases")
    public void shouldShowOrNotDateStampAccordingToModelAndSettings(TestCase testCase) {
        //given
        ModelChartImpl modelChart = new ModelChartImpl();
        GCPreferences preferences = new GCPreferences();
        GCModel gcModel = Mockito.mock(GCModel.class);
        Mockito.when(gcModel.hasDateStamp()).thenReturn(testCase.hasDateStamp());
        Mockito.when(gcModel.getFirstDateStamp()).thenReturn(ZonedDateTime.now());
        Mockito.when(gcModel.getPause()).thenReturn(new DoubleData());
        preferences.setShowDateStamp(testCase.isShowDateStamp());

        //when
        modelChart.setModel(gcModel, preferences);

        //then
        assertThat(modelChart.isShowDateStamp(), equalTo(testCase.isExpectedShowDateStamp()));
    }

    private static class TestCase {
        private boolean hasDateStamp;
        private boolean showDateStamp;
        private boolean expectedShowDateStamp;

        public TestCase withDateStamp(boolean hasDateStamp) {
            this.hasDateStamp = hasDateStamp;
            return this;
        }

        public TestCase withShowDateStamp(boolean showDateStamp) {
            this.showDateStamp = showDateStamp;
            return this;
        }


        public TestCase withExpectedShowDateStamp(boolean expectedShowDateStamp) {
            this.expectedShowDateStamp = expectedShowDateStamp;
            return this;
        }

        private boolean hasDateStamp() {
            return hasDateStamp;
        }

        private boolean isShowDateStamp() {
            return showDateStamp;
        }

        private boolean isExpectedShowDateStamp() {
            return expectedShowDateStamp;
        }
    }
}
