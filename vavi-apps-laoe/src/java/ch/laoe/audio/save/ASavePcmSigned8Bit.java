
package ch.laoe.audio.save;

import javax.sound.sampled.AudioFormat;


/*********************************************************************************************************************************
 * 
 * This file is part of LAoE.
 * 
 * LAoE is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * LAoE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with LAoE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * 
 * Class: ASavePcmSigned8Bit @author olivier g�umann, neuch�tel (switzerland) @target JDK 1.3
 * 
 * PCM signed 8bit file saver
 * 
 * @version 27.11.00 new stream-technique oli4
 * 
 */
public class ASavePcmSigned8Bit extends ASave {
    /**
     * constructor
     */
    public ASavePcmSigned8Bit() {
        super();
        samplePtr = 0;
        channelPtr = 0;
        bytePtr = 0;
    }

    public ASave duplicate() {
        return new ASavePcmSigned8Bit();
    }

    public boolean supports(AudioFormat af) {
        if ((af.getEncoding() == AudioFormat.Encoding.PCM_SIGNED) && (af.getSampleSizeInBits() == 8)) {
            return true;
        } else {
            return false;
        }
    }

    private int samplePtr;

    private int channelPtr;

    private int bytePtr;

    public int read() {
        // calculate index
        int data = 0;

        // calculate data
        if (samplePtr < layer.getChannel(channelPtr).sample.length) {
            switch (bytePtr) {
            case 0:
                data = (int) layer.getChannel(channelPtr).sample[samplePtr] & 0x000000FF;
                break;

            case 1:
                data = ((int) layer.getChannel(channelPtr).sample[samplePtr] >> 8) & 0x000000FF;
                break;
            }
        }

        // update pointers
        bytePtr = ++bytePtr % 1;
        if (bytePtr == 0) {
            channelPtr = ++channelPtr % channels;
            if (channelPtr == 0) {
                samplePtr++;
            }
        }

        // is it the end of the layer ?
        boolean isEnd = true;
        for (int i = 0; i < channels; i++) {
            if (samplePtr < layer.getChannel(i).sample.length) {
                isEnd = false;
            }
        }

        if (isEnd)
            return -1;
        else
            return data;
    }
}
