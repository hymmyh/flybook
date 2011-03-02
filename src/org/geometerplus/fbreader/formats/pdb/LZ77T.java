package org.geometerplus.fbreader.formats.pdb;
/**
 * @author hym E-mail:hymmyh@gmail.com
 * @version 创建时间：2011-2-8 下午08:44:00
 * 类说明
 */
public class LZ77T {
	  private final int MAX_WND_SIZE = 10;
	  private final int OFFSET_CODING_LENGTH = 9;
	  private final int constM = 1;

	  private byte[] pOutputBuffer;
	  private int pulNumberOfBytes;

	  public LZ77T() {
	    pOutputBuffer=new byte[400000];
	    pulNumberOfBytes=0;

	  }

	  public void lz77decompress(byte[] pDataBuffer,
	                             int ulNumberOfBits) {
	    int iSlideWindowPtr = 0;
	    int offset;
	    int offset2;
	    int length = 0, wndOffset = 0;
	    int bit = 0;
	    byte cc = 0;
	    int i = 0;
	    int ulBytesDecoded = 0;
	    int ulBitOffset = 0;
	    ReturnData returnData = new ReturnData();

	    int ulCodingLength = 0;

	    iSlideWindowPtr = -MAX_WND_SIZE;

	    ulBitOffset = 0;
	    ulBytesDecoded = 0;
	    offset2 = 0;
	    offset = 0;

	    while (ulBitOffset < ulNumberOfBits) {
	      bit = ReadBitFromBitStream(pDataBuffer, ulBitOffset);
	      ulBitOffset++;

	      if (bit == 1) {

	        if (iSlideWindowPtr >= 0) {
	          offset = iSlideWindowPtr;

	        }
	        else if (iSlideWindowPtr >= -MAX_WND_SIZE) {
	          offset = 0;
	        }
	        else {
	          offset = 0;
	        }

	        for (i = 0, wndOffset = 0; i < OFFSET_CODING_LENGTH; i++, ulBitOffset++) {
	          bit = ReadBitFromBitStream(pDataBuffer, ulBitOffset);
	          wndOffset |= (bit << i);
	        }

	        returnData = ReadGolombCode(pDataBuffer, ulBitOffset);

	        length = returnData.getByteNum();

	        ulCodingLength = returnData.getBitNum();

	        wndOffset = wndOffset % MAX_WND_SIZE;

	        if (length > MAX_WND_SIZE) {
	          length = length % MAX_WND_SIZE;
	        }
	        ulBitOffset += ulCodingLength;

	        for (i = 0; i < length; i++) {
	          pOutputBuffer[offset2 + i] = pOutputBuffer[offset + wndOffset];
	          offset++;
	        }

	        offset2 += length;

	        iSlideWindowPtr += length;
	        ulBytesDecoded += length;
	      }
	      else {
	        for (i = 0, cc = 0; i < 8; i++, ulBitOffset++) {
	          bit = ReadBitFromBitStream(pDataBuffer, ulBitOffset);
	          cc |= ( (byte) bit << i);
	        }
	        pOutputBuffer[offset2] = cc;

	        offset2++;

	        iSlideWindowPtr++;
	        ulBytesDecoded++;
	      }

	    }
	    pulNumberOfBytes = ulBytesDecoded;

	    setPulNumberOfBytes(pulNumberOfBytes);
	    setPOutputBuffer(pOutputBuffer);
	  }

	  private int ReadBitFromBitStream(byte[] pBuffer, int ulBitOffset) {
	    int ulByteBoundary = 0;
	    int ulOffsetInByte = 0;

	    ulByteBoundary = (int) (ulBitOffset >> 3);
	    ulOffsetInByte = ulBitOffset & 7;

	    return ( ( (byte) (pBuffer[ulByteBoundary] >> ulOffsetInByte)) &
	            ( (byte) 0x01));
	  }

	  private ReturnData ReadGolombCode(byte[] pBuffer, int ulBitOffset) {
	    int q, r;
	    int bit = 0;
	    int i;
	    int pulCodingLength;
	    ReturnData returnData = new ReturnData();

	    for (q = 0; ; q++) {
	      bit = ReadBitFromBitStream(pBuffer, ulBitOffset);
	      ulBitOffset++;
	      if (bit != 1) {
	        break;
	      }
	    }

	    for (i = 0, r = 0; (int) i < constM; i++, ulBitOffset++) {
	      bit = (int) ReadBitFromBitStream(pBuffer, ulBitOffset);
	      bit <<= i;
	      r |= bit;
	    }

	    pulCodingLength = constM + q + 1;

	    returnData.setBitNum(pulCodingLength);
	    returnData.setByteNum(r + (q << constM) + 1);

	    return returnData;
	  }

	  public byte[] getPOutputBuffer() {
	    return pOutputBuffer;
	  }

	  public int getPulNumberOfBytes() {
	    return pulNumberOfBytes;
	  }

	  private void setPOutputBuffer(byte[] pOutputBuffer) {
	    this.pOutputBuffer = pOutputBuffer;
	  }

	  private void setPulNumberOfBytes(int pulNumberOfBytes) {
	    this.pulNumberOfBytes = pulNumberOfBytes;
	  }

	}
