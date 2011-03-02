package org.geometerplus.fbreader.formats.pdb;
/**
 * @author hym E-mail:hymmyh@gmail.com
 * @version 创建时间：2011-2-8 下午08:45:03
 * 类说明
 */

public class ReturnData {
  public int byteNum;
  public int bitNum;

  public ReturnData() {
  }
  public int getBitNum() {
    return bitNum;
  }
  public int getByteNum() {
    return byteNum;
  }
  public void setBitNum(int bitNum) {
    this.bitNum = bitNum;
  }
  public void setByteNum(int byteNum) {
    this.byteNum = byteNum;
  }
}