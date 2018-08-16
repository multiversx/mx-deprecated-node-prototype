package network.elrond.data;

import java.util.Map;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toMap;


public enum LocationType {
    NETWORK(1),
    LOCAL(2),
    BOTH(3);

    private final int locationIdx;
    private final static Map<Integer, LocationType> map =
            stream(LocationType.values()).collect(toMap(leg -> leg.locationIdx, leg -> leg));


    LocationType(final int locationIdx) {
        this.locationIdx = locationIdx;
    }

    public static LocationType valueOf(int locationIdx) {
        return map.get(locationIdx);
    }

    public int getIndex() {
        return (locationIdx);
    }
}
