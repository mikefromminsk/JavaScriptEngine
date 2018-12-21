package com.metabrain.gdb;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InfinityConstArrayTest {

    class TestCellParserConst implements InfinityConstArrayCell {

        long field1;
        long field2;

        TestCellParserConst() {
            this.field1 = 0;
            this.field2 = 0;
        }

        TestCellParserConst(long field1, long field2) {
            this.field1 = field1;
            this.field2 = field2;
        }

        @Override
        public void parse(byte[] data) {
            long[] longData = Bytes.toLongArray(data);
            field1 = longData[0];
            field2 = longData[1];
        }

        @Override
        public byte[] build() {
            long[] longs = new long[2];
            longs[0] = field1;
            longs[1] = field2;
            byte[] data = Bytes.fromLongArray(longs);
            return data;
        }

        @Override
        public int getSize() {
            return 2 * Long.BYTES;
        }
    }

    @Test
    void add() {
        InfinityConstArray testConstArray = new InfinityConstArray("constArrayTest");
        long index1 = testConstArray.add(new TestCellParserConst(258, 789));
        long index2 = testConstArray.add(new TestCellParserConst(345, 674));
        TestCellParserConst destination = new TestCellParserConst();
        testConstArray.get(index1, destination);
        assertEquals(258, destination.field1);
        testConstArray.get(index2, destination);
        assertEquals(674, destination.field2);
    }
}