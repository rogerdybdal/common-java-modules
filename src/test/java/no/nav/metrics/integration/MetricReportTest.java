package no.nav.metrics.integration;

import no.nav.metrics.MetricsFactory;
import no.nav.metrics.TestUtil;
import no.nav.metrics.aspects.Count;
import no.nav.metrics.aspects.CountAspect;
import no.nav.metrics.aspects.Timed;
import no.nav.metrics.aspects.TimerAspect;
import org.junit.Test;

import java.net.ServerSocket;
import java.util.List;

import static no.nav.metrics.TestUtil.*;
import static org.junit.Assert.assertEquals;

public class MetricReportTest {

    @Test
    public void aspectOgProxySkalRapportereLikeDataForTimer() throws Exception {
        TestUtil.resetMetrics();
        Thread.sleep(100);

        final TimeMe timerProxy = MetricsFactory.createTimerProxy("TimeMe", new TimeMeImpl(), TimeMe.class);
        final TimeMe timerAspect = TestUtil.lagAspectProxy(new TimeMeImpl(), new TimerAspect());

        new Thread(new Runnable() {
            @Override
            public void run() {
                timerProxy.time();
                timerAspect.time();
            }
        }).start();

        sjekkLiktPaSocketData();
    }

    @Test
    public void aspectOgProxySkalRapportereLikeDataForEvent() throws Exception {
        TestUtil.resetMetrics();
        Thread.sleep(100);

        final EventMe eventProxy = MetricsFactory.createEventProxy("EventMe", new EventMeImpl(), EventMe.class);
        final EventMe eventAspect = TestUtil.lagAspectProxy(new EventMeImpl(), new CountAspect());

        new Thread(new Runnable() {
            @Override
            public void run() {
                eventProxy.event();
                eventAspect.event();
            }
        }).start();

        sjekkLiktPaSocketData();
    }

    private void sjekkLiktPaSocketData() throws Exception {
        ServerSocket serverSocket = new ServerSocket(getSensuClientPort());

        String line1 = lesLinjeFraSocket(serverSocket);
        String line2 = lesLinjeFraSocket(serverSocket);

        if (line2 == null) { // Om meldingene kom grupper i samme melding
            List<String> strings = splitStringsFraMelding(line1);
            line1 = strings.get(0);
            line2 = strings.get(1);
        }

        assertEquals(fjernTimestamps(line1), fjernTimestamps(line2));

        serverSocket.close();
    }

    private String fjernTimestamps(String data) {
        return data
                .replaceAll("value=\\d+", "value=<dummy>")
                .replaceAll("\\d{19}", "<timestamp>");
    }

    public interface TimeMe {
        void time();
    }

    public static class TimeMeImpl implements TimeMe {
        @Timed
        @Override
        public void time() {

        }
    }

    public interface EventMe {
        void event();
    }

    public static class EventMeImpl implements EventMe {
        @Count
        @Override
        public void event() {

        }
    }


}
