package com.tagtraum.perf.gcviewer;

import com.tagtraum.perf.gcviewer.model.GCModel;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.util.Date;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(Theories.class)
public class ModelChartImplTest {

    @DataPoint
    public static TestCase HAS_DATE_STAMP_DATE_STAMP_TURNED_ON_IN_PREF = new TestCase().withDateStamp(true).withShowDateStamp(true).withExpectedShowDateStamp(true);
    @DataPoint
    public static TestCase HAS_DATE_STAMP_DATE_STAMP_TURNED_OFF_IN_PREF = new TestCase().withDateStamp(true).withShowDateStamp(false).withExpectedShowDateStamp(false);
    @DataPoint
    public static TestCase NO_DATE_STAMP_DATE_STAMP_TURNED_ON_IN_PREF = new TestCase().withDateStamp(false).withShowDateStamp(true).withExpectedShowDateStamp(false);
    @DataPoint
    public static TestCase NO_DATE_STAMP_DATE_STAMP_TURNED_OFF_IN_PREF = new TestCase().withDateStamp(false).withShowDateStamp(false).withExpectedShowDateStamp(false);


    @Theory
    public void shouldShowOrNotDateStampAccordingToModelAndSettings(TestCase testCase) throws Exception {
        //given
        ModelChartImpl modelChart = new ModelChartImpl();
        GCPreferences preferences = new GCPreferences();
        GCModel gcModel = Mockito.mock(GCModel.class);
        Mockito.when(gcModel.hasDateStamp()).thenReturn(testCase.hasDateStamp());
        Mockito.when(gcModel.getFirstDateStamp()).thenReturn(new Date());
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
