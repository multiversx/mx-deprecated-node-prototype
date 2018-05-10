package network.elrond;


import network.elrond.consensus.Validator;
import java.util.List;

public class UtilTest {
    public static void displayListValidators(List<Validator> list) {
        for (int i = 0; i < list.size(); i++) {
            Validator v = list.get(i);

            System.out.println(v.getPubKey() + ", S: " + v.getStake().toString(10) + ", R: " + v.getRating());
        }
        System.out.println();
    }
}
