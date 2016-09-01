package org.logstash.common.io;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.logstash.ackedqueue.StringElement;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class FileMmapIOTest {
    private String folder;
    private MmapPageIO writeIo;
    private MmapPageIO readIo;
    private int pageNum;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        pageNum = 0;
        folder = temporaryFolder
                .newFolder("pages")
                .getPath();
        writeIo = new MmapPageIO(pageNum, 1024, folder);
        readIo = new MmapPageIO(pageNum, 1024, folder);
    }

    @Test
    public void roundTrip() throws Exception {
        List<StringElement> list = new ArrayList<>();
        List<StringElement> readList = new ArrayList<>();
        writeIo.create();
        for (int i = 1; i < 17; i++) {
            StringElement input = new StringElement("element-" + i, i);
            list.add(input);
            writeIo.write(input.serializeWithoutSeqNum(), input);
        }
        writeIo.close();
        readIo.open(1, 16);
        List<ReadElementValue> result = readIo.read(1, 16);
        for (ReadElementValue v : result) {
            StringElement element = StringElement.deserializeWithoutSeqNum(v.getBinaryValue(), v.getSeqNum());
            readList.add(element);
        }
        assertThat(readList, is(equalTo(list)));
    }
}