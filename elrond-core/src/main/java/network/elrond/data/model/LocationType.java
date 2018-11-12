package network.elrond.data.model;

import java.util.Map;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toMap;


public enum LocationType {
    NETWORK(1),
    LOCAL(2),
    BOTH(3);

    private final int locationIdx;
    private final static Map<Integer, LocationType> MAP =
            stream(LocationType.values()).collect(toMap(leg -> leg.locationIdx, leg -> leg));


    private LocationType(final int locationIdx) {
        this.locationIdx = locationIdx;
    }

    public static LocationType valueOf(int locationIdx) {
        return MAP.get(locationIdx);
    }

    public int getIndex() {
        return (locationIdx);
    }
}
