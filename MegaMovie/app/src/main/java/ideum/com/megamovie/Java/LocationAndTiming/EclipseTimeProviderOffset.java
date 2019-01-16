package ideum.com.megamovie.Java.LocationAndTiming;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Calendar;
import java.util.EnumMap;

import ideum.com.megamovie.Java.Application.Config;
import ideum.com.megamovie.R;

import static ideum.com.megamovie.Java.LocationAndTiming.EclipseTimes.Phase.c1;
import static ideum.com.megamovie.Java.LocationAndTiming.EclipseTimes.Phase.c2;
import static ideum.com.megamovie.Java.LocationAndTiming.EclipseTimes.Phase.c3;
import static ideum.com.megamovie.Java.LocationAndTiming.EclipseTimes.Phase.c4;
import static ideum.com.megamovie.Java.LocationAndTiming.EclipseTimes.Phase.cm;

public class EclipseTimeProviderOffset extends EclipseTimeProvider {

    private Long dummyC2Time = null;

    @Override protected EnumMap<EclipseTimes.Phase,Long> getContactTimes() {
        if(dummyC2Time == null) {
            dummyC2Time = Calendar.getInstance().getTimeInMillis() + Config.DUMMY_C2_LEAD_TIME;
        }

      Long offset = dummyC2Time - contactTimes.get(c2);
        EnumMap<EclipseTimes.Phase,Long> offsetTimes = new EnumMap<>(EclipseTimes.Phase.class);
        for(EclipseTimes.Phase phase : contactTimes.keySet()) {
            offsetTimes.put(phase,contactTimes.get(phase) + offset);
        }
        return offsetTimes;
   }


}
