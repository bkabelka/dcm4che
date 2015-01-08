//
/////////////////////////////////////////////////////////////////
//                 C O P Y R I G H T  (c) 2014                 //
//    A G F A   H E A L T H C A R E   C O R P O R A T I O N    //
//                    All Rights Reserved                      //
/////////////////////////////////////////////////////////////////
//
//        THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF
//                      AGFA CORPORATION
//       The copyright notice above does not evidence any
//      actual or intended publication of such source code.
//
/////////////////////////////////////////////////////////////////
//

package org.dcm4che3.imageio.stream;

import java.io.IOException;
import java.io.InputStream;

import javax.imageio.stream.ImageInputStreamImpl;

import org.dcm4che3.data.BulkData;
import org.dcm4che3.util.StreamUtils;

/**
 * Image input stream based on a {@link BulkData} object.
 * 
 * @author bernhard.kabelka
 */
public class BulkDataImageInputStream extends ImageInputStreamImpl {
    
    private final BulkData data;
    private InputStream stream;
    
    public BulkDataImageInputStream(BulkData data) {
        super();
        this.data = data;
    }
    
    @Override
    public void seek(long pos) throws IOException {
        long oldPos = streamPos;
        super.seek(pos);
        if (pos < oldPos || stream == null) {
            closeCurrentStream();
            stream = data.openStream();
            // The data.offset was already skipped automatically upon opening the stream.
            oldPos = data.offset;
        }
        StreamUtils.skipFully(stream, pos - oldPos);
    }
    
    @Override
    public int read() throws IOException {
        if (!prepareRead())
            return -1;

        bitOffset = 0;
        int val = stream.read();
        if (val != -1) {
            ++streamPos;
        }
        return val;
    }

    private boolean prepareRead() {
        return streamPos < data.offset + data.length;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (!prepareRead())
            return -1;

        bitOffset = 0;
        int nbytes = stream.read(b, off,
                Math.min(len, (int) (data.offset + data.length - streamPos)));
        if (nbytes != -1) {
            streamPos += nbytes;
        }
        return nbytes;
    }
    
    @Override
    public void close() throws IOException {
        super.close();
        closeCurrentStream();
    }
    
    private void closeCurrentStream() throws IOException {
        if (stream != null) {
            stream.close();
            stream = null;
        }
    }

}
