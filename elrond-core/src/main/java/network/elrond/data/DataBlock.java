package network.elrond.data;

import com.fasterxml.jackson.annotation.JsonFilter;
import network.elrond.core.Util;

@JsonFilter(Util.SIGNATURE_FILTER)
public class DataBlock extends Block {

}
