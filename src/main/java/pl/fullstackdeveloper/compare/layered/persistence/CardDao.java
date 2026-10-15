package pl.fullstackdeveloper.compare.layered.persistence;

import java.util.HashMap;
import java.util.Map;

public class CardDao {

    private final Map<String, CardRecord> records = new HashMap<>();

    public CardRecord findByNumber(String number) {
        return records.get(number);
    }

    public void update(CardRecord record) {
        records.put(record.getNumber(), record);
    }

}
