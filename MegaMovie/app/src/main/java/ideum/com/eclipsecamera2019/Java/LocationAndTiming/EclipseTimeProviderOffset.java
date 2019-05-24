package ideum.com.eclipsecamera2019.Java.LocationAndTiming;

import java.util.Calendar;
import java.util.EnumMap;

import ideum.com.eclipsecamera2019.Java.Application.Config;

import static ideum.com.eclipsecamera2019.Java.LocationAndTiming.EclipseTimes.Phase.c2;

public class EclipseTimeProviderOffset extends EclipseTimeProvider {

    private Long dummyC2Time = null;

    @Override protected EnumMap<EclipseTimes.Phase,Long> getContactTimes() {
        if(dummyC2Time == null) {
            dummyC2Time = getTimeInMillisSinceEpoch() + Config.DUMMY_C2_LEAD_TIME;
        }

      Long offset = dummyC2Time - contactTimes.get(c2);
        EnumMap<EclipseTimes.Phase,Long> offsetTimes = new EnumMap<>(EclipseTimes.Phase.class);
        for(EclipseTimes.Phase phase : contactTimes.keySet()) {
            offsetTimes.put(phase,contactTimes.get(phase) + offset);
        }
        return offsetTimes;
   }


}
